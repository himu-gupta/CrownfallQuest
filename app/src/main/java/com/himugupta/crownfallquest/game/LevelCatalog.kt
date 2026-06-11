package com.himugupta.crownfallquest.game

object LevelCatalog {
  val levels: List<LevelDefinition> =
    listOf(
      dawnwood(),
      glasswaterGrotto(),
      cloudstepRuins(),
      emberFoundry(),
      mooncapMarsh(),
      crownfallKeep(),
    )

  fun byId(id: Int): LevelDefinition = levels.first { it.id == id }

  private fun ground(vararg segments: Pair<Float, Float>, y: Float = 8.2f): List<PlatformSpec> =
    segments.map { (x, width) -> PlatformSpec(Rect(x, y, x + width, 10f)) }

  private fun ledge(x: Float, y: Float, width: Float, oneWay: Boolean = true): PlatformSpec =
    PlatformSpec(Rect(x, y, x + width, y + 0.34f), oneWay = oneWay)

  private fun moving(x: Float, y: Float, width: Float, moveX: Float = 0f, moveY: Float = 0f): PlatformSpec =
    PlatformSpec(Rect(x, y, x + width, y + 0.32f), oneWay = true, moveX = moveX, moveY = moveY, movePeriod = 3f)

  private fun breakable(x: Float, y: Float, width: Float = 1f): PlatformSpec =
    PlatformSpec(Rect(x, y, x + width, y + 0.72f), breakable = true)

  private fun crystals(start: Float, count: Int, y: Float, spacing: Float = 1.2f): List<PickupSpec> =
    List(count) { PickupSpec(PickupKind.SUN_CRYSTAL, start + it * spacing, y) }

  private fun dawnwood(): LevelDefinition =
    LevelDefinition(
      id = 1,
      name = "Dawnwood Trail",
      subtitle = "First light beneath the whispering boughs",
      biome = Biome.DAWNWOOD,
      width = 54f,
      start = Vec2(1.5f, 7.1f),
      goal = Rect(51.2f, 6.1f, 52.4f, 8.2f),
      timeLimitSeconds = 180,
      platforms = ground(0f to 13f, 15f to 10f, 27f to 12f, 41f to 13f) + listOf(
        ledge(5f, 6.3f, 3f), ledge(10f, 5.2f, 2.5f), ledge(17f, 6.4f, 3f),
        ledge(22f, 4.9f, 2.3f), moving(29f, 6.2f, 2.8f, moveX = 3f),
        ledge(35f, 5.1f, 3f), breakable(39.5f, 5.7f, 1.4f), ledge(43f, 6.1f, 2.6f), ledge(47f, 4.8f, 2.4f),
      ),
      enemies = listOf(
        EnemySpec(EnemyKind.WALKER, 8f, 7.38f, 5f, 11f),
        EnemySpec(EnemyKind.WALKER, 18f, 7.38f, 16f, 23f),
        EnemySpec(EnemyKind.FLYER, 31f, 4.5f, 28f, 35f),
        EnemySpec(EnemyKind.ARMORED, 44f, 7.38f, 42f, 49f),
      ),
      pickups = crystals(3.5f, 6, 6.7f) + crystals(17f, 5, 5.7f) + crystals(42f, 6, 6.8f) + listOf(
        PickupSpec(PickupKind.MOONCAP, 11f, 4.5f),
        PickupSpec(PickupKind.EMBER_BLOOM, 35.8f, 4.3f),
        PickupSpec(PickupKind.CROWN_SHARD, 48f, 4f),
      ),
      hazards = listOf(
        HazardSpec(HazardKind.SPIKES, Rect(13f, 7.75f, 15f, 8.2f)),
        HazardSpec(HazardKind.DEEP_WATER, Rect(25f, 8.2f, 27f, 10f)),
        HazardSpec(HazardKind.SPIKES, Rect(39f, 7.75f, 41f, 8.2f)),
      ),
      checkpoints = listOf(CheckpointSpec(27.6f, 7.1f)),
      intro = "The Crownlight has been scattered. Follow the sun crystals toward the keep.",
      outro = "A distant bell answers. The road descends beneath the mountain.",
    )

