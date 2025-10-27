package com.example.cmuapp.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cmuapp.R
import com.example.cmuapp.data.entities.Establishment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import getCurrentLocation
import kotlinx.coroutines.launch

@Composable
fun UserLocationMap(establishments: List<Establishment>, navController: NavController) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val obter_localizacao = context.getString(R.string.a_obter_localizacao)
    val tentar_novamente = context.getString(R.string.tentar_novamente)
    val nao_foi_possivel = context.getString(R.string.nao_foi_possivel_obter_localizacao)
    val avaliacao = context.getString(R.string.avaliacao)

    val cameraPositionState = rememberCameraPositionState()

    val filteredEstablishments = establishments.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(loading) {
        if (loading) {
            error = null
            val location = getCurrentLocation(context)
            if (location != null) {
                userLocation = location
                loading = false
                cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
            } else {
                error = nao_foi_possivel
                loading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Establishments") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        if (searchQuery.isNotEmpty() && filteredEstablishments.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                items(filteredEstablishments) { est ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable {
                                est.lat?.let { lat ->
                                    est.lon?.let { lon ->
                                        scope.launch {
                                            cameraPositionState.animate(
                                                update = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 18f)
                                            )
                                        }

                                    }
                                }
                                searchQuery = ""
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Text(
                            text = est.name,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                userLocation != null -> {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true)
                    ) {
                        establishments.forEach { est ->
                            est.lat?.let { lat ->
                                est.lon?.let { lon ->
                                    Marker(
                                        state = MarkerState(LatLng(lat, lon)),
                                        title = est.name,
                                        snippet = "$avaliacao: ${est.avgRating ?: 0f}",
                                        onClick = { false },
                                        onInfoWindowClick = {
                                            navController.navigate("establishment/${est.id}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(error!!)
                        Button(
                            onClick = {
                                loading = true
                                userLocation = null
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(tentar_novamente)
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text(obter_localizacao, modifier = Modifier.padding(top = 16.dp))
                    }
                }
            }
        }
    }
}
