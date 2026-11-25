package se.kth.hi1033.weatherapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import se.kth.hi1033.weatherapp.model.TemperatureUnit
import se.kth.hi1033.weatherapp.model.WeatherCondition
import kotlin.math.roundToInt

fun WeatherCondition.toIcon(): ImageVector = when (this) {
    WeatherCondition.CLEAR_SKY -> Icons.Default.WbSunny
    WeatherCondition.PARTLY_CLOUDY -> Icons.Default.WbCloudy
    WeatherCondition.OVERCAST -> Icons.Default.Cloud
    WeatherCondition.FOG -> Icons.Default.Dehaze
    WeatherCondition.DRIZZLE -> Icons.Default.Grain
    WeatherCondition.RAIN, WeatherCondition.SHOWERS -> Icons.Default.WaterDrop
    WeatherCondition.SNOW -> Icons.Default.AcUnit
    WeatherCondition.THUNDERSTORM -> Icons.Default.Thunderstorm
    WeatherCondition.UNKNOWN -> Icons.Default.Cloud
}

fun WeatherCondition.toTint(): Color = when (this) {
    WeatherCondition.CLEAR_SKY -> Color(0xFFF9A825)
    WeatherCondition.PARTLY_CLOUDY -> Color(0xFF42A5F5)
    WeatherCondition.OVERCAST -> Color(0xFF78909C)
    WeatherCondition.FOG -> Color(0xFF90A4AE)
    WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.SHOWERS -> Color(0xFF1E88E5)
    WeatherCondition.SNOW -> Color(0xFF4DD0E1)
    WeatherCondition.THUNDERSTORM -> Color(0xFF7E57C2)
    WeatherCondition.UNKNOWN -> Color(0xFF9E9E9E)
}

fun formatTemperature(celsius: Double, unit: TemperatureUnit): String {
    val value = when (unit) {
        TemperatureUnit.CELSIUS -> celsius
        TemperatureUnit.FAHRENHEIT -> (celsius * 9.0 / 5.0) + 32.0
    }
    return "${value.roundToInt()}${unit.symbol}"
}
