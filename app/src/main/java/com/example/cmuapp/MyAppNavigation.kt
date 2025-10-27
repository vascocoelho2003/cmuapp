package com.example.cmuapp

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cmuapp.components.MainScaffold
import com.example.cmuapp.pages.EstablishmentPage
import com.example.cmuapp.pages.HomePage
import com.example.cmuapp.pages.LeaderboardPage
import com.example.cmuapp.pages.LoginPage
import com.example.cmuapp.pages.MapsPageWithPermissionsAndNotifications
import com.example.cmuapp.pages.MyProfilePage
import com.example.cmuapp.pages.MyReviews
import com.example.cmuapp.pages.ReviewsPage
import com.example.cmuapp.pages.SignupPage
import com.example.cmuapp.pages.VisitedEstablishments
import com.example.cmuapp.pages.addReview
import com.example.cmuapp.viewmodel.AuthState
import com.example.cmuapp.viewmodel.AuthViewModel
import com.example.cmuapp.viewmodel.EstablishmentViewModel
import com.example.cmuapp.viewmodel.RankingViewModel
import com.example.cmuapp.viewmodel.ReviewViewModel
import com.example.cmuapp.viewmodel.UserViewModel

@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    shouldNavigateHome: MutableState<Boolean>,
    establishmentViewModel: EstablishmentViewModel,
    reviewViewModel: ReviewViewModel,
    rankingViewModel: RankingViewModel,
    context : Context
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.observeAsState(AuthState.Loading)

    LaunchedEffect(Unit) {
        authViewModel.checkAuthStatus()
    }

    LaunchedEffect (shouldNavigateHome){
        if (shouldNavigateHome.value) {
            navController.navigate("home") {
                popUpTo(0)
            }
        }
        shouldNavigateHome.value = false
    }

    when (authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }

        is AuthState.Unauthenticated -> {
            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginPage(modifier, navController, authViewModel)
                }
                composable("signup") {
                    SignupPage(modifier, navController, authViewModel)
                }
            }
        }

        is AuthState.Authenticated -> {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    HomePage(modifier, navController, authViewModel, establishmentViewModel, reviewViewModel, context )
                }
                composable("myprofile") {
                    MyProfilePage(modifier, navController, authViewModel, userViewModel)
                }
                composable("my_reviews") {
                    MyReviews(navController, authViewModel, establishmentViewModel, reviewViewModel)
                }
                composable("visited_establishments") {
                    val userId = userViewModel.currentUserId
                    if (userId != null) {
                        VisitedEstablishments(
                            navController = navController,
                            authViewModel = authViewModel,
                            establishmentViewModel = establishmentViewModel,
                            userId = userId,
                            modifier = Modifier
                        )
                    } else {
                        MainScaffold(navController, authViewModel) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Utilizador não autenticado")
                            }
                        }
                    }
                }

                composable("maps") {
                    val establishmentViewModel = hiltViewModel<EstablishmentViewModel>()
                    MapsPageWithPermissionsAndNotifications(
                        navController,
                        authViewModel,
                        establishmentViewModel
                    )
                }
                composable("establishment/{establishmentId}") { backStackEntry ->
                    val establishmentId = backStackEntry.arguments?.getString("establishmentId")
                    val establishment = establishmentViewModel.establishments.value.find { it.id == establishmentId }
                    if(establishment!=null){
                        EstablishmentPage(navController, authViewModel, establishment, establishmentViewModel )
                    }else{
                        MainScaffold(navController, authViewModel) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Estabelecimento não encontrado.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                composable("leaderboard"){
                    LeaderboardPage(navController, authViewModel, establishmentViewModel, rankingViewModel)
                }
                composable("reviewspage/{establishmentId}") { backStackEntry ->
                    val establishmentId = backStackEntry.arguments?.getString("establishmentId")
                    if (establishmentId != null) {
                        ReviewsPage(
                            navController = navController,
                            authViewModel = authViewModel,
                            reviewViewModel = reviewViewModel,
                            establishmentId = establishmentId
                        )
                    } else {
                        MainScaffold(navController, authViewModel) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Estabelecimento não encontrado.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                composable("addReview/{establishmentId}") { backStackEntry ->
                    val establishmentId = backStackEntry.arguments?.getString("establishmentId")
                    val establishment = establishmentViewModel.establishments.value.find { it.id == establishmentId }
                    if(establishment!=null){
                        if (establishmentId != null) {
                            addReview(
                                navController,
                                authViewModel,
                                establishment,
                                reviewViewModel,
                            )
                        }
                    }else{
                        MainScaffold(navController, authViewModel) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Estabelecimento não encontrado.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                }
            }
        }

        is AuthState.Error -> {
            val errorMessage = (authState as AuthState.Error).message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Erro: $errorMessage",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                        authViewModel.signout()
                    }) {
                        Text("Voltar ao Login")
                    }
                }
            }
        }
    }
}