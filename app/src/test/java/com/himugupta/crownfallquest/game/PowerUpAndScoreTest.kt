package com.himugupta.crownfallquest.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PowerUpAndScoreTest {
  @Test fun mooncapGrowsScoutIntoGuardian() {
    val engine = GameEngine(testLevel(pickups = listOf(PickupSpec(PickupKind.MOONCAP, 1f, 7f))))
    engine.advance(0.05f)
    assertEquals(HeroForm.GUARDIAN, engine.state.player.form)
    assertTrue(engine.state.pickups.single().collected)
  }

  @Test fun emberBloomGrantsProjectileForm() {
    val engine = GameEngine(testLevel(pickups = listOf(PickupSpec(PickupKind.EMBER_BLOOM, 1f, 7f))))
    engine.advance(0.05f)
    assertEquals(HeroForm.EMBER, engine.state.player.form)
  }

  @Test fun crystalsIncreaseCountAndScore() {
    val engine = GameEngine(testLevel(pickups = listOf(PickupSpec(PickupKind.SUN_CRYSTAL, 1f, 7f))))
    engine.advance(0.05f)
    assertEquals(1, engine.state.crystals)
    assertEquals(100, engine.state.score)
  }

  @Test fun heartAddsALifeAndCrownShardAddsLargeBonus() {
    val engine = GameEngine(
      testLevel(
        pickups = listOf(
          PickupSpec(PickupKind.HEART, 1f, 7f),
          PickupSpec(PickupKind.CROWN_SHARD, 1.1f, 7f),
        ),
      ),
    )
    engine.advance(0.05f)
    assertEquals(4, engine.state.player.lives)
    assertEquals(1, engine.state.crownShards)
    assertEquals(1500, engine.state.score)
  }

  @Test fun goalAwardsRemainingTimeBonus() {
    val engine = GameEngine(testLevel(goal = Rect(1f, 6.5f, 2.5f, 8f)))
    engine.advance(0.05f)
    assertEquals(GameMode.LEVEL_COMPLETE, engine.state.mode)
    assertTrue(engine.state.score >= 1100)
  }
}
