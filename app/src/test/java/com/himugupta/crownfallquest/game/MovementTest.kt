package com.himugupta.crownfallquest.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovementTest {
  @Test fun playerAcceleratesAndFacesMovementDirection() {
    val engine = GameEngine(testLevel())
    engine.advance(0.3f)
    val start = engine.state.player.position.x
    engine.advance(0.5f, GameInput(moveAxis = 1f))
    assertTrue(engine.state.player.position.x > start + 1f)
    assertEquals(Facing.RIGHT, engine.state.player.facing)

    engine.advance(0.2f, GameInput(moveAxis = -1f))
    assertEquals(Facing.LEFT, engine.state.player.facing)
  }

  @Test fun playerLandsOnSolidGround() {
    val engine = GameEngine(testLevel(start = Vec2(2f, 2f)))
    engine.advance(1f)
    assertTrue(engine.state.player.onGround)
    assertEquals(8f - engine.state.player.height, engine.state.player.position.y, 0.02f)
    assertEquals(0f, engine.state.player.velocity.y, 0.01f)
  }

  @Test fun oneWayPlatformCanBePassedFromBelowAndLandedOn() {
    val level = testLevel(
      start = Vec2(4f, 7f),
      platforms = listOf(
        PlatformSpec(Rect(0f, 8f, 24f, 10f)),
        PlatformSpec(Rect(3f, 5f, 7f, 5.3f), oneWay = true),
      ),
    )
    val engine = GameEngine(level)
    engine.advance(0.2f)
    engine.state.player.velocity.y = -15f
    engine.advance(0.28f, GameInput(jumpHeld = true))
    assertTrue(engine.state.player.position.y < 5f)
    engine.advance(0.9f, GameInput(jumpHeld = true))
    assertTrue(engine.state.player.onGround)
    assertEquals(5f - engine.state.player.height, engine.state.player.position.y, 0.04f)
  }

  @Test fun jumpBufferLaunchesPlayerOnLanding() {
    val engine = GameEngine(testLevel(start = Vec2(2f, 6.9f)))
    engine.update(GameEngine.FIXED_STEP, GameInput(jumpPressed = true, jumpHeld = true))
    engine.advance(0.15f, GameInput(jumpHeld = true))
    assertFalse(engine.state.player.onGround)
    assertTrue(engine.state.player.velocity.y < 0f)
  }

  @Test fun releasingJumpEarlyProducesLowerJump() {
    val held = GameEngine(testLevel())
    val released = GameEngine(testLevel())
    held.advance(0.2f)
    released.advance(0.2f)
    held.update(GameEngine.FIXED_STEP, GameInput(jumpPressed = true, jumpHeld = true))
    released.update(GameEngine.FIXED_STEP, GameInput(jumpPressed = true, jumpHeld = true))
    held.advance(0.32f, GameInput(jumpHeld = true))
    released.advance(0.32f, GameInput(jumpHeld = false))
    assertTrue(held.state.player.position.y < released.state.player.position.y - 0.3f)
  }

  @Test fun guardianBreaksBlocksFromBelow() {
    val block = PlatformSpec(Rect(3f, 5f, 4.4f, 5.72f), breakable = true)
    val engine = GameEngine(
      testLevel(
        start = Vec2(3.2f, 6.5f),
        platforms = listOf(PlatformSpec(Rect(0f, 8f, 24f, 10f)), block),
      ),
    )
    engine.state.player.form = HeroForm.GUARDIAN
    engine.state.player.position.y = 6f
    engine.state.player.velocity.y = -12f
    engine.advance(0.12f, GameInput(jumpHeld = true))
    assertTrue(engine.state.platforms.last().broken)
    assertTrue(engine.state.score >= 75)
  }
}
