package se.kth.hi1033.weatherapp.ui.state

import se.kth.hi1033.weatherapp.model.FavouritePlace
import se.kth.hi1033.weatherapp.model.PlaceResult
import se.kth.hi1033.weatherapp.model.UserSettings
import se.kth.hi1033.weatherapp.model.WeatherForecast

enum class SearchMode {
    CITY,
    COORDINATES
}

data class WeatherUiState(
    val searchMode: SearchMode = SearchMode.CITY,
    val cityQuery: String = "",
    val latInput: String = "",
    val lonInput: String = "",
    val inputError: String? = null,
    val placeSuggestions: List<PlaceResult> = emptyList(),
    val isSearchingPlaces: Boolean = false,
    val isLoadingForecast: Boolean = false,
    val forecast: WeatherForecast? = null,
    val isFromCache: Boolean = false,
    val statusNotice: String? = null,
    val errorMessage: String? = null,
    val favourites: List<FavouritePlace> = emptyList(),
    val settings: UserSettings = UserSettings()
) {
    val isCurrentLocationFavourite: Boolean
        get() {
            val current = forecast ?: return false
            return favourites.any {
                it.name.equals(current.locationName, ignoreCase = true) &&
                    kotlin.math.abs(it.latitude - current.latitude) < 0.01 &&
                    kotlin.math.abs(it.longitude - current.longitude) < 0.01
            }
        }
}
