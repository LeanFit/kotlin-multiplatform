package com.gongbaek.garangbi.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GarangbiTheme {
    val typography: GarangbiTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val colors: GarangbiColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGarangbiColors.current

    val spacing: GarangbiSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalGarangbiSpacing.current
}

private val LocalTypography =
    staticCompositionLocalOf<GarangbiTypography> { error("No Typography provided") }
private val LocalGarangbiColors =
    staticCompositionLocalOf<GarangbiColors> { error("No Colors provided") }
private val LocalGarangbiSpacing =
    staticCompositionLocalOf<GarangbiSpacing> { error("No Spacing provided") }

@Composable
fun GarangbiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val typography = provideTypography(density)
    val colors = provideColors(darkTheme)
    val spacing = provideSpacing()

    val colorScheme =
        if (darkTheme) {
            createDarkColorScheme(colors)
        } else {
            createLightColorScheme(colors)
        }

    CompositionLocalProvider(
        LocalTypography provides typography,
        LocalGarangbiColors provides colors,
        LocalGarangbiSpacing provides spacing,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

private fun createLightColorScheme(colors: GarangbiColors) =
    lightColorScheme(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        primaryContainer = colors.primaryContainer,
        onPrimaryContainer = colors.onPrimaryContainer,
        secondary = colors.secondary,
        onSecondary = colors.onSecondary,
        secondaryContainer = colors.secondaryContainer,
        onSecondaryContainer = colors.onSecondaryContainer,
        background = colors.background,
        onBackground = colors.onBackground,
        surface = colors.surface,
        onSurface = colors.onSurface,
        surfaceVariant = colors.surfaceVariant,
        onSurfaceVariant = colors.onSurfaceVariant,
        outline = colors.outline,
        error = colors.error,
        onError = colors.onError,
    )

private fun createDarkColorScheme(colors: GarangbiColors) =
    darkColorScheme(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        primaryContainer = colors.primaryContainer,
        onPrimaryContainer = colors.onPrimaryContainer,
        secondary = colors.secondary,
        onSecondary = colors.onSecondary,
        secondaryContainer = colors.secondaryContainer,
        onSecondaryContainer = colors.onSecondaryContainer,
        background = colors.background,
        onBackground = colors.onBackground,
        surface = colors.surface,
        onSurface = colors.onSurface,
        surfaceVariant = colors.surfaceVariant,
        onSurfaceVariant = colors.onSurfaceVariant,
        outline = colors.outline,
        error = colors.error,
        onError = colors.onError,
    )

data class GarangbiColors(
    val primary: Color,
    val primarySoft: Color,
    val primaryStrong: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondarySoft: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val savings: Color,
    val savingsSoft: Color,
    val onSavings: Color,
    val warning: Color,
    val warningSoft: Color,
    val onWarning: Color,
    val error: Color,
    val errorSoft: Color,
    val onError: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
)

@Composable
fun provideColors(isDark: Boolean): GarangbiColors =
    if (isDark) {
        GarangbiColors(
            primary = Primary400,
            primarySoft = Primary900,
            primaryStrong = Primary300,
            onPrimary = Gray900,
            primaryContainer = Primary800,
            onPrimaryContainer = Primary100,
            secondary = Secondary400,
            secondarySoft = Secondary700,
            onSecondary = Gray900,
            secondaryContainer = Secondary700,
            onSecondaryContainer = Secondary100,
            background = Gray900,
            onBackground = Gray50,
            surface = Gray800,
            surfaceElevated = Gray700,
            surfaceVariant = Gray700,
            onSurface = Gray50,
            onSurfaceVariant = Gray300,
            outline = Gray600,
            outlineVariant = Gray700,
            savings = Success500,
            savingsSoft = Success700,
            onSavings = White,
            warning = Warning500,
            warningSoft = Warning600,
            onWarning = Gray900,
            error = Error500,
            errorSoft = Error700,
            onError = White,
            textPrimary = Gray50,
            textSecondary = Gray400,
            textTertiary = Gray500,
            textDisabled = Gray600,
        )
    } else {
        GarangbiColors(
            primary = Primary600,
            primarySoft = Primary50,
            primaryStrong = Primary700,
            onPrimary = White,
            primaryContainer = Primary100,
            onPrimaryContainer = Primary700,
            secondary = Secondary600,
            secondarySoft = Secondary50,
            onSecondary = White,
            secondaryContainer = Secondary100,
            onSecondaryContainer = Secondary700,
            background = Gray50,
            onBackground = Gray900,
            surface = White,
            surfaceElevated = White,
            surfaceVariant = Gray100,
            onSurface = Gray900,
            onSurfaceVariant = Gray600,
            outline = Gray300,
            outlineVariant = Gray200,
            savings = Success600,
            savingsSoft = Success50,
            onSavings = White,
            warning = Warning600,
            warningSoft = Warning50,
            onWarning = White,
            error = Error600,
            errorSoft = Error50,
            onError = White,
            textPrimary = Gray900,
            textSecondary = Gray600,
            textTertiary = Gray500,
            textDisabled = Gray400,
        )
    }

data class GarangbiSpacing(
    val none: Dp,
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
    val doubleExtraLarge: Dp,
    val tripleExtraLarge: Dp,
)

@Composable
fun provideSpacing(): GarangbiSpacing =
    GarangbiSpacing(
        none = 0.dp,
        extraSmall = 4.dp,
        small = 8.dp,
        medium = 12.dp,
        large = 16.dp,
        extraLarge = 24.dp,
        doubleExtraLarge = 32.dp,
        tripleExtraLarge = 48.dp,
    )
