package com.himugupta.crownfallquest.data

import android.content.Context
import com.himugupta.crownfallquest.game.LevelCatalog

data class ProgressSnapshot(
  val unlockedLevel: Int = 1,
  val bestScores: Map<Int, Int> = emptyMap(),
  val crownShards: Set<Int> = emptySet(),
  val musicEnabled: Boolean = true,
  val soundEnabled: Boolean = true,
)

interface ProgressStore {
  fun load(): ProgressSnapshot
  fun save(snapshot: ProgressSnapshot)
}

class ProgressTracker(private val store: ProgressStore) {
  fun recordCompletion(levelId: Int, score: Int, collectedCrownShard: Boolean): ProgressSnapshot {
    val current = store.load()
    val nextUnlocked = (levelId + 1).coerceAtMost(LevelCatalog.levels.size)
    val updated =
      current.copy(
        unlockedLevel = maxOf(current.unlockedLevel, nextUnlocked),
        bestScores = current.bestScores + (levelId to maxOf(score, current.bestScores[levelId] ?: 0)),
        crownShards = if (collectedCrownShard) current.crownShards + levelId else current.crownShards,
      )
    store.save(updated)
    return updated
  }

  fun setAudio(musicEnabled: Boolean, soundEnabled: Boolean): ProgressSnapshot {
    val updated = store.load().copy(musicEnabled = musicEnabled, soundEnabled = soundEnabled)
    store.save(updated)
    return updated
  }
}

class SharedPreferencesProgressStore(context: Context) : ProgressStore {
  private val preferences = context.getSharedPreferences("crownfall_progress", Context.MODE_PRIVATE)

  override fun load(): ProgressSnapshot {
    val scoreEntries = preferences.all.filterKeys { it.startsWith("score_") }
    val scores = scoreEntries.mapNotNull { (key, value) ->
      val id = key.removePrefix("score_").toIntOrNull() ?: return@mapNotNull null
      id to (value as? Int ?: 0)
    }.toMap()
    val shards = preferences.getStringSet("crown_shards", emptySet()).orEmpty().mapNotNull(String::toIntOrNull).toSet()
    return ProgressSnapshot(
      unlockedLevel = preferences.getInt("unlocked_level", 1),
      bestScores = scores,
      crownShards = shards,
      musicEnabled = preferences.getBoolean("music_enabled", true),
      soundEnabled = preferences.getBoolean("sound_enabled", true),
    )
  }

  override fun save(snapshot: ProgressSnapshot) {
    preferences.edit().apply {
      putInt("unlocked_level", snapshot.unlockedLevel)
      putStringSet("crown_shards", snapshot.crownShards.map(Int::toString).toSet())
      putBoolean("music_enabled", snapshot.musicEnabled)
      putBoolean("sound_enabled", snapshot.soundEnabled)
      snapshot.bestScores.forEach { (level, score) -> putInt("score_$level", score) }
    }.apply()
  }
}
