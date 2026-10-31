package com.tapout.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapout.TapOutApplication
import com.tapout.data.repository.SubscriptionPlan
import com.tapout.ui.screens.dance.DanceMissionScreen
import com.tapout.ui.screens.face.SmileKissMissionScreen
import com.tapout.ui.screens.permissions.RevokePermissionsDialog
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
import com.tapout.vision.DanceDetector
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onOpenPermissionsWizard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val registeredTags by TapOutApplication.settingsRepository.allNfcTags.collectAsState(initial = emptyList())
    val isTestMode by TapOutApplication.settingsRepository.isTestMode.collectAsState(initial = true)
    val plan by TapOutApplication.billingManager.currentPlan.collectAsState()

    var showNfcDialog by remember { mutableStateOf(false) }
    var showNfcProLimitDialog by remember { mutableStateOf(false) }
    var showRevokeDialog by remember { mutableStateOf(false) }
    var isTestingDanceMission by remember { mutableStateOf(false) }
    var isTestingSmileKissMission by remember { mutableStateOf(false) }

    if (isTestingDanceMission) {
        DanceMissionScreen(
            targetSeconds = DanceDetector.DANCE_REQUIRED_SECONDS,
            onMissionCompleted = {
                isTestingDanceMission = false
            },
            onCancel = {
                isTestingDanceMission = false
            }
        )
        return
    }

    if (isTestingSmileKissMission) {
        SmileKissMissionScreen(
            onMissionCompleted = {
                isTestingSmileKissMission = false
            },
            onCancel = {
                isTestingSmileKissMission = false
            }
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Header Impostazioni
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Impostazioni",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                    )
                    Text(
                        text = "HARDWARE NFC, SICUREZZA E PRIVACY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonPinkBright,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sezione Tag NFC Hardware (Supporto Multi-Tag PRO)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tag NFC Fisici",
                    style = MaterialTheme.typography.titleLarge.copy(color = TextWhite)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (plan == SubscriptionPlan.PREMIUM) NeonMint.copy(alpha = 0.2f)
                            else ElectricPurple.copy(alpha = 0.2f)
                        )
                        .border(
                            1.dp,
                            if (plan == SubscriptionPlan.PREMIUM) NeonMint.copy(alpha = 0.5f)
                            else ElectricPurple.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (plan == SubscriptionPlan.PREMIUM) "${registeredTags.size} Tag (PRO VIP 👑)"
                        else "${registeredTags.size} / 1 Tag (Base)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (plan == SubscriptionPlan.PREMIUM) NeonMint else NeonPurpleBright,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (registeredTags.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nessun Tag Registrato",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            Text(
                                text = "Qualsiasi tag NFC spegnerà la sveglia come fallback.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    registeredTags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(ElectricPurple.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Nfc,
                                            contentDescription = null,
                                            tint = NeonPurpleBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = tag.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite,
                                                fontSize = 15.sp
                                            )
                                        )
                                        Text(
                                            text = "UID: ${tag.uid}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = NeonMint,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            TapOutApplication.settingsRepository.deleteNfcTag(tag.id)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Rimuovi Tag",
                                        tint = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (plan == SubscriptionPlan.PREMIUM || registeredTags.isEmpty()) {
                        showNfcDialog = true
                    } else {
                        showNfcProLimitDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricPurple,
                    contentColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (registeredTags.isEmpty()) "Registra Tag NFC Principale" else "Aggiungi Altro Tag NFC (Multi-Tag)",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sezione Collaudo & Simulatore
            Text(
                text = "Collaudo & Simulatore",
                style = MaterialTheme.typography.titleLarge.copy(color = TextWhite)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Pulsante Prova Dance Mission
            SettingsActionRow(
                icon = Icons.Default.DirectionsRun,
                iconTint = NeonPink,
                title = "Collauda Dance Mission (30s)",
                subtitle = "Avvia la fotocamera per testare il riconoscimento del movimento in tempo reale",
                onClick = { isTestingDanceMission = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pulsante Prova Smile & Kiss Mission
            SettingsActionRow(
                icon = Icons.Default.SentimentVerySatisfied,
                iconTint = ElectricPurple,
                title = "Collauda Sorrisi & Baci (5 + 5)",
                subtitle = "Verifica il riconoscimento facciale on-device di 5 sorrisi e 5 baci",
                onClick = { isTestingSmileKissMission = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsToggleRow(
                icon = Icons.Default.TouchApp,
                iconTint = NeonMint,
                title = "Modalit├á Test Senza Tag",
                subtitle = "Consente di simulare il tap NFC a schermo per testare l'app prima dell'arrivo del tag fisico",
                checked = isTestMode,
                onCheckedChange = { enable ->
                    scope.launch {
                        TapOutApplication.settingsRepository.setTestMode(enable)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sezione Freemium & Simulazione Abbonamento Premium
            Text(
                text = "Modello Freemium & Licenza",
                style = MaterialTheme.typography.titleLarge.copy(color = TextWhite)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SurfaceDark, SurfaceElevated)
                        )
                    )
                    .border(
                        1.5.dp,
                        if (plan == SubscriptionPlan.PREMIUM) NeonMint else SurfaceBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        val next = plan != SubscriptionPlan.PREMIUM
                        TapOutApplication.billingManager.toggleSimulatedPremium(next)
                    }
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (plan == SubscriptionPlan.PREMIUM) NeonMint.copy(alpha = 0.2f)
                                        else NeonPink.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (plan == SubscriptionPlan.PREMIUM) NeonMint else NeonPink,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (plan == SubscriptionPlan.PREMIUM) "TapOut PRO (Attivo Ô¡É)" else "TapOut Base (Gratuito)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (plan == SubscriptionPlan.PREMIUM) NeonMint else TextWhite
                                    )
                                )
                                Text(
                                    text = if (plan == SubscriptionPlan.PREMIUM) "Tutte le feature avanzate sbloccate" else "Funzionalit├á essenziali gratuite",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                            }
                        }

                        Switch(
                            checked = plan == SubscriptionPlan.PREMIUM,
                            onCheckedChange = { isPremium ->
                                TapOutApplication.billingManager.toggleSimulatedPremium(isPremium)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberBlack,
                                checkedTrackColor = NeonMint,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceElevated
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tabella di confronto Feature Base vs PRO
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberBlack.copy(alpha = 0.5f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PlanFeatureRow(
                            feature = "App Bloccabili:",
                            baseValue = "Max 2 App",
                            proValue = "Illimitate ÔÜí",
                            isPro = plan == SubscriptionPlan.PREMIUM
                        )
                        PlanFeatureRow(
                            feature = "Sveglie & Dance/Sorrisi:",
                            baseValue = "Illimitate",
                            proValue = "Illimitate",
                            isPro = plan == SubscriptionPlan.PREMIUM
                        )
                        PlanFeatureRow(
                            feature = "Tag NFC:",
                            baseValue = "1 Tag Registrato",
                            proValue = "Multi-Tag Fisici ÔÜí",
                            isPro = plan == SubscriptionPlan.PREMIUM
                        )
                        PlanFeatureRow(
                            feature = "Badge Profilo:",
                            baseValue = "Base Free",
                            proValue = "Badge PRO VIP ­ƒææ",
                            isPro = plan == SubscriptionPlan.PREMIUM
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val next = plan != SubscriptionPlan.PREMIUM
                            TapOutApplication.billingManager.toggleSimulatedPremium(next)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (plan == SubscriptionPlan.PREMIUM) SurfaceElevated else NeonMint,
                            contentColor = if (plan == SubscriptionPlan.PREMIUM) TextWhite else CyberBlack
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (plan == SubscriptionPlan.PREMIUM) "Simula Ritorno a Piano Base" else "Simula Passaggio a TapOut PRO ⭐",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Permessi e Privacy
            Text(
                text = "Autorizzazioni & Privacy",
                style = MaterialTheme.typography.titleLarge.copy(color = TextWhite)
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingsActionRow(
                icon = Icons.Default.Security,
                iconTint = NeonPink,
                title = "Wizard Verifica Permessi di Sistema",
                subtitle = "Verifica lo stato di Notifiche, Fotocamera, Sveglie e Accessibilità",
                onClick = onOpenPermissionsWizard
            )

            // Scritta discreta per togliere o gestire permessi singoli
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = { showRevokeDialog = true },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Gestisci o revoca autorizzazioni singolarmente",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted.copy(alpha = 0.65f),
                            fontSize = 11.5.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SettingsActionRow(
                icon = Icons.Default.Lock,
                iconTint = NeonMint,
                title = "Privacy-First: Visione Esclusivamente On-Device",
                subtitle = "I fotogrammi per la Dance Mission transitano unicamente in RAM per l'inferenza di ML Kit e non vengono mai salvati né trasmessi.",
                onClick = {}
            )
        }
    }

    if (showNfcDialog) {
        NfcManagementDialog(
            onDismiss = { showNfcDialog = false },
            onTagSaved = { uid, name ->
                scope.launch {
                    TapOutApplication.settingsRepository.saveNfcTag(uid, name)
                    showNfcDialog = false
                }
            }
        )
    }

    if (showNfcProLimitDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showNfcProLimitDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(1.5.dp, NeonMint, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(NeonMint.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = NeonMint,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Multi-Tag Fisici (Solo TapOut PRO 👑)",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Nel piano Base gratuito puoi registrare 1 singolo Tag NFC.\n\nCon TapOut PRO puoi registrare molteplici tag fisici (es. uno in bagno, uno in cucina, il tuo badge aziendale) e spegnere la sveglia con uno qualsiasi di essi!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            TapOutApplication.billingManager.toggleSimulatedPremium(true)
                            showNfcProLimitDialog = false
                            showNfcDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonMint,
                            contentColor = CyberBlack
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Attiva TapOut PRO Ora", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    if (showRevokeDialog) {
        RevokePermissionsDialog(
            onDismiss = { showRevokeDialog = false }
        )
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyberBlack,
                checkedTrackColor = iconTint,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SurfaceElevated
            )
        )
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
            )
        }
    }
}

@Composable
private fun PlanFeatureRow(
    feature: String,
    baseValue: String,
    proValue: String,
    isPro: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontSize = 12.sp
            )
        )
        Text(
            text = if (isPro) proValue else baseValue,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isPro) NeonMint else TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        )
    }
}
