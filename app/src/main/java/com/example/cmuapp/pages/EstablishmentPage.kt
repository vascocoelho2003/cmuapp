package com.example.cmuapp.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cmuapp.R
import com.example.cmuapp.components.MainScaffold
import com.example.cmuapp.data.entities.Establishment
import com.example.cmuapp.viewmodel.AuthState
import com.example.cmuapp.viewmodel.AuthViewModel
import com.example.cmuapp.viewmodel.EstablishmentViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun EstablishmentPage(
    navController: NavController,
    authViewModel: AuthViewModel,
    establishment: Establishment,
    establishmentViewModel: EstablishmentViewModel,
) {
    val authState = authViewModel.authState.observeAsState()
    val establishmentState = establishmentViewModel.syncAndGetEstablishmentById(establishment.id)
    val establishment by establishmentState.collectAsState(initial = null)
    val context = LocalContext.current

    val morada = context.getString(R.string.morada)
    val ver_reviews = context.getString(R.string.ver_review)
    val adicionar_review = context.getString(R.string.adicionar_review)

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }


    MainScaffold(navController, authViewModel) { padding ->
        if (establishment == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@MainScaffold
        }

        val est = establishment!!

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    est.imageUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Imagem do estabelecimento",
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }

                    Text(
                        text = est.name,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text =morada+"${est.address}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Google Rating: ${est.rating ?: "N/A"}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Local Rating: ${est.avgRating?.let { String.format("%.1f", it) } ?: "N/A"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val establishmentLocation = LatLng(est.lat ?: 0.0, est.lon ?: 0.0)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(establishmentLocation, 15f)
            }

            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = establishmentLocation),
                    title = est.name,
                    snippet = est.address
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("reviewspage/${est.id}") }) {
                Text(text = ver_reviews)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { navController.navigate("addReview/${est.id}") }) {
                Text(text = adicionar_review)
            }
        }
    }
}
