package com.example.cmuapp.pages

import android.content.Context
import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cmuapp.components.MainScaffold
import com.example.cmuapp.viewmodel.AuthState
import com.example.cmuapp.viewmodel.AuthViewModel
import com.example.cmuapp.viewmodel.EstablishmentViewModel
import com.example.cmuapp.viewmodel.ReviewViewModel
import com.example.cmuapp.viewmodel.UserViewModel
import com.google.android.gms.maps.model.LatLng
import getCurrentLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    establishmentViewModel: EstablishmentViewModel,
    reviewViewModel: ReviewViewModel,
    context: Context
) {

    val authState = authViewModel.authState.observeAsState()
    val isLoading by establishmentViewModel.isLoading.collectAsState()
    val nearbyEstablishments by establishmentViewModel.establishments.collectAsState()
    val userLocation = remember { mutableStateOf<LatLng?>(null) }


    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
        userLocation.value = getCurrentLocation(context)
    }

    LaunchedEffect(Unit) {
        establishmentViewModel.fetchEstablishments(context)
        val userId = authViewModel.currentUserId
        if (userId != null) {
            reviewViewModel.refreshUserReviews(userId,context)
        }
    }

    MainScaffold(navController, authViewModel) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (nearbyEstablishments.isEmpty()) {
                Text(text = "No establishments found nearby.")
            } else {
                LazyColumn {
                    items(nearbyEstablishments) { est ->
                        EstablishmentItem(
                            name = est.name,
                            address = est.address,
                            rating = est.rating,
                            avgRating = est.avgRating,
                            userLocation = userLocation.value,
                            estLocation = LatLng(est.lat ?: 0.0, est.lon ?: 0.0)
                        ) {
                            navController.navigate("establishment/${est.id}")
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun EstablishmentItem(
    name: String,
    address: String,
    rating: Float?,
    avgRating: Double?,
    userLocation: LatLng?,
    estLocation: LatLng,
    onClick: () -> Unit
) {
    val distanceText = if (userLocation != null) {
        val results = FloatArray(1)
        Location.distanceBetween(
            userLocation.latitude,
            userLocation.longitude,
            estLocation.latitude,
            estLocation.longitude,
            results
        )
        val distanceInMeters = results[0]
        if (distanceInMeters < 1000) "${distanceInMeters.toInt()} m"
        else String.format("%.1f km", distanceInMeters / 1000)
    } else {
        ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(text = name, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = address, fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Google: ", fontSize = 14.sp)
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Estrela",
                    tint = androidx.compose.ui.graphics.Color.Yellow
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = rating?.let { String.format("%.1f", it) } ?: "N/A",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Local: ", fontSize = 14.sp)
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Estrela",
                    tint = androidx.compose.ui.graphics.Color.Yellow
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = avgRating?.let { String.format("%.1f", it) } ?: "N/A",
                    fontSize = 14.sp
                )
            }

            if (distanceText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Distance: $distanceText",
                    fontSize = 12.sp,
                    color = androidx.compose.ui.graphics.Color.Gray
                )
            }
        }
    }
}

