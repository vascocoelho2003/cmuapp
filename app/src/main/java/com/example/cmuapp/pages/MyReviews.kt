package com.example.cmuapp.pages

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cmuapp.R
import com.example.cmuapp.components.MainScaffold
import com.example.cmuapp.data.entities.Review
import com.example.cmuapp.viewmodel.AuthState
import com.example.cmuapp.viewmodel.AuthViewModel
import com.example.cmuapp.viewmodel.EstablishmentViewModel
import com.example.cmuapp.viewmodel.ReviewViewModel

@Composable
fun MyReviews(
    navController: NavController,
    authViewModel: AuthViewModel,
    establishmentViewModel: EstablishmentViewModel,
    reviewViewModel: ReviewViewModel
) {
    val userId = authViewModel.currentUserId ?: return
    val authState = authViewModel.authState.observeAsState()
    val isLoading by reviewViewModel.isLoading.observeAsState(false)
    val context = LocalContext.current
    val reviews by reviewViewModel.getUserReviews(userId,context).collectAsState(initial = emptyList())

    val nenhuma_review = context.getString(R.string.nenhuma_review)

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    LaunchedEffect(userId) {
        reviewViewModel.refreshUserReviews(userId,context)
    }
    MainScaffold(navController, authViewModel) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            }
            reviews.isEmpty() -> {
                Text(nenhuma_review, modifier = Modifier.padding(16.dp))
            }
            else -> {
                LazyColumn {
                    items(reviews) { review ->
                        ReviewItem(review, reviewViewModel::getEstablishmentName)
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review, getEstablishmentName: suspend (String) -> String) {
    val context = LocalContext.current
    val a_carregar = context.getString(R.string.a_carregar)
    val imagem_review = context.getString(R.string.imagem_review)
    val comentario = context.getString(R.string.comentario)
    val docaria = context.getString(R.string.docaria)

    var estName by remember { mutableStateOf(a_carregar) }

    LaunchedEffect(review.establishmentId) {
        estName = getEstablishmentName(review.establishmentId)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Establishment: $estName", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Rating: ${review.rating}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$docaria: ${review.docaria}", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$comentario: ${review.comment}", fontSize = 14.sp)
            review.imageUrl?.let { imageUrl ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = imageUrl,
                    contentDescription = imagem_review,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}