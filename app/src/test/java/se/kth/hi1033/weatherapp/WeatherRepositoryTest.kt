package se.kth.hi1033.weatherapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import se.kth.hi1033.weatherapp.data.WeatherErrorType
import se.kth.hi1033.weatherapp.data.WeatherRepository
import se.kth.hi1033.weatherapp.data.WeatherResult
import se.kth.hi1033.weatherapp.data.local.NetworkChecker
import se.kth.hi1033.weatherapp.data.local.WeatherLocalStorage
import se.kth.hi1033.weatherapp.data.remote.DailyDto
import se.kth.hi1033.weatherapp.data.remote.ForecastResponseDto
import se.kth.hi1033.weatherapp.data.remote.GeocodingResponseDto
import se.kth.hi1033.weatherapp.data.remote.OpenMeteoForecastApi
import se.kth.hi1033.weatherapp.data.remote.OpenMeteoGeocodingApi
import se.kth.hi1033.weatherapp.model.FavouritePlace
import se.kth.hi1033.weatherapp.model.UserSettings
import se.kth.hi1033.weatherapp.model.WeatherCondition
import se.kth.hi1033.weatherapp.model.WeatherForecast
import java.net.SocketTimeoutException

class WeatherRepositoryTest {

    private class FakeStorage(
        var lastForecast: WeatherForecast? = null
    ) : WeatherLocalStorage {
        private val favs = MutableStateFlow<List<FavouritePlace>>(emptyList())
        private val settings = MutableStateFlow(UserSettings())

        override suspend fun saveLastForecast(forecast: WeatherForecast) {
            lastForecast = forecast
        }

        override suspend fun getLastForecast(): WeatherForecast? = lastForecast
        override fun observeFavourites(): Flow<List<FavouritePlace>> = favs
        override suspend fun saveFavourites(favourites: List<FavouritePlace>) {
            favs.value = favourites
        }

        override fun observeSettings(): Flow<UserSettings> = settings
        override suspend fun saveSettings(settings: UserSettings) {
            this.settings.value = settings
        }
    }

    private class FakeNetworkChecker(var online: Boolean) : NetworkChecker {
        override fun isOnline(): Boolean = online
    }

    private val dummyGeocodingApi = object : OpenMeteoGeocodingApi {
        override suspend fun searchPlaces(
            query: String,
            count: Int,
            language: String,
            format: String
        ) = GeocodingResponseDto()
    }

    private val sampleCachedForecast = WeatherForecast(
        locationName = "Stockholm",
        latitude = 59.33,
        longitude = 18.07,
        fetchedAtEpochMs = 1000L,
        currentTemperatureCelsius = 4.0,
        currentCondition = WeatherCondition.CLEAR_SKY,
        currentWindSpeedKmh = 10.0,
        todayHourly = emptyList(),
        dailyForecasts = emptyList()
    )

