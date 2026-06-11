package com.himugupta.crownfallquest.ui

internal enum class GameControl { LEFT, RIGHT, JUMP, FIRE, PAUSE, NONE }

internal data class ControlButton(
  val centerX: Float,
  val centerY: Float,
  val radius: Float,
) {
  fun contains(x: Float, y: Float): Boolean {
    val hitRadius = radius * 1.12f
    val dx = x - centerX
    val dy = y - centerY
    return dx * dx + dy * dy <= hitRadius * hitRadius
  }
}

internal data class GameControlLayout(
  val left: ControlButton,
  val right: ControlButton,
  val jump: ControlButton,
  val fire: ControlButton?,
  val pause: ControlButton,
) {
  fun controlAt(x: Float, y: Float): GameControl = when {
    pause.contains(x, y) -> GameControl.PAUSE
    fire?.contains(x, y) == true -> GameControl.FIRE
    jump.contains(x, y) -> GameControl.JUMP
    right.contains(x, y) -> GameControl.RIGHT
    left.contains(x, y) -> GameControl.LEFT
    else -> GameControl.NONE
  }

  companion object {
    fun forSize(width: Float, height: Float, showFire: Boolean): GameControlLayout {
      val radius = height * 0.095f
      return GameControlLayout(
        left = ControlButton(radius * 1.15f, height - radius * 1.15f, radius),
        right = ControlButton(radius * 3.4f, height - radius * 1.15f, radius),
        jump = ControlButton(width - radius * 2.8f, height - radius * 1.15f, radius),
        fire = if (showFire) ControlButton(width - radius * 0.8f, height - radius * 2.55f, radius * 0.78f) else null,
        pause = ControlButton(width - 45f, 40f, 28f),
      )
    }
  }
}