  private fun glasswaterGrotto(): LevelDefinition =
    LevelDefinition(
      id = 2,
      name = "Glasswater Grotto",
      subtitle = "Echoes below the silver falls",
      biome = Biome.GROTTO,
      width = 60f,
      start = Vec2(1.2f, 7.1f),
      goal = Rect(57.3f, 5.8f, 58.5f, 8.2f),
      timeLimitSeconds = 210,
      platforms = ground(0f to 9f, 12f to 8f, 23f to 7f, 34f to 9f, 47f to 13f) + listOf(
        ledge(4f, 5.8f, 2.5f), moving(9f, 7.1f, 3f, moveY = -2.4f),
        ledge(14f, 5.2f, 3f), ledge(18f, 3.8f, 2f), moving(20f, 6.6f, 3f, moveX = 3f),
        ledge(25f, 5.4f, 2.5f), moving(30f, 6.8f, 4f, moveY = -3f),
        ledge(36f, 4.4f, 3f), breakable(39.5f, 5.3f, 1.2f), ledge(41f, 5.9f, 2f), moving(43f, 7f, 4f, moveX = 3f),
        ledge(50f, 5.1f, 3f), ledge(54f, 3.8f, 2.6f),
      ),
      enemies = listOf(
        EnemySpec(EnemyKind.FLYER, 7f, 4f, 3f, 10f), EnemySpec(EnemyKind.WALKER, 14f, 7.38f, 12f, 19f),
        EnemySpec(EnemyKind.TURRET, 26f, 7.38f, 25f, 27f), EnemySpec(EnemyKind.FLYER, 38f, 3f, 34f, 42f),
        EnemySpec(EnemyKind.ARMORED, 50f, 7.38f, 48f, 56f),
      ),
      pickups = crystals(3f, 5, 6.4f) + crystals(14f, 5, 4.4f) + crystals(35f, 6, 3.6f) + listOf(
        PickupSpec(PickupKind.MOONCAP, 19f, 3.1f), PickupSpec(PickupKind.EMBER_BLOOM, 42f, 5.1f),
        PickupSpec(PickupKind.CROWN_SHARD, 55f, 3f),
      ),
      hazards = listOf(
        HazardSpec(HazardKind.DEEP_WATER, Rect(9f, 8f, 12f, 10f)),
        HazardSpec(HazardKind.DEEP_WATER, Rect(20f, 8f, 23f, 10f)),
        HazardSpec(HazardKind.DEEP_WATER, Rect(30f, 8f, 34f, 10f)),
        HazardSpec(HazardKind.DEEP_WATER, Rect(43f, 8f, 47f, 10f)),
      ),
      checkpoints = listOf(CheckpointSpec(30.5f, 5.8f)),
      intro = "The grotto turns every footstep into a warning. Ride the old lift stones.",
      outro = "Daylight breaks through the ceiling, high above the clouds.",
    )

