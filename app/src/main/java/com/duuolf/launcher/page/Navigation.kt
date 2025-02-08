package com.duuolf.launcher.page

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.duuolf.launcher.utils.LauncherUtils

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavHost(navController: NavHostController) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (!LauncherUtils.isDefaultLauncher(context)) {
            LauncherUtils.requestSetDefaultLauncher(context)
        }
    }
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HomeScreen(navController) }
        dialog("init") { InitScreen(navController) }
        composable("set") { SetScreen(navController) }
        composable("addContact") { AddContactScreen(navController) }
    }
}
