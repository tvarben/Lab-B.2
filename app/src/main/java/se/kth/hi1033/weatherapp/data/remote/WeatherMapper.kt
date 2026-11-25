package se.kth.hi1033.weatherapp.data.remote

import se.kth.hi1033.weatherapp.model.DailyForecast
import se.kth.hi1033.weatherapp.model.HourlyForecast
import se.kth.hi1033.weatherapp.model.PlaceResult
import se.kth.hi1033.weatherapp.model.WeatherCondition
import se.kth.hi1033.weatherapp.model.WeatherForecast
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Pure Kotlin mapper converting Open-Meteo JSON DTOs into internal domain models.
 * Guards against missing fields and mismatched parallel array lengths.
 */
object WeatherMapper {

    fun mapForecast(
        dto: ForecastResponseDto,
        locationName: String,
        todayDateIso: String = LocalDate.now().toString(),
        currentHourPrefix: String = "",
        fetchedAtEpochMs: Long = System.currentTimeMillis()
    ): WeatherForecast {
        val allHourly = mapHourlyList(dto.hourly)
        val hourlyByDate = allHourly.groupBy { it.timeIso.take(10) }

        val dailyList = mapDailyList(dto.daily, hourlyByDate, todayDateIso)
        val effectiveTodayDate = dailyList.firstOrNull()?.dateIso ?: todayDateIso
        val todayHours = hourlyByDate[effectiveTodayDate].orEmpty()

        val currentHour = todayHours.firstOrNull {
            currentHourPrefix.isNotEmpty() && it.timeIso.startsWith(currentHourPrefix)
        } ?: todayHours.firstOrNull()

        val firstDay = dailyList.firstOrNull()

        return WeatherForecast(
            locationName = locationName,
            latitude = dto.latitude,
            longitude = dto.longitude,
            fetchedAtEpochMs = fetchedAtEpochMs,
            currentTemperatureCelsius = currentHour?.temperatureCelsius
                ?: firstDay?.maxTempCelsius
                ?: 0.0,
            currentCondition = currentHour?.condition
                ?: firstDay?.condition
                ?: WeatherCondition.UNKNOWN,
            currentWindSpeedKmh = currentHour?.windSpeedKmh
                ?: firstDay?.windSpeedMaxKmh
                ?: 0.0,
            todayHourly = todayHours,
            dailyForecasts = dailyList
        )
    }

    fun mapPlaceResults(dto: GeocodingResponseDto): List<PlaceResult> {
        return dto.results
            .filter { it.name.isNotBlank() }
            .map {
                PlaceResult(
                    id = it.id,
                    name = it.name,
                    country = it.country,
                    admin1 = it.admin1,
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }
    }

    private fun mapHourlyList(hourly: HourlyDto?): List<HourlyForecast> {
        if (hourly == null || hourly.time.isEmpty()) return emptyList()
        return hourly.time.mapIndexed { index, timeIso ->
            val hourLabel = if (timeIso.length >= 16) timeIso.substring(11, 16) else timeIso
            HourlyForecast(
                timeIso = timeIso,
                hourLabel = hourLabel,
                temperatureCelsius = hourly.temperature2m.getOrNull(index) ?: 0.0,
                condition = WeatherCondition.fromWmoCode(hourly.weatherCode.getOrNull(index)),
                windSpeedKmh = hourly.windSpeed10m.getOrNull(index) ?: 0.0,
                precipitationMm = hourly.precipitation.getOrNull(index) ?: 0.0
            )
        }
    }

    private fun mapDailyList(
        daily: DailyDto?,
        hourlyByDate: Map<String, List<HourlyForecast>>,
        todayDateIso: String
    ): List<DailyForecast> {
        if (daily == null || daily.time.isEmpty()) return emptyList()
        return daily.time.mapIndexed { index, dateIso ->
            DailyForecast(
                dateIso = dateIso,
                dayLabel = formatDayLabel(dateIso, todayDateIso),
                minTempCelsius = daily.temperature2mMin.getOrNull(index) ?: 0.0,
                maxTempCelsius = daily.temperature2mMax.getOrNull(index) ?: 0.0,
                condition = WeatherCondition.fromWmoCode(daily.weatherCode.getOrNull(index)),
                windSpeedMaxKmh = daily.windSpeed10mMax.getOrNull(index) ?: 0.0,
                precipitationSumMm = daily.precipitationSum.getOrNull(index) ?: 0.0,
                hourlyBreakdown = hourlyByDate[dateIso].orEmpty()
            )
        }
    }

    private fun formatDayLabel(dateIso: String, todayDateIso: String): String {
        if (dateIso == todayDateIso) return "Today"
        return try {
            val date = LocalDate.parse(dateIso, DateTimeFormatter.ISO_LOCAL_DATE)
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            val monthDay = date.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH))
            "$dayName, $monthDay"
        } catch (_: Exception) {
            dateIso
        }
    }
}
