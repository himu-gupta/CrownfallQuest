package com.himugupta.crownfallquest.game

enum class HeroForm { SCOUT, GUARDIAN, EMBER }

enum class Facing(val sign: Float) { LEFT(-1f), RIGHT(1f) }

enum class EnemyKind { WALKER, FLYER, TURRET, ARMORED, BOSS }

enum class PickupKind { SUN_CRYSTAL, MOONCAP, EMBER_BLOOM, HEART, CROWN_SHARD }

enum class HazardKind { SPIKES, LAVA, DEEP_WATER }

enum class Biome { DAWNWOOD, GROTTO, CLOUD_RUINS, FOUNDRY, MARSH, KEEP }

enum class GameMode { PLAYING, PAUSED, LEVEL_COMPLETE, GAME_OVER, CAMPAIGN_COMPLETE }

data class GameInput(
  val moveAxis: Float = 0f,
  val jumpPressed: Boolean = false,
  val jumpHeld: Boolean = false,
  val firePressed: Boolean = false,
  val pausePressed: Boolean = false,
)

data class PlatformSpec(
  val rect: Rect,
  val oneWay: Boolean = false,
  val breakable: Boolean = false,
  val moveX: Float = 0f,
  val moveY: Float = 0f,
  val movePeriod: Float = 0f,
)

data class EnemySpec(
  val kind: EnemyKind,
  val x: Float,
  val y: Float,
  val patrolStart: Float = x - 2f,
  val patrolEnd: Float = x + 2f,
)

data class PickupSpec(val kind: PickupKind, val x: Float, val y: Float)

data class HazardSpec(val kind: HazardKind, val rect: Rect)

data class CheckpointSpec(val x: Float, val y: Float)

data class NpcSpec(val name: String, val title: String, val x: Float, val y: Float)

data class LevelDefinition(
  val id: Int,
  val name: String,
  val subtitle: String,
  val biome: Biome,
  val width: Float,
  val height: Float = 10f,
  val start: Vec2,
  val goal: Rect,
  val timeLimitSeconds: Int,
  val platforms: List<PlatformSpec>,
  val enemies: List<EnemySpec>,
  val pickups: List<PickupSpec>,
  val hazards: List<HazardSpec>,
  val checkpoints: List<CheckpointSpec>,
  val npcs: List<NpcSpec> = emptyList(),
  val intro: String,
  val outro: String,
)

data class PlayerState(
  val position: Vec2,
  val velocity: Vec2 = Vec2(),
  var form: HeroForm = HeroForm.SCOUT,
  var facing: Facing = Facing.RIGHT,
  var onGround: Boolean = false,
  var coyoteSeconds: Float = 0f,
  var jumpBufferSeconds: Float = 0f,
  var invulnerableSeconds: Float = 0f,
  var fireCooldownSeconds: Float = 0f,
  var animationSeconds: Float = 0f,
  var lives: Int = 3,
) {
  val width: Float get() = if (form == HeroForm.SCOUT) 0.72f else 0.86f
  val height: Float get() = if (form == HeroForm.SCOUT) 0.94f else 1.36f
  val bounds: Rect get() = rectAt(position, width, height)
}

data class PlatformState(
  val spec: PlatformSpec,
  val position: Vec2 = Vec2(spec.rect.left, spec.rect.top),
  var broken: Boolean = false,
) {
  val bounds: Rect
    get() = Rect(position.x, position.y, position.x + spec.rect.width, position.y + spec.rect.height)
}

data class EnemyState(
  val id: Int,
  val spec: EnemySpec,
  val position: Vec2 = Vec2(spec.x, spec.y),
  val velocity: Vec2 = Vec2(),
  var facing: Facing = Facing.LEFT,
  var health: Int = if (spec.kind == EnemyKind.BOSS) 8 else if (spec.kind == EnemyKind.ARMORED) 2 else 1,
  var alive: Boolean = true,
  var stateSeconds: Float = 0f,
  var shotCooldownSeconds: Float = 1.5f,
) {
  val width: Float get() = if (spec.kind == EnemyKind.BOSS) 1.9f else 0.82f
  val height: Float get() = if (spec.kind == EnemyKind.BOSS) 2.1f else 0.82f
  val bounds: Rect get() = rectAt(position, width, height)
}

data class PickupState(val id: Int, val spec: PickupSpec, var collected: Boolean = false) {
  val bounds: Rect get() = Rect(spec.x, spec.y, spec.x + 0.62f, spec.y + 0.62f)
}

data class ProjectileState(
  val id: Int,
  val position: Vec2,
  val velocity: Vec2,
  val friendly: Boolean,
  var alive: Boolean = true,
  var remainingSeconds: Float = 2.4f,
) {
  val bounds: Rect get() = rectAt(position, 0.34f, 0.34f)
}

sealed interface GameEvent {
  data class Sound(val cue: SoundCue) : GameEvent
  data class Message(val text: String) : GameEvent
  data class Score(val amount: Int) : GameEvent
  data class Checkpoint(val index: Int) : GameEvent
  data object PlayerDamaged : GameEvent
  data object PlayerDied : GameEvent
  data object BossDefeated : GameEvent
  data object LevelFinished : GameEvent
}

enum class SoundCue { JUMP, CRYSTAL, POWER_UP, FIRE, HIT, STOMP, CHECKPOINT, DEATH, GOAL, BOSS_HIT }

data class GameState(
  val level: LevelDefinition,
  val player: PlayerState,
  val platforms: MutableList<PlatformState>,
  val enemies: MutableList<EnemyState>,
  val pickups: MutableList<PickupState>,
  val projectiles: MutableList<ProjectileState> = mutableListOf(),
  var mode: GameMode = GameMode.PLAYING,
  var score: Int = 0,
  var crystals: Int = 0,
  var crownShards: Int = 0,
  var elapsedSeconds: Float = 0f,
  var checkpointIndex: Int = -1,
  var respawnPoint: Vec2 = level.start.copy(),
  var cameraX: Float = 0f,
  var message: String? = null,
  var messageSeconds: Float = 0f,
  var shakeSeconds: Float = 0f,
)
