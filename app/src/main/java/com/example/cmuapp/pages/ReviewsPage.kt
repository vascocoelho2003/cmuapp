package com.example.cmuapp.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cmuapp.R
import com.example.cmuapp.components.MainScaffold
import com.example.cmuapp.data.entities.Review
import com.example.cmuapp.viewmodel.AuthState
import com.example.cmuapp.viewmodel.AuthViewModel
import com.example.cmuapp.viewmodel.ReviewViewModel

@Composable
fun ReviewsPage(
    navController: NavController,
    authViewModel: AuthViewModel,
    reviewViewModel: ReviewViewModel,
    establishmentId: String
) {
    val reviews by reviewViewModel.reviews.observeAsState(emptyList())
    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()
    val avaliacoes = context.getString(R.string.avaliacoes)
    val nenhuma_review = context.getString(R.string.nenhuma_review)


    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    LaunchedEffect(establishmentId) {
        reviewViewModel.loadReviews(establishmentId)
    }

    MainScaffold(navController = navController, authViewModel = authViewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                text = avaliacoes,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (reviews.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nenhuma_review,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(reviews) { review ->
                        ReviewCard(review, reviewViewModel::getUserName)
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review, getUserName: suspend (String) -> String) {
    val context = LocalContext.current
    val a_carregar = context.getString(R.string.a_carregar)
    val imagem_review = context.getString(R.string.imagem_review)
    var userName by remember { mutableStateOf(a_carregar) }
    val docaria = context.getString(R.string.docaria)
    val comment = context.getString(R.string.comentario)

    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(review.userId) {
        userName = getUserName(review.userId)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Username: $userName",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Estrela",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = review.rating.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$docaria: ${review.docaria}" ?: "Sem comentário",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$comment: ${review.comment}" ?: "Sem comentário",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            review.imageUrl?.let { imageUrl ->
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = imageUrl,
                    contentDescription = imagem_review,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            review.audioUrl?.let { audioPath ->
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (mediaPlayer == null) {
                            mediaPlayer = android.media.MediaPlayer().apply {
                                setDataSource(audioPath)
                                prepare()
                                start()
                            }
                            isPlaying = true
                        } else {
                            if (isPlaying) {
                                mediaPlayer?.pause()
                                isPlaying = false
                            } else {
                                mediaPlayer?.start()
                                isPlaying = true
                            }
                        }
                    }
                ) {
                    Text(if (isPlaying) "Pausar áudio" else "Ouvir áudio")
                }
            }
        }
    }
}
