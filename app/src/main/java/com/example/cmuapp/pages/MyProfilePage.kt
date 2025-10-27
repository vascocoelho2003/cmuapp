package com.example.cmuapp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cmuapp.R
import com.example.cmuapp.components.MainScaffold
import com.example.cmuapp.viewmodel.AuthState
import com.example.cmuapp.viewmodel.AuthViewModel
import com.example.cmuapp.viewmodel.UserViewModel

@Composable
fun MyProfilePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, viewModel: UserViewModel){

    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()
    val user by viewModel.userData.observeAsState()

    val profile = context.getString(R.string.profile)
    val minhas_reviews = context.getString(R.string.minhas_reviews)
    val historico = context.getString(R.string.historico)
    val a_carregar = context.getString(R.string.a_carregar)

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> viewModel.fetchUserData()
        }
    }

    MainScaffold(navController, authViewModel) { mod ->
        Column(
            modifier = mod.fillMaxSize().then(mod),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = profile,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Username: ${user?.username ?: a_carregar}")
            Text(text = "Email: ${user?.email ?: a_carregar}")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {  navController.navigate("my_reviews") }) {
                Text(minhas_reviews)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {  navController.navigate("visited_establishments") }) {
                Text(historico)
            }
        }
    }
}