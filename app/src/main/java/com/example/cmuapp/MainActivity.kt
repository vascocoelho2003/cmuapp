package com.example.cmuapp

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cmuapp.ui.theme.CmuAppTheme
import com.example.cmuapp.viewmodel.AuthViewModel
import com.example.cmuapp.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import androidx.compose.runtime.mutableStateOf
import com.example.cmuapp.utils.NotificationUtils
import com.example.cmuapp.viewmodel.EstablishmentViewModel
import com.example.cmuapp.viewmodel.ReviewViewModel
import com.example.cmuapp.viewmodel.RankingViewModel as RankingViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val shouldNavigateHome = mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationUtils.createNotificationChannel(this)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                100
            )
        }

        val authViewModel: AuthViewModel by viewModels()
        val userViewModel: UserViewModel by viewModels()
        val reviewViewModel: ReviewViewModel by viewModels()
        val establishmentViewModel: EstablishmentViewModel by viewModels()
        val rankingViewModel: RankingViewModel by viewModels()


        setContent {
            CmuAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation( modifier = Modifier.padding(innerPadding) ,authViewModel = authViewModel, userViewModel, shouldNavigateHome = shouldNavigateHome, establishmentViewModel = establishmentViewModel, context = this , reviewViewModel = reviewViewModel , rankingViewModel = rankingViewModel  )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            shouldNavigateHome.value = true
        }
    }
}
