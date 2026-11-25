package se.kth.hi1033.weatherapp.model

import kotlinx.serialization.Serializable

/**
 * Simplified weather conditions mapped from Open-Meteo WMO weather interpretation codes.
 */
enum class WeatherCondition(val label: String) {
    CLEAR_SKY("Clear sky"),
    PARTLY_CLOUDY("Partly cloudy"),
    OVERCAST("Overcast"),
    FOG("Fog"),
    DRIZZLE("Drizzle"),
    RAIN("Rain"),
    SNOW("Snow"),
    SHOWERS("Rain showers"),
    THUNDERSTORM("Thunderstorm"),
    UNKNOWN("Unknown");

    companion object {
        fun fromWmoCode(code: Int?): WeatherCondition = when (code) {
            0 -> CLEAR_SKY
            1, 2 -> PARTLY_CLOUDY
            3 -> OVERCAST
            45, 48 -> FOG
            51, 53, 55, 56, 57 -> DRIZZLE
            61, 63, 65, 66, 67 -> RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            80, 81, 82 -> SHOWERS
            95, 96, 99 -> THUNDERSTORM
            else -> UNKNOWN
        }
    }
}

@Serializable
data class HourlyForecast(
    val timeIso: String,
    val hourLabel: String,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val windSpeedKmh: Double,
    val precipitationMm: Double
)

@Serializable
data class DailyForecast(
    val dateIso: String,
    val dayLabel: String,
    val minTempCelsius: Double,
    val maxTempCelsius: Double,
    val condition: WeatherCondition,
    val windSpeedMaxKmh: Double,
    val precipitationSumMm: Double,
    val hourlyBreakdown: List<HourlyForecast> = emptyList()
)

@Serializable
data class WeatherForecast(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val fetchedAtEpochMs: Long,
    val currentTemperatureCelsius: Double,
    val currentCondition: WeatherCondition,
    val currentWindSpeedKmh: Double,
    val todayHourly: List<HourlyForecast>,
    val dailyForecasts: List<DailyForecast>
)

data class PlaceResult(
    val id: Long,
    val name: String,
    val country: String,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String
        get() = listOfNotNull(name, admin1?.takeIf { it.isNotBlank() }, country.takeIf { it.isNotBlank() })
            .joinToString(", ")
}

@Serializable
data class FavouritePlace(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F")
}

@Serializable
data class UserSettings(
    val refreshIntervalMinutes: Int = 30,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val useDarkTheme: Boolean = false
)
