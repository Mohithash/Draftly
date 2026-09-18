@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.draftly.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mohithash.draftly.ui.screens.HistoryScreen
import com.mohithash.draftly.ui.screens.OnboardingScreen
import com.mohithash.draftly.ui.screens.SettingsScreen
import com.mohithash.draftly.ui.screens.WriteScreen

@Composable
fun Nav(vm: AppViewModel) {
    val s by vm.settings.collectAsState()
    if (!s.onboarded) { OnboardingScreen(vm); return }
    val nav = rememberNavController()
    NavHost(nav, "write") {
        composable("write") { WriteScreen(vm, onHistory = { nav.navigate("history") }, onSettings = { nav.navigate("settings") }) }
        composable("history") { HistoryScreen(vm, onBack = { nav.popBackStack() }) }
        composable("settings") { SettingsScreen(vm, onBack = { nav.popBackStack() }) }
    }
}
