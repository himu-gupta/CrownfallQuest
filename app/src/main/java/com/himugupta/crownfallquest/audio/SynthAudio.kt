package com.himugupta.crownfallquest.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.himugupta.crownfallquest.game.Biome
import com.himugupta.crownfallquest.game.SoundCue
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

class SynthAudio {
  private val sampleRate = 22_050
  private var musicTrack: AudioTrack? = null
  var musicEnabled: Boolean = true
    set(value) {
      field = value
      if (!value) stopMusic()
    }
  var soundEnabled: Boolean = true

  fun startMusic(biome: Biome) {
    if (!musicEnabled) return
    stopMusic()
    thread(name = "CrownfallMusic", isDaemon = true) {
      val data = composeLoop(biome)
      val track = createStaticTrack(data)
      track.setLoopPoints(0, data.size / 2, -1)
      musicTrack = track
      track.play()
    }
  }

  fun play(cue: SoundCue) {
    if (!soundEnabled) return
    thread(name = "CrownfallSfx", isDaemon = true) {
      val notes = when (cue) {
        SoundCue.JUMP -> listOf(440f to 0.05f, 659f to 0.08f)
        SoundCue.CRYSTAL -> listOf(880f to 0.05f, 1175f to 0.08f)
        SoundCue.POWER_UP -> listOf(523f to 0.06f, 659f to 0.06f, 784f to 0.1f)
        SoundCue.FIRE -> listOf(330f to 0.05f, 220f to 0.06f)
        SoundCue.HIT -> listOf(180f to 0.12f)
        SoundCue.STOMP -> listOf(140f to 0.07f, 220f to 0.05f)
        SoundCue.CHECKPOINT -> listOf(392f to 0.06f, 523f to 0.06f, 784f to 0.12f)
        SoundCue.DEATH -> listOf(330f to 0.1f, 247f to 0.1f, 165f to 0.2f)
        SoundCue.GOAL -> listOf(523f to 0.08f, 659f to 0.08f, 784f to 0.08f, 1047f to 0.18f)
        SoundCue.BOSS_HIT -> listOf(110f to 0.16f, 82f to 0.12f)
      }
      val data = synthesize(notes, volume = 0.34f, square = cue != SoundCue.POWER_UP)
      createStaticTrack(data).apply { play() }
    }
  }

  fun release() {
    stopMusic()
  }

  private fun stopMusic() {
    musicTrack?.runCatching { stop() }
    musicTrack?.release()
    musicTrack = null
  }

  private fun composeLoop(biome: Biome): ShortArray {
    val root = when (biome) {
      Biome.DAWNWOOD -> 261.63f
      Biome.GROTTO -> 220f
      Biome.CLOUD_RUINS -> 293.66f
      Biome.FOUNDRY -> 196f
      Biome.MARSH -> 233.08f
      Biome.KEEP -> 174.61f
    }
    val melodySteps = intArrayOf(0, 4, 7, 11, 7, 4, 2, 7, 0, 4, 9, 7, 4, 2, -1, 2)
    val notes = melodySteps.map { step -> (root * Math.pow(2.0, step / 12.0)).toFloat() to 0.24f }
    val melody = synthesize(notes, volume = 0.16f, square = false)
    val bassNotes = listOf(root / 2f to 0.96f, root * 0.75f to 0.96f, root * 0.625f to 0.96f, root * 0.75f to 0.96f)
    val bass = synthesize(bassNotes, volume = 0.12f, square = true)
    return ShortArray(maxOf(melody.size, bass.size)) { index ->
      val mixed = (melody.getOrElse(index) { 0 } + bass.getOrElse(index) { 0 }).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
      mixed.toShort()
    }
  }

  private fun synthesize(notes: List<Pair<Float, Float>>, volume: Float, square: Boolean): ShortArray {
    val output = ArrayList<Short>()
    notes.forEach { (frequency, duration) ->
      val count = (duration * sampleRate).toInt()
      repeat(count) { sample ->
        val phase = 2.0 * PI * frequency * sample / sampleRate
        val wave = if (square) if (sin(phase) >= 0) 1.0 else -1.0 else sin(phase) * 0.78 + sin(phase * 2) * 0.22
        val envelope = minOf(1.0, sample / (sampleRate * 0.012)) * minOf(1.0, (count - sample) / (sampleRate * 0.025))
        output += (wave * envelope * Short.MAX_VALUE * volume).toInt().toShort()
      }
    }
    return output.toShortArray()
  }

  private fun createStaticTrack(samples: ShortArray): AudioTrack {
    val track =
      AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
        )
        .setAudioFormat(
          AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build(),
        )
        .setBufferSizeInBytes(samples.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()
    track.write(samples, 0, samples.size)
    return track
  }
}
