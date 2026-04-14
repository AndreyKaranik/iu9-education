package com.andreykaranik.gpstracker.presentation.theme

import android.os.Build
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    surface = Color.White,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Gray,
    background = Color.White,
    surfaceVariant = SurfaceContainerColor,
    surfaceContainer = SurfaceContainerColor,
    surfaceContainerLow = Color.White
)

@Composable
fun GPSTrackerTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    SideEffect {
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)

            window.statusBarColor = android.graphics.Color.TRANSPARENT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                window.decorView.systemUiVisibility = (android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            }
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
