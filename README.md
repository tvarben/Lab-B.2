# Weather App (HI1033 Lab B:2)

An Android weather forecast application built with **Kotlin**, **Jetpack Compose**, and **MVVM** architecture, fetching 7-day forecast and geocoding data from the **Open-Meteo API**.

## Architecture Overview

The codebase is organized into three distinct layers (`model`, `data`, `ui`) with unidirectional data flow:

```text
Compose UI (screens/)  ──observes StateFlow──▶  WeatherViewModel (ui/viewmodel/)
       ▲                                                  │
       │ UI events (search, refresh, favourite)           │ calls suspend / Flow
       └──────────────────────────────────────────────────▼
                                              WeatherRepository (data/)
                                               ├── OpenMeteoApiService (data/remote/) + WeatherMapper
                                               ├── DataStoreWeatherStorage (data/local/)
                                               └── AndroidNetworkChecker (data/local/)
```

### Package & File Responsibilities

- **`model/`** *(Pure Kotlin, zero Android imports)*
  - `WeatherModels.kt`: Domain models (`WeatherForecast`, `HourlyForecast`, `DailyForecast`, `PlaceResult`, `FavouritePlace`, `UserSettings`, `TemperatureUnit`, `WeatherCondition`).
  - `CoordinateValidator.kt`: Validates user-entered latitude (`-90..90`) and longitude (`-180..180`) strings, normalizing decimal commas and rejecting `NaN`/`Infinity`.

- **`data/`** *(Networking, JSON parsing, local caching, connectivity)*
  - `remote/OpenMeteoDtos.kt`: `@Serializable` DTOs matching Open-Meteo Forecast & Geocoding JSON responses, with nullable/default fields for resilience.
  - `remote/WeatherMapper.kt`: Maps DTOs to domain models, groups today's hourly forecast separately from the 7-day daily overview, and handles mismatched array lengths or unknown WMO codes.
  - `remote/OpenMeteoApiService.kt`: Retrofit interfaces (`OpenMeteoForecastApi`, `OpenMeteoGeocodingApi`) and singleton `NetworkClient` (`OkHttpClient` with 10s timeouts + `kotlinx.serialization`).
  - `local/WeatherLocalStorage.kt`: `WeatherLocalStorage` interface and `DataStoreWeatherStorage` (Preferences DataStore) storing the last fetched forecast JSON, favourites list, and user settings.
  - `local/NetworkChecker.kt`: `NetworkChecker` interface and `AndroidNetworkChecker` using `ConnectivityManager` (`ACCESS_NETWORK_STATE`).
  - `WeatherRepository.kt`: Coordinates API calls on `Dispatchers.IO`, checks connectivity, classifies errors (`OFFLINE`, `TIMEOUT`, `HTTP_ERROR`, `PARSE_ERROR`, `EMPTY_RESULTS`), and falls back to cached data when offline or on failure.

- **`ui/`** *(State management & Jetpack Compose UI)*
  - `state/WeatherUiState.kt`: Immutable UI state (`SearchMode`, inputs, loading flags, `WeatherForecast`, offline/error notices, favourites, settings).
  - `viewmodel/WeatherViewModel.kt`: Manages `StateFlow<WeatherUiState>`, cancels in-flight search jobs on rapid consecutive searches, schedules auto-refresh, and provides `WeatherViewModelFactory`.
  - `navigation/WeatherNavHost.kt`: Compose Navigation routes (`Home`, `Detail/{dayIndex}`, `Favourites`, `Settings`) and bottom navigation bar.
  - `components/WeatherVisuals.kt`: Maps `WeatherCondition` to Material icons/colors and formats temperatures (`°C` / `°F`).
  - `screens/HomeScreen.kt`: City/coordinate search, place suggestions, offline/error banners, current weather hero card, today's hourly `LazyRow`, 7-day `LazyColumn`, and responsive portrait/landscape layout.
  - `screens/DetailScreen.kt`: Selected day summary + hourly breakdown list.
  - `screens/FavouritesScreen.kt`: Saved locations quick-access and removal.
  - `screens/SettingsScreen.kt`: Refresh interval (`15/30/60 min` or `Manual`), temperature unit, and dark theme toggle.
  - `theme/Theme.kt`: Material 3 light/dark color schemes.
