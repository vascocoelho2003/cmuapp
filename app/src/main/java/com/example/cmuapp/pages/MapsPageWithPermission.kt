package com.example.cmuapp.pages

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import cmu.example.cmuapp.utils.GeofenceHelper
import com.example.cmuapp.components.MainScaffold
import com.example.cmuapp.viewmodel.AuthState
import com.example.cmuapp.viewmodel.AuthViewModel
import com.example.cmuapp.viewmodel.EstablishmentViewModel

@Composable
fun MapsPageWithPermissionsAndNotifications(
    navController: NavController,
    authViewModel: AuthViewModel,
    establishmentViewModel: EstablishmentViewModel
) {
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val ativar_notificacoes = context.getString(com.example.cmuapp.R.string.ativar_notificacoes)

    val perms = remember {
        mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val bg = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false
        val notif = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: true
        locationPermissionGranted = fine && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || bg) && notif

        if (locationPermissionGranted) {
            val establishments = establishmentViewModel.establishments.value
            establishments.forEach { est ->
                GeofenceHelper.addGeofenceWithPermissionCheck(
                    context,
                    est.lat ?: 0.0,
                    est.lon ?: 0.0,
                    est.id,
                    est.name ?: est.id
                )
            }

        }
    }

    LaunchedEffect(Unit) {
        multiplePermissionLauncher.launch(perms.toTypedArray())
    }

    if (locationPermissionGranted) {
        val establishments by establishmentViewModel.establishments.collectAsState()
        LaunchedEffect(Unit) {
            establishmentViewModel.fetchEstablishments(context)
        }
        MainScaffold(navController, authViewModel) { modifier ->
            Box(modifier = modifier) {
                UserLocationMap(establishments = establishments, navController)
            }
        }
    } else {
        MainScaffold(navController, authViewModel) { modifier ->
            Box(modifier = modifier) {
                Button(onClick = {
                    multiplePermissionLauncher.launch(perms.toTypedArray())
                }) {
                    Text(ativar_notificacoes)
                }
            }
        }
    }
}
