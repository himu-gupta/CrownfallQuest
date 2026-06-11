package com.himugupta.crownfallquest.ui

import android.content.Context
import android.graphics.Canvas
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.himugupta.crownfallquest.R
import com.himugupta.crownfallquest.audio.SynthAudio
import com.himugupta.crownfallquest.game.GameEngine
import com.himugupta.crownfallquest.game.GameEvent
import com.himugupta.crownfallquest.game.GameInput
import com.himugupta.crownfallquest.game.GameMode
import com.himugupta.crownfallquest.game.LevelDefinition

class GameView(
  context: Context,
  level: LevelDefinition,
  private val musicEnabled: Boolean,
  private val soundEnabled: Boolean,
  private val onModeChanged: (GameMode, Int, Int) -> Unit,
) : View(context) {
  private enum class Control { LEFT, RIGHT, JUMP, FIRE, PAUSE, NONE }

  private val renderer = GameRenderer()
  private val audio = SynthAudio().apply {
    this.musicEnabled = this@GameView.musicEnabled
    this.soundEnabled = this@GameView.soundEnabled
  }
  private val engine = GameEngine(level)
  private val pointerControls = mutableMapOf<Int, Control>()
  private var keyboardLeft = false
  private var keyboardRight = false
  private var keyboardJump = false
  private var jumpPressed = false
  private var firePressed = false
  private var lastFrameNanos = 0L
  private var running = false
  private var lastMode = engine.state.mode

  init {
    isFocusable = true
    isFocusableInTouchMode = true
    contentDescription = context.getString(R.string.game_view_description)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    running = true
    requestFocus()
    audio.startMusic(engine.state.level.biome)
    postOnAnimation(frame)
  }

  override fun onDetachedFromWindow() {
    running = false
    audio.release()
    super.onDetachedFromWindow()
  }

  private val frame = object : Runnable {
    override fun run() {
      if (!running) return
      val now = System.nanoTime()
      val dt = if (lastFrameNanos == 0L) 0f else (now - lastFrameNanos) / 1_000_000_000f
      lastFrameNanos = now
      val events = engine.update(dt, currentInput())
      jumpPressed = false
      firePressed = false
      events.filterIsInstance<GameEvent.Sound>().forEach { audio.play(it.cue) }
      if (engine.state.mode != lastMode) {
        lastMode = engine.state.mode
        onModeChanged(lastMode, engine.state.score, engine.state.crownShards)
      }
      invalidate()
      postOnAnimation(this)
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    renderer.draw(canvas, engine.state, visualControls())
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    when (event.actionMasked) {
      MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
        val index = event.actionIndex
        val id = event.getPointerId(index)
        val control = controlAt(event.getX(index), event.getY(index))
        pointerControls[id] = control
        if (control == Control.JUMP) jumpPressed = true
        if (control == Control.FIRE) firePressed = true
        if (control == Control.PAUSE) {
          engine.togglePause()
          lastMode = engine.state.mode
          onModeChanged(lastMode, engine.state.score, engine.state.crownShards)
        }
      }
      MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> pointerControls.remove(event.getPointerId(event.actionIndex))
      MotionEvent.ACTION_CANCEL -> pointerControls.clear()
      MotionEvent.ACTION_MOVE -> {
        repeat(event.pointerCount) { index -> pointerControls[event.getPointerId(index)] = controlAt(event.getX(index), event.getY(index)) }
      }
    }
    return true
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    when (keyCode) {
      KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_DPAD_LEFT -> keyboardLeft = true
      KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_DPAD_RIGHT -> keyboardRight = true
      KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_BUTTON_A -> {
        if (!keyboardJump) jumpPressed = true
        keyboardJump = true
      }
      KeyEvent.KEYCODE_F, KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_CTRL_LEFT -> firePressed = true
      KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_BUTTON_START -> {
        engine.togglePause()
        lastMode = engine.state.mode
        onModeChanged(lastMode, engine.state.score, engine.state.crownShards)
      }
      else -> return super.onKeyDown(keyCode, event)
    }
    return true
  }

  override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
    when (keyCode) {
      KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_DPAD_LEFT -> keyboardLeft = false
      KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_DPAD_RIGHT -> keyboardRight = false
      KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_BUTTON_A -> keyboardJump = false
      else -> return super.onKeyUp(keyCode, event)
    }
    return true
  }

  fun resumeGame() {
    if (engine.state.mode == GameMode.PAUSED) engine.togglePause()
    lastMode = engine.state.mode
    onModeChanged(lastMode, engine.state.score, engine.state.crownShards)
  }

  fun pauseGame() {
    if (engine.state.mode == GameMode.PLAYING) engine.togglePause()
    lastMode = engine.state.mode
    onModeChanged(lastMode, engine.state.score, engine.state.crownShards)
  }

  fun restartGame() {
    engine.restartLevel()
    lastMode = engine.state.mode
    onModeChanged(lastMode, engine.state.score, engine.state.crownShards)
    audio.startMusic(engine.state.level.biome)
  }

  private fun currentInput(): GameInput {
    val left = keyboardLeft || Control.LEFT in pointerControls.values
    val right = keyboardRight || Control.RIGHT in pointerControls.values
    val axis = when {
      left && !right -> -1f
      right && !left -> 1f
      else -> 0f
    }
    return GameInput(
      moveAxis = axis,
      jumpPressed = jumpPressed,
      jumpHeld = keyboardJump || Control.JUMP in pointerControls.values,
      firePressed = firePressed,
    )
  }

  private fun visualControls(): ControlVisualState =
    ControlVisualState(
      left = keyboardLeft || Control.LEFT in pointerControls.values,
      right = keyboardRight || Control.RIGHT in pointerControls.values,
      jump = keyboardJump || Control.JUMP in pointerControls.values,
      fire = Control.FIRE in pointerControls.values,
    )

  private fun controlAt(x: Float, y: Float): Control = when {
    y < height * 0.18f && x > width * 0.84f -> Control.PAUSE
    y < height * 0.56f -> Control.NONE
    x < width * 0.18f -> Control.LEFT
    x < width * 0.38f -> Control.RIGHT
    x > width * 0.84f && y < height * 0.78f -> Control.FIRE
    x > width * 0.6f -> Control.JUMP
    else -> Control.NONE
  }
}
