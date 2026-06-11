package com.himugupta.crownfallquest.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class GameControlLayoutTest {
  private val layout = GameControlLayout.forSize(width = 2400f, height = 1080f, showFire = true)

  @Test fun visibleButtonCentersMapToTheirOwnControls() {
    assertEquals(GameControl.LEFT, layout.controlAt(layout.left.centerX, layout.left.centerY))
    assertEquals(GameControl.RIGHT, layout.controlAt(layout.right.centerX, layout.right.centerY))
    assertEquals(GameControl.JUMP, layout.controlAt(layout.jump.centerX, layout.jump.centerY))
    assertEquals(GameControl.FIRE, layout.controlAt(layout.fire!!.centerX, layout.fire.centerY))
    assertEquals(GameControl.PAUSE, layout.controlAt(layout.pause.centerX, layout.pause.centerY))
  }

  @Test fun emptySpaceDoesNotActivateAControl() {
    assertEquals(GameControl.NONE, layout.controlAt(1200f, 540f))
  }

  @Test fun fireRegionIsDisabledUntilTheHeroHasEmberPower() {
    val noFire = GameControlLayout.forSize(width = 2400f, height = 1080f, showFire = false)
    assertEquals(GameControl.NONE, noFire.controlAt(layout.fire!!.centerX, layout.fire.centerY))
  }
}
