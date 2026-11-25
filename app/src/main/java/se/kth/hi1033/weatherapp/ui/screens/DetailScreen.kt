package se.kth.hi1033.weatherapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.kth.hi1033.weatherapp.model.DailyForecast
import se.kth.hi1033.weatherapp.model.TemperatureUnit
import se.kth.hi1033.weatherapp.ui.components.formatTemperature
import se.kth.hi1033.weatherapp.ui.components.toIcon
import se.kth.hi1033.weatherapp.ui.components.toTint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    day: DailyForecast?,
    locationName: String,
    unit: TemperatureUnit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(day?.let { "$locationName · ${it.dayLabel}" } ?: "Day Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (day == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Forecast details are unavailable for this day.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = day.condition.toIcon(),
                                contentDescription = day.condition.label,
                                tint = day.condition.toTint(),
                                modifier = Modifier.size(48.dp)
                            )
                            Column {
                                Text(
                                    text = day.condition.label,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Low ${formatTemperature(day.minTempCelsius, unit)} · High ${formatTemperature(day.maxTempCelsius, unit)}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Max wind: ${day.windSpeedMaxKmh} km/h")
                            Text("Precipitation: ${day.precipitationSumMm} mm")
                        }
                    }
                }
            }

            if (day.hourlyBreakdown.isNotEmpty()) {
                item {
                    Text(
                        text = "Hourly Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(day.hourlyBreakdown) { hour ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(hour.hourLabel, fontWeight = FontWeight.Medium)
                            Icon(
                                imageVector = hour.condition.toIcon(),
                                contentDescription = hour.condition.label,
                                tint = hour.condition.toTint(),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(formatTemperature(hour.temperatureCelsius, unit), fontWeight = FontWeight.Bold)
                            Text("${hour.windSpeedKmh} km/h", style = MaterialTheme.typography.bodySmall)
                            Text("${hour.precipitationMm} mm", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
