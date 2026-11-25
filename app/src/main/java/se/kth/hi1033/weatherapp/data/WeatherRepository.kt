package se.kth.hi1033.weatherapp.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import se.kth.hi1033.weatherapp.data.local.NetworkChecker
import se.kth.hi1033.weatherapp.data.local.WeatherLocalStorage
import se.kth.hi1033.weatherapp.data.remote.OpenMeteoForecastApi
import se.kth.hi1033.weatherapp.data.remote.OpenMeteoGeocodingApi
import se.kth.hi1033.weatherapp.data.remote.WeatherMapper
import se.kth.hi1033.weatherapp.model.FavouritePlace
import se.kth.hi1033.weatherapp.model.PlaceResult
import se.kth.hi1033.weatherapp.model.UserSettings
import se.kth.hi1033.weatherapp.model.WeatherForecast
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class WeatherErrorType {
    OFFLINE,
    TIMEOUT,
    HTTP_ERROR,
    PARSE_ERROR,
    EMPTY_RESULTS,
    UNKNOWN
}

sealed class WeatherResult<out T> {
    data class Success<T>(
        val data: T,
        val isFromCache: Boolean = false,
        val notice: String? = null
    ) : WeatherResult<T>()

    data class Error<T>(
        val errorType: WeatherErrorType,
        val message: String,
        val cachedFallback: T? = null
    ) : WeatherResult<T>()
}

/**
 * Coordinates remote API calls, offline cache fallback, and error classification.
 */
class WeatherRepository(
    private val forecastApi: OpenMeteoForecastApi,
    private val geocodingApi: OpenMeteoGeocodingApi,
    private val localStorage: WeatherLocalStorage,
    private val networkChecker: NetworkChecker,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getForecast(
        latitude: Double,
        longitude: Double,
        locationName: String
    ): WeatherResult<WeatherForecast> = withContext(ioDispatcher) {
        if (!networkChecker.isOnline()) {
            val cached = localStorage.getLastForecast()
            return@withContext if (cached != null) {
                WeatherResult.Success(
                    data = cached,
                    isFromCache = true,
                    notice = "You are offline. Showing last saved forecast for ${cached.locationName}."
                )
            } else {
                WeatherResult.Error(
                    errorType = WeatherErrorType.OFFLINE,
                    message = "No internet connection and no saved forecast available yet."
                )
            }
        }

        try {
            val dto = forecastApi.getForecast(latitude = latitude, longitude = longitude)
            val now = LocalDateTime.now()
            val todayIso = now.toLocalDate().toString()
            val hourPrefix = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH"))

            val forecast = WeatherMapper.mapForecast(
                dto = dto,
                locationName = locationName,
                todayDateIso = todayIso,
                currentHourPrefix = hourPrefix
            )

            if (forecast.dailyForecasts.isEmpty()) {
                val cached = localStorage.getLastForecast()
                return@withContext WeatherResult.Error(
                    errorType = WeatherErrorType.EMPTY_RESULTS,
                    message = "Weather service returned no forecast days.",
                    cachedFallback = cached
                )
            }

            localStorage.saveLastForecast(forecast)
            WeatherResult.Success(data = forecast, isFromCache = false)
        } catch (e: Exception) {
            val (type, msg) = classifyException(e)
            val cached = localStorage.getLastForecast()
            WeatherResult.Error(
                errorType = type,
                message = msg,
                cachedFallback = cached
            )
        }
    }

    suspend fun searchPlaces(query: String): WeatherResult<List<PlaceResult>> = withContext(ioDispatcher) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext WeatherResult.Error(
                errorType = WeatherErrorType.EMPTY_RESULTS,
                message = "Enter a city name to search."
            )
        }

        if (!networkChecker.isOnline()) {
            return@withContext WeatherResult.Error(
                errorType = WeatherErrorType.OFFLINE,
                message = "Cannot search for places while offline."
            )
        }

        try {
            val dto = geocodingApi.searchPlaces(query = trimmed)
            val places = WeatherMapper.mapPlaceResults(dto)
            if (places.isEmpty()) {
                WeatherResult.Error(
                    errorType = WeatherErrorType.EMPTY_RESULTS,
                    message = "No places found matching \"$trimmed\"."
                )
            } else {
                WeatherResult.Success(data = places)
            }
        } catch (e: Exception) {
            val (type, msg) = classifyException(e)
            WeatherResult.Error(errorType = type, message = msg)
        }
    }

    suspend fun loadCachedForecast(): WeatherForecast? = withContext(ioDispatcher) {
        localStorage.getLastForecast()
    }

    fun observeFavourites(): Flow<List<FavouritePlace>> = localStorage.observeFavourites()

    suspend fun toggleFavourite(place: FavouritePlace) = withContext(ioDispatcher) {
        val current = localStorage.observeFavourites().first()
        val exists = current.any {
            it.name.equals(place.name, ignoreCase = true) &&
                kotlin.math.abs(it.latitude - place.latitude) < 0.01 &&
                kotlin.math.abs(it.longitude - place.longitude) < 0.01
        }
        val updated = if (exists) {
            current.filterNot {
                it.name.equals(place.name, ignoreCase = true) &&
                    kotlin.math.abs(it.latitude - place.latitude) < 0.01 &&
                    kotlin.math.abs(it.longitude - place.longitude) < 0.01
            }
        } else {
            current + place
        }
        localStorage.saveFavourites(updated)
    }

    suspend fun removeFavourite(place: FavouritePlace) = withContext(ioDispatcher) {
        val current = localStorage.observeFavourites().first()
        localStorage.saveFavourites(current.filterNot { it == place })
    }

    fun observeSettings(): Flow<UserSettings> = localStorage.observeSettings()

    suspend fun saveSettings(settings: UserSettings) = withContext(ioDispatcher) {
        localStorage.saveSettings(settings)
    }

    private fun classifyException(e: Exception): Pair<WeatherErrorType, String> = when (e) {
        is SocketTimeoutException -> WeatherErrorType.TIMEOUT to "Request timed out. Please try again."
        is HttpException -> WeatherErrorType.HTTP_ERROR to "Server error (HTTP ${e.code()}). Please try again later."
        is SerializationException -> WeatherErrorType.PARSE_ERROR to "Received unexpected data format from server."
        is IOException -> WeatherErrorType.OFFLINE to "Network error. Check your internet connection."
        else -> WeatherErrorType.UNKNOWN to (e.localizedMessage ?: "An unexpected error occurred.")
    }
}
