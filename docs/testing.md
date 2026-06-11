# Testing strategy

The simulation is pure Kotlin and receives explicit input and time steps. This keeps movement, collision, combat, scoring, and progression tests fast and deterministic.

## Test layers

- Local unit tests: geometry, player movement, power-ups, enemy AI, projectiles, score, checkpoints, death, boss behavior, level validation, and save-policy logic.
- Integration tests: complete scripted journeys through representative level sections.
- Compose UI tests: menu state, level selection, pause overlay, and state restoration.
- Instrumented smoke test: launch in landscape, start a level, verify the game surface, pause, and return to the menu.

## Commands

```bash
./gradlew :app:testDebugUnitTest
./gradlew jacocoTestReport
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:assembleDebug
```

The HTML coverage report is generated at `app/build/reports/jacoco/jacocoTestReport/html/index.html`.
