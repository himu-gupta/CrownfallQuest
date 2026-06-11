package com.himugupta.crownfallquest.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.himugupta.crownfallquest.R
import com.himugupta.crownfallquest.data.ProgressSnapshot
import com.himugupta.crownfallquest.data.ProgressStore
import com.himugupta.crownfallquest.data.ProgressTracker
import com.himugupta.crownfallquest.game.GameMode
import com.himugupta.crownfallquest.game.LevelCatalog
import com.himugupta.crownfallquest.game.LevelDefinition

private enum class AppScreen { MENU, LEVELS, GAME, CREDITS }

@Composable
fun CrownfallQuestApp(progressStore: ProgressStore) {
  val tracker = remember(progressStore) { ProgressTracker(progressStore) }
  var progress by remember { mutableStateOf(progressStore.load()) }
  var screenName by rememberSaveable { mutableStateOf(AppScreen.MENU.name) }
  var selectedLevel by rememberSaveable { mutableIntStateOf(1) }
  val screen = AppScreen.valueOf(screenName)

  when (screen) {
    AppScreen.MENU -> MainMenu(
      progress = progress,
      onPlay = {
        selectedLevel = progress.unlockedLevel
        screenName = AppScreen.GAME.name
      },
      onLevels = { screenName = AppScreen.LEVELS.name },
      onCredits = { screenName = AppScreen.CREDITS.name },
      onAudioChange = { music, sound -> progress = tracker.setAudio(music, sound) },
    )
    AppScreen.LEVELS -> LevelSelect(
      progress = progress,
      onBack = { screenName = AppScreen.MENU.name },
      onSelect = {
        selectedLevel = it
        screenName = AppScreen.GAME.name
      },
    )
    AppScreen.GAME -> GameHost(
      level = LevelCatalog.byId(selectedLevel),
      progress = progress,
      onExit = { screenName = AppScreen.MENU.name },
      onCompleted = { levelId, score, shard ->
        progress = tracker.recordCompletion(levelId, score, shard)
      },
      onNext = {
        selectedLevel = (selectedLevel + 1).coerceAtMost(LevelCatalog.levels.size)
      },
    )
    AppScreen.CREDITS -> CreditsScreen(onBack = { screenName = AppScreen.MENU.name })
  }
}

@Composable
private fun MainMenu(
  progress: ProgressSnapshot,
  onPlay: () -> Unit,
  onLevels: () -> Unit,
  onCredits: () -> Unit,
  onAudioChange: (Boolean, Boolean) -> Unit,
) {
  BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF25233A))) {
    val compactLandscape = maxWidth / maxHeight < 1.65f
    Image(
      painter = painterResource(R.drawable.title_art),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )
    Box(
      Modifier.fillMaxSize().background(
        Brush.horizontalGradient(listOf(Color(0xE6202434), Color(0x98202434), Color(0x24202434))),
      ),
    )
    if (compactLandscape) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        MenuTitle(compact = true, modifier = Modifier.weight(0.46f))
        MenuActions(
          progress = progress,
          compact = true,
          onPlay = onPlay,
          onLevels = onLevels,
          onCredits = onCredits,
          onAudioChange = onAudioChange,
          modifier = Modifier.weight(0.54f),
        )
      }
    } else {
      Column(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(0.47f).padding(horizontal = 36.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center,
      ) {
        MenuTitle(compact = false)
        MenuActions(
          progress = progress,
          compact = false,
          onPlay = onPlay,
          onLevels = onLevels,
          onCredits = onCredits,
          onAudioChange = onAudioChange,
        )
      }
    }
    if (!compactLandscape) {
      Text(
        "Original game, art direction, levels, and synthesized score",
        color = Color(0xFFDDE5E8),
        modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp),
      )
    }
  }
}

@Composable
private fun MenuTitle(compact: Boolean, modifier: Modifier = Modifier) {
  Column(modifier) {
    Text(if (compact) "CROWN\nFALL" else "CROWNFALL", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFFFD479))
    Text("QUEST", style = MaterialTheme.typography.headlineLarge, color = Color.White)
    Text(
      if (compact) "Restore the Crownlight across six realms." else "Restore the scattered Crownlight across six handcrafted realms.",
      color = Color(0xFFE8EDF2),
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.padding(top = 2.dp, bottom = if (compact) 0.dp else 10.dp),
    )
  }
}

