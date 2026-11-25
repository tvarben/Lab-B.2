package se.kth.hi1033.weatherapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.kth.hi1033.weatherapp.model.TemperatureUnit
import se.kth.hi1033.weatherapp.model.UserSettings

private val refreshOptions = listOf(
    15 to "15 min",
    30 to "30 min",
    60 to "60 min",
    0 to "Manual"
)

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onRefreshIntervalChange: (Int) -> Unit,
    onTemperatureUnitChange: (TemperatureUnit) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Data Refresh Rate", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                refreshOptions.forEach { (minutes, label) ->
                    FilterChip(
                        selected = settings.refreshIntervalMinutes == minutes,
                        onClick = { onRefreshIntervalChange(minutes) },
                        label = { Text(label) }
                    )
                }
            }
        }

        HorizontalDivider()

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Temperature Unit", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TemperatureUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = settings.temperatureUnit == unit,
                        onClick = { onTemperatureUnitChange(unit) },
                        label = { Text(unit.symbol) }
                    )
                }
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Dark Theme", style = MaterialTheme.typography.titleMedium)
                Text("Switch between light and dark appearance", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = settings.useDarkTheme,
                onCheckedChange = onDarkThemeChange
            )
        }
    }
}
