package com.example.cmuapp.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.cmuapp.R
import com.example.cmuapp.components.MainScaffold
import com.example.cmuapp.data.entities.Establishment
import com.example.cmuapp.data.entities.Review
import com.example.cmuapp.viewmodel.AuthState
import com.example.cmuapp.viewmodel.AuthViewModel
import com.example.cmuapp.viewmodel.ReviewViewModel
import getCurrentLocation
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun addReview(
    navController: androidx.navigation.NavController,
    authViewModel: AuthViewModel,
    establishment: Establishment,
    reviewViewModel: ReviewViewModel
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tmpPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraLaunch by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf(TextFieldValue("")) }
    var docariaa by remember { mutableStateOf(TextFieldValue("")) }

    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var audioFilePath by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }


    val authState = authViewModel.authState.observeAsState()
    val establishmentId = establishment.id
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val canSubmit by reviewViewModel.canSubmit.observeAsState(false)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) imageUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) imageUri = tmpPhotoUri else tmpPhotoUri = null
        pendingCameraLaunch = false
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingCameraLaunch) {
            pendingCameraLaunch = false
            val created = tryCreateImageFileAndLaunchCamera(context, cameraLauncher) { tmpPhotoUri = it }
            if (!created) Toast.makeText(context, context.getString(R.string.erro_criar_imagem_review), Toast.LENGTH_SHORT).show()
        } else if (!granted) {
            Toast.makeText(context, context.getString(R.string.permissao_negada_camara), Toast.LENGTH_SHORT).show()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startRecordingAudio(context)?.let { (recorder, path) ->
                mediaRecorder = recorder
                audioFilePath = path
                isRecording = true
                Toast.makeText(context, "A gravar áudio...", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permissão de microfone negada", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) navController.navigate("login")
    }

    LaunchedEffect(establishmentId, authViewModel.currentUserId) {
        val location = getCurrentLocation(context)
        authViewModel.currentUserId?.let { userId ->
            if (location != null) {
                reviewViewModel.checkIfUserCanSubmit(
                    establishmentId, userId, location.latitude ?: 0.0, location.longitude
                )
            }
        }
    }

    MainScaffold(navController = navController, authViewModel = authViewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(context.getString(R.string.adicionar_review), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { galleryLauncher.launch("image/*") }) { Text(context.getString(R.string.escolher_galeria)) }
                Button(onClick = {
                    val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        val created = tryCreateImageFileAndLaunchCamera(context, cameraLauncher) { tmpPhotoUri = it }
                        if (!created) Toast.makeText(context, context.getString(R.string.ficheiro_nao_criado), Toast.LENGTH_SHORT).show()
                    } else {
                        pendingCameraLaunch = true
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) { Text(context.getString(R.string.tirar_foto)) }
            }

            imageUri?.let {
                Spacer(Modifier.height(8.dp))
                AsyncImage(model = it, contentDescription = "Imagem escolhida", modifier = Modifier.height(200.dp).fillMaxWidth())
            }

            Spacer(Modifier.height(16.dp))
            Text(context.getString(R.string.avaliacao))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..5) {
                    IconButton(onClick = { rating = i }) {
                        Icon(imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = "$i estrela")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text(context.getString(R.string.comentario)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .widthIn(max = 600.dp)
            )

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = docariaa,
                onValueChange = { docariaa = it },
                label = { Text(context.getString(R.string.docaria)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .widthIn(max = 600.dp)
            )


            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = {
                    if (!isRecording) {
                        startRecordingAudio(context)?.let { (recorder, path) ->
                            mediaRecorder = recorder
                            audioFilePath = path
                            isRecording = true
                            Toast.makeText(context, "A gravar áudio...", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        mediaRecorder?.apply {
                            stop()
                            release()
                        }
                        Toast.makeText(context, "Gravação terminada", Toast.LENGTH_SHORT).show()
                        isRecording = false
                    }
                }) {
                    Text(if (!isRecording) "Gravar áudio" else "Parar gravação")
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val review = Review(
                        establishmentId = establishmentId,
                        userId = authViewModel.currentUserId ?: "anonymous",
                        rating = rating,
                        comment = comment.text,
                        docaria = docariaa.text,
                        imageUrl = imageUri?.toString(),
                        audioUrl = audioFilePath,
                        synced = false
                    )
                    reviewViewModel.submitReview(review)
                    reviewViewModel.refreshUserReviews(authViewModel.currentUserId ?: "anonymous", context)
                    Toast.makeText(context, context.getString(R.string.review_submetida), Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                enabled = canSubmit && rating > 0 && comment.text.isNotBlank()
            ) {
                Text(context.getString(R.string.submeter_review))
            }
        }
    }
}

fun startRecordingAudio(context: Context): Pair<MediaRecorder, String>? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val audioFile = File(context.filesDir, "AUD_$timeStamp.3gp")

        val recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFile.absolutePath)
            prepare()
            start()
        }

        Pair(recorder, audioFile.absolutePath)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Erro a iniciar gravação", Toast.LENGTH_SHORT).show()
        null
    }
}


fun stopRecordingAudio(recorder: MediaRecorder?, context: Context) {
    try {
        recorder?.apply {
            stop()
            release()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Erro a parar gravação", Toast.LENGTH_SHORT).show()
    }
}

private fun tryCreateImageFileAndLaunchCamera(
    context: Context,
    cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    onUriReady: (Uri?) -> Unit
): Boolean {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
        val photoUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
        onUriReady(photoUri)
        cameraLauncher.launch(photoUri)
        true
    } catch (e: IOException) {
        e.printStackTrace()
        onUriReady(null)
        false
    }
}

