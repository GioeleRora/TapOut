package com.tapout.ui.screens.alarmactive

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
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapout.ui.theme.CyberBlack
import com.tapout.ui.theme.ElectricPurple
import com.tapout.ui.theme.NeonMint
import com.tapout.ui.theme.NeonPink
import com.tapout.ui.theme.NeonPinkBright
import com.tapout.ui.theme.NeonPurpleBright
import com.tapout.ui.theme.SurfaceDark
import com.tapout.ui.theme.SurfaceElevated
import com.tapout.ui.theme.TextMuted
import com.tapout.ui.theme.TextWhite
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlarmActiveScreen(
    alarmLabel: String,
    requireNfc: Boolean,
    requireDance: Boolean,
    requireSmileKiss: Boolean = false,
    isTestMode: Boolean,
    isNfcHardwareEnabled: Boolean = true,
    registeredTagNames: List<String> = emptyList(),
    scanFeedbackMessage: String? = null,
    isScanError: Boolean = false,
    onOpenNfcPanel: () -> Unit = {},
    onSimulateNfcTap: () -> Unit,
    onStartDanceMission: () -> Unit,
    onStartSmileKissMission: () -> Unit = {}
) {
    // Orologio in tempo reale ad alta precisione con secondi
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ITALIAN) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeString = timeFormatter.format(now)
            currentDateString = dateFormatter.format(now).replaceFirstChar { it.uppercase() }
            delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseRadar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Sezione Superiore: Badge e Orologio Gigante
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Badge di blocco sistema
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonPink.copy(alpha = 0.15f))
                        .border(1.dp, NeonPink.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NeonPink,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BLOCCO SISTEMA ATTIVO • NESSUN SNOOZE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = NeonPink,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Orologio Digitale Gigante (HH:mm:ss)
                Text(
                    text = currentTimeString.ifBlank { "--:--:--" },
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 72.sp,
                        letterSpacing = (-2).sp,
                        color = TextWhite
                    ),
                    textAlign = TextAlign.Center
                )

                // Data Completa
                Text(
                    text = currentDateString,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = NeonPinkBright,
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )

                val showCustomLabel = alarmLabel.isNotBlank() &&
                        alarmLabel != "Sveglia TapOut" &&
                        alarmLabel != "Sveglia"
                if (showCustomLabel) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = alarmLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                }
            }

            // Sezione Centrale Dinamica: cambia in base ai metodi di spegnimento configurati
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (requireNfc) {
                    if (!isNfcHardwareEnabled) {
                        Button(
                            onClick = onOpenNfcPanel,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color(0xFFFF9800),
                                contentColor = CyberBlack
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "⚠️ Chip NFC Spento! Tocca per Accenderlo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Box(contentAlignment = Alignment.Center) {
                        // Alone pulsante
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(ElectricPurple.copy(alpha = 0.12f))
                        )

                        // Cerchio principale NFC
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(ElectricPurple.copy(alpha = 0.35f), SurfaceDark)
                                    )
                                )
                                .border(2.dp, ElectricPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = null,
                                tint = NeonPurpleBright,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (scanFeedbackMessage != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isScanError) androidx.compose.ui.graphics.Color(0xFFE53935).copy(alpha = 0.25f)
                                    else NeonMint.copy(alpha = 0.25f)
                                )
                                .border(
                                    1.dp,
                                    if (isScanError) androidx.compose.ui.graphics.Color(0xFFE53935) else NeonMint,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = scanFeedbackMessage,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isScanError) androidx.compose.ui.graphics.Color(0xFFFF8A80) else NeonMint,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = "Avvicina il Tag NFC al retro del telefono",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        ),
                        textAlign = TextAlign.Center
                    )

                    if (registeredTagNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tag configurati: ${registeredTagNames.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonMint,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = if (requireDance || requireSmileKiss)
                            "Oppure completa una delle missioni qui sotto"
                        else
                            "Spegni la sveglia con il contatto fisico",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                        textAlign = TextAlign.Center
                    )
                } else {
                    // NFC NON configurato: missione visiva o motoria primaria
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(NeonPink.copy(alpha = 0.12f))
                        )

                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(NeonPink.copy(alpha = 0.35f), SurfaceDark)
                                    )
                                )
                                .border(2.dp, NeonPink, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (requireSmileKiss && !requireDance) "💋" else "💃",
                                fontSize = 48.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = when {
                            requireDance && requireSmileKiss -> "Missione Visiva Richiesta"
                            requireDance -> "Dance Mission Richiesta"
                            requireSmileKiss -> "Sorrisi & Baci Richiesti"
                            else -> "Sveglia in Corso"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = when {
                            requireDance && requireSmileKiss -> "Completa una delle sfide qui sotto per spegnere l'allarme"
                            requireDance -> "Balla 30 secondi davanti alla fotocamera per spegnere l'allarme"
                            requireSmileKiss -> "Fai 5 sorrisi e 5 baci alla fotocamera per spegnere l'allarme"
                            else -> "Completa l'azione richiesta per spegnere"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Sezione Inferiore: Dance Mission (5s) & Simulatore Test
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (requireDance) {
                    Button(
                        onClick = onStartDanceMission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPink,
                            contentColor = CyberBlack
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AVVIA DANCE MISSION (30s)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (requireSmileKiss) {
                    Button(
                        onClick = onStartSmileKissMission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricPurple,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("💋", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AVVIA SORRISI & BACI (5+5)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (isTestMode) {
                    Button(
                        onClick = onSimulateNfcTap,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceElevated,
                            contentColor = NeonMint
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (requireNfc) "Simula Tap NFC (Modalità Test Senza Tag)" else "Spegni Sveglia (Modalità Test)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
