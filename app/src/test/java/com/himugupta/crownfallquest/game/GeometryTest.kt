package com.himugupta.crownfallquest.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometryTest {
  @Test fun overlappingRectanglesReportIntersection() {
    val first = Rect(0f, 0f, 2f, 2f)
    val second = Rect(1f, 1f, 3f, 3f)
    assertTrue(first.overlaps(second))
    assertEquals(1f, first.intersectionArea(second), 0.001f)
  }

  @Test fun touchingEdgesAreNotAnOverlap() {
    assertFalse(Rect(0f, 0f, 1f, 1f).overlaps(Rect(1f, 0f, 2f, 1f)))
  }

  @Test fun expansionGrowsEveryEdge() {
    assertEquals(Rect(-1f, -1f, 3f, 3f), Rect(0f, 0f, 2f, 2f).expanded(1f))
  }

  @Test fun clampConstrainsValues() {
    assertEquals(2f, 3f.clamp(0f, 2f), 0f)
    assertEquals(0f, (-1f).clamp(0f, 2f), 0f)
  }
}
