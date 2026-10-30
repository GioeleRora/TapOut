package com.tapout.ui.screens.dance

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tapout.ui.theme.CyberBlack
import com.tapout.ui.theme.ElectricPurple
import com.tapout.ui.theme.NeonMint
import com.tapout.ui.theme.NeonPink
import com.tapout.ui.theme.NeonPinkBright
import com.tapout.ui.theme.SurfaceBorder
import com.tapout.ui.theme.SurfaceDark
import com.tapout.ui.theme.SurfaceElevated
import com.tapout.ui.theme.TextMuted
import com.tapout.ui.theme.TextWhite
import com.tapout.vision.DanceDetector
import com.tapout.vision.DancePoseAnalyzer
import com.tapout.vision.DanceState
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun DanceMissionScreen(
    targetSeconds: Float = DanceDetector.DANCE_REQUIRED_SECONDS,
    onMissionCompleted: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Richiedi automaticamente il permesso fotocamera se non concesso
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var danceState by remember {
        mutableStateOf(
            DanceState(
                isUserDetected = false,
                isDancing = false,
                currentEnergy = 0f,
                secondsDanced = 0f,
                targetSeconds = targetSeconds,
                message = "Inquadrati a figura intera per iniziare"
            )
        )
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val danceDetector = remember {
        DanceDetector(
            targetSeconds = targetSeconds,
            onMissionComplete = {
                onMissionCompleted()
            }
        )
    }

    val poseAnalyzer = remember {
        DancePoseAnalyzer(
            danceDetector = danceDetector,
            onStateUpdate = { newState ->
                danceState = newState
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (_: Exception) {}
            cameraExecutor.shutdown()
            poseAnalyzer.close()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dance Mission",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                    )
                    Text(
                        text = "MOVIMENTO INTRA-CORPOREO • ZERO CAMERA SHAKE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonPinkBright,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                if (onCancel != null) {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Chiudi", tint = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Box Fotocamera Frontale con Circular Progress Ring Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceDark)
                    .border(2.dp, if (danceState.isDancing) NeonPink else SurfaceBorder, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    // CameraX Preview: TextureView compatibile con clipping e overlay Compose
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                // Obbligatorio per rendering corretto in Compose ed evitare schermo nero su Oppo/OnePlus
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also {
                                            it.setAnalyzer(cameraExecutor, poseAnalyzer)
                                        }

                                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                } catch (_: Exception) {}
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay circolare di progresso dei secondi richiesti
                    val progress = (danceState.secondsDanced / targetSeconds).coerceIn(0f, 1f)
                    val animatedProgress by animateFloatAsState(targetValue = progress, label = "danceProgress")

                    Canvas(
                        modifier = Modifier
                            .size(240.dp)
                            .padding(16.dp)
                    ) {
                        val strokeWidth = 14.dp.toPx()

                        // Sfondo anello
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.5f),
                            radius = size.minDimension / 2 - strokeWidth / 2,
                            style = Stroke(width = strokeWidth)
                        )

                        // Progresso dinamico
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(NeonPink, NeonPinkBright, ElectricPurple, NeonPink)
                            ),
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Centro dell'anello: secondi accumulati
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(24.dp)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.1fs", danceState.secondsDanced),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = if (danceState.isDancing) NeonPink else TextWhite
                            )
                        )
                        Text(
                            text = "su ${targetSeconds.toInt()}s",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Badge Privacy on-device in basso alla preview
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = NeonMint,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "RAM-Only • Movimento Relativo • Zero Shaking",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonMint,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                } else {
                    // Stato Permesso Mancante: Pulsante Esplicito di Concessione
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(NeonPink.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = NeonPink,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Permesso Fotocamera Richiesto",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "La Dance Mission richiede la fotocamera per analizzare il movimento in RAM (zero salvataggio su disco).",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonPink,
                                contentColor = CyberBlack
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("AUTORIZZA FOTOCAMERA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Box Feedback Cinetico e Istruzioni
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = danceState.message,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (danceState.isDancing) NeonPinkBright else TextWhite,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Barra indicatore energia cinetica
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Energia Corporea Rilevata",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if (danceState.isDancing) "BALLO RILEVATO 🕺" else "IN PAUSA ⏸️",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (danceState.isDancing) NeonMint else TextMuted
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (danceState.currentEnergy / 1.0f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (danceState.isDancing) NeonPink else TextMuted,
                        trackColor = SurfaceElevated
                    )
                }
            }
        }
    }
}
