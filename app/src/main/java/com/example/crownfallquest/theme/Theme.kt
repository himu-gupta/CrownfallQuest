package com.himugupta.crownfallquest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(primary = AmberLight, secondary = TealLight, tertiary = Cream, background = Violet)

private val LightColorScheme =
  lightColorScheme(
    primary = Teal,
    secondary = Amber,
    tertiary = Violet,
    background = Cream,
  )

@Composable
fun CrownfallQuestTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
