package com.example.tarimobileas.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import androidx.compose.material3.TextFieldDefaults
import com.example.tarimobileas.R

/* -----------------------------------------------------------------
   1️⃣  Font family (using .otf placed in res/font/)
   ----------------------------------------------------------------- */
private val Comfortaa = FontFamily(
    Font(R.font.comfortaa_regular, FontWeight.Normal),
    Font(R.font.comfortaa_bold, FontWeight.Bold),
    Font(R.font.comfortaa_light, FontWeight.Light),
    Font(R.font.comfortaa_medium, FontWeight.Medium),
    Font(R.font.comfortaa_semibold, FontWeight.SemiBold)
)

/* -----------------------------------------------------------------
   2️⃣  Custom Typography that uses the font family
   ----------------------------------------------------------------- */
private val AppTypography = Typography(
    bodyLarge = TextStyle(fontFamily = Comfortaa, fontSize = 16.sp),
    titleLarge = TextStyle(fontFamily = Comfortaa, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    labelMedium = TextStyle(fontFamily = Comfortaa)
)

/* -----------------------------------------------------------------
   3️⃣  Light / Dark colour schemes (you can keep the defaults)
   ----------------------------------------------------------------- */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color.White,
    background = Color(0xFFF2F2F2),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

/* -----------------------------------------------------------------
   4️⃣  Global TextField colour defaults (used by every TextField)
   ----------------------------------------------------------------- */
private val LocalTextFieldColors = staticCompositionLocalOf<TextFieldColors> {
    error("No TextFieldColors provided")
}

/** Helper that supplies the same colors to all children in its subtree. */
@Composable
fun ProvideTextFieldDefaults(
    colors: TextFieldColors,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalTextFieldColors provides colors, content = content)
}

/* -----------------------------------------------------------------
   5️⃣  The actual Theme composable used by the whole app
   ----------------------------------------------------------------- */
@Composable
fun TariMobileASTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // ---- colour scheme (kept as you had it) ----
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // ---- default TextField colours for the whole app ----
    val tfColors = TextFieldDefaults.colors(

        cursorColor = MaterialTheme.colorScheme.primary,
        // ---- text colour (used for the entered text) ----

        focusedTextColor = MaterialTheme.colorScheme.onBackground,
        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,

        // ---- container / background colours ----
        focusedContainerColor   = MaterialTheme.colorScheme.surface,          // when the field has focus
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,          // default state
        disabledContainerColor  = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f),

        // ---- indicator (underline) colours – optional but nice ----
        focusedIndicatorColor   = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),

    )

    // ---- apply MaterialTheme + provide defaults globally ----
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = {
            // Global text style (font & colour) for all composables
            CompositionLocalProvider(
                LocalTextStyle provides TextStyle(
                    fontFamily = Comfortaa,
                    color = colorScheme.onBackground,
                    fontSize = 16.sp
                )
            ) {
                // Provide the TextField colours to every child
                ProvideTextFieldDefaults(colors = tfColors) {
                    content()
                }
            }
        }
    )
}
