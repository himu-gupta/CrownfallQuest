package com.himugupta.crownfallquest.game

import kotlin.math.max
import kotlin.math.min

data class Vec2(var x: Float = 0f, var y: Float = 0f)

data class Rect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
  val width: Float get() = right - left
  val height: Float get() = bottom - top
  val centerX: Float get() = (left + right) * 0.5f
  val centerY: Float get() = (top + bottom) * 0.5f

  fun overlaps(other: Rect): Boolean =
    left < other.right && right > other.left && top < other.bottom && bottom > other.top

  fun intersectionArea(other: Rect): Float {
    val overlapWidth = max(0f, min(right, other.right) - max(left, other.left))
    val overlapHeight = max(0f, min(bottom, other.bottom) - max(top, other.top))
    return overlapWidth * overlapHeight
  }

  fun expanded(amount: Float): Rect =
    Rect(left - amount, top - amount, right + amount, bottom + amount)
}

fun rectAt(position: Vec2, width: Float, height: Float): Rect =
  Rect(position.x, position.y, position.x + width, position.y + height)

fun Float.clamp(minimum: Float, maximum: Float): Float = coerceIn(minimum, maximum)
