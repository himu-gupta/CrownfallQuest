package com.himugupta.crownfallquest.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CombatTest {
  @Test fun stompingDefeatsWalkerAndBouncesPlayer() {
    val engine = GameEngine(testLevel(enemies = listOf(EnemySpec(EnemyKind.WALKER, 3f, 7.18f, 3f, 4f))))
    engine.state.player.position.x = 3f
    engine.state.player.position.y = 6.1f
    engine.state.player.velocity.y = 7f
    engine.advance(0.08f)
    assertFalse(engine.state.enemies.single().alive)
    assertTrue(engine.state.player.velocity.y < 0f)
    assertTrue(engine.state.score >= 200)
  }

  @Test fun emberProjectileDefeatsEnemy() {
    val engine = GameEngine(
      testLevel(
        enemies = listOf(EnemySpec(EnemyKind.WALKER, 3f, 7.18f, 3f, 4f)),
        pickups = listOf(PickupSpec(PickupKind.EMBER_BLOOM, 1f, 7f)),
      ),
    )
    engine.advance(0.05f)
    engine.update(GameEngine.FIXED_STEP, GameInput(firePressed = true))
    engine.advance(0.4f)
    assertFalse(engine.state.enemies.single().alive)
  }

  @Test fun poweredPlayerLosesOneFormBeforeLosingLife() {
    val engine = GameEngine(
      testLevel(
        enemies = listOf(EnemySpec(EnemyKind.TURRET, 1f, 7f)),
        pickups = listOf(PickupSpec(PickupKind.EMBER_BLOOM, 1f, 7f)),
      ),
    )
    engine.advance(0.8f)
    assertEquals(HeroForm.GUARDIAN, engine.state.player.form)
    assertEquals(3, engine.state.player.lives)
  }

  @Test fun scoutContactCostsOneLifeAndRespawns() {
    val engine = GameEngine(testLevel(enemies = listOf(EnemySpec(EnemyKind.TURRET, 1f, 7f))))
    engine.advance(0.05f)
    assertEquals(2, engine.state.player.lives)
    assertEquals(engine.state.respawnPoint.x, engine.state.player.position.x, 0.01f)
  }

  @Test fun bossBlocksGoalUntilDefeated() {
    val level = testLevel(
      goal = Rect(1f, 6f, 2.5f, 8f),
      enemies = listOf(EnemySpec(EnemyKind.BOSS, 8f, 5.9f, 8f, 12f)),
    )
    val engine = GameEngine(level)
    engine.advance(0.05f)
    assertEquals(GameMode.PLAYING, engine.state.mode)
    engine.state.enemies.single().alive = false
    engine.advance(0.05f)
    assertEquals(GameMode.LEVEL_COMPLETE, engine.state.mode)
  }
}
