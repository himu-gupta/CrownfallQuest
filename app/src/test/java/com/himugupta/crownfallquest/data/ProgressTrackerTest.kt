package com.himugupta.crownfallquest.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressTrackerTest {
  private class FakeStore(var snapshot: ProgressSnapshot = ProgressSnapshot()) : ProgressStore {
    override fun load(): ProgressSnapshot = snapshot
    override fun save(snapshot: ProgressSnapshot) { this.snapshot = snapshot }
  }

  @Test fun completionUnlocksNextLevelAndStoresBestScore() {
    val store = FakeStore()
    val result = ProgressTracker(store).recordCompletion(1, 2400, true)
    assertEquals(2, result.unlockedLevel)
    assertEquals(2400, result.bestScores[1])
    assertTrue(1 in result.crownShards)
  }

  @Test fun replayCannotReplaceBestScoreWithLowerValue() {
    val store = FakeStore(ProgressSnapshot(unlockedLevel = 3, bestScores = mapOf(2 to 5000)))
    val result = ProgressTracker(store).recordCompletion(2, 1200, false)
    assertEquals(5000, result.bestScores[2])
    assertEquals(3, result.unlockedLevel)
  }

  @Test fun finalLevelNeverUnlocksPastCampaignSize() {
    val store = FakeStore(ProgressSnapshot(unlockedLevel = 6))
    assertEquals(6, ProgressTracker(store).recordCompletion(6, 9000, true).unlockedLevel)
  }

  @Test fun audioPreferencesPersistTogether() {
    val store = FakeStore()
    val result = ProgressTracker(store).setAudio(musicEnabled = false, soundEnabled = true)
    assertEquals(false, result.musicEnabled)
    assertEquals(true, result.soundEnabled)
  }
}
