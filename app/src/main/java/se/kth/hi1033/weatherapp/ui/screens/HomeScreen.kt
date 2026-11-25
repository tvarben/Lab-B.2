package se.kth.hi1033.weatherapp.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import se.kth.hi1033.weatherapp.model.DailyForecast
import se.kth.hi1033.weatherapp.model.HourlyForecast
import se.kth.hi1033.weatherapp.model.PlaceResult
import se.kth.hi1033.weatherapp.model.TemperatureUnit
import se.kth.hi1033.weatherapp.model.WeatherForecast
import se.kth.hi1033.weatherapp.ui.components.formatTemperature
import se.kth.hi1033.weatherapp.ui.components.toIcon
import se.kth.hi1033.weatherapp.ui.components.toTint
import se.kth.hi1033.weatherapp.ui.state.SearchMode
import se.kth.hi1033.weatherapp.ui.state.WeatherUiState

@Composable
fun HomeScreen(
    uiState: WeatherUiState,
    onSearchModeChange: (SearchMode) -> Unit,
    onCityQueryChange: (String) -> Unit,
    onLatInputChange: (String) -> Unit,
    onLonInputChange: (String) -> Unit,
    onSubmitSearch: () -> Unit,
    onSelectPlace: (PlaceResult) -> Unit,
    onRefresh: () -> Unit,
    onToggleFavourite: () -> Unit,
    onDayClick: (Int) -> Unit
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchSection(
                    uiState = uiState,
                    onSearchModeChange = onSearchModeChange,
                    onCityQueryChange = onCityQueryChange,
                    onLatInputChange = onLatInputChange,
                    onLonInputChange = onLonInputChange,
                    onSubmitSearch = onSubmitSearch,
                    onSelectPlace = onSelectPlace
                )
                StatusBanners(uiState = uiState, onRetry = onRefresh)
                uiState.forecast?.let { forecast ->
                    CurrentWeatherCard(
                        forecast = forecast,
                        unit = uiState.settings.temperatureUnit,
                        isFavourite = uiState.isCurrentLocationFavourite,
                        isLoading = uiState.isLoadingForecast,
                        onRefresh = onRefresh,
                        onToggleFavourite = onToggleFavourite
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight()
            ) {
                ForecastContentList(
                    uiState = uiState,
                    onDayClick = onDayClick,
                    onRetry = onSubmitSearch,
                    includeHeaderSections = false
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SearchSection(
                    uiState = uiState,
                    onSearchModeChange = onSearchModeChange,
                    onCityQueryChange = onCityQueryChange,
                    onLatInputChange = onLatInputChange,
                    onLonInputChange = onLonInputChange,
                    onSubmitSearch = onSubmitSearch,
                    onSelectPlace = onSelectPlace
                )
            }
            item {
                StatusBanners(uiState = uiState, onRetry = onRefresh)
            }

            if (uiState.isLoadingForecast && uiState.forecast == null) {
                item { LoadingStateView() }
            } else if (uiState.forecast == null) {
                item {
                    EmptyOrErrorInitialState(
                        errorMessage = uiState.errorMessage,
                        onRetry = onSubmitSearch
                    )
                }
            } else {
                val forecast = uiState.forecast
                item {
                    CurrentWeatherCard(
                        forecast = forecast,
                        unit = uiState.settings.temperatureUnit,
                        isFavourite = uiState.isCurrentLocationFavourite,
                        isLoading = uiState.isLoadingForecast,
                        onRefresh = onRefresh,
                        onToggleFavourite = onToggleFavourite
                    )
                }
                if (forecast.todayHourly.isNotEmpty()) {
                    item {
                        Text(
                            text = "Today (Hourly)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    item {
                        HourlyForecastRow(
                            hours = forecast.todayHourly,
                            unit = uiState.settings.temperatureUnit
                        )
                    }
                }
                item {
                    Text(
                        text = "7-Day Forecast",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                itemsIndexed(forecast.dailyForecasts) { index, day ->
                    DailyForecastRow(
                        day = day,
                        unit = uiState.settings.temperatureUnit,
                        onClick = { onDayClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastContentList(
    uiState: WeatherUiState,
    onDayClick: (Int) -> Unit,
    onRetry: () -> Unit,
    includeHeaderSections: Boolean
) {
    if (uiState.isLoadingForecast && uiState.forecast == null) {
        LoadingStateView()
        return
    }
    val forecast = uiState.forecast
    if (forecast == null) {
        EmptyOrErrorInitialState(errorMessage = uiState.errorMessage, onRetry = onRetry)
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (forecast.todayHourly.isNotEmpty()) {
            item {
                Text(
                    text = "Today (Hourly)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                HourlyForecastRow(
                    hours = forecast.todayHourly,
                    unit = uiState.settings.temperatureUnit
                )
            }
        }
        item {
            Text(
                text = "7-Day Forecast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        itemsIndexed(forecast.dailyForecasts) { index, day ->
            DailyForecastRow(
                day = day,
                unit = uiState.settings.temperatureUnit,
                onClick = { onDayClick(index) }
            )
        }
    }
}

@Composable
private fun SearchSection(
    uiState: WeatherUiState,
    onSearchModeChange: (SearchMode) -> Unit,
    onCityQueryChange: (String) -> Unit,
    onLatInputChange: (String) -> Unit,
    onLonInputChange: (String) -> Unit,
    onSubmitSearch: () -> Unit,
    onSelectPlace: (PlaceResult) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.searchMode == SearchMode.CITY,
                onClick = { onSearchModeChange(SearchMode.CITY) },
                label = { Text("City") }
            )
            FilterChip(
                selected = uiState.searchMode == SearchMode.COORDINATES,
                onClick = { onSearchModeChange(SearchMode.COORDINATES) },
                label = { Text("Coordinates") }
            )
        }

        when (uiState.searchMode) {
            SearchMode.CITY -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.cityQuery,
                        onValueChange = onCityQueryChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("City or place name") },
                        singleLine = true,
                        isError = uiState.inputError != null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSubmitSearch() })
                    )
                    Button(
                        onClick = onSubmitSearch,
                        enabled = !uiState.isSearchingPlaces
                    ) {
                        if (uiState.isSearchingPlaces) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                }
            }
            SearchMode.COORDINATES -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.latInput,
                        onValueChange = onLatInputChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Lat (-90..90)") },
                        singleLine = true,
                        isError = uiState.inputError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = uiState.lonInput,
                        onValueChange = onLonInputChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Lon (-180..180)") },
                        singleLine = true,
                        isError = uiState.inputError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = { onSubmitSearch() })
                    )
                    Button(onClick = onSubmitSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            }
        }

        uiState.inputError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (uiState.placeSuggestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column {
                    uiState.placeSuggestions.forEachIndexed { idx, place ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectPlace(place) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(place.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                        if (idx < uiState.placeSuggestions.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBanners(uiState: WeatherUiState, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (uiState.isFromCache && uiState.statusNotice != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CloudOff, contentDescription = null)
                    Text(
                        text = uiState.statusNotice,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (uiState.errorMessage != null && uiState.forecast != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRetry) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentWeatherCard(
    forecast: WeatherForecast,
    unit: TemperatureUnit,
    isFavourite: Boolean,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = forecast.locationName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "%.2f, %.2f".format(forecast.latitude, forecast.longitude),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row {
                    IconButton(onClick = onToggleFavourite) {
                        Icon(
                            imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavourite) "Remove favourite" else "Add favourite",
                            tint = if (isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(onClick = onRefresh, enabled = !isLoading) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = forecast.currentCondition.toIcon(),
                    contentDescription = forecast.currentCondition.label,
                    tint = forecast.currentCondition.toTint(),
                    modifier = Modifier.size(48.dp)
                )
                Column {
                    Text(
                        text = formatTemperature(forecast.currentTemperatureCelsius, unit),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${forecast.currentCondition.label} · Wind ${forecast.currentWindSpeedKmh} km/h",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun HourlyForecastRow(hours: List<HourlyForecast>, unit: TemperatureUnit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(hours) { hour ->
            Card(modifier = Modifier.width(84.dp)) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(hour.hourLabel, style = MaterialTheme.typography.labelMedium)
                    Icon(
                        imageVector = hour.condition.toIcon(),
                        contentDescription = hour.condition.label,
                        tint = hour.condition.toTint(),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = formatTemperature(hour.temperatureCelsius, unit),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${hour.precipitationMm} mm",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    day: DailyForecast,
    unit: TemperatureUnit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = day.dayLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = day.condition.label,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                imageVector = day.condition.toIcon(),
                contentDescription = day.condition.label,
                tint = day.condition.toTint(),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${formatTemperature(day.minTempCelsius, unit)} / ${formatTemperature(day.maxTempCelsius, unit)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoadingStateView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text("Fetching forecast...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptyOrErrorInitialState(errorMessage: String?, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (errorMessage != null) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = errorMessage ?: "Search for a city or enter coordinates to view the 7-day weather forecast.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (errorMessage != null) {
                Button(onClick = onRetry) {
                    Text("Try Again")
                }
            }
        }
    }
}
