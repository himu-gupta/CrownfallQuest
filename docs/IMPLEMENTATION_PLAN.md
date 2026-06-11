# Crownfall Quest implementation plan

**Status: Completed.** All five phases have met their exit criteria. Release screenshots are stored in [`docs/screenshots`](screenshots/) and embedded in the project README.

## Product target

Ship a polished landscape Android platformer with a 20-35 minute first playthrough, replayable score targets, six levels, original animated visuals, original music, reliable touch controls, and deterministic game logic that can be tested without Android.

## Phase 1: Foundation

Status: Complete.

- Create the Android project and Git repository.
- Separate pure Kotlin simulation from rendering, input, audio, and persistence.
- Define the game-state model, collision primitives, event stream, and fixed-step clock.
- Add CI-friendly build and Jacoco tasks.

Exit criteria: clean debug build, deterministic engine skeleton, first pushed commit.

## Phase 2: Playable vertical slice

Status: Complete.

- Implement acceleration, friction, variable jump height, coyote time, and jump buffering.
- Add solid and one-way platforms, hazards, checkpoints, camera tracking, and respawning.
- Add crystals, growth mooncaps, ember blooms, stompable enemies, projectiles, lives, score, and timer.
- Add touch, keyboard, and controller input plus HUD and pause/restart flows.

Exit criteria: Level 1 is completable and core mechanics have unit tests.

## Phase 3: Full campaign

Status: Complete.

- Add six data-driven levels: Dawnwood, Glasswater Grotto, Cloudstep Ruins, Ember Foundry, Mooncap Marsh, and Crownfall Keep.
- Add walkers, flyers, turrets, armored enemies, moving platforms, breakable blocks, lava, water, and a multi-phase boss.
- Add King Rowan, Princess Astra, story cards, level unlocks, checkpoints, best scores, and campaign completion.

Exit criteria: complete start-to-ending campaign with no dead-end levels.

## Phase 4: Presentation and audio

Status: Complete.

- Integrate original title artwork and procedural animated sprites.
- Add parallax biomes, particles, squash-and-stretch, hit flashes, screen shake, and transitions.
- Add an original synthesized chiptune score with per-biome arrangements and sound effects.
- Polish adaptive landscape layout for phones, tablets, and foldables.

Exit criteria: coherent audiovisual identity and readable controls across supported landscape sizes.

## Phase 5: Quality and release

Status: Complete.

- Unit-test simulation, collisions, scoring, combat, AI, level validation, and progression.
- Add Compose behavior tests, persistence tests, and an instrumented launch/play smoke test.
- Run unit tests, coverage, debug build, and emulator verification; capture a screenshot.
- Commit and push each phase, then publish the GitHub repository with documentation.

Exit criteria: green build and tests, playable emulator build, public source repository.

## Release evidence

- 42 deterministic local tests covering geometry, touch-control alignment, movement, jump timing, collisions, terrain-aware enemy travel, breakable blocks, power-ups, scoring, checkpoints, AI, projectiles, three boss phases, level validation, and progression.
- 3 device tests covering level selection, credits/back navigation, and the menu-to-story-to-playable-surface journey.
- Successful `assembleDebug`, `testDebugUnitTest`, `connectedDebugAndroidTest`, and `jacocoTestReport` runs.
- Emulator-reviewed menu, story transition, gameplay, particles, HUD, controls, and pause flow.
- Five release screenshots committed under `docs/screenshots` and displayed on GitHub.

## Out of scope for 1.0

- Online accounts, multiplayer, ads, analytics, and in-app purchases
- Content copied from existing commercial platformers
- User-generated levels and cloud saves
