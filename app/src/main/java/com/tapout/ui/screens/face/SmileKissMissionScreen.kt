package com.tapout.ui.screens.face

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.tapout.ui.theme.NeonPurpleBright
import com.tapout.ui.theme.SurfaceBorder
import com.tapout.ui.theme.SurfaceDark
import com.tapout.ui.theme.SurfaceElevated
import com.tapout.ui.theme.TextMuted
import com.tapout.ui.theme.TextWhite
import com.tapout.vision.SmileKissAnalyzer
import com.tapout.vision.SmileKissDetector
import com.tapout.vision.SmileKissState
import java.util.concurrent.Executors

@Composable
fun SmileKissMissionScreen(
    targetSmiles: Int = SmileKissDetector.TARGET_COUNT,
    targetKisses: Int = SmileKissDetector.TARGET_COUNT,
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

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var state by remember {
        mutableStateOf(
            SmileKissState(
                isFaceDetected = false,
                smilesCount = 0,
                targetSmiles = targetSmiles,
                kissesCount = 0,
                targetKisses = targetKisses,
                isSmilingNow = false,
                isKissingNow = false,
                isCompleted = false,
                message = "Inquadra il tuo viso nella fotocamera"
            )
        )
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val detector = remember {
        SmileKissDetector(
            targetSmiles = targetSmiles,
            targetKisses = targetKisses,
            onMissionComplete = {
                onMissionCompleted()
            }
        )
    }

    val analyzer = remember {
        SmileKissAnalyzer(
            detector = detector,
            onStateUpdate = { newState ->
                state = newState
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
            analyzer.close()
        }
    }

    val borderColor by animateColorAsState(
        targetValue = when {
            state.isCompleted -> NeonMint
            state.isSmilingNow -> NeonPink
            state.isKissingNow -> ElectricPurple
            else -> SurfaceBorder
        },
        label = "borderColor"
    )

    val infinitePulse = rememberInfiniteTransition(label = "pulseGlow")
    val glowScale by infinitePulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

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
                        text = "Sorrisi & Baci",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                    )
                    Text(
                        text = "RISVEGLIO AFFETTUOSO • 5 SORRISI + 5 BACI",
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

            Spacer(modifier = Modifier.height(14.dp))

            // Indicatori Punteggio (Sorrisi & Baci)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card Sorrisi
                ScoreCard(
                    modifier = Modifier.weight(1f),
                    title = "Sorrisi",
                    emoji = "😊",
                    current = state.smilesCount,
                    target = targetSmiles,
                    isActive = state.isSmilingNow,
                    activeColor = NeonPink
                )

                // Card Baci
                ScoreCard(
                    modifier = Modifier.weight(1f),
                    title = "Baci",
                    emoji = "💋",
                    current = state.kissesCount,
                    target = targetKisses,
                    isActive = state.isKissingNow,
                    activeColor = ElectricPurple
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inquadratura Fotocamera Frontale
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceDark)
                    .border(2.dp, borderColor, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
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
                                            it.setAnalyzer(cameraExecutor, analyzer)
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

                    // Overlay icona dinamica quando rileva sorriso o bacio
                    if (state.isSmilingNow || state.isKissingNow) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(glowScale)
                                .clip(CircleShape)
                                .background(
                                    if (state.isSmilingNow) NeonPink.copy(alpha = 0.85f)
                                    else ElectricPurple.copy(alpha = 0.85f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.isSmilingNow) "😊" else "💋",
                                fontSize = 44.sp
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = NeonPink,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Permesso Fotocamera Richiesto",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Necessario per analizzare le espressioni facciali 100% on-device in RAM.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonPink,
                                contentColor = CyberBlack
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Consenti Accesso", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Box Istruzioni & Feedback in tempo reale
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            state.isCompleted -> NeonMint
                            state.isSmilingNow -> NeonPinkBright
                            state.isKissingNow -> NeonPurpleBright
                            else -> TextWhite
                        },
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ScoreCard(
    modifier: Modifier = Modifier,
    title: String,
    emoji: String,
    current: Int,
    target: Int,
    isActive: Boolean,
    activeColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(
                1.5.dp,
                if (isActive || current >= target) activeColor else SurfaceBorder,
                RoundedCornerShape(20.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5 slot di badge per visualizzare i progressi
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..target) {
                    val isDone = i <= current
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDone) activeColor else SurfaceElevated
                            )
                            .border(
                                1.dp,
                                if (isDone) activeColor else SurfaceBorder,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CyberBlack,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$current / $target",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = if (current >= target) activeColor else TextMuted
                )
            )
        }
    }
}