  private fun cloudstepRuins(): LevelDefinition =
    LevelDefinition(
      id = 3,
      name = "Cloudstep Ruins",
      subtitle = "A city suspended in the blue",
      biome = Biome.CLOUD_RUINS,
      width = 66f,
      start = Vec2(1.2f, 7.1f),
      goal = Rect(63.2f, 4.6f, 64.5f, 6.9f),
      timeLimitSeconds = 220,
      platforms = ground(0f to 7f, 13f to 6f, 27f to 7f, 43f to 8f, 59f to 7f, y = 8.4f) + listOf(
        moving(7f, 7f, 3f, moveX = 3f), moving(10f, 5.2f, 3f, moveY = 2f),
        ledge(14f, 5.2f, 3f), moving(19f, 6.8f, 3f, moveX = 5f), ledge(23f, 4.4f, 2.5f),
        moving(34f, 6.2f, 3.5f, moveY = -3f), ledge(37f, 3.7f, 3f), breakable(40.2f, 4.7f, 1.2f), moving(41.5f, 5.4f, 3f, moveX = 3f),
        ledge(46f, 5.1f, 2.5f), moving(51f, 6.8f, 4f, moveX = 4f), ledge(55f, 3.8f, 3f),
        ledge(61f, 6.9f, 4f),
      ),
      enemies = listOf(
        EnemySpec(EnemyKind.FLYER, 9f, 3.5f, 5f, 14f), EnemySpec(EnemyKind.TURRET, 16f, 7.58f),
        EnemySpec(EnemyKind.FLYER, 29f, 4f, 27f, 35f), EnemySpec(EnemyKind.TURRET, 45f, 7.58f),
        EnemySpec(EnemyKind.ARMORED, 60f, 7.58f, 59f, 64f),
      ),
      pickups = crystals(4f, 5, 6.4f) + crystals(15f, 6, 4.3f) + crystals(37f, 5, 2.9f) + crystals(55f, 7, 3f) + listOf(
        PickupSpec(PickupKind.MOONCAP, 24f, 3.6f), PickupSpec(PickupKind.EMBER_BLOOM, 47f, 4.3f),
        PickupSpec(PickupKind.CROWN_SHARD, 57f, 3f),
      ),
      hazards = listOf(
        HazardSpec(HazardKind.DEEP_WATER, Rect(7f, 8.4f, 13f, 10f)),
        HazardSpec(HazardKind.DEEP_WATER, Rect(19f, 8.4f, 27f, 10f)),
        HazardSpec(HazardKind.DEEP_WATER, Rect(34f, 8.4f, 43f, 10f)),
        HazardSpec(HazardKind.DEEP_WATER, Rect(51f, 8.4f, 59f, 10f)),
      ),
      checkpoints = listOf(CheckpointSpec(37.5f, 2.6f)),
      intro = "The wind remembers the old sky-road. Keep moving when the stones do.",
      outro = "Beyond the ruins, the Foundry paints the horizon red.",
    )

  private fun emberFoundry(): LevelDefinition =
    LevelDefinition(
      id = 4,
      name = "Ember Foundry",
      subtitle = "Where the stolen crown was reforged",
      biome = Biome.FOUNDRY,
      width = 70f,
      start = Vec2(1.2f, 7.1f),
      goal = Rect(67.1f, 6f, 68.4f, 8.2f),
      timeLimitSeconds = 230,
      platforms = ground(0f to 11f, 15f to 9f, 28f to 10f, 42f to 9f, 55f to 15f) + listOf(
        ledge(4f, 5.5f, 3f), ledge(9f, 4f, 2f), moving(11f, 6.7f, 4f, moveX = 3f),
        ledge(17f, 5.6f, 2.5f), ledge(21f, 4.1f, 2f), moving(24f, 6.8f, 4f, moveY = -2.5f),
        ledge(30f, 5f, 3f), ledge(35f, 3.7f, 2f), breakable(37.5f, 5.2f, 1.3f), moving(39f, 6.5f, 3f, moveX = 3f),
        ledge(44f, 5.2f, 3f), ledge(49f, 3.8f, 2f), moving(51f, 6.6f, 4f, moveY = -2.8f),
        ledge(58f, 5.2f, 3f), ledge(63f, 4f, 2.5f),
      ),
      enemies = listOf(
        EnemySpec(EnemyKind.ARMORED, 7f, 7.38f, 3f, 10f), EnemySpec(EnemyKind.TURRET, 18f, 7.38f),
        EnemySpec(EnemyKind.FLYER, 31f, 3.5f, 28f, 38f), EnemySpec(EnemyKind.TURRET, 46f, 7.38f),
        EnemySpec(EnemyKind.ARMORED, 59f, 7.38f, 56f, 66f), EnemySpec(EnemyKind.FLYER, 64f, 3f, 60f, 68f),
      ),
      pickups = crystals(3f, 6, 6.6f) + crystals(17f, 6, 4.8f) + crystals(30f, 7, 4.2f) + crystals(56f, 8, 6.5f) + listOf(
        PickupSpec(PickupKind.MOONCAP, 22f, 3.3f), PickupSpec(PickupKind.EMBER_BLOOM, 36f, 2.9f),
        PickupSpec(PickupKind.HEART, 50f, 3f), PickupSpec(PickupKind.CROWN_SHARD, 64f, 3.2f),
      ),
      hazards = listOf(
        HazardSpec(HazardKind.LAVA, Rect(11f, 8.05f, 15f, 10f)), HazardSpec(HazardKind.LAVA, Rect(24f, 8.05f, 28f, 10f)),
        HazardSpec(HazardKind.LAVA, Rect(38f, 8.05f, 42f, 10f)), HazardSpec(HazardKind.LAVA, Rect(51f, 8.05f, 55f, 10f)),
      ),
      checkpoints = listOf(CheckpointSpec(35f, 2.7f)),
      intro = "The Foundry feeds on Crownlight. Ember blooms can answer its armored guards.",
      outro = "The furnaces dim, and a moonlit marsh opens beyond the gate.",
    )