    @Test
    fun offlineWithNoCache_returnsOfflineError() = runTest {
        val repo = WeatherRepository(
            forecastApi = object : OpenMeteoForecastApi {
                override suspend fun getForecast(
                    latitude: Double,
                    longitude: Double,
                    hourly: String,
                    daily: String,
                    timezone: String
                ) = ForecastResponseDto()
            },
            geocodingApi = dummyGeocodingApi,
            localStorage = FakeStorage(lastForecast = null),
            networkChecker = FakeNetworkChecker(online = false),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val result = repo.getForecast(59.33, 18.07, "Stockholm")
        assertTrue(result is WeatherResult.Error)
        val err = result as WeatherResult.Error
        assertEquals(WeatherErrorType.OFFLINE, err.errorType)
        assertNull(err.cachedFallback)
    }

    @Test
    fun offlineWithCache_returnsCachedSuccess() = runTest {
        val repo = WeatherRepository(
            forecastApi = object : OpenMeteoForecastApi {
                override suspend fun getForecast(
                    latitude: Double,
                    longitude: Double,
                    hourly: String,
                    daily: String,
                    timezone: String
                ) = ForecastResponseDto()
            },
            geocodingApi = dummyGeocodingApi,
            localStorage = FakeStorage(lastForecast = sampleCachedForecast),
            networkChecker = FakeNetworkChecker(online = false),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val result = repo.getForecast(59.33, 18.07, "Stockholm")
        assertTrue(result is WeatherResult.Success)
        val success = result as WeatherResult.Success
        assertTrue(success.isFromCache)
        assertEquals("Stockholm", success.data.locationName)
    }

    @Test
    fun http500Error_distinguishesHttpErrorAndProvidesCache() = runTest {
        val repo = WeatherRepository(
            forecastApi = object : OpenMeteoForecastApi {
                override suspend fun getForecast(
                    latitude: Double,
                    longitude: Double,
                    hourly: String,
                    daily: String,
                    timezone: String
                ): ForecastResponseDto {
                    throw HttpException(Response.error<Any>(500, "Server error".toResponseBody()))
                }
            },
            geocodingApi = dummyGeocodingApi,
            localStorage = FakeStorage(lastForecast = sampleCachedForecast),
            networkChecker = FakeNetworkChecker(online = true),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val result = repo.getForecast(59.33, 18.07, "Stockholm")
        assertTrue(result is WeatherResult.Error)
        val err = result as WeatherResult.Error
        assertEquals(WeatherErrorType.HTTP_ERROR, err.errorType)
        assertEquals("Stockholm", err.cachedFallback?.locationName)
    }

    @Test
    fun timeoutAndParseErrors_areClassifiedDistinctly() = runTest {
        val timeoutRepo = WeatherRepository(
            forecastApi = object : OpenMeteoForecastApi {
                override suspend fun getForecast(
                    latitude: Double,
                    longitude: Double,
                    hourly: String,
                    daily: String,
                    timezone: String
                ): ForecastResponseDto = throw SocketTimeoutException("Timed out")
            },
            geocodingApi = dummyGeocodingApi,
            localStorage = FakeStorage(),
            networkChecker = FakeNetworkChecker(online = true),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val parseRepo = WeatherRepository(
            forecastApi = object : OpenMeteoForecastApi {
                override suspend fun getForecast(
                    latitude: Double,
                    longitude: Double,
                    hourly: String,
                    daily: String,
                    timezone: String
                ): ForecastResponseDto = throw SerializationException("Bad JSON")
            },
            geocodingApi = dummyGeocodingApi,
            localStorage = FakeStorage(),
            networkChecker = FakeNetworkChecker(online = true),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val timeoutResult = timeoutRepo.getForecast(59.33, 18.07, "Stockholm") as WeatherResult.Error
        val parseResult = parseRepo.getForecast(59.33, 18.07, "Stockholm") as WeatherResult.Error

        assertEquals(WeatherErrorType.TIMEOUT, timeoutResult.errorType)
        assertEquals(WeatherErrorType.PARSE_ERROR, parseResult.errorType)
    }

    @Test
    fun successfulFetch_savesToLocalStorage() = runTest {
        val storage = FakeStorage()
        val repo = WeatherRepository(
            forecastApi = object : OpenMeteoForecastApi {
                override suspend fun getForecast(
                    latitude: Double,
                    longitude: Double,
                    hourly: String,
                    daily: String,
                    timezone: String
                ) = ForecastResponseDto(
                    latitude = 59.33,
                    longitude = 18.07,
                    daily = DailyDto(
                        time = listOf("2026-11-25"),
                        weatherCode = listOf(0),
                        temperature2mMax = listOf(5.0),
                        temperature2mMin = listOf(1.0)
                    )
                )
            },
            geocodingApi = dummyGeocodingApi,
            localStorage = storage,
            networkChecker = FakeNetworkChecker(online = true),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val result = repo.getForecast(59.33, 18.07, "Stockholm")
        assertTrue(result is WeatherResult.Success)
        assertEquals("Stockholm", storage.lastForecast?.locationName)
    }
}
