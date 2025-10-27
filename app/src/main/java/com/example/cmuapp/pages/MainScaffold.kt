package com.example.cmuapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cmuapp.R
import com.example.cmuapp.utils.NotificationUtils
import com.example.cmuapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavController,
    authViewModel: AuthViewModel,
    content: @Composable (Modifier) -> Unit
) {
    val context = LocalContext.current
    val nomeApp = context.getString(R.string.app_name)
    val profile = context.getString(R.string.profile)
    val map = context.getString(R.string.map)

    var notificationsEnabled by remember {
        mutableStateOf(NotificationUtils.isNotificationsEnabled(context))
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nomeApp) },
                actions = {
                    IconButton(onClick = {
                        notificationsEnabled = !notificationsEnabled
                        NotificationUtils.setNotificationsEnabled(context, notificationsEnabled)
                    }) {
                        Icon(
                            imageVector = if (notificationsEnabled) {
                                Icons.Default.Notifications
                            } else {
                                Icons.Default.NotificationsOff
                            },
                            contentDescription = if (notificationsEnabled) {
                                "Desativar notificações"
                            } else {
                                "Ativar notificações"
                            }
                        )
                    }

                    IconButton(onClick = { authViewModel.signout() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("leaderboard") }
            ) {
                Icon(Icons.Filled.Leaderboard, contentDescription = "Leaderboard")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = currentRoute == "myprofile",
                    onClick = { navController.navigate("myprofile") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = profile) },
                    label = { Text(profile) }
                )
                NavigationBarItem(
                    selected = currentRoute == "maps",
                    onClick = { navController.navigate("maps") },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Maps") },
                    label = { Text(map) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content(Modifier)
        }
    }
}