  private fun mooncapMarsh(): LevelDefinition =
    LevelDefinition(
      id = 5,
      name = "Mooncap Marsh",
      subtitle = "Lanterns adrift in violet mist",
      biome = Biome.MARSH,
      width = 72f,
      start = Vec2(1.2f, 7.1f),
      goal = Rect(69f, 5.8f, 70.4f, 8.2f),
      timeLimitSeconds = 240,
      platforms = ground(0f to 10f, 14f to 8f, 26f to 9f, 40f to 10f, 55f to 17f) + listOf(
        ledge(4f, 5.8f, 3f), moving(10f, 7f, 4f, moveY = -2.2f), ledge(16f, 5f, 3f),
        moving(22f, 6.8f, 4f, moveX = 4f), ledge(28f, 4.9f, 3f), ledge(33f, 3.5f, 2f),
        moving(35f, 6.8f, 5f, moveY = -3f), breakable(40.5f, 6.2f, 1.2f), ledge(42f, 5f, 3f), moving(50f, 6.6f, 5f, moveX = 4f),
        ledge(57f, 5.2f, 3f), ledge(62f, 3.8f, 3f), ledge(66f, 5.4f, 3f),
      ),
      enemies = listOf(
        EnemySpec(EnemyKind.WALKER, 6f, 7.38f, 2f, 9f), EnemySpec(EnemyKind.FLYER, 18f, 3.5f, 14f, 24f),
        EnemySpec(EnemyKind.ARMORED, 29f, 7.38f, 27f, 34f), EnemySpec(EnemyKind.TURRET, 43f, 7.38f),
        EnemySpec(EnemyKind.FLYER, 53f, 3f, 48f, 58f), EnemySpec(EnemyKind.ARMORED, 64f, 7.38f, 57f, 69f),
      ),
      pickups = crystals(3f, 6, 6.5f) + crystals(16f, 7, 4.2f) + crystals(28f, 6, 4.1f) + crystals(57f, 9, 4.4f) + listOf(
        PickupSpec(PickupKind.MOONCAP, 34f, 2.7f), PickupSpec(PickupKind.EMBER_BLOOM, 44f, 4.2f),
        PickupSpec(PickupKind.HEART, 63f, 3f), PickupSpec(PickupKind.CROWN_SHARD, 68f, 4.6f),
      ),
      hazards = listOf(
        HazardSpec(HazardKind.DEEP_WATER, Rect(10f, 8f, 14f, 10f)), HazardSpec(HazardKind.DEEP_WATER, Rect(22f, 8f, 26f, 10f)),
        HazardSpec(HazardKind.DEEP_WATER, Rect(35f, 8f, 40f, 10f)), HazardSpec(HazardKind.DEEP_WATER, Rect(50f, 8f, 55f, 10f)),
      ),
      checkpoints = listOf(CheckpointSpec(40.8f, 7.1f)),
      intro = "The marsh lights lie, but the old mooncaps always lean toward the keep.",
      outro = "Crownfall Keep rises from the fog. The royal signal still burns.",
    )