@Composable
private fun MenuActions(
  progress: ProgressSnapshot,
  compact: Boolean,
  onPlay: () -> Unit,
  onLevels: () -> Unit,
  onCredits: () -> Unit,
  onAudioChange: (Boolean, Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.Center) {
    val primaryHeight = if (compact) 38.dp else 44.dp
    val secondaryHeight = if (compact) 34.dp else 40.dp
    val gap = if (compact) 3.dp else 6.dp
    Button(onClick = onPlay, modifier = Modifier.fillMaxWidth().height(primaryHeight).testTag("menu_play")) {
      Text(if (progress.unlockedLevel > 1) "CONTINUE: LEVEL ${progress.unlockedLevel}" else "BEGIN QUEST")
    }
    Spacer(Modifier.height(gap))
    OutlinedButton(onClick = onLevels, modifier = Modifier.fillMaxWidth().height(secondaryHeight).testTag("menu_levels")) {
      Text("LEVEL SELECT")
    }
    Spacer(Modifier.height(gap))
    OutlinedButton(onClick = onCredits, modifier = Modifier.fillMaxWidth().height(secondaryHeight).testTag("menu_credits")) {
      Text("STORY & CREDITS")
    }
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = if (compact) 0.dp else 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      AudioToggle("Music", progress.musicEnabled) { onAudioChange(it, progress.soundEnabled) }
      AudioToggle("Sound", progress.soundEnabled) { onAudioChange(progress.musicEnabled, it) }
    }
  }
}

@Composable
private fun AudioToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(label, color = Color.White)
    Switch(checked = checked, onCheckedChange = onChange, modifier = Modifier.padding(start = 6.dp))
  }
}

@Composable
private fun LevelSelect(progress: ProgressSnapshot, onBack: () -> Unit, onSelect: (Int) -> Unit) {
  BackHandler(onBack = onBack)
  Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF29263D), Color(0xFF182E36)))).padding(24.dp)) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      OutlinedButton(onClick = onBack, modifier = Modifier.testTag("levels_back")) { Text("BACK") }
      Text("Choose a realm", style = MaterialTheme.typography.headlineLarge, color = Color.White, modifier = Modifier.padding(start = 22.dp))
      Spacer(Modifier.weight(1f))
      Text("Crown shards ${progress.crownShards.size}/${LevelCatalog.levels.size}", color = Color(0xFFFFD479))
    }
    LazyRow(
      modifier = Modifier.fillMaxSize().padding(top = 22.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      items(LevelCatalog.levels) { level ->
        val unlocked = level.id <= progress.unlockedLevel
        LevelCard(
          level = level,
          unlocked = unlocked,
          bestScore = progress.bestScores[level.id] ?: 0,
          hasShard = level.id in progress.crownShards,
          onClick = { if (unlocked) onSelect(level.id) },
        )
      }
    }
  }
}

@Composable
private fun LevelCard(level: LevelDefinition, unlocked: Boolean, bestScore: Int, hasShard: Boolean, onClick: () -> Unit) {
  val colors = listOf(Color(0xFF176B6B), Color(0xFF49365E), Color(0xFF9A5538), Color(0xFF384B65), Color(0xFF4C5D45), Color(0xFF343547))
  Column(
    modifier = Modifier
      .width(270.dp)
      .fillMaxHeight(0.76f)
      .clip(RoundedCornerShape(24.dp))
      .background(if (unlocked) colors[level.id - 1] else Color(0xFF343640))
      .border(2.dp, if (hasShard) Color(0xFFFFD479) else Color(0x66FFFFFF), RoundedCornerShape(24.dp))
      .clickable(enabled = unlocked, onClick = onClick)
      .padding(22.dp)
      .testTag("level_card_${level.id}"),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    Column {
      Text(if (unlocked) "REALM ${level.id}" else "LOCKED", color = Color(0xFFFFD479), fontWeight = FontWeight.Bold)
      Text(level.name, style = MaterialTheme.typography.headlineLarge, color = Color.White, modifier = Modifier.padding(top = 8.dp))
      Text(level.subtitle, color = Color(0xFFE1E7EA), modifier = Modifier.padding(top = 8.dp))
    }
    Column {
      Text("Best score", color = Color(0xFFCCD6DA))
      Text(bestScore.toString(), style = MaterialTheme.typography.titleLarge, color = Color.White)
      Text(if (hasShard) "CROWN SHARD FOUND" else "Crown shard hidden", color = if (hasShard) Color(0xFFFFD479) else Color(0xFFCCD6DA), modifier = Modifier.padding(top = 10.dp))
    }
  }
}

