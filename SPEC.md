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
| HI1033 | B:2 | Android (Kotlin + Jetpack Compose) | MVVM + Repository | Higher grade (A / VG) | SMHI (KTH proxy for dev; real SMHI API for submission) |

- **Weather API (Real)**: `https://opendata-download-metfcst.smhi.se/`
- **Geocoding API (Real)**: `https://www.smhi.se/wpta/backend_solr/autocomplete/search/{place}`
- **Dev Proxy (Testing only — replace before submission)**:
  - Forecast: `https://maceo.sth.kth.se/weather/forecast?lonLat=lon/14.333/lat/60.383`
  - Search: `https://maceo.sth.kth.se/weather/search?location=Sigfridstorp`

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

### Example template:
```markdown
### ComponentName
- Purpose: ...
- Inputs / Outputs: ...
- Edge cases: ...
- Test ideas: ...
- Requirements: ...
```

## 5. Open questions
- [ ] Inspect SMHI forecast JSON and autocomplete JSON responses to confirm exact parameter names (`t`, `Wsymb2`, `ws`, `pmean`, etc.) and coordinate rounding rules.
- [ ] Decide whether to start against KTH proxy (`maceo.sth.kth.se`) during early dev and switch base URLs to real SMHI endpoints before final testing.

## 6. Decision log
| Date | Decision | Rationale / Trade-off |
| :--- | :--- | :--- |
| 2026-11-25 | Kotlin + Jetpack Compose + MVVM | Matches lab Android recommendation; clean separation between Compose UI, `StateFlow` in ViewModel, and Repository. |
| 2026-11-25 | SMHI API (with KTH proxy for dev) | Swedish weather + place autocomplete; fallback to Open-Meteo if needed. |
| 2026-11-25 | Retrofit + `kotlinx.serialization` | Declarative HTTP interface, type-safe JSON DTOs, minimal boilerplate. |
| 2026-11-25 | Preferences DataStore | Simpler than Room for caching the latest forecast JSON, favourites, and settings; avoids SQL/DAO boilerplate while keeping persistence clean and explainable. |

## 7. Oral exam map
*(Fill in Code location and Test name as each component is completed. Q26 is always the opening question.)*

| # | Question | Code location | Test name |
| :--- | :--- | :--- | :--- |
| Q1 | Walk through the function that makes the API request. Where is it, and what does it return? | | |
| Q2 | How is the JSON response parsed into your data model? Show the parsing code. | | |
| Q3 | What fields from the API response does your app use, and what does it ignore? Why those? | | |
| Q4 | What happens if the API returns a successful HTTP 200 but the JSON is missing a field your model expects? | | |
| Q5 | Trace a complete data flow from the user pressing "search" to the forecast appearing on screen. | | |
| Q6 | How does your app handle caching — what is stored locally, and when is it refreshed? | | |
| Q7 | Where does the fetched weather data live while the app is running? Show the declaration. | | |
| Q8 | What happens to the displayed data when the user searches for a new city? | | |
| Q9 | How does the UI know when new data has arrived? Trace the update from data layer to screen. | | |
| Q10 | What state is preserved if the user leaves the app and returns? | | |
| Q11 | Where in the code is the loading state managed? How do you prevent showing stale data while a new request is in flight? | | |
| Q12 | If the user rapidly searches for two cities, what happens? Can a race condition occur, and how do you guard against it? | | |
| Q13 | What does the app display if the network request times out? | | |
| Q14 | Where do you distinguish between a network error and an API error response (e.g., HTTP 404 or 500)? | | |
| Q15 | What happens if the user has no internet connection and opens the app for the first time? | | |
| Q16 | What happens if they open it a second time, after having successfully loaded data before? | | |
| Q17 | Where is the error state declared, and how does it flow to the UI? | | |
| Q18 | What was the hardest error case to handle? Show the code. | | |
| Q19 | Show the MVVM layers in your code: View, ViewModel, Model/Repository. | | |
| Q20 | Why is the network call not made directly from the View? | | |
| Q21 | Where in the code is testable logic separated from UI code? What made that separation easy or hard? | | |
| Q22 | How does navigation between screens work? Why does the ViewModel not navigate itself? | | |
| Q23 | Open `SPEC.md`. Walk through how one requirement ID maps to the code that implements it. | | |
| Q24 | What would you change about your architecture if you had to add hourly forecast support tomorrow? | | |
| Q25 | Show a function the AI helped you write. Explain it line by line in your own words. | | |
| Q26 | Open `SPEC.md` and show us the AI meta-instructions at the top. Walk us through one requirement and find the code that implements it. | `SPEC.md` Section 1 & 3 | |
| Q27 | Where did the AI suggest something you pushed back on or changed? What was your reasoning? | | |
| Q28 | How did you verify that AI-generated code actually works correctly? Show a test case. | | |
| Q29 | What was the most effective prompt you wrote during this lab? What made it work? | | |
| Q30 | If you were starting this lab again with the same AI tools, what would you do differently? | | |

## 8. Plan and status
| Phase | Goal | Status |
| :--- | :--- | :--- |
| Phase 0 | Project setup, `SPEC.md`, `STYLE.md`, git init, API JSON inspection | In progress |
| Phase 1 | Domain models, JSON parser/mapper, hourly/daily grouping, coordinate validation + unit/mutation tests | Pending |
| Phase 2 | Retrofit service, DataStore cache, `ConnectivityManager`, Repository error handling + unit tests | Pending |
| Phase 3 | `WeatherViewModel` (`StateFlow`, search cancellation, favourites, settings) | Pending |
| Phase 4 | Jetpack Compose UI (Home, Detail, Favourites, Settings), portrait/landscape, phone/tablet | Pending |
| Phase 5 | Emulator verification, 20 UX scenarios, `README.md`, `TEST_REPORT.md`, `LLM_PROCESS.md`, `REFLECTION.md` | Pending |

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
