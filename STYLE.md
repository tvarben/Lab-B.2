# STYLE.md — Coding Philosophy and AI Role

This file defines how the student and AI collaborate so the student learns deeply and can defend every line of code at the oral exam.

## 1. Three Code Tiers

| Tier | What belongs here | Who writes | AI's role |
| :--- | :--- | :--- | :--- |
| **Core** | Domain & business logic the lab tests: JSON-to-domain mapping (`WeatherMapper`), today-hourly vs. 7-day-daily grouping, coordinate input validation, error/offline state-machine logic, rapid-search cancellation logic | **Student** | Tutor and reviewer only. Must **NOT** write full Core code without following the Hint Ladder. |
| **Support** | Layer wiring, Retrofit API interfaces, Repository orchestration, DataStore persistence, `ViewModel` wiring, unit tests | **Shared** | May draft in small pieces; student reads, understands, and can explain every line. |
| **Boilerplate** | Gradle configuration, AndroidManifest permissions, Compose UI layout scaffolding, Material3 theme, navigation boilerplate | **AI** | Writes directly and explains key parts. |

## 2. Hint Ladder for Core Code
When the student is stuck on Core logic, go **one step at a time** and stop as soon as the student can continue:
1. **Concept** — explain the idea in plain words with a small example.
2. **Questions** — ask what the function must guarantee and what could go wrong.
3. **Pseudocode** — outline the steps without Kotlin syntax.
4. **Skeleton** — provide only the function signature and `// TODO` comments.
5. **Review** — the student writes the code; the AI reviews and points out issues without fixing them directly.
6. **Full solution** — only if the student explicitly asks after steps 1–5; the student then closes it and rewrites from memory.

## 3. Teach-Back After Every Core or Support Piece
Before marking any Core or Support component done, the student should be able to answer:
- *"Walk me through this function with a concrete input."*
- *"What happens if this line is removed? Which test fails?"*
- *"Which oral exam question in `SPEC.md` Section 7 does this answer?"*

## 4. Coding Conventions (Kotlin / MVVM)
- Keep functions small, single-purpose, and readable with shallow nesting.
- Use immutable `data class`es for domain models and `UiState`.
- Keep all Android/Compose imports out of `model/` and `data/` mapping/logic classes so they are pure Kotlin and easy to unit-test on the JVM.
- All code, comments, and documentation must be in **English**.

## 5. Session Start Prompt (copy–paste into new sessions)
```text
Read SPEC.md and STYLE.md before anything else. We follow the coding approach
in STYLE.md: my goal is to learn, I write Core code myself, and you act as
tutor and reviewer. Today I want to work on: ______. Ask me what I have tried
first. Run the end-of-session ritual in SPEC.md Section 9 at the end.
```
