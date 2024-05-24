package com.example.chargingstations

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreenContent()
        }
    }

    @Composable
    fun SplashScreenContent() {
        val alpha = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            val fadeOut = tween<Float>(durationMillis = 1000)
            val scaleUp = tween<Float>(durationMillis = 1000)

            alpha.animateTo(1f, animationSpec = fadeOut)
            delay(1000L)
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            finish()
        }

        val text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Black.copy(alpha = alpha.value), fontWeight = FontWeight.Bold)) {
                append("Charging")
            }
            withStyle(style = SpanStyle(color = Color.Gray.copy(alpha = alpha.value), fontWeight = FontWeight.Bold)) {
                append("Stations")
            }
        }

        Surface(color = Color.White) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 30.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}