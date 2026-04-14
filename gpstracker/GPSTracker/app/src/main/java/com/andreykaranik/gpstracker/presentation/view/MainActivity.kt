package com.andreykaranik.gpstracker.presentation.view

import android.Manifest
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.app.ActivityCompat
import com.andreykaranik.gpstracker.presentation.theme.GPSTrackerTheme
import com.andreykaranik.gpstracker.presentation.viewmodel.CreateGroupScreenViewModel
import com.andreykaranik.gpstracker.presentation.viewmodel.GroupEntryScreenViewModel
import com.andreykaranik.gpstracker.presentation.viewmodel.JoinGroupScreenViewModel
import com.andreykaranik.gpstracker.presentation.viewmodel.LoginScreenViewModel
import com.andreykaranik.gpstracker.presentation.viewmodel.MainScreenViewModel
import com.andreykaranik.gpstracker.presentation.viewmodel.RegistrationScreenViewModel
import com.andreykaranik.gpstracker.presentation.viewmodel.StartScreenViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val registrationScreenViewModel: RegistrationScreenViewModel by viewModels()
    private val loginScreenViewModel: LoginScreenViewModel by viewModels()
    private val groupEntryScreenViewModel: GroupEntryScreenViewModel by viewModels()
    private val startScreenViewModel: StartScreenViewModel by viewModels()
    private val createGroupScreenViewModel: CreateGroupScreenViewModel by viewModels()
    private val joinGroupScreenViewModel: JoinGroupScreenViewModel by viewModels()
    private val mainScreenViewModel: MainScreenViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            100
        )

        Configuration.getInstance()
            .load(this, applicationContext.getSharedPreferences("osmdroid", MODE_PRIVATE))

        setContent {
            GPSTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberAnimatedNavController()
                    AnimatedNavHost(
                        navController = navController,
                        startDestination = "start_screen",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(
                            "start_screen"
                        ) {
                            StartScreen(
                                navController = navController,
                                viewModel = startScreenViewModel
                            )
                        }
                        composable(
                            "main_screen"
                        ) {
                            MainScreen(
                                navController = navController,
                                viewModel = mainScreenViewModel
                            )
                        }
                        composable(
                            "create_group_screen",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(300)
                                )
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300)
                                )
                            }
                        ) {
                            CreateGroupScreen(
                                navController = navController,
                                viewModel = createGroupScreenViewModel
                            )
                        }
                        composable(
                            "join_group_screen",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(300)
                                )
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300)
                                )
                            }
                        ) {
                            JoinGroupScreen(
                                navController = navController,
                                viewModel = joinGroupScreenViewModel
                            )
                        }
                        composable(
                            "group_entry_screen",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(300)
                                )
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(300)
                                )
                            }
                        ) {
                            GroupEntryScreen(
                                navController = navController,
                                viewModel = groupEntryScreenViewModel
                            )
                        }
                        composable(
                            "login_screen",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(300)
                                )
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(300)
                                )
                            }
                        ) {
                            LoginScreen(
                                navController = navController,
                                viewModel = loginScreenViewModel
                            )
                        }
                        composable(
                            "registration_screen",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(300)
                                )
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300)
                                )
                            }
                        ) {
                            RegistrationScreen(
                                navController = navController,
                                viewModel = registrationScreenViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getSystemBarHeight(): Dp {
    val insets = WindowInsets.systemBars
    val density = LocalDensity.current
    return with(density) { insets.getBottom(density).toDp() }
}

@Composable
fun getStatusBarHeight(): Dp {
    val insets = WindowInsets.systemBars
    val density = LocalDensity.current
    return with(density) { insets.getTop(density).toDp() }
}
