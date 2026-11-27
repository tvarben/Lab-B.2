package se.kth.hi1033.weatherapp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
//Q2 DTOs that are used by the domain model
//Q3 we use nullables or defaults to handle missing fields that can be caught in getForecast
@Serializable
data class ForecastResponseDto(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val hourly: HourlyDto? = null,
    val daily: DailyDto? = null
)

@Serializable
data class HourlyDto(
    val time: List<String> = emptyList(),
    @SerialName("temperature_2m")
    val temperature2m: List<Double?> = emptyList(),
    @SerialName("weather_code")
    val weatherCode: List<Int?> = emptyList(),
    @SerialName("wind_speed_10m")
    val windSpeed10m: List<Double?> = emptyList(),
    val precipitation: List<Double?> = emptyList()
)

@Serializable
data class DailyDto(
    val time: List<String> = emptyList(),
    @SerialName("weather_code")
    val weatherCode: List<Int?> = emptyList(),
    @SerialName("temperature_2m_max")
    val temperature2mMax: List<Double?> = emptyList(),
    @SerialName("temperature_2m_min")
    val temperature2mMin: List<Double?> = emptyList(),
    @SerialName("precipitation_sum")
    val precipitationSum: List<Double?> = emptyList(),
    @SerialName("wind_speed_10m_max")
    val windSpeed10mMax: List<Double?> = emptyList()
)

@Serializable //not sure about this one
data class GeocodingResponseDto(
    val results: List<GeocodingResultDto> = emptyList()
)

@Serializable //not sure about this one
data class GeocodingResultDto(
    val id: Long = 0L,
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "",
    val admin1: String? = null
)
