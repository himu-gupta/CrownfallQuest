package com.himugupta.crownfallquest.game

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnemyAiTest {
  @Test fun walkerPatrolStaysInsideAssignedRange() {
    val engine = GameEngine(testLevel(enemies = listOf(EnemySpec(EnemyKind.WALKER, 4f, 7.18f, 3f, 6f))))
    engine.advance(6f)
    val enemy = engine.state.enemies.single()
    assertTrue(enemy.position.x >= 3f)
    assertTrue(enemy.position.x + enemy.width <= 6f + 0.01f)
  }

  @Test fun flyerOscillatesInTwoDimensions() {
    val engine = GameEngine(testLevel(enemies = listOf(EnemySpec(EnemyKind.FLYER, 4f, 4f, 2f, 8f))))
    val initialY = engine.state.enemies.single().position.y
    engine.advance(0.6f)
    val enemy = engine.state.enemies.single()
    assertNotEquals(4f, enemy.position.x)
    assertNotEquals(initialY, enemy.position.y)
  }

  @Test fun turretFiresWhenPlayerIsInRange() {
    val engine = GameEngine(testLevel(enemies = listOf(EnemySpec(EnemyKind.TURRET, 7f, 7.18f))))
    engine.advance(2.3f)
    assertTrue(engine.state.projectiles.any { !it.friendly })
  }

  @Test fun bossEscalatesFromSingleShotToStarstorm() {
    val engine = GameEngine(testLevel(enemies = listOf(EnemySpec(EnemyKind.BOSS, 7f, 5.9f, 6f, 12f))))
    val boss = engine.state.enemies.single()

    boss.health = 5
    boss.shotCooldownSeconds = 0f
    engine.advance(GameEngine.FIXED_STEP)
    assertEquals(2, boss.phase)
    assertEquals(2, engine.state.projectiles.size)

    engine.state.projectiles.clear()
    boss.health = 2
    boss.shotCooldownSeconds = 0f
    engine.advance(GameEngine.FIXED_STEP)
    assertEquals(3, boss.phase)
    assertEquals(3, engine.state.projectiles.size)
  }
}
