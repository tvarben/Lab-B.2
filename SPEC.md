# SPEC.md — Native Weather App (Kotlin / Jetpack Compose)

## 1. AI meta-instructions
> Read `SPEC.md` and `STYLE.md` first. Where they conflict with the student's request, point out the conflict — student decides.
- Never mark a requirement done unless the student can explain it.
- Never write test results, tick requirements, or write `docs/REFLECTION.md` or `docs/LLM_PROCESS.md`.
- All code, comments, and documentation must be in **English**.
- Do not add new tests or dependencies without student approval.
- Remind the student of the **End-of-Session Ritual (Section 9)** at the end of every session.

## 2. Project facts
| Course | Lab | Platform | Architecture | Grade target | Primary API |
| :--- | :--- | :--- | :--- | :--- | :--- |
| HI1033 | B:2 | Android (Kotlin + Jetpack Compose) | MVVM + Repository | Higher grade (A / VG) | Open-Meteo (Forecast + Geocoding) |

- **Weather API**: `https://api.open-meteo.com/v1/forecast` (no API key required)
- **Geocoding API**: `https://geocoding-api.open-meteo.com/v1/search` (no API key required)
- *(Note: SMHI autocomplete and `pmp3g` v2 endpoints returned HTTP 404 on 2026-11-25, and `geocode.maps.co` required an API key (HTTP 401), so we switched to Open-Meteo per our fallback plan.)*

## 3. Requirements checklist
### Basic Requirements
| ID | Requirement | Status |
| :--- | :--- | :--- |
| B-01 | Search / Home screen: enter city name or geographic coordinates; show current conditions and 7-day forecast summary | ☐ |
| B-02 | Place name search resolves city name to coordinates using a geocoding API (SMHI autocomplete) | ☐ |
| B-03 | Detail screen: full forecast for a selected day (temperature, weather condition, wind, precipitation, etc.) | ☐ |
| B-04 | Favourites: mark places as favourites and show a dedicated quick-access view | ☐ |
| B-05 | Settings screen: configure data refresh rate and other UI preferences | ☐ |
| B-06 | Minimum two screens (we have Home, Detail, Favourites, Settings) | ☐ |

### Detailed Requirements
| ID | Requirement | Status |
| :--- | :--- | :--- |
| D-01 | Fetch forecast data from SMHI API | ☐ |
| D-02 | Display temperature and visual weather condition (icon, colour, or text code) for each of the next 7 days | ☐ |
| D-03 | Group received data: hourly detail for today and daily overview for remaining days | ☐ |
| D-04 | Store parsed data locally (DataStore) so app works offline | ☐ |
| D-05 | Offline handling: never crash, show meaningful message, display last successfully fetched data if available | ☐ |
| D-06 | Support both portrait and landscape orientation (phone and tablet AVDs) | ☐ |
| D-07 | Separate class/mapper for parsing JSON response into internal domain model | ☐ |
| D-08 | Forecast data displayed in a scrollable list | ☐ |
| D-09 | Coordinate input accepts only valid floating-point values | ☐ |
| D-10 | All network requests run off the main thread (`viewModelScope` / `Dispatchers.IO`) | ☐ |
| D-11 | All errors and exceptions handled: failed request never causes crash or silent blank screen | ☐ |
| D-12 | Runnable unit tests for at least two of: JSON parsing, error-state logic, data transformation (+ mutation tests) | ☐ |

### Higher Grade Requirements
| ID | Requirement | Status |
| :--- | :--- | :--- |
| H-01 | Structured code: clear separation of concerns (networking, parsing/transformation, state, UI); deliberate naming | ☐ |
| H-02 | Designed UI: consistent visual hierarchy, spacing, and designed loading/empty/error/offline states | ☐ |
| H-03 | Oral exam depth: able to explain *why* each structural decision was made, trade-offs, and failure modes | ☐ |

