package com.example.cmuapp.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cmuapp.R
import com.example.cmuapp.components.MainScaffold
import com.example.cmuapp.data.entities.Establishment
import com.example.cmuapp.data.repository.DocariaStats
import com.example.cmuapp.viewmodel.AuthState
import com.example.cmuapp.viewmodel.AuthViewModel
import com.example.cmuapp.viewmodel.EstablishmentViewModel
import com.example.cmuapp.viewmodel.RankingViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeaderboardPage(
    navController: NavController,
    authViewModel: AuthViewModel,
    establishmentViewModel: EstablishmentViewModel,
    rankingViewModel: RankingViewModel
) {
    val scope = rememberCoroutineScope()

    var establishments by remember { mutableStateOf<List<Establishment>>(emptyList()) }
    var ratingType by remember { mutableStateOf("google") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val docarias by rankingViewModel.ranking.observeAsState(emptyList())

    val context = LocalContext.current
    val erro_carregar = context.getString(R.string.erro_carregar_leaderboard)

    val currentAuthState by authViewModel.authState.observeAsState()

    if (currentAuthState is AuthState.Unauthenticated) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("leaderboard") { inclusive = true }
            }
        }
    }


    LaunchedEffect(ratingType) {
        if (ratingType == "docaria") {
            establishments = emptyList()
            errorMessage = null
            rankingViewModel.loadRanking(context)
        } else {
            scope.launch {
                try {
                    errorMessage = null
                    establishments = establishmentViewModel.getLeaderboard(context, ratingType)
                } catch (e: Exception) {
                    errorMessage = erro_carregar + " ${e.message}"
                    establishments = emptyList()
                }
            }
        }
    }

    MainScaffold(navController, authViewModel) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val buttonColors = @Composable { selected: Boolean ->
                    ButtonDefaults.buttonColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(onClick = { ratingType = "google" }, colors = buttonColors(ratingType == "google")) {
                    Text("Google Rating")
                }

                Button(onClick = { ratingType = "local" }, colors = buttonColors(ratingType == "local")) {
                    Text("Local Rating")
                }

                Button(onClick = { ratingType = "docaria" }, colors = buttonColors(ratingType == "docaria")) {
                    Text("Doçarias")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (ratingType == "docaria") {
                    itemsIndexed(docarias) { index, docaria ->
                        DocariaItem(docaria = docaria, position = index, navController = navController)
                    }
                } else {
                    itemsIndexed(establishments) { index, est ->
                        LeaderboardItem(
                            establishment = est,
                            position = index,
                            ratingType = ratingType
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
fun DocariaItem(docaria: DocariaStats, position: Int, navController: NavController) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val titleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val ratingColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                docaria.establishmentId?.let { id ->
                    navController.navigate("establishment/$id")
                }
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "#${position + 1} ${docaria.name}",
                fontSize = 20.sp,
                color = titleColor
            )

            if (!docaria.establishmentName.isNullOrEmpty()) {
                Text(
                    text = docaria.establishmentName ?: "",
                    fontSize = 16.sp,
                    color = titleColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Estrela",
                    tint = ratingColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", docaria.avg),
                    fontSize = 16.sp,
                    color = ratingColor
                )
            }
        }
    }
}



@Composable
fun LeaderboardItem(establishment: Establishment, position: Int, ratingType: String, onClick: () -> Unit) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val titleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val ratingColor = MaterialTheme.colorScheme.primary

    val displayedRating: Double = if (ratingType == "google") {
        (establishment.rating ?: 0f).toDouble()
    } else {
        establishment.avgRating
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "#${position + 1} ${establishment.name}",
                    fontSize = 20.sp,
                    color = titleColor
                )
                Text(
                    text = establishment.address,
                    fontSize = 14.sp,
                    color = titleColor
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Estrela",
                    tint = ratingColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", displayedRating),
                    fontSize = 16.sp,
                    color = ratingColor
                )
            }
        }
    }
}
