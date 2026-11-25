package se.kth.hi1033.weatherapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.kth.hi1033.weatherapp.data.WeatherRepository
import se.kth.hi1033.weatherapp.data.WeatherResult
import se.kth.hi1033.weatherapp.data.local.AndroidNetworkChecker
import se.kth.hi1033.weatherapp.data.local.DataStoreWeatherStorage
import se.kth.hi1033.weatherapp.data.remote.NetworkClient
import se.kth.hi1033.weatherapp.model.CoordinateValidator
import se.kth.hi1033.weatherapp.model.FavouritePlace
import se.kth.hi1033.weatherapp.model.PlaceResult
import se.kth.hi1033.weatherapp.model.TemperatureUnit
import se.kth.hi1033.weatherapp.model.UserSettings
import se.kth.hi1033.weatherapp.ui.state.SearchMode
import se.kth.hi1033.weatherapp.ui.state.WeatherUiState

class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var forecastJob: Job? = null
    private var placeSearchJob: Job? = null
    private var autoRefreshJob: Job? = null

    init {
        observeFavouritesAndSettings()
        loadInitialCachedForecast()
    }

    private fun observeFavouritesAndSettings() {
        viewModelScope.launch {
            repository.observeFavourites().collect { favs ->
                _uiState.update { it.copy(favourites = favs) }
            }
        }
        viewModelScope.launch {
            repository.observeSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
                scheduleAutoRefresh(settings.refreshIntervalMinutes)
            }
        }
    }

    private fun loadInitialCachedForecast() {
        viewModelScope.launch {
            val cached = repository.loadCachedForecast()
            if (cached != null) {
                _uiState.update {
                    it.copy(
                        forecast = cached,
                        isFromCache = true,
                        statusNotice = "Loaded saved forecast for ${cached.locationName}."
                    )
                }
            }
        }
    }

    private fun scheduleAutoRefresh(intervalMinutes: Int) {
        autoRefreshJob?.cancel()
        if (intervalMinutes <= 0) return
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(intervalMinutes * 60_000L)
                val current = _uiState.value.forecast
                if (current != null) {
                    fetchForecast(current.latitude, current.longitude, current.locationName)
                }
            }
        }
    }

    fun setSearchMode(mode: SearchMode) {
        _uiState.update {
            it.copy(searchMode = mode, inputError = null, placeSuggestions = emptyList())
        }
    }

    fun onCityQueryChange(query: String) {
        _uiState.update { it.copy(cityQuery = query, inputError = null) }
    }

    fun onLatInputChange(lat: String) {
        _uiState.update { it.copy(latInput = lat, inputError = null) }
    }

    fun onLonInputChange(lon: String) {
        _uiState.update { it.copy(lonInput = lon, inputError = null) }
    }

    fun submitSearch() {
        val state = _uiState.value
        when (state.searchMode) {
            SearchMode.CITY -> searchCity(state.cityQuery)
            SearchMode.COORDINATES -> searchByCoordinates(state.latInput, state.lonInput)
        }
    }

    private fun searchCity(query: String) {
        placeSearchJob?.cancel()
        if (query.trim().isEmpty()) {
            _uiState.update { it.copy(inputError = "Enter a city name.") }
            return
        }
        placeSearchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSearchingPlaces = true,
                    errorMessage = null,
                    placeSuggestions = emptyList()
                )
            }
            when (val result = repository.searchPlaces(query)) {
                is WeatherResult.Success -> {
                    val places = result.data
                    _uiState.update {
                        it.copy(
                            isSearchingPlaces = false,
                            placeSuggestions = places
                        )
                    }
                    if (places.size == 1) {
                        selectPlace(places.first())
                    }
                }
                is WeatherResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSearchingPlaces = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun searchByCoordinates(latInput: String, lonInput: String) {
        when (val validation = CoordinateValidator.validate(latInput, lonInput)) {
            is CoordinateValidator.ValidationResult.Invalid -> {
                _uiState.update { it.copy(inputError = validation.reason) }
            }
            is CoordinateValidator.ValidationResult.Valid -> {
                val label = "%.2f, %.2f".format(validation.latitude, validation.longitude)
                fetchForecast(validation.latitude, validation.longitude, label)
            }
        }
    }

    fun selectPlace(place: PlaceResult) {
        _uiState.update {
            it.copy(
                cityQuery = place.name,
                placeSuggestions = emptyList(),
                inputError = null
            )
        }
        fetchForecast(place.latitude, place.longitude, place.name)
    }

    fun loadFavourite(favourite: FavouritePlace) {
        fetchForecast(favourite.latitude, favourite.longitude, favourite.name)
    }

    fun refreshCurrentForecast() {
        val current = _uiState.value.forecast ?: return
        fetchForecast(current.latitude, current.longitude, current.locationName)
    }

    private fun fetchForecast(latitude: Double, longitude: Double, locationName: String) {
        // Cancel any in-flight request to prevent stale results overwriting newer searches.
        forecastJob?.cancel()
        forecastJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingForecast = true,
                    errorMessage = null,
                    statusNotice = null
                )
            }
            when (val result = repository.getForecast(latitude, longitude, locationName)) {
                is WeatherResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingForecast = false,
                            forecast = result.data,
                            isFromCache = result.isFromCache,
                            statusNotice = result.notice,
                            errorMessage = null
                        )
                    }
                }
                is WeatherResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingForecast = false,
                            forecast = result.cachedFallback ?: it.forecast,
                            isFromCache = (result.cachedFallback ?: it.forecast) != null,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun toggleCurrentFavourite() {
        val current = _uiState.value.forecast ?: return
        viewModelScope.launch {
            repository.toggleFavourite(
                FavouritePlace(
                    name = current.locationName,
                    country = "",
                    latitude = current.latitude,
                    longitude = current.longitude
                )
            )
        }
    }

    fun removeFavourite(place: FavouritePlace) {
        viewModelScope.launch {
            repository.removeFavourite(place)
        }
    }

    fun updateRefreshInterval(minutes: Int) {
        val updated = _uiState.value.settings.copy(refreshIntervalMinutes = minutes)
        saveSettings(updated)
    }

    fun updateTemperatureUnit(unit: TemperatureUnit) {
        val updated = _uiState.value.settings.copy(temperatureUnit = unit)
        saveSettings(updated)
    }

    fun updateDarkTheme(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(useDarkTheme = enabled)
        saveSettings(updated)
    }

    private fun saveSettings(settings: UserSettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
        }
    }
}

class WeatherViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = WeatherRepository(
            forecastApi = NetworkClient.forecastApi,
            geocodingApi = NetworkClient.geocodingApi,
            localStorage = DataStoreWeatherStorage(appContext.applicationContext),
            networkChecker = AndroidNetworkChecker(appContext.applicationContext)
        )
        return WeatherViewModel(repository) as T
    }
}