## 4. Component specs (spec-first principle)
*(Write each component's purpose, inputs/outputs, edge cases, test ideas, and requirement IDs here BEFORE coding it.)*

### CoordinateValidator (Core)
- **Purpose:** Validate user-entered latitude and longitude strings before making an API request.
- **Inputs / Outputs:** `(latInput: String, lonInput: String) -> ValidationResult` (either `Valid(lat: Double, lon: Double)` or `Invalid(reason: String)`).
- **Edge cases:** Empty/blank strings, non-numeric text, `NaN`/`Infinity`, comma vs. dot decimal separator, out-of-range coordinates (`lat < -90 || lat > 90`, `lon < -180 || lon > 180`).
- **Test ideas:** Valid decimals (`"59.33", "18.07"`), negative coordinates (`"-33.86", "151.20"`), out-of-range (`"91.0", "0.0"`), garbage text (`"abc", "18.0"`), blank input.
- **Requirements:** D-09, D-11

### WeatherMapper (Core)
- **Purpose:** Transform Open-Meteo `ForecastDto` (parallel arrays in `hourly` and `daily`) and `GeocodingResponseDto` into immutable internal domain models (`WeatherForecast`, `HourlyForecast`, `DailyForecast`, `PlaceResult`), grouping today's hours separately from the 7-day daily overview.
- **Inputs / Outputs:** `(dto: ForecastDto, locationName: String, currentDate: LocalDate, currentHourPrefix: String) -> WeatherForecast`
- **Edge cases:** Missing/null fields in JSON arrays, mismatched array lengths (e.g., `temperature_2m` shorter than `time`), empty `hourly` or `daily` lists, unknown WMO `weather_code`.
- **Test ideas:** Maps valid 7-day response into today's hourly list + 7 daily summaries; handles mismatched array lengths without crashing; maps missing/null optional fields safely; maps unknown WMO codes to an `UNKNOWN` condition.
- **Requirements:** D-02, D-03, D-07, D-11, D-12

## 5. Open questions
- [x] Inspect SMHI and Open-Meteo JSON responses to confirm endpoints and exact parameter names.
- [ ] Decide which UI preferences to include in Settings alongside refresh rate (e.g., °C/°F unit toggle, wind unit m/s vs km/h).

## 6. Decision log
| Date | Decision | Rationale / Trade-off |
| :--- | :--- | :--- |
| 2026-11-25 | Kotlin + Jetpack Compose + MVVM | Matches lab Android recommendation; clean separation between Compose UI, `StateFlow` in ViewModel, and Repository. |
| 2026-11-25 | Open-Meteo (Forecast + Geocoding) | SMHI autocomplete & `pmp3g` v2 returned HTTP 404 and `geocode.maps.co` returned 401 (API key required). Open-Meteo works out-of-the-box with no key and provides both hourly and 7-day daily data. |
| 2026-11-25 | Retrofit + `kotlinx.serialization` | Declarative HTTP interface, type-safe JSON DTOs, minimal boilerplate. |
| 2026-11-25 | Preferences DataStore | Simpler than Room for caching the latest forecast JSON, favourites, and settings; avoids SQL/DAO boilerplate while keeping persistence clean and explainable. |

## 7. Oral exam map
*(Fill in Code location and Test name as each component is completed. Q26 is always the opening question.)*

| # | Question | Code location | Test name |
| :--- | :--- | :--- | :--- |
| Q1 | Walk through the function that makes the API request. Where is it, and what does it return? | `WeatherRepository.getForecast()` & `OpenMeteoForecastApi.getForecast()` | `WeatherRepositoryTest.successfulFetch_savesToLocalStorage` |
| Q2 | How is the JSON response parsed into your data model? Show the parsing code. | `OpenMeteoDtos.kt` & `WeatherMapper.mapForecast()` | `WeatherMapperTest.mapForecast_groupsTodayHourlyAndDailyOverview` |
| Q3 | What fields from the API response does your app use, and what does it ignore? Why those? | `OpenMeteoDtos.kt` (`hourly`, `daily`, `results`) | `WeatherMapperTest.mapForecast_groupsTodayHourlyAndDailyOverview` |
| Q4 | What happens if the API returns a successful HTTP 200 but the JSON is missing a field your model expects? | `OpenMeteoDtos.kt` (defaults/nullables) & `WeatherMapper.mapForecast()` | `WeatherMapperTest.mapForecast_handlesMismatchedArraysAndUnknownCodesSafely` |
| Q5 | Trace a complete data flow from the user pressing "search" to the forecast appearing on screen. | `HomeScreen.kt` -> `WeatherViewModel.submitSearch()` -> `WeatherRepository.getForecast()` | |
| Q6 | How does your app handle caching — what is stored locally, and when is it refreshed? | `DataStoreWeatherStorage` & `WeatherRepository.getForecast()` | `WeatherRepositoryTest.successfulFetch_savesToLocalStorage` |
| Q7 | Where does the fetched weather data live while the app is running? Show the declaration. | `WeatherViewModel._uiState` (`MutableStateFlow<WeatherUiState>`) | |
| Q8 | What happens to the displayed data when the user searches for a new city? | `WeatherViewModel.fetchForecast()` | |
| Q9 | How does the UI know when new data has arrived? Trace the update from data layer to screen. | `WeatherViewModel.uiState` -> `MainActivity` (`collectAsState()`) -> `HomeScreen` | |
| Q10 | What state is preserved if the user leaves the app and returns? | `DataStoreWeatherStorage` (last forecast, favourites, settings) | |
| Q11 | Where in the code is the loading state managed? How do you prevent showing stale data while a new request is in flight? | `WeatherUiState.isLoadingForecast` in `WeatherViewModel.fetchForecast()` | |
| Q12 | If the user rapidly searches for two cities, what happens? Can a race condition occur, and how do you guard against it? | `forecastJob?.cancel()` & `placeSearchJob?.cancel()` in `WeatherViewModel.kt` | |
| Q13 | What does the app display if the network request times out? | `WeatherRepository.classifyException()` (`SocketTimeoutException`) -> `HomeScreen` error banner | `WeatherRepositoryTest.timeoutAndParseErrors_areClassifiedDistinctly` |
| Q14 | Where do you distinguish between a network error and an API error response (e.g., HTTP 404 or 500)? | `WeatherRepository.classifyException()` (`HttpException` vs `IOException`) | `WeatherRepositoryTest.http500Error_distinguishesHttpErrorAndProvidesCache` |
| Q15 | What happens if the user has no internet connection and opens the app for the first time? | `WeatherRepository.getForecast()` (`OFFLINE`, no cache) -> `EmptyOrErrorInitialState` | `WeatherRepositoryTest.offlineWithNoCache_returnsOfflineError` |
| Q16 | What happens if they open it a second time, after having successfully loaded data before? | `WeatherViewModel.loadInitialCachedForecast()` & `WeatherRepository.getForecast()` | `WeatherRepositoryTest.offlineWithCache_returnsCachedSuccess` |
| Q17 | Where is the error state declared, and how does it flow to the UI? | `WeatherUiState.errorMessage` -> `StatusBanners` / `EmptyOrErrorInitialState` | |
| Q18 | What was the hardest error case to handle? Show the code. | `WeatherMapper.mapForecast()` (mismatched parallel arrays) / `WeatherRepository.classifyException()` | `WeatherMapperTest.mapForecast_handlesMismatchedArraysAndUnknownCodesSafely` |
| Q19 | Show the MVVM layers in your code: View, ViewModel, Model/Repository. | `ui/screens/`, `ui/viewmodel/WeatherViewModel.kt`, `model/` & `data/WeatherRepository.kt` | |
| Q20 | Why is the network call not made directly from the View? | `HomeScreen.kt` -> `WeatherViewModel.kt` -> `WeatherRepository.kt` | |
| Q21 | Where in the code is testable logic separated from UI code? What made that separation easy or hard? | `CoordinateValidator.kt`, `WeatherMapper.kt`, `WeatherRepository.kt` (interfaces) | `CoordinateValidatorTest`, `WeatherMapperTest`, `WeatherRepositoryTest` |
| Q22 | How does navigation between screens work? Why does the ViewModel not navigate itself? | `WeatherNavHost.kt` (`NavHost` + `Screen` routes) | |
| Q23 | Open `SPEC.md`. Walk through how one requirement ID maps to the code that implements it. | `SPEC.md` Section 3 & 4 -> e.g. `D-09` -> `CoordinateValidator.kt` | `CoordinateValidatorTest` |
| Q24 | What would you change about your architecture if you had to add hourly forecast support tomorrow? | Already supported via `WeatherMapper.mapForecast()` & `HourlyForecastRow` | |
| Q25 | Show a function the AI helped you write. Explain it line by line in your own words. | *(Student picks function during inspection)* | |
| Q26 | Open `SPEC.md` and show us the AI meta-instructions at the top. Walk us through one requirement and find the code that implements it. | `SPEC.md` Section 1 & 3 | |
| Q27 | Where did the AI suggest something you pushed back on or changed? What was your reasoning? | *(Student documents in `LLM_PROCESS.md`)* | |
| Q28 | How did you verify that AI-generated code actually works correctly? Show a test case. | `app/src/test/java/se/kth/hi1033/weatherapp/` | `WeatherRepositoryTest`, `WeatherMapperTest` |
| Q29 | What was the most effective prompt you wrote during this lab? What made it work? | *(Student documents in `LLM_PROCESS.md`)* | |
| Q30 | If you were starting this lab again with the same AI tools, what would you do differently? | *(Student documents in `REFLECTION.md`)* | |

## 8. Plan and status
| Phase | Goal | Status |
| :--- | :--- | :--- |
| Phase 0 | Project setup, `SPEC.md`, `STYLE.md`, git init, API JSON inspection | Done |
| Phase 1 | Domain models, JSON parser/mapper, hourly/daily grouping, coordinate validation + unit tests | Code & tests written (pending student review & Android Studio test run) |
| Phase 2 | Retrofit service, DataStore cache, `ConnectivityManager`, Repository error handling + unit tests | Code & tests written (pending student review & Android Studio test run) |
| Phase 3 | `WeatherViewModel` (`StateFlow`, search cancellation, favourites, settings) | Code written (pending student review) |
| Phase 4 | Jetpack Compose UI (Home, Detail, Favourites, Settings), portrait/landscape, phone/tablet | Code written (pending student review & emulator check) |
| Phase 5 | Emulator verification, 20 UX scenarios, mutation tests, `TEST_REPORT.md`, `LLM_PROCESS.md`, `REFLECTION.md` | Pending (Student) |

## 9. End-of-session ritual
1. Summarise the session: what was coded, tested, or designed (1–2 sentences).
2. Update the plan table in `SPEC.md` Section 8.
3. Tick completed requirements in Section 3 (only if the student can explain them).
4. Update the decision log (Section 6) with any new architectural choices.
5. Update the oral exam map (Section 7) with code locations and test names for newly completed components.
6. Student writes a log entry in `docs/LLM_PROCESS.md` (date, what was done, approximate time).
7. Student writes running notes in `docs/REFLECTION.md` (what was hard today? what do you understand better now?).

## 10. Done checklist
- [ ] All `B-*`, `D-*`, `H-*` requirements ticked and explainable
- [ ] Unit tests + mutation tests passing and documented in `docs/TEST_REPORT.md`
- [ ] 20 UX scenarios verified on Android Emulator (phone & tablet, portrait & landscape)
- [ ] Real SMHI API enabled (not KTH test proxy)
- [ ] `README.md`, `SPEC.md`, `docs/TEST_REPORT.md`, `docs/LLM_PROCESS.md`, `docs/REFLECTION.md` complete
