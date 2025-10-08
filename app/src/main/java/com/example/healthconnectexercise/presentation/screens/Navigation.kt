package com.example.healthconnectexercise.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.health.connect.client.PermissionController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.healthconnectexercise.presentation.viewmodel.ExerciseViewModel


@Composable
fun NavGraph(navController: NavHostController) {

    val sharedVm: ExerciseViewModel = hiltViewModel()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (sharedVm.healthConnectManager.requiredPermissions.all { it in grantedPermissions }) {
            sharedVm.load()
        }
    }

    LaunchedEffect(Unit) {
        if (sharedVm.healthConnectManager.hasAllPermissions()) {
            sharedVm.load()
        }
    }
    NavHost(navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onAdd = { navController.navigate("add") },
                onConflicts = { navController.navigate("conflicts") },
                onSync = {
                    sharedVm.onSyncClick()
                },
                vm = sharedVm,
                permissionLauncher = permissionLauncher
            )
        }

        composable("add") {
            AddManualScreen(onDone = { navController.popBackStack() }, vm = sharedVm)
        }

        composable("conflicts") {
            ConflictResolutionScreen(onDone = { navController.popBackStack() }, vm = sharedVm)
        }
    }
}