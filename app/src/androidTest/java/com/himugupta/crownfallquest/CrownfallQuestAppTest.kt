package com.himugupta.crownfallquest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.himugupta.crownfallquest.data.ProgressSnapshot
import com.himugupta.crownfallquest.data.ProgressStore
import com.himugupta.crownfallquest.ui.CrownfallQuestApp
import com.himugupta.crownfallquest.ui.theme.CrownfallQuestTheme
import org.junit.Rule
import org.junit.Test

class CrownfallQuestAppTest {
  @get:Rule val compose = createComposeRule()

  private class FakeStore : ProgressStore {
    private var snapshot = ProgressSnapshot(unlockedLevel = 2)
    override fun load(): ProgressSnapshot = snapshot
    override fun save(snapshot: ProgressSnapshot) { this.snapshot = snapshot }
  }

  @Test fun menuOpensLevelSelectAndShowsUnlockedCards() {
    compose.setContent { CrownfallQuestTheme { CrownfallQuestApp(FakeStore()) } }
    compose.onNodeWithTag("menu_levels").performClick()
    compose.onNodeWithText("Choose a realm").assertIsDisplayed()
    compose.onNodeWithTag("level_card_1").assertIsDisplayed()
  }

  @Test fun creditsCanReturnToMenu() {
    compose.setContent { CrownfallQuestTheme { CrownfallQuestApp(FakeStore()) } }
    compose.onNodeWithTag("menu_credits").performClick()
    compose.onNodeWithText("The story").assertIsDisplayed()
    compose.onNodeWithTag("credits_back").performClick()
    compose.onNodeWithTag("menu_play").assertIsDisplayed()
  }

  @Test fun beginQuestShowsStoryCardAndLaunchesPlayableSurface() {
    compose.setContent { CrownfallQuestTheme { CrownfallQuestApp(FakeStore()) } }
    compose.onNodeWithTag("menu_play").performClick()
    compose.onNodeWithTag("intro_card").assertIsDisplayed()
    compose.onNodeWithText("Glasswater Grotto").assertIsDisplayed()
    compose.onNodeWithTag("start_level").performClick()
    compose.onNodeWithTag("game_surface").assertExists()
  }
}