@Composable
private fun GameHost(
  level: LevelDefinition,
  progress: ProgressSnapshot,
  onExit: () -> Unit,
  onCompleted: (Int, Int, Boolean) -> Unit,
  onNext: () -> Unit,
) {
  var mode by remember(level.id) { mutableStateOf(GameMode.PLAYING) }
  var score by remember(level.id) { mutableIntStateOf(0) }
  var shards by remember(level.id) { mutableIntStateOf(0) }
  var gameView by remember(level.id) { mutableStateOf<GameView?>(null) }
  var completionRecorded by remember(level.id) { mutableStateOf(false) }

  BackHandler {
    if (mode == GameMode.PAUSED) onExit() else gameView?.let {
      it.pauseGame()
    }
  }

  DisposableEffect(level.id) {
    onDispose { gameView = null }
  }

  Box(Modifier.fillMaxSize().background(Color.Black)) {
    key(level.id) {
      AndroidView(
        factory = { context ->
          GameView(
            context = context,
            level = level,
            musicEnabled = progress.musicEnabled,
            soundEnabled = progress.soundEnabled,
          ) { newMode, newScore, newShards ->
            mode = newMode
            score = newScore
            shards = newShards
            if (newMode in setOf(GameMode.LEVEL_COMPLETE, GameMode.CAMPAIGN_COMPLETE) && !completionRecorded) {
              completionRecorded = true
              onCompleted(level.id, newScore, newShards > 0)
            }
          }.also { gameView = it }
        },
        modifier = Modifier.fillMaxSize().testTag("game_surface"),
      )
    }

    when (mode) {
      GameMode.PAUSED -> GameOverlay(
        title = "Quest paused",
        subtitle = level.name,
        primary = "RESUME",
        onPrimary = { gameView?.resumeGame() },
        secondary = "RESTART",
        onSecondary = { gameView?.restartGame() },
        onExit = onExit,
      )
      GameMode.LEVEL_COMPLETE -> GameOverlay(
        title = "Realm restored",
        subtitle = "Score $score - ${level.outro}",
        primary = "NEXT REALM",
        onPrimary = {
          onNext()
          mode = GameMode.PLAYING
        },
        secondary = "PLAY AGAIN",
        onSecondary = {
          completionRecorded = false
          gameView?.restartGame()
        },
        onExit = onExit,
      )
      GameMode.CAMPAIGN_COMPLETE -> GameOverlay(
        title = "The Crownlight returns",
        subtitle = "King Rowan and Princess Astra reopen the sky roads. Final score $score.",
        primary = "PLAY AGAIN",
        onPrimary = {
          completionRecorded = false
          gameView?.restartGame()
        },
        secondary = "LEVEL SELECT",
        onSecondary = onExit,
        onExit = onExit,
      )
      GameMode.GAME_OVER -> GameOverlay(
        title = "The light fades",
        subtitle = "The checkpoint waits. Score $score.",
        primary = "TRY AGAIN",
        onPrimary = { gameView?.restartGame() },
        secondary = "MAIN MENU",
        onSecondary = onExit,
        onExit = onExit,
      )
      GameMode.PLAYING -> Unit
    }
  }
}

@Composable
private fun GameOverlay(
  title: String,
  subtitle: String,
  primary: String,
  onPrimary: () -> Unit,
  secondary: String,
  onSecondary: () -> Unit,
  onExit: () -> Unit,
) {
  Box(Modifier.fillMaxSize().background(Color(0xB8181B26)), contentAlignment = Alignment.Center) {
    Surface(shape = RoundedCornerShape(28.dp), color = Color(0xF52B3040), modifier = Modifier.fillMaxWidth(0.55f)) {
      Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = Color(0xFFFFD479), textAlign = TextAlign.Center)
        Text(subtitle, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Button(onClick = onPrimary, modifier = Modifier.testTag("overlay_primary")) { Text(primary) }
          OutlinedButton(onClick = onSecondary) { Text(secondary) }
          OutlinedButton(onClick = onExit) { Text("MENU") }
        }
      }
    }
  }
}

@Composable
private fun CreditsScreen(onBack: () -> Unit) {
  BackHandler(onBack = onBack)
  Row(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF29263D), Color(0xFF183B42)))).padding(32.dp)) {
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
      Text("The story", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFFFD479))
      Text(
        "When the Star Warden scatters the Dawn Crown, a road-mender named Pip follows its light across the realm. King Rowan guards the final beacon while Princess Astra maps a way through the sealed sky roads.",
        color = Color.White,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 14.dp),
      )
    }
    Spacer(Modifier.width(40.dp))
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
      Text("Made for this project", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFFFD479))
      Text(
        "Original game design, Kotlin engine, level layouts, characters, procedural animation, title artwork direction, and synthesized music. No copyrighted game assets or music are included.",
        color = Color.White,
        modifier = Modifier.padding(vertical = 14.dp),
      )
      Button(onClick = onBack, modifier = Modifier.testTag("credits_back")) { Text("BACK TO MENU") }
    }
  }
}
