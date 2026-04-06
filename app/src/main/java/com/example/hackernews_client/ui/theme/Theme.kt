package com.example.hackernews_client.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

sealed class AppTheme(val name: String, val colorScheme: ColorScheme) {
    object Default : AppTheme("Default", LightColorScheme)
    object HackerNews : AppTheme("Hacker News", HNColorScheme)
    object DeepBlue : AppTheme("Deep Blue", BlueColorScheme)
    object Kanagawa : AppTheme("Kanagawa", KanagawaColorScheme)
    object Solarized : AppTheme("Solarized Dark", SolarizedColorScheme)
    object Nord : AppTheme("Nord", NordColorScheme)
    object Darcula : AppTheme("Darcula", DarculaColorScheme)

    companion object {
        val themes = listOf(Default, HackerNews, DeepBlue, Kanagawa, Solarized, Nord, Darcula)
        fun fromName(name: String?): AppTheme = themes.find { it.name == name } ?: Default
    }
}
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val HNColorScheme = lightColorScheme(
    primary = HNOrenge,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    background = HNBackground,
    surface = HNBackground,
)

private val BlueColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueTertiary
)

private val KanagawaColorScheme = darkColorScheme(
    primary = KanagawaPrimary,
    secondary = KanagawaSecondary,
    tertiary = KanagawaTertiary,
    background = KanagawaBg,
    surface = KanagawaSurface,
    onPrimary = KanagawaBg,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = KanagawaFg,
    onSurface = KanagawaFg,
)

private val SolarizedColorScheme = darkColorScheme(
    primary = SolarizedPrimary,
    secondary = SolarizedSecondary,
    background = SolarizedBg,
    surface = SolarizedSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onBackground = SolarizedFg,
    onSurface = SolarizedFg,
)

private val NordColorScheme = darkColorScheme(
    primary = NordPrimary,
    secondary = NordSecondary,
    background = NordBg,
    surface = NordSurface,
    onPrimary = NordBg,
    onBackground = NordFg,
    onSurface = NordFg,
)

private val DarculaColorScheme = darkColorScheme(
    primary = DarculaPrimary,
    secondary = DarculaSecondary,
    background = DarculaBg,
    surface = DarculaSurface,
    onPrimary = DarculaBg,
    onBackground = DarculaFg,
    onSurface = DarculaFg,
)

@Composable
fun Hackernews_clientTheme(
    theme: AppTheme = AppTheme.Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = theme.let {
        if (it == AppTheme.Default
            && dynamicColor
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
           val context = LocalContext.current
           if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
           it.colorScheme
        }
    };
//    when (theme) {
//        AppTheme.HACKER_NEWS -> HNColorScheme
//        AppTheme.DEEP_BLUE -> BlueColorScheme
//        AppTheme.KANAGAWA -> KanagawaColorScheme
//        AppTheme.DEFAULT -> {
//            when {
//                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//                    val context = LocalContext.current
//                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//                }
//                darkTheme -> DarkColorScheme
//                else -> LightColorScheme
//            }
//        }
//    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
