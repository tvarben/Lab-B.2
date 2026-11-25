# Weather App (HI1033 Lab B:2)

An Android weather forecast application built with **Kotlin**, **Jetpack Compose**, and **MVVM** architecture, fetching forecast and place-search data from the **SMHI API**.

## Architecture Overview

*(To be completed as layers are implemented so a reader can understand the architecture before opening Android Studio.)*

- **`model/`**: Pure Kotlin domain models (`WeatherForecast`, `HourlyForecast`, `DailyForecast`, `PlaceResult`, `UserSettings`).
- **`data/`**:
  - `remote/`: Retrofit service interfaces and JSON DTOs for SMHI forecast & autocomplete APIs, plus the JSON-to-domain mapper.
  - `local/`: DataStore persistence for cached forecasts, favourite locations, and user settings.
  - `WeatherRepository.kt`: Coordinates network calls, connectivity checks, offline cache fallback, and error mapping.
- **`ui/`**:
  - `state/`: Immutable `WeatherUiState` and navigation/event types.
  - `viewmodel/`: `WeatherViewModel` managing `StateFlow`, `viewModelScope` coroutines, and search job cancellation.
  - `screens/`: Jetpack Compose screens (`HomeScreen`, `DetailScreen`, `FavouritesScreen`, `SettingsScreen`).
  - `theme/`: Material 3 theme, colors, and typography.

## How Layers Connect
`Compose UI (View)` ──observes `StateFlow`──▶ `WeatherViewModel` ──calls `suspend`──▶ `WeatherRepository` ──uses──▶ `SMHI Retrofit API` / `DataStore Cache`
