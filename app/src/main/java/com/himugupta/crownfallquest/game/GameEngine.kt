package com.himugupta.crownfallquest.game

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class GameEngine(
  level: LevelDefinition,
  lives: Int = 3,
  startingScore: Int = 0,
) {
  companion object {
    const val FIXED_STEP = 1f / 120f
    private const val GRAVITY = 28f
    private const val MAX_FALL_SPEED = 18f
    private const val RUN_SPEED = 6.2f
    private const val GROUND_ACCELERATION = 48f
    private const val AIR_ACCELERATION = 27f
    private const val GROUND_FRICTION = 52f
    private const val JUMP_SPEED = 11.2f
    private const val COYOTE_TIME = 0.11f
    private const val JUMP_BUFFER_TIME = 0.12f
  }

  val state: GameState = createState(level, lives, startingScore)
  private val pendingEvents = mutableListOf<GameEvent>()
  private var accumulator = 0f
  private var projectileId = 1

  fun update(realDeltaSeconds: Float, input: GameInput): List<GameEvent> {
    if (input.pausePressed && state.mode in setOf(GameMode.PLAYING, GameMode.PAUSED)) {
      state.mode = if (state.mode == GameMode.PLAYING) GameMode.PAUSED else GameMode.PLAYING
    }
    if (state.mode != GameMode.PLAYING) return drainEvents()

    accumulator = min(accumulator + realDeltaSeconds.coerceIn(0f, 0.1f), FIXED_STEP * 12f)
    var stepInput = input
    while (accumulator >= FIXED_STEP) {
      step(FIXED_STEP, stepInput)
      stepInput = stepInput.copy(jumpPressed = false, firePressed = false, pausePressed = false)
      accumulator -= FIXED_STEP
    }
    return drainEvents()
  }

  fun togglePause() {
    if (state.mode == GameMode.PLAYING) state.mode = GameMode.PAUSED
    else if (state.mode == GameMode.PAUSED) state.mode = GameMode.PLAYING
  }

  fun restartLevel() {
    val fresh = createState(state.level, lives = 3, score = 0)
    copyState(fresh)
    accumulator = 0f
  }

  fun respawnForTest() = loseLife()

  private fun step(dt: Float, input: GameInput) {
    state.elapsedSeconds += dt
    state.messageSeconds = max(0f, state.messageSeconds - dt)
    state.shakeSeconds = max(0f, state.shakeSeconds - dt)
    if (state.messageSeconds == 0f) state.message = null

    updatePlatforms()
    updatePlayerTimers(dt)
    handlePlayerInput(dt, input)
    movePlayer(dt)
    updateEnemies(dt)
    updateProjectiles(dt)
    resolvePickups()
    resolveEnemyContacts()
    resolveHazardsAndFalls()
    resolveCheckpoints()
    resolveGoal()

    state.cameraX = (state.player.position.x - 5.5f).clamp(0f, max(0f, state.level.width - 16f))
    if (state.elapsedSeconds >= state.level.timeLimitSeconds && state.mode == GameMode.PLAYING) loseLife()
  }

  private fun updatePlatforms() {
    state.platforms.forEach { platform ->
      val spec = platform.spec
      if (spec.movePeriod <= 0f) return@forEach
      val phase = sin((state.elapsedSeconds / spec.movePeriod) * (PI * 2.0)).toFloat()
      platform.position.x = spec.rect.left + spec.moveX * phase
      platform.position.y = spec.rect.top + spec.moveY * phase
    }
  }

  private fun updatePlayerTimers(dt: Float) {
    val player = state.player
    player.animationSeconds += dt
    player.invulnerableSeconds = max(0f, player.invulnerableSeconds - dt)
    player.fireCooldownSeconds = max(0f, player.fireCooldownSeconds - dt)
    player.coyoteSeconds = if (player.onGround) COYOTE_TIME else max(0f, player.coyoteSeconds - dt)
    player.jumpBufferSeconds = max(0f, player.jumpBufferSeconds - dt)
  }

  private fun handlePlayerInput(dt: Float, input: GameInput) {
    val player = state.player
    val axis = input.moveAxis.clamp(-1f, 1f)
    if (abs(axis) > 0.08f) {
      player.facing = if (axis < 0f) Facing.LEFT else Facing.RIGHT
      val acceleration = if (player.onGround) GROUND_ACCELERATION else AIR_ACCELERATION
      player.velocity.x = approach(player.velocity.x, axis * RUN_SPEED, acceleration * dt)
    } else if (player.onGround) {
      player.velocity.x = approach(player.velocity.x, 0f, GROUND_FRICTION * dt)
    }

    if (input.jumpPressed) player.jumpBufferSeconds = JUMP_BUFFER_TIME
    if (player.jumpBufferSeconds > 0f && player.coyoteSeconds > 0f) {
      player.velocity.y = -JUMP_SPEED
      player.onGround = false
      player.coyoteSeconds = 0f
      player.jumpBufferSeconds = 0f
      emit(GameEvent.Sound(SoundCue.JUMP))
    }
    if (!input.jumpHeld && player.velocity.y < -4.2f) player.velocity.y = -4.2f

    if (input.firePressed && player.form == HeroForm.EMBER && player.fireCooldownSeconds <= 0f) {
      val spawnX = if (player.facing == Facing.RIGHT) player.position.x + player.width else player.position.x - 0.34f
      state.projectiles +=
        ProjectileState(
          id = projectileId++,
          position = Vec2(spawnX, player.position.y + player.height * 0.42f),
          velocity = Vec2(10.5f * player.facing.sign, -0.6f),
          friendly = true,
        )
      player.fireCooldownSeconds = 0.32f
      emit(GameEvent.Sound(SoundCue.FIRE))
    }
  }

  private fun movePlayer(dt: Float) {
    val player = state.player
    val previous = player.bounds
    player.velocity.y = min(MAX_FALL_SPEED, player.velocity.y + GRAVITY * dt)

    player.position.x += player.velocity.x * dt
    resolveHorizontal(player)
    player.position.x = player.position.x.clamp(0f, state.level.width - player.width)

    player.onGround = false
    player.position.y += player.velocity.y * dt
    resolveVertical(player, previous)
  }

  private fun resolveHorizontal(player: PlayerState) {
    state.platforms.asSequence().filter { !it.broken && !it.spec.oneWay }.forEach { platform ->
      if (!player.bounds.overlaps(platform.bounds)) return@forEach
      if (player.velocity.x > 0f) player.position.x = platform.bounds.left - player.width
      else if (player.velocity.x < 0f) player.position.x = platform.bounds.right
      player.velocity.x = 0f
    }
  }

  private fun resolveVertical(player: PlayerState, previous: Rect) {
    for (platform in state.platforms.filter { !it.broken }) {
      val platformBounds = platform.bounds
      if (!player.bounds.overlaps(platformBounds)) continue
      val crossingTop = player.velocity.y >= 0f && previous.bottom <= platformBounds.top + 0.14f
      if (crossingTop) {
        player.position.y = platformBounds.top - player.height
        player.velocity.y = 0f
        player.onGround = true
        continue
      }
      if (platform.spec.oneWay) continue
      val crossingBottom = player.velocity.y < 0f && previous.top >= platformBounds.bottom - 0.12f
      if (crossingBottom) {
        player.position.y = platformBounds.bottom
        player.velocity.y = 0f
        if (platform.spec.breakable && player.form != HeroForm.SCOUT) {
          platform.broken = true
          addScore(75)
        }
      }
    }
  }

  private fun updateEnemies(dt: Float) {
    val player = state.player
    state.enemies.filter { it.alive }.forEach { enemy ->
      enemy.stateSeconds += dt
      enemy.shotCooldownSeconds -= dt
      when (enemy.spec.kind) {
        EnemyKind.WALKER, EnemyKind.ARMORED, EnemyKind.BOSS -> {
          val speed = if (enemy.spec.kind == EnemyKind.BOSS) 2.2f else 1.45f
          enemy.velocity.x = speed * enemy.facing.sign
          enemy.position.x += enemy.velocity.x * dt
          if (enemy.position.x <= enemy.spec.patrolStart) {
            enemy.position.x = enemy.spec.patrolStart
            enemy.facing = Facing.RIGHT
          } else if (enemy.position.x + enemy.width >= enemy.spec.patrolEnd) {
            enemy.position.x = enemy.spec.patrolEnd - enemy.width
            enemy.facing = Facing.LEFT
          }
        }
        EnemyKind.FLYER -> {
          val range = max(1f, enemy.spec.patrolEnd - enemy.spec.patrolStart)
          val phase = (enemy.stateSeconds * 0.75f) % 2f
          val progress = if (phase < 1f) phase else 2f - phase
          enemy.position.x = enemy.spec.patrolStart + range * progress
          enemy.position.y = enemy.spec.y + sin(enemy.stateSeconds * 2.2f) * 0.6f
          enemy.facing = if (phase < 1f) Facing.RIGHT else Facing.LEFT
        }
        EnemyKind.TURRET -> Unit
      }

      val canShoot = enemy.spec.kind == EnemyKind.TURRET || enemy.spec.kind == EnemyKind.BOSS
      if (canShoot && enemy.shotCooldownSeconds <= 0f && abs(player.position.x - enemy.position.x) < 10f) {
        val direction = if (player.position.x < enemy.position.x) -1f else 1f
        state.projectiles +=
          ProjectileState(
            id = projectileId++,
            position = Vec2(enemy.position.x + enemy.width * 0.5f, enemy.position.y + enemy.height * 0.35f),
            velocity = Vec2(direction * if (enemy.spec.kind == EnemyKind.BOSS) 6.2f else 4.8f, 0f),
            friendly = false,
            remainingSeconds = 3f,
          )
        enemy.shotCooldownSeconds = if (enemy.spec.kind == EnemyKind.BOSS) 1.1f else 2.2f
      }
    }
  }

  private fun updateProjectiles(dt: Float) {
    for (projectile in state.projectiles.filter { it.alive }) {
      projectile.remainingSeconds -= dt
      projectile.velocity.y += if (projectile.friendly) 5.5f * dt else 0f
      projectile.position.x += projectile.velocity.x * dt
      projectile.position.y += projectile.velocity.y * dt
      if (projectile.remainingSeconds <= 0f || projectile.position.y > state.level.height + 2f) {
        projectile.alive = false
        continue
      }
      if (state.platforms.any { !it.broken && projectile.bounds.overlaps(it.bounds) }) {
        projectile.alive = false
        continue
      }
      if (projectile.friendly) {
        val target = state.enemies.firstOrNull { it.alive && projectile.bounds.overlaps(it.bounds) }
        if (target != null) {
          projectile.alive = false
          damageEnemy(target, projectileHit = true)
        }
      } else if (projectile.bounds.overlaps(state.player.bounds)) {
        projectile.alive = false
        damagePlayer()
      }
    }
    state.projectiles.removeAll { !it.alive }
  }

  private fun resolvePickups() {
    state.pickups.filter { !it.collected && state.player.bounds.overlaps(it.bounds) }.forEach { pickup ->
      pickup.collected = true
      when (pickup.spec.kind) {
        PickupKind.SUN_CRYSTAL -> {
          state.crystals += 1
          addScore(100)
          if (state.crystals % 50 == 0) state.player.lives += 1
          emit(GameEvent.Sound(SoundCue.CRYSTAL))
        }
        PickupKind.MOONCAP -> {
          if (state.player.form == HeroForm.SCOUT) growPlayer(HeroForm.GUARDIAN)
          else addScore(500)
          showMessage("Mooncap guard awakened")
          emit(GameEvent.Sound(SoundCue.POWER_UP))
        }
        PickupKind.EMBER_BLOOM -> {
          growPlayer(HeroForm.EMBER)
          showMessage("Ember orbs ready")
          emit(GameEvent.Sound(SoundCue.POWER_UP))
        }
        PickupKind.HEART -> {
          state.player.lives += 1
          addScore(500)
          showMessage("Extra light")
          emit(GameEvent.Sound(SoundCue.POWER_UP))
        }
        PickupKind.CROWN_SHARD -> {
          state.crownShards += 1
          addScore(1000)
          showMessage("Crown shard recovered")
          emit(GameEvent.Sound(SoundCue.POWER_UP))
        }
      }
    }
  }

  private fun growPlayer(newForm: HeroForm) {
    val player = state.player
    val oldHeight = player.height
    player.form = newForm
    player.position.y -= player.height - oldHeight
    player.invulnerableSeconds = 0.7f
  }

  private fun resolveEnemyContacts() {
    val player = state.player
    for (enemy in state.enemies.filter { it.alive && player.bounds.overlaps(it.bounds) }) {
      val descendingStomp = player.velocity.y > 1f && player.bounds.bottom - enemy.bounds.top < 0.48f
      if (descendingStomp) {
        player.position.y = enemy.bounds.top - player.height
        player.velocity.y = -7.3f
        damageEnemy(enemy, projectileHit = false)
        emit(GameEvent.Sound(SoundCue.STOMP))
      } else {
        damagePlayer(enemy.position.x)
      }
    }
  }

  private fun damageEnemy(enemy: EnemyState, projectileHit: Boolean) {
    enemy.health -= 1
    if (enemy.health <= 0) {
      enemy.alive = false
      val points = if (enemy.spec.kind == EnemyKind.BOSS) 5000 else if (enemy.spec.kind == EnemyKind.ARMORED) 400 else 200
      addScore(points)
      if (enemy.spec.kind == EnemyKind.BOSS) {
        showMessage("The Star Warden has fallen")
        emit(GameEvent.BossDefeated)
      }
    } else {
      addScore(if (projectileHit) 100 else 150)
      state.shakeSeconds = 0.16f
      emit(GameEvent.Sound(if (enemy.spec.kind == EnemyKind.BOSS) SoundCue.BOSS_HIT else SoundCue.HIT))
    }
  }

  private fun damagePlayer(sourceX: Float? = null) {
    val player = state.player
    if (player.invulnerableSeconds > 0f || state.mode != GameMode.PLAYING) return
    when (player.form) {
      HeroForm.EMBER -> player.form = HeroForm.GUARDIAN
      HeroForm.GUARDIAN -> {
        val oldHeight = player.height
        player.form = HeroForm.SCOUT
        player.position.y += oldHeight - player.height
      }
      HeroForm.SCOUT -> {
        loseLife()
        return
      }
    }
    player.invulnerableSeconds = 1.5f
    player.velocity.x = if (sourceX != null && sourceX < player.position.x) 5f else -5f
    player.velocity.y = -5f
    state.shakeSeconds = 0.2f
    emit(GameEvent.PlayerDamaged)
    emit(GameEvent.Sound(SoundCue.HIT))
  }

  private fun resolveHazardsAndFalls() {
    if (state.player.position.y > state.level.height + 1f) {
      loseLife()
      return
    }
    if (state.level.hazards.any { state.player.bounds.overlaps(it.rect) }) loseLife()
  }

  private fun resolveCheckpoints() {
    state.level.checkpoints.forEachIndexed { index, checkpoint ->
      if (index <= state.checkpointIndex) return@forEachIndexed
      val area = Rect(checkpoint.x - 0.35f, checkpoint.y - 1.3f, checkpoint.x + 0.6f, checkpoint.y + 1f)
      if (state.player.bounds.overlaps(area)) {
        state.checkpointIndex = index
        state.respawnPoint = Vec2(checkpoint.x, checkpoint.y)
        addScore(250)
        showMessage("Crownlight checkpoint")
        emit(GameEvent.Checkpoint(index))
        emit(GameEvent.Sound(SoundCue.CHECKPOINT))
      }
    }
  }

  private fun resolveGoal() {
    if (!state.player.bounds.overlaps(state.level.goal)) return
    val bossAlive = state.enemies.any { it.alive && it.spec.kind == EnemyKind.BOSS }
    if (bossAlive) {
      showMessage("Defeat the Star Warden")
      return
    }
    val timeBonus = max(0, state.level.timeLimitSeconds - state.elapsedSeconds.toInt()) * 10
    addScore(timeBonus)
    state.mode = if (state.level.id == LevelCatalog.levels.last().id) GameMode.CAMPAIGN_COMPLETE else GameMode.LEVEL_COMPLETE
    emit(GameEvent.Sound(SoundCue.GOAL))
    emit(GameEvent.LevelFinished)
  }

  private fun loseLife() {
    if (state.mode != GameMode.PLAYING) return
    val player = state.player
    player.lives -= 1
    emit(GameEvent.PlayerDied)
    emit(GameEvent.Sound(SoundCue.DEATH))
    if (player.lives <= 0) {
      state.mode = GameMode.GAME_OVER
      return
    }
    player.position.x = state.respawnPoint.x
    player.position.y = state.respawnPoint.y
    player.velocity.x = 0f
    player.velocity.y = 0f
    player.form = HeroForm.SCOUT
    player.invulnerableSeconds = 1.5f
    state.projectiles.clear()
    state.elapsedSeconds = max(0f, state.elapsedSeconds - 8f)
    showMessage("Crownlight restored")
  }

  private fun addScore(amount: Int) {
    state.score += amount
    emit(GameEvent.Score(amount))
  }

  private fun showMessage(text: String) {
    state.message = text
    state.messageSeconds = 2.2f
    emit(GameEvent.Message(text))
  }

  private fun emit(event: GameEvent) {
    pendingEvents += event
  }

  private fun drainEvents(): List<GameEvent> = pendingEvents.toList().also { pendingEvents.clear() }

  private fun createState(level: LevelDefinition, lives: Int, score: Int): GameState =
    GameState(
      level = level,
      player = PlayerState(level.start.copy(), lives = lives),
      platforms = level.platforms.map(::PlatformState).toMutableList(),
      enemies = level.enemies.mapIndexed { index, spec -> EnemyState(index, spec) }.toMutableList(),
      pickups = level.pickups.mapIndexed { index, spec -> PickupState(index, spec) }.toMutableList(),
      score = score,
      respawnPoint = level.start.copy(),
      message = level.intro,
      messageSeconds = 3.5f,
    )

  private fun copyState(source: GameState) {
    state.player.position.x = source.player.position.x
    state.player.position.y = source.player.position.y
    state.player.velocity.x = 0f
    state.player.velocity.y = 0f
    state.player.form = HeroForm.SCOUT
    state.player.lives = source.player.lives
    state.platforms.clear()
    state.platforms += source.platforms
    state.enemies.clear()
    state.enemies += source.enemies
    state.pickups.clear()
    state.pickups += source.pickups
    state.projectiles.clear()
    state.mode = source.mode
    state.score = source.score
    state.crystals = 0
    state.crownShards = 0
    state.elapsedSeconds = 0f
    state.checkpointIndex = -1
    state.respawnPoint = source.respawnPoint
    state.cameraX = 0f
    state.message = source.message
    state.messageSeconds = source.messageSeconds
  }

  private fun approach(value: Float, target: Float, delta: Float): Float =
    when {
      value < target -> min(value + delta, target)
      value > target -> max(value - delta, target)
      else -> value
    }
}
