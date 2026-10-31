package com.tapout.ui.screens.settings

import android.app.Activity
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tapout.nfc.NfcHelper
import com.tapout.ui.theme.CyberBlack
import com.tapout.ui.theme.ElectricPurple
import com.tapout.ui.theme.NeonMint
import com.tapout.ui.theme.NeonPurpleBright
import com.tapout.ui.theme.SurfaceBorder
import com.tapout.ui.theme.SurfaceDark
import com.tapout.ui.theme.SurfaceElevated
import com.tapout.ui.theme.TextMuted
import com.tapout.ui.theme.TextWhite

@Composable
fun NfcManagementDialog(
    onDismiss: () -> Unit,
    onTagSaved: (uid: String, name: String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val nfcHelper = remember { NfcHelper(context) }

    var scannedUid by remember { mutableStateOf<String?>(null) }
    var tagName by remember { mutableStateOf("Tag Sveglia Bagno") }

    DisposableEffect(Unit) {
        if (activity != null) {
            nfcHelper.enableReaderMode(activity) { uid ->
                scannedUid = uid
                NfcHelper.vibrateFeedback(activity, isSuccess = true)
            }
        }
        onDispose {
            if (activity != null) {
                nfcHelper.disableReaderMode(activity)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dialogPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (scannedUid == null) "Registra Tag NFC" else "Tag Rilevato!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(contentAlignment = Alignment.Center) {
                    if (scannedUid == null) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(ElectricPurple.copy(alpha = 0.15f))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (scannedUid != null) NeonMint.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(
                                2.dp,
                                if (scannedUid != null) NeonMint else ElectricPurple,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (scannedUid != null) Icons.Default.Check else Icons.Default.Nfc,
                            contentDescription = null,
                            tint = if (scannedUid != null) NeonMint else NeonPurpleBright,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (scannedUid == null) {
                    Text(
                        text = "Tocca il retro dello smartphone con qualsiasi tag NFC o tessera magnetica.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    )
                } else {
                    Text(
                        text = "UID Hardware Rilevato:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scannedUid!!,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonMint
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = tagName,
                        onValueChange = { tagName = it },
                        label = { Text("Nome descrittivo del tag") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricPurple,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annulla", color = TextMuted)
                    }
                    if (scannedUid != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                onTagSaved(scannedUid!!, tagName.ifBlank { "Tag Sveglia" })
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricPurple,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Salva Tag", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
