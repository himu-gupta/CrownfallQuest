package com.himugupta.crownfallquest.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckpointAndLifecycleTest {
  @Test fun checkpointBecomesRespawnPoint() {
    val engine = GameEngine(testLevel(checkpoints = listOf(CheckpointSpec(2f, 7f))))
    engine.state.player.position.x = 2f
    engine.advance(0.05f)
    assertEquals(0, engine.state.checkpointIndex)
    assertEquals(2f, engine.state.respawnPoint.x, 0.01f)
  }

  @Test fun hazardCostsLifeAndUsesCheckpoint() {
    val engine = GameEngine(
      testLevel(
        checkpoints = listOf(CheckpointSpec(2f, 7f)),
        hazards = listOf(HazardSpec(HazardKind.SPIKES, Rect(5f, 7f, 6f, 8f))),
      ),
    )
    engine.state.player.position.x = 2f
    engine.advance(0.05f)
    engine.state.player.position.x = 5f
    engine.advance(0.05f)
    assertEquals(2, engine.state.player.lives)
    assertEquals(2f, engine.state.player.position.x, 0.01f)
  }

  @Test fun threeDeathsLeadToGameOver() {
    val engine = GameEngine(testLevel())
    repeat(3) { engine.respawnForTest() }
    assertEquals(GameMode.GAME_OVER, engine.state.mode)
  }

  @Test fun pauseStopsSimulationClock() {
    val engine = GameEngine(testLevel())
    engine.update(0.1f, GameInput(pausePressed = true))
    val elapsed = engine.state.elapsedSeconds
    engine.update(1f, GameInput())
    assertEquals(GameMode.PAUSED, engine.state.mode)
    assertEquals(elapsed, engine.state.elapsedSeconds, 0f)
  }

  @Test fun restartRestoresCollectiblesAndEnemies() {
    val engine = GameEngine(
      testLevel(
        pickups = listOf(PickupSpec(PickupKind.SUN_CRYSTAL, 1f, 7f)),
        enemies = listOf(EnemySpec(EnemyKind.WALKER, 3f, 7.18f)),
      ),
    )
    engine.advance(0.05f)
    engine.state.enemies.single().alive = false
    engine.restartLevel()
    assertTrue(!engine.state.pickups.single().collected)
    assertTrue(engine.state.enemies.single().alive)
  }
}
