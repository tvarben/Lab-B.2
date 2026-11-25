package se.kth.hi1033.weatherapp

import org.junit.Assert.assertEquals
import org.junit.Test
import se.kth.hi1033.weatherapp.data.remote.DailyDto
import se.kth.hi1033.weatherapp.data.remote.ForecastResponseDto
import se.kth.hi1033.weatherapp.data.remote.HourlyDto
import se.kth.hi1033.weatherapp.data.remote.WeatherMapper
import se.kth.hi1033.weatherapp.model.WeatherCondition

class WeatherMapperTest {

    @Test
    fun mapForecast_groupsTodayHourlyAndDailyOverview() {
        val dto = ForecastResponseDto(
            latitude = 59.33,
            longitude = 18.07,
            hourly = HourlyDto(
                time = listOf(
                    "2026-11-25T14:00",
                    "2026-11-25T15:00",
                    "2026-11-26T12:00"
                ),
                temperature2m = listOf(1.4, 2.0, -1.0),
                weatherCode = listOf(0, 3, 71),
                windSpeed10m = listOf(10.0, 12.0, 8.0),
                precipitation = listOf(0.0, 0.2, 1.5)
            ),
            daily = DailyDto(
                time = listOf("2026-11-25", "2026-11-26"),
                weatherCode = listOf(3, 71),
                temperature2mMax = listOf(2.0, 0.0),
                temperature2mMin = listOf(-0.5, -2.5),
                precipitationSum = listOf(0.2, 1.5),
                windSpeed10mMax = listOf(12.0, 8.0)
            )
        )

        val result = WeatherMapper.mapForecast(
            dto = dto,
            locationName = "Stockholm",
            todayDateIso = "2026-11-25",
            currentHourPrefix = "2026-11-25T15"
        )

        assertEquals("Stockholm", result.locationName)
        assertEquals(2.0, result.currentTemperatureCelsius, 0.01)
        assertEquals(WeatherCondition.OVERCAST, result.currentCondition)
        assertEquals(2, result.todayHourly.size)
        assertEquals("14:00", result.todayHourly[0].hourLabel)
        assertEquals(2, result.dailyForecasts.size)
        assertEquals("Today", result.dailyForecasts[0].dayLabel)
        assertEquals(1, result.dailyForecasts[1].hourlyBreakdown.size)
    }

    @Test
    fun mapForecast_handlesMismatchedArraysAndUnknownCodesSafely() {
        val dto = ForecastResponseDto(
            latitude = 59.33,
            longitude = 18.07,
            hourly = HourlyDto(
                time = listOf("2026-11-25T14:00", "2026-11-25T15:00"),
                temperature2m = listOf(5.0), // shorter than time
                weatherCode = listOf(999, null), // unknown and null
                windSpeed10m = emptyList(),
                precipitation = emptyList()
            ),
            daily = DailyDto(
                time = listOf("2026-11-25"),
                weatherCode = listOf(0),
                temperature2mMax = listOf(5.0),
                temperature2mMin = listOf(1.0)
            )
        )

        val result = WeatherMapper.mapForecast(
            dto = dto,
            locationName = "Test",
            todayDateIso = "2026-11-25"
        )

        assertEquals(2, result.todayHourly.size)
        assertEquals(WeatherCondition.UNKNOWN, result.todayHourly[0].condition)
        assertEquals(0.0, result.todayHourly[1].temperatureCelsius, 0.01)
        assertEquals(0.0, result.todayHourly[0].windSpeedKmh, 0.01)
    }
}
