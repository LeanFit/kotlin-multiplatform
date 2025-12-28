package com.gongbaek.garangbi.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import leanfit.composeapp.generated.resources.Res
import leanfit.composeapp.generated.resources.pretendard_bold
import leanfit.composeapp.generated.resources.pretendard_medium
import leanfit.composeapp.generated.resources.pretendard_regular
import leanfit.composeapp.generated.resources.pretendard_semi_bold
import org.jetbrains.compose.resources.Font

data class LeanFitFontFamilies(
    val bold: FontFamily,
    val semiBold: FontFamily,
    val medium: FontFamily,
    val regular: FontFamily,
)

@Composable
fun provideFontFamilies(): LeanFitFontFamilies =
    LeanFitFontFamilies(
        bold = FontFamily(Font(Res.font.pretendard_bold, weight = FontWeight.Bold)),
        semiBold = FontFamily(Font(Res.font.pretendard_semi_bold, weight = FontWeight.SemiBold)),
        medium = FontFamily(Font(Res.font.pretendard_medium, weight = FontWeight.Medium)),
        regular = FontFamily(Font(Res.font.pretendard_regular, weight = FontWeight.Normal)),
    )

data class LeanFitTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
)

@Composable
fun provideTypography(density: Density): LeanFitTypography {
    val fonts = provideFontFamilies()

    val textStyle = { family: FontFamily, weight: FontWeight, sizeDp: Dp, lineDp: Dp, letterSpacing: Dp ->
        TextStyle(
            fontFamily = family,
            fontWeight = weight,
            fontSize = with(density) { sizeDp.toSp() },
            lineHeight = with(density) { lineDp.toSp() },
            letterSpacing = with(density) { letterSpacing.toSp() },
        )
    }

    return LeanFitTypography(
        displayLarge = textStyle(fonts.bold, FontWeight.Bold, 40.dp, 48.dp, (-0.5).dp),
        displayMedium = textStyle(fonts.bold, FontWeight.Bold, 32.dp, 40.dp, (-0.25).dp),
        displaySmall = textStyle(fonts.bold, FontWeight.Bold, 28.dp, 36.dp, 0.dp),
        headlineLarge = textStyle(fonts.bold, FontWeight.Bold, 24.dp, 32.dp, 0.dp),
        headlineMedium = textStyle(fonts.semiBold, FontWeight.SemiBold, 20.dp, 28.dp, 0.dp),
        headlineSmall = textStyle(fonts.semiBold, FontWeight.SemiBold, 18.dp, 26.dp, 0.dp),
        titleLarge = textStyle(fonts.semiBold, FontWeight.SemiBold, 16.dp, 24.dp, 0.dp),
        titleMedium = textStyle(fonts.medium, FontWeight.Medium, 15.dp, 22.dp, 0.dp),
        titleSmall = textStyle(fonts.medium, FontWeight.Medium, 14.dp, 20.dp, 0.dp),
        bodyLarge = textStyle(fonts.regular, FontWeight.Normal, 16.dp, 24.dp, 0.dp),
        bodyMedium = textStyle(fonts.regular, FontWeight.Normal, 14.dp, 20.dp, 0.dp),
        bodySmall = textStyle(fonts.regular, FontWeight.Normal, 12.dp, 16.dp, 0.dp),
        labelLarge = textStyle(fonts.semiBold, FontWeight.SemiBold, 14.dp, 20.dp, 0.dp),
        labelMedium = textStyle(fonts.medium, FontWeight.Medium, 12.dp, 16.dp, 0.dp),
        labelSmall = textStyle(fonts.medium, FontWeight.Medium, 10.dp, 14.dp, 0.dp),
    )
}
