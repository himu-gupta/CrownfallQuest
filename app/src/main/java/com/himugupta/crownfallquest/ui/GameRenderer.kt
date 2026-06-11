package com.himugupta.crownfallquest.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.himugupta.crownfallquest.game.Biome
import com.himugupta.crownfallquest.game.EnemyKind
import com.himugupta.crownfallquest.game.Facing
import com.himugupta.crownfallquest.game.GameMode
import com.himugupta.crownfallquest.game.GameState
import com.himugupta.crownfallquest.game.HazardKind
import com.himugupta.crownfallquest.game.HeroForm
import com.himugupta.crownfallquest.game.PickupKind
import com.himugupta.crownfallquest.game.Rect
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class GameRenderer {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
  private var scale = 1f
  private var cameraX = 0f

  fun draw(canvas: Canvas, state: GameState, controls: ControlVisualState) {
    scale = canvas.height / state.level.height
    cameraX = state.cameraX
    val palette = Palette.forBiome(state.level.biome)
    drawBackdrop(canvas, state, palette)
    drawWorld(canvas, state, palette)
    drawHud(canvas, state)
    drawControls(canvas, state, controls)
    drawMessage(canvas, state)
  }

  private fun drawBackdrop(canvas: Canvas, state: GameState, palette: Palette) {
    canvas.drawColor(palette.sky)
    paint.color = palette.sun
    canvas.drawCircle(canvas.width * 0.78f, canvas.height * 0.2f, canvas.height * 0.1f, paint)

    repeat(4) { layer ->
      val depth = (layer + 1) * 0.08f
      val baseY = canvas.height * (0.52f + layer * 0.08f)
      val shift = -(state.cameraX * scale * depth) % (canvas.width * 0.62f)
      paint.color = palette.hills[layer]
      val path = Path().apply {
        moveTo(-canvas.width * 0.3f + shift, canvas.height.toFloat())
        var x = -canvas.width * 0.3f + shift
        while (x < canvas.width * 1.4f) {
          lineTo(x, baseY)
          lineTo(x + canvas.width * 0.16f, baseY - canvas.height * (0.14f - layer * 0.015f))
          lineTo(x + canvas.width * 0.32f, baseY)
          x += canvas.width * 0.3f
        }
        lineTo(canvas.width * 1.4f, canvas.height.toFloat())
        close()
      }
      canvas.drawPath(path, paint)
    }

    paint.color = Color.argb(95, 255, 255, 255)
    repeat(8) { index ->
      val x = ((index * 223f - state.cameraX * scale * 0.16f) % (canvas.width + 220f)) - 80f
      val y = canvas.height * (0.12f + (index % 3) * 0.1f)
      canvas.drawOval(RectF(x, y, x + 95f, y + 24f), paint)
    }
  }

  private fun drawWorld(canvas: Canvas, state: GameState, palette: Palette) {
    state.platforms.filter { !it.broken }.forEach { platform ->
      val rect = screenRect(platform.bounds)
      paint.color = if (platform.spec.oneWay) palette.ledge else palette.ground
      canvas.drawRoundRect(rect, 5f, 5f, paint)
      paint.color = palette.grass
      canvas.drawRect(rect.left, rect.top, rect.right, rect.top + maxOf(4f, scale * 0.13f), paint)
      if (platform.spec.movePeriod > 0f) {
        stroke.color = Color.argb(130, 255, 255, 255)
        stroke.strokeWidth = 2f
        canvas.drawRoundRect(rect, 5f, 5f, stroke)
      }
    }

    state.level.hazards.forEach { hazard ->
      val rect = screenRect(hazard.rect)
      when (hazard.kind) {
        HazardKind.SPIKES -> drawSpikes(canvas, rect)
        HazardKind.LAVA -> {
          paint.color = Color.rgb(229, 68, 40)
          canvas.drawRect(rect, paint)
          paint.color = Color.rgb(255, 190, 55)
          repeat(5) { i -> canvas.drawCircle(rect.left + rect.width() * (i + 0.5f) / 5f, rect.top + sin(state.elapsedSeconds * 4 + i) * 5f, 5f, paint) }
        }
        HazardKind.DEEP_WATER -> {
          paint.color = Color.rgb(44, 115, 156)
          canvas.drawRect(rect, paint)
          stroke.color = Color.rgb(145, 225, 234)
          stroke.strokeWidth = 3f
          canvas.drawLine(rect.left, rect.top + 3f, rect.right, rect.top + 3f, stroke)
        }
      }
    }

    state.level.checkpoints.forEachIndexed { index, checkpoint ->
      val x = sx(checkpoint.x)
      val y = sy(checkpoint.y)
      paint.color = if (index <= state.checkpointIndex) Color.rgb(255, 215, 95) else Color.rgb(110, 124, 132)
      canvas.drawRect(x, y - scale * 1.5f, x + scale * 0.1f, y + scale * 0.5f, paint)
      val flag = Path().apply {
        moveTo(x + scale * 0.1f, y - scale * 1.45f)
        lineTo(x + scale * 0.85f, y - scale * 1.15f)
        lineTo(x + scale * 0.1f, y - scale * 0.85f)
        close()
      }
      canvas.drawPath(flag, paint)
    }

    state.pickups.filter { !it.collected }.forEach { pickup ->
      val bob = sin(state.elapsedSeconds * 3.2f + pickup.id) * scale * 0.08f
      val rect = screenRect(pickup.bounds).apply { offset(0f, bob) }
      drawPickup(canvas, pickup.spec.kind, rect, state.elapsedSeconds)
    }

    state.level.npcs.forEachIndexed { index, npc -> drawNpc(canvas, sx(npc.x), sy(npc.y), index) }
    drawGoal(canvas, state)
    state.enemies.filter { it.alive }.forEach { drawEnemy(canvas, it, state.elapsedSeconds) }
    state.projectiles.forEach { projectile ->
      val rect = screenRect(projectile.bounds)
      paint.color = if (projectile.friendly) Color.rgb(255, 143, 48) else Color.rgb(178, 82, 218)
      canvas.drawOval(rect, paint)
      paint.color = Color.argb(120, 255, 240, 180)
      canvas.drawOval(RectF(rect.left - rect.width(), rect.top + rect.height() * 0.25f, rect.left + 3f, rect.bottom - rect.height() * 0.25f), paint)
    }
    drawPlayer(canvas, state)
  }

  private fun drawPlayer(canvas: Canvas, state: GameState) {
    val player = state.player
    if (player.invulnerableSeconds > 0f && (player.animationSeconds * 12).toInt() % 2 == 0) return
    val rect = screenRect(player.bounds)
    val running = player.onGround && abs(player.velocity.x) > 0.5f
    val stride = if (running) sin(player.animationSeconds * 16f) * rect.width() * 0.18f else 0f
    val squash = if (!player.onGround) 0.04f else abs(sin(player.animationSeconds * 10f)) * 0.015f
    val body = RectF(rect.left, rect.top + rect.height() * squash, rect.right, rect.bottom)

    paint.color = Color.argb(130, 24, 30, 40)
    canvas.drawOval(RectF(body.left - 3f, body.bottom - 6f, body.right + 3f, body.bottom + 5f), paint)

    paint.color = Color.rgb(245, 174, 78)
    canvas.drawOval(RectF(body.left + body.width() * 0.2f + stride, body.bottom - body.height() * 0.2f, body.left + body.width() * 0.48f + stride, body.bottom), paint)
    canvas.drawOval(RectF(body.right - body.width() * 0.48f - stride, body.bottom - body.height() * 0.2f, body.right - body.width() * 0.2f - stride, body.bottom), paint)

    paint.color = when (player.form) {
      HeroForm.SCOUT -> Color.rgb(28, 126, 126)
      HeroForm.GUARDIAN -> Color.rgb(37, 112, 105)
      HeroForm.EMBER -> Color.rgb(176, 67, 49)
    }
    canvas.drawRoundRect(RectF(body.left + body.width() * 0.12f, body.top + body.height() * 0.25f, body.right - body.width() * 0.12f, body.bottom - body.height() * 0.14f), body.width() * 0.28f, body.width() * 0.28f, paint)

    paint.color = Color.rgb(255, 210, 152)
    canvas.drawOval(RectF(body.left + body.width() * 0.16f, body.top + body.height() * 0.03f, body.right - body.width() * 0.16f, body.top + body.height() * 0.5f), paint)
    paint.color = Color.rgb(20, 84, 88)
    val hood = Path().apply {
      moveTo(body.left + body.width() * 0.05f, body.top + body.height() * 0.3f)
      lineTo(body.centerX(), body.top - body.height() * 0.08f)
      lineTo(body.right - body.width() * 0.05f, body.top + body.height() * 0.3f)
      close()
    }
    canvas.drawPath(hood, paint)

    val eyeX = if (player.facing == Facing.RIGHT) body.right - body.width() * 0.3f else body.left + body.width() * 0.3f
    paint.color = Color.rgb(31, 37, 48)
    canvas.drawCircle(eyeX, body.top + body.height() * 0.26f, maxOf(2.4f, body.width() * 0.045f), paint)

    paint.color = Color.rgb(238, 107, 43)
    val scarfDirection = -player.facing.sign
    val scarfY = body.top + body.height() * 0.42f + sin(player.animationSeconds * 9f) * 3f
    val scarf = Path().apply {
      moveTo(body.centerX(), scarfY)
      lineTo(body.centerX() + scarfDirection * body.width() * 0.8f, scarfY + stride * 0.3f)
      lineTo(body.centerX() + scarfDirection * body.width() * 0.6f, scarfY + body.height() * 0.11f)
      close()
    }
    canvas.drawPath(scarf, paint)

    if (player.form == HeroForm.EMBER) {
      paint.color = Color.rgb(255, 197, 63)
      canvas.drawCircle(body.centerX(), body.top + body.height() * 0.58f, body.width() * 0.11f, paint)
    }
  }

  private fun drawEnemy(canvas: Canvas, enemy: com.himugupta.crownfallquest.game.EnemyState, time: Float) {
    val rect = screenRect(enemy.bounds)
    when (enemy.spec.kind) {
      EnemyKind.WALKER -> {
        paint.color = Color.rgb(103, 76, 130)
        canvas.drawOval(rect, paint)
        drawEnemyEyes(canvas, rect, enemy.facing)
        paint.color = Color.rgb(60, 50, 78)
        canvas.drawCircle(rect.left + rect.width() * 0.28f, rect.bottom, rect.width() * 0.13f, paint)
        canvas.drawCircle(rect.right - rect.width() * 0.28f, rect.bottom, rect.width() * 0.13f, paint)
      }
      EnemyKind.FLYER -> {
        paint.color = Color.rgb(93, 84, 145)
        canvas.drawOval(rect, paint)
        paint.color = Color.argb(180, 210, 235, 255)
        val wing = abs(sin(time * 11f)) * rect.height() * 0.34f
        canvas.drawOval(RectF(rect.left - rect.width() * 0.45f, rect.centerY() - wing, rect.left + rect.width() * 0.2f, rect.centerY() + wing), paint)
        canvas.drawOval(RectF(rect.right - rect.width() * 0.2f, rect.centerY() - wing, rect.right + rect.width() * 0.45f, rect.centerY() + wing), paint)
        drawEnemyEyes(canvas, rect, enemy.facing)
      }
      EnemyKind.TURRET -> {
        paint.color = Color.rgb(70, 77, 89)
        canvas.drawRoundRect(rect, 7f, 7f, paint)
        paint.color = Color.rgb(187, 108, 69)
        canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() * 0.24f, paint)
        stroke.color = Color.rgb(33, 36, 44)
        stroke.strokeWidth = rect.width() * 0.14f
        canvas.drawLine(rect.centerX(), rect.centerY(), rect.centerX() + enemy.facing.sign * rect.width() * 0.6f, rect.centerY(), stroke)
      }
      EnemyKind.ARMORED -> {
        paint.color = Color.rgb(95, 105, 116)
        canvas.drawRoundRect(rect, rect.width() * 0.2f, rect.width() * 0.2f, paint)
        stroke.color = Color.rgb(205, 169, 86)
        stroke.strokeWidth = 4f
        canvas.drawRoundRect(rect, rect.width() * 0.2f, rect.width() * 0.2f, stroke)
        drawEnemyEyes(canvas, rect, enemy.facing)
      }
      EnemyKind.BOSS -> {
        paint.color = if ((enemy.stateSeconds * 8).toInt() % 2 == 0) Color.rgb(74, 47, 91) else Color.rgb(91, 54, 104)
        canvas.drawRoundRect(rect, rect.width() * 0.2f, rect.width() * 0.2f, paint)
        paint.color = Color.rgb(214, 153, 65)
        val crown = Path().apply {
          moveTo(rect.left + rect.width() * 0.18f, rect.top + rect.height() * 0.12f)
          lineTo(rect.left + rect.width() * 0.28f, rect.top - rect.height() * 0.18f)
          lineTo(rect.centerX(), rect.top + rect.height() * 0.02f)
          lineTo(rect.right - rect.width() * 0.24f, rect.top - rect.height() * 0.18f)
          lineTo(rect.right - rect.width() * 0.14f, rect.top + rect.height() * 0.14f)
          close()
        }
        canvas.drawPath(crown, paint)
        drawEnemyEyes(canvas, rect, enemy.facing)
        paint.color = Color.argb(180, 25, 20, 31)
        canvas.drawRect(rect.left, rect.top - 14f, rect.right, rect.top - 7f, paint)
        paint.color = Color.rgb(232, 76, 72)
        canvas.drawRect(rect.left, rect.top - 14f, rect.left + rect.width() * enemy.health / 8f, rect.top - 7f, paint)
      }
    }
  }

  private fun drawEnemyEyes(canvas: Canvas, rect: RectF, facing: Facing) {
    paint.color = Color.WHITE
    val x = if (facing == Facing.RIGHT) rect.right - rect.width() * 0.28f else rect.left + rect.width() * 0.28f
    canvas.drawCircle(x, rect.top + rect.height() * 0.38f, rect.width() * 0.09f, paint)
    paint.color = Color.rgb(35, 28, 45)
    canvas.drawCircle(x + facing.sign * rect.width() * 0.025f, rect.top + rect.height() * 0.39f, rect.width() * 0.04f, paint)
  }

  private fun drawPickup(canvas: Canvas, kind: PickupKind, rect: RectF, time: Float) {
    when (kind) {
      PickupKind.SUN_CRYSTAL, PickupKind.CROWN_SHARD -> {
        paint.color = if (kind == PickupKind.SUN_CRYSTAL) Color.rgb(255, 212, 82) else Color.rgb(151, 232, 238)
        val spin = abs(sin(time * 4f)) * 0.55f + 0.45f
        val cx = rect.centerX()
        val halfWidth = rect.width() * 0.45f * spin
        val path = Path().apply {
          moveTo(cx, rect.top)
          lineTo(cx + halfWidth, rect.centerY())
          lineTo(cx, rect.bottom)
          lineTo(cx - halfWidth, rect.centerY())
          close()
        }
        canvas.drawPath(path, paint)
      }
      PickupKind.MOONCAP -> {
        paint.color = Color.rgb(239, 218, 171)
        canvas.drawRoundRect(RectF(rect.centerX() - rect.width() * 0.13f, rect.centerY(), rect.centerX() + rect.width() * 0.13f, rect.bottom), 4f, 4f, paint)
        paint.color = Color.rgb(125, 76, 145)
        canvas.drawArc(rect, 180f, 180f, true, paint)
        paint.color = Color.rgb(235, 191, 99)
        canvas.drawCircle(rect.centerX() - rect.width() * 0.2f, rect.top + rect.height() * 0.3f, rect.width() * 0.07f, paint)
      }
      PickupKind.EMBER_BLOOM -> {
        paint.color = Color.rgb(255, 121, 52)
        repeat(6) { index ->
          val angle = index * PI.toFloat() / 3f
          canvas.drawCircle(rect.centerX() + sin(angle) * rect.width() * 0.26f, rect.centerY() + kotlin.math.cos(angle) * rect.height() * 0.26f, rect.width() * 0.2f, paint)
        }
        paint.color = Color.rgb(255, 224, 90)
        canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() * 0.18f, paint)
      }
      PickupKind.HEART -> {
        paint.color = Color.rgb(231, 73, 93)
        val path = Path().apply {
          moveTo(rect.centerX(), rect.bottom)
          cubicTo(rect.left, rect.centerY(), rect.left, rect.top, rect.centerX(), rect.top + rect.height() * 0.28f)
          cubicTo(rect.right, rect.top, rect.right, rect.centerY(), rect.centerX(), rect.bottom)
        }
        canvas.drawPath(path, paint)
      }
    }
  }

  private fun drawSpikes(canvas: Canvas, rect: RectF) {
    paint.color = Color.rgb(198, 205, 210)
    val count = maxOf(2, (rect.width() / maxOf(16f, scale * 0.35f)).toInt())
    repeat(count) { index ->
      val left = rect.left + rect.width() * index / count
      val right = rect.left + rect.width() * (index + 1) / count
      val path = Path().apply {
        moveTo(left, rect.bottom)
        lineTo((left + right) * 0.5f, rect.top)
        lineTo(right, rect.bottom)
        close()
      }
      canvas.drawPath(path, paint)
    }
  }

  private fun drawGoal(canvas: Canvas, state: GameState) {
    val rect = screenRect(state.level.goal)
    paint.color = Color.rgb(238, 199, 81)
    canvas.drawRect(rect.centerX() - 4f, rect.top, rect.centerX() + 4f, rect.bottom, paint)
    paint.color = Color.argb(130, 255, 237, 157)
    canvas.drawCircle(rect.centerX(), rect.top + rect.width() * 0.25f, rect.width() * 0.48f + sin(state.elapsedSeconds * 3f) * 4f, paint)
    stroke.color = Color.WHITE
    stroke.strokeWidth = 3f
    canvas.drawCircle(rect.centerX(), rect.top + rect.width() * 0.25f, rect.width() * 0.25f, stroke)
  }

  private fun drawNpc(canvas: Canvas, x: Float, y: Float, index: Int) {
    paint.color = if (index == 0) Color.rgb(91, 58, 116) else Color.rgb(36, 125, 139)
    canvas.drawRoundRect(RectF(x, y - scale * 1.2f, x + scale * 0.65f, y), scale * 0.22f, scale * 0.22f, paint)
    paint.color = Color.rgb(255, 211, 160)
    canvas.drawCircle(x + scale * 0.32f, y - scale * 1.15f, scale * 0.25f, paint)
    paint.color = Color.rgb(238, 190, 68)
    val crown = Path().apply {
      moveTo(x + scale * 0.1f, y - scale * 1.34f)
      lineTo(x + scale * 0.2f, y - scale * 1.58f)
      lineTo(x + scale * 0.32f, y - scale * 1.38f)
      lineTo(x + scale * 0.48f, y - scale * 1.58f)
      lineTo(x + scale * 0.56f, y - scale * 1.32f)
      close()
    }
    canvas.drawPath(crown, paint)
  }

  private fun drawHud(canvas: Canvas, state: GameState) {
    paint.color = Color.argb(170, 20, 24, 34)
    canvas.drawRoundRect(RectF(18f, 14f, canvas.width * 0.67f, 66f), 20f, 20f, paint)
    paint.color = Color.WHITE
    paint.textSize = maxOf(20f, canvas.height * 0.038f)
    paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
    val time = maxOf(0, state.level.timeLimitSeconds - state.elapsedSeconds.toInt())
    canvas.drawText("LIGHT ${state.player.lives}", 34f, 48f, paint)
    canvas.drawText("CRYSTALS ${state.crystals}", canvas.width * 0.2f, 48f, paint)
    canvas.drawText("SCORE ${state.score}", canvas.width * 0.39f, 48f, paint)
    canvas.drawText("TIME $time", canvas.width * 0.58f, 48f, paint)

    paint.color = Color.argb(170, 20, 24, 34)
    canvas.drawCircle(canvas.width - 45f, 40f, 28f, paint)
    paint.color = Color.WHITE
    canvas.drawRect(canvas.width - 54f, 28f, canvas.width - 49f, 52f, paint)
    canvas.drawRect(canvas.width - 41f, 28f, canvas.width - 36f, 52f, paint)
  }

  private fun drawControls(canvas: Canvas, state: GameState, controls: ControlVisualState) {
    val radius = canvas.height * 0.095f
    drawControlCircle(canvas, radius * 1.15f, canvas.height - radius * 1.15f, radius, controls.left, "◀")
    drawControlCircle(canvas, radius * 3.4f, canvas.height - radius * 1.15f, radius, controls.right, "▶")
    drawControlCircle(canvas, canvas.width - radius * 2.8f, canvas.height - radius * 1.15f, radius, controls.jump, "JUMP")
    if (state.player.form == HeroForm.EMBER) {
      drawControlCircle(canvas, canvas.width - radius * 0.8f, canvas.height - radius * 2.55f, radius * 0.78f, controls.fire, "EMBER")
    }
  }

  private fun drawControlCircle(canvas: Canvas, x: Float, y: Float, radius: Float, active: Boolean, label: String) {
    paint.color = if (active) Color.argb(205, 238, 151, 66) else Color.argb(118, 20, 26, 36)
    canvas.drawCircle(x, y, radius, paint)
    stroke.color = Color.argb(180, 255, 255, 255)
    stroke.strokeWidth = 3f
    canvas.drawCircle(x, y, radius, stroke)
    paint.color = Color.WHITE
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = radius * if (label.length > 2) 0.32f else 0.66f
    paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
    canvas.drawText(label, x, y + paint.textSize * 0.35f, paint)
    paint.textAlign = Paint.Align.LEFT
  }

  private fun drawMessage(canvas: Canvas, state: GameState) {
    val message = state.message ?: return
    paint.textSize = maxOf(20f, canvas.height * 0.038f)
    paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
    paint.textAlign = Paint.Align.CENTER
    val width = paint.measureText(message) + 54f
    paint.color = Color.argb(205, 24, 28, 40)
    canvas.drawRoundRect(RectF(canvas.width / 2f - width / 2f, 82f, canvas.width / 2f + width / 2f, 136f), 20f, 20f, paint)
    paint.color = Color.WHITE
    canvas.drawText(message, canvas.width / 2f, 118f, paint)
    paint.textAlign = Paint.Align.LEFT
  }

  private fun screenRect(rect: Rect): RectF = RectF(sx(rect.left), sy(rect.top), sx(rect.right), sy(rect.bottom))
  private fun sx(worldX: Float): Float = (worldX - cameraX) * scale
  private fun sy(worldY: Float): Float = worldY * scale

  private data class Palette(val sky: Int, val sun: Int, val hills: IntArray, val ground: Int, val grass: Int, val ledge: Int) {
    companion object {
      fun forBiome(biome: Biome): Palette = when (biome) {
        Biome.DAWNWOOD -> Palette(Color.rgb(123, 203, 212), Color.rgb(255, 224, 139), intArrayOf(0xFF9BC9B1.toInt(), 0xFF6FA68E.toInt(), 0xFF497C69.toInt(), 0xFF315748.toInt()), 0xFF75553D.toInt(), 0xFF6FAE56.toInt(), 0xFF947254.toInt())
        Biome.GROTTO -> Palette(0xFF173D5D.toInt(), 0xFF8FDDE0.toInt(), intArrayOf(0xFF285779.toInt(), 0xFF234B6B.toInt(), 0xFF1D3E58.toInt(), 0xFF152E43.toInt()), 0xFF344D61.toInt(), 0xFF6BC3B4.toInt(), 0xFF52758B.toInt())
        Biome.CLOUD_RUINS -> Palette(0xFF86C9EA.toInt(), 0xFFFFE8A3.toInt(), intArrayOf(0xFFC7DDE1.toInt(), 0xFFA7C4CF.toInt(), 0xFF829EAB.toInt(), 0xFF617784.toInt()), 0xFF7D7C77.toInt(), 0xFFC9B66D.toInt(), 0xFFA09B8C.toInt())
        Biome.FOUNDRY -> Palette(0xFF572E3C.toInt(), 0xFFFF8E4C.toInt(), intArrayOf(0xFF6B3841.toInt(), 0xFF512D37.toInt(), 0xFF3F2732.toInt(), 0xFF2D1F29.toInt()), 0xFF514B4A.toInt(), 0xFFB45F3D.toInt(), 0xFF77706B.toInt())
        Biome.MARSH -> Palette(0xFF3B375C.toInt(), 0xFFC4B6E8.toInt(), intArrayOf(0xFF514C72.toInt(), 0xFF3E465D.toInt(), 0xFF30424C.toInt(), 0xFF24373D.toInt()), 0xFF4A443B.toInt(), 0xFF728A52.toInt(), 0xFF6B6355.toInt())
        Biome.KEEP -> Palette(0xFF24263C.toInt(), 0xFFFFD77A.toInt(), intArrayOf(0xFF41435A.toInt(), 0xFF35374B.toInt(), 0xFF2D2E40.toInt(), 0xFF202130.toInt()), 0xFF4D4D58.toInt(), 0xFFB08B4F.toInt(), 0xFF6A6975.toInt())
      }
    }
  }
}

data class ControlVisualState(val left: Boolean, val right: Boolean, val jump: Boolean, val fire: Boolean)
