package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.PinLockScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.SectionNotesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.NoteFlowTheme
import com.example.ui.viewmodel.MainViewModel

@Composable
fun NoteFlowApp(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()

    val navController = rememberNavController()

    // Handle Toast events
    LaunchedEffect(Unit) {
        viewModel.messageEvent.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    NoteFlowTheme(
        themeMode = themeMode,
        dynamicColor = dynamicColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (appLockEnabled && isLocked) {
                PinLockScreen(
                    onVerifyPin = { pin ->
                        viewModel.unlockWithPin(pin)
                    },
                    onBiometricClick = {
                        viewModel.unlockByBiometrics()
                    },
                    isBiometricAvailable = false
                )
            } else {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToEditor = { noteId, type ->
                                val id = noteId ?: 0L
                                navController.navigate("editor/$id/$type")
                            },
                            onNavigateToSection = { route ->
                                navController.navigate("section/$route")
                            }
                        )
                    }

                    composable(
                        route = "editor/{noteId}/{type}",
                        arguments = listOf(
                            navArgument("noteId") { type = NavType.LongType },
                            navArgument("type") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                        val type = backStackEntry.arguments?.getString("type") ?: "TEXT"
                        NoteEditorScreen(
                            viewModel = viewModel,
                            noteId = noteId,
                            type = type,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "section/{route}",
                        arguments = listOf(navArgument("route") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val route = backStackEntry.arguments?.getString("route") ?: "favorites"
                        if (route == "settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        } else {
                            SectionNotesScreen(
                                viewModel = viewModel,
                                sectionRoute = route,
                                onBack = { navController.popBackStack() },
                                onNavigateToEditor = { noteId, type ->
                                    navController.navigate("editor/$noteId/$type")
                                }
                            )
                        }
                    }

                    composable("settings") {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
