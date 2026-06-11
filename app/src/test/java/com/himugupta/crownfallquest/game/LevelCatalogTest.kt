package com.himugupta.crownfallquest.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelCatalogTest {
  @Test fun campaignHasSixOrderedUniqueLevels() {
    assertEquals(6, LevelCatalog.levels.size)
    assertEquals((1..6).toList(), LevelCatalog.levels.map { it.id })
    assertEquals(6, LevelCatalog.levels.map { it.name }.toSet().size)
  }

  @Test fun everyLevelHasRequiredGameplayContent() {
    LevelCatalog.levels.forEach { level ->
      assertTrue("${level.name} needs platforms", level.platforms.isNotEmpty())
      assertTrue("${level.name} needs enemies", level.enemies.isNotEmpty())
      assertTrue("${level.name} needs crystals", level.pickups.any { it.kind == PickupKind.SUN_CRYSTAL })
      assertTrue("${level.name} needs growth", level.pickups.any { it.kind == PickupKind.MOONCAP })
      assertTrue("${level.name} needs ember power", level.pickups.any { it.kind == PickupKind.EMBER_BLOOM })
      assertTrue("${level.name} needs a shard", level.pickups.any { it.kind == PickupKind.CROWN_SHARD })
      assertTrue("${level.name} needs a breakable secret", level.platforms.any { it.breakable })
      assertTrue(level.start.x in 0f..level.width)
      assertTrue(level.goal.left > level.start.x)
      assertTrue(level.goal.right <= level.width)
      assertTrue(level.timeLimitSeconds >= 180)
    }
  }

  @Test fun campaignIncludesEveryBiome() {
    assertEquals(Biome.entries.toSet(), LevelCatalog.levels.map { it.biome }.toSet())
  }

  @Test fun finalLevelIncludesBossAndRoyalCharacters() {
    val final = LevelCatalog.levels.last()
    assertTrue(final.enemies.any { it.kind == EnemyKind.BOSS })
    assertTrue(final.npcs.any { it.name == "King Rowan" })
    assertTrue(final.npcs.any { it.name == "Princess Astra" })
  }

  @Test fun allEntitiesAreInsideLevelBounds() {
    LevelCatalog.levels.forEach { level ->
      level.platforms.forEach { assertTrue(it.rect.left >= 0f && it.rect.right <= level.width) }
      level.enemies.forEach { assertTrue(it.x in 0f..level.width) }
      level.pickups.forEach { assertTrue(it.x in 0f..level.width && it.y in 0f..level.height) }
      level.checkpoints.forEach { assertTrue(it.x in 0f..level.width) }
    }
  }
}
