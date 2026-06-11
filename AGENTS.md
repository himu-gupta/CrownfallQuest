# Project guidance

- Keep game simulation code under `game/` free of Android dependencies.
- Levels are data, validated by `LevelCatalogTest`.
- Rendering may consume simulation state but must not mutate it.
- Add or update tests for every gameplay rule change.
- See [docs/testing.md](docs/testing.md) for the complete test strategy.
