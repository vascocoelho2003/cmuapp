package com.example.cmuapp.pages

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cmuapp.R
import com.example.cmuapp.components.MainScaffold
import com.example.cmuapp.data.entities.Establishment
import com.example.cmuapp.viewmodel.AuthState
import com.example.cmuapp.viewmodel.AuthViewModel
import com.example.cmuapp.viewmodel.EstablishmentViewModel

@Composable
fun VisitedEstablishments(
    navController: NavController,
    authViewModel: AuthViewModel,
    establishmentViewModel: EstablishmentViewModel,
    userId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val establishments by establishmentViewModel.userEstablishments.collectAsState()
    val authState = authViewModel.authState.observeAsState()
    val isLoading by establishmentViewModel.isLoading.collectAsState()
    val nenhum_visitado = context.getString(R.string.nenhum_visitado)

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }
    LaunchedEffect(userId) {
        establishmentViewModel.loadUserEstablishments(userId, context)
    }

    MainScaffold(
        navController = navController,
        authViewModel = authViewModel,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Visitados", style = MaterialTheme.typography.titleLarge)

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else if (establishments.isEmpty()) {
                Text(nenhum_visitado)
            } else {
                LazyColumn {
                    items(establishments) { est ->
                        EstablishmentItem(est, onClick = {
                            navController.navigate("establishment/${est.id}")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun EstablishmentItem(est: Establishment, onClick:() -> Unit) {
    val context = LocalContext.current
    val morada = context.getString(R.string.morada)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp).
            clickable{ onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(est.name, style = MaterialTheme.typography.titleMedium)
            Text("$morada: ${est.address}")
        }
    }
}