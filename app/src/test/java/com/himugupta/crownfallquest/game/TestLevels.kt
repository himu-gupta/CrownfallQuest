package com.himugupta.crownfallquest.game

internal fun testLevel(
  width: Float = 24f,
  start: Vec2 = Vec2(1f, 7f),
  goal: Rect = Rect(22f, 6f, 23f, 8f),
  platforms: List<PlatformSpec> = listOf(PlatformSpec(Rect(0f, 8f, width, 10f))),
  enemies: List<EnemySpec> = emptyList(),
  pickups: List<PickupSpec> = emptyList(),
  hazards: List<HazardSpec> = emptyList(),
  checkpoints: List<CheckpointSpec> = emptyList(),
): LevelDefinition =
  LevelDefinition(
    id = 99,
    name = "Test Realm",
    subtitle = "Deterministic fixture",
    biome = Biome.DAWNWOOD,
    width = width,
    start = start,
    goal = goal,
    timeLimitSeconds = 120,
    platforms = platforms,
    enemies = enemies,
    pickups = pickups,
    hazards = hazards,
    checkpoints = checkpoints,
    intro = "Test start",
    outro = "Test complete",
  )

internal fun GameEngine.advance(seconds: Float, input: GameInput = GameInput()) {
  repeat((seconds / GameEngine.FIXED_STEP).toInt()) { update(GameEngine.FIXED_STEP, input) }
}