  private fun crownfallKeep(): LevelDefinition =
    LevelDefinition(
      id = 6,
      name = "Crownfall Keep",
      subtitle = "The last climb to the star chamber",
      biome = Biome.KEEP,
      width = 82f,
      start = Vec2(1.2f, 7.1f),
      goal = Rect(78.8f, 5.2f, 80.5f, 8.2f),
      timeLimitSeconds = 300,
      platforms = ground(0f to 14f, 18f to 11f, 33f to 10f, 47f to 13f, 64f to 18f) + listOf(
        ledge(5f, 5.6f, 3f), ledge(10f, 4.1f, 2.5f), moving(14f, 6.7f, 4f, moveX = 3f),
        ledge(20f, 5.2f, 3f), moving(25f, 6.4f, 4f, moveY = -2.5f), ledge(29f, 3.8f, 3f),
        moving(43f, 6.6f, 4f, moveX = 4f), ledge(49f, 4.8f, 3f), breakable(52.5f, 5.5f, 1.3f), ledge(54f, 3.5f, 3f),
        moving(60f, 6.5f, 4f, moveY = -2.5f), ledge(66f, 5f, 3f), ledge(71f, 3.8f, 3f),
        ledge(76f, 6.3f, 5f),
      ),
      enemies = listOf(
        EnemySpec(EnemyKind.ARMORED, 7f, 7.38f, 2f, 13f), EnemySpec(EnemyKind.TURRET, 21f, 7.38f),
        EnemySpec(EnemyKind.FLYER, 29f, 3f, 24f, 34f), EnemySpec(EnemyKind.ARMORED, 36f, 7.38f, 34f, 42f),
        EnemySpec(EnemyKind.TURRET, 50f, 7.38f), EnemySpec(EnemyKind.FLYER, 58f, 3f, 54f, 64f),
        EnemySpec(EnemyKind.BOSS, 70f, 6.1f, 66f, 77f),
      ),
      pickups = crystals(3f, 7, 6.5f) + crystals(20f, 7, 4.4f) + crystals(34f, 7, 6.5f) + crystals(49f, 8, 4f) + listOf(
        PickupSpec(PickupKind.MOONCAP, 30f, 3f), PickupSpec(PickupKind.EMBER_BLOOM, 55f, 2.7f),
        PickupSpec(PickupKind.HEART, 65f, 4.2f), PickupSpec(PickupKind.CROWN_SHARD, 73f, 3f),
      ),
      hazards = listOf(
        HazardSpec(HazardKind.SPIKES, Rect(14f, 7.75f, 18f, 8.2f)), HazardSpec(HazardKind.LAVA, Rect(29f, 8f, 33f, 10f)),
        HazardSpec(HazardKind.SPIKES, Rect(43f, 7.75f, 47f, 8.2f)), HazardSpec(HazardKind.LAVA, Rect(60f, 8f, 64f, 10f)),
      ),
      checkpoints = listOf(CheckpointSpec(47.8f, 7.1f), CheckpointSpec(65f, 7.1f)),
      npcs = listOf(
        NpcSpec("King Rowan", "Keeper of the Dawn Crown", 79f, 6.8f),
        NpcSpec("Princess Astra", "Cartographer of the Sky Roads", 80f, 6.8f),
      ),
      intro = "King Rowan and Princess Astra wait beyond the star chamber. Break the Warden's hold.",
      outro = "The Crownlight returns. Every road in the realm shines open once more.",
    )
}
