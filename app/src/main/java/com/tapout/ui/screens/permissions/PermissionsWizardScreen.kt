package com.tapout.ui.screens.permissions

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.tapout.TapOutApplication
import com.tapout.nfc.NfcHelper
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PermissionsWizardScreen(
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(false) }
    var hasExactAlarmPermission by remember { mutableStateOf(false) }
    var hasOverlayPermission by remember { mutableStateOf(false) }
    var hasAccessibilityPermission by remember { mutableStateOf(false) }
    var isBatteryIgnored by remember { mutableStateOf(false) }
    var isNfcEnabled by remember { mutableStateOf(false) }
    var showRevokeDialog by remember { mutableStateOf(false) }

    fun refreshPermissions() {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        hasExactAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isBatteryIgnored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }

        val nfcHelper = NfcHelper(context)
        isNfcEnabled = nfcHelper.isNfcEnabled

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        hasAccessibilityPermission = enabledServices.contains(context.packageName)
    }

    LaunchedEffect(Unit) {
        while (true) {
            refreshPermissions()
            delay(1200)
        }
    }

    // Launcher Combinato per permessi di sistema runtime (Fotocamera + Notifiche in 1 tap!)
    val batchPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshPermissions()
    }

    val isBaseGranted = hasCameraPermission && hasNotificationPermission

    val systemPermsCount = listOf(hasExactAlarmPermission, isBatteryIgnored, hasOverlayPermission, hasAccessibilityPermission).count { it }
    val isAllSystemGranted = systemPermsCount == 4
    val allCrucialGranted = isBaseGranted && isAllSystemGranted

    fun requestNextPendingSystem() {
        if (!hasExactAlarmPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
            return
        }

        if (!isBatteryIgnored && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
            return
        }

        if (!hasOverlayPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
            return
        }

        if (!hasAccessibilityPermission) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
            return
        }
    }

    fun requestAutoNext() {
        if (!isBaseGranted) {
            val perms = mutableListOf(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            batchPermissionLauncher.launch(perms.toTypedArray())
            return
        }
        requestNextPendingSystem()
    }

    val infinitePulse = rememberInfiniteTransition(label = "nfcRadarPulse")
    val radarScale by infinitePulse.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarScale"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))

            // Header Wizard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Configurazione Permessi",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            fontSize = 24.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ACCORPAMENTO RAPIDO IN 2 PASSAGGI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonPinkBright,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeonPink.copy(alpha = 0.2f))
                        .border(1.dp, NeonPink.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NeonPink,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Attiva le autorizzazioni per assicurare che la sveglia suoni sempre con precisione e che il blocco app funzioni.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pulsante Master Configurazione Rapida
            if (!allCrucialGranted) {
                Button(
                    onClick = { requestAutoNext() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPink,
                        contentColor = CyberBlack
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚡ Configurazione Rapida Continua",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        // ============================================================
        // 1. MACRO-SCHEDA: Permessi Base App (Fotocamera & Notifiche in 1 Tap)
        // ============================================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceDark)
                    .border(
                        1.dp,
                        if (isBaseGranted) NeonMint.copy(alpha = 0.6f) else SurfaceBorder,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(16.dp)
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isBaseGranted) NeonMint.copy(alpha = 0.2f) else NeonPink.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isBaseGranted) Icons.Default.Check else Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = if (isBaseGranted) NeonMint else NeonPink,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "1. Permessi Base dell'App",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 15.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isBaseGranted) NeonMint.copy(alpha = 0.15f) else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isBaseGranted) NeonMint.copy(alpha = 0.4f) else SurfaceBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isBaseGranted) "CONCESSO" else "1 TAP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isBaseGranted) NeonMint else NeonPink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Fotocamera per le sfide visive (Dance & Sorrisi) e Notifiche per gli avvisi della sveglia.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )

                    if (!isBaseGranted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val perms = mutableListOf(Manifest.permission.CAMERA)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                batchPermissionLauncher.launch(perms.toTypedArray())
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonPink,
                                contentColor = CyberBlack
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Consenti Fotocamera e Notifiche Insieme", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // ============================================================
        // 2. MACRO-SCHEDA: Servizi di Sistema & Protezione (Griglia 2x2 Spaziosa)
        // ============================================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceDark)
                    .border(
                        1.dp,
                        if (isAllSystemGranted) NeonMint.copy(alpha = 0.6f) else SurfaceBorder,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(16.dp)
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isAllSystemGranted) NeonMint.copy(alpha = 0.2f) else ElectricPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAllSystemGranted) Icons.Default.Check else Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = if (isAllSystemGranted) NeonMint else ElectricPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "2. Servizi di Sistema",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 15.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAllSystemGranted) NeonMint.copy(alpha = 0.15f) else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isAllSystemGranted) NeonMint.copy(alpha = 0.4f) else SurfaceBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isAllSystemGranted) "ATTIVO (4/4)" else "$systemPermsCount / 4 ATTIVI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isAllSystemGranted) NeonMint else TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Sveglie Esatte, Risparmio Batteria, Schermo Intero e Blocco App.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Griglia 2x2 con testo completo e leggibile
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SystemPermissionChip(
                                modifier = Modifier.weight(1f),
                                name = "Sveglie Esatte",
                                isGranted = hasExactAlarmPermission
                            )
                            SystemPermissionChip(
                                modifier = Modifier.weight(1f),
                                name = "No Batteria Doze",
                                isGranted = isBatteryIgnored
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SystemPermissionChip(
                                modifier = Modifier.weight(1f),
                                name = "Schermo Intero",
                                isGranted = hasOverlayPermission
                            )
                            SystemPermissionChip(
                                modifier = Modifier.weight(1f),
                                name = "Accessibilità Blocco",
                                isGranted = hasAccessibilityPermission
                            )
                        }
                    }

                    if (!isAllSystemGranted) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { requestNextPendingSystem() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricPurple,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Configura Servizi Mancanti in Sequenza", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ============================================================
        // 3. SEZIONE DEDICATA: Sensore Hardware NFC (Design Cyber Hardware Distinto)
        // ============================================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (isNfcEnabled) NeonMint.copy(alpha = 0.12f) else Color(0xFFFF9800).copy(alpha = 0.12f),
                                SurfaceDark
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        if (isNfcEnabled) NeonMint else Color(0xFFFF9800).copy(alpha = 0.7f),
                        RoundedCornerShape(20.dp)
                    )
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
                                    .scale(if (isNfcEnabled) 1f else radarScale)
                                    .clip(CircleShape)
                                    .background(
                                        if (isNfcEnabled) NeonMint.copy(alpha = 0.2f)
                                        else Color(0xFFFF9800).copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = null,
                                    tint = if (isNfcEnabled) NeonMint else Color(0xFFFF9800),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Sensore Hardware NFC",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = TextWhite,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = "CHIP FISICO DEL DISPOSITIVO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isNfcEnabled) NeonMint else Color(0xFFFF9800),
                                        letterSpacing = 0.8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isNfcEnabled) NeonMint.copy(alpha = 0.18f)
                                    else Color(0xFFFF9800).copy(alpha = 0.18f)
                                )
                                .border(
                                    1.dp,
                                    if (isNfcEnabled) NeonMint else Color(0xFFFF9800),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isNfcEnabled) "🟢 CHIP ATTIVO" else "⚠️ SPENTO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isNfcEnabled) NeonMint else Color(0xFFFFB74D),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isNfcEnabled)
                            "Il chip NFC è attivo e pronto a rilevare i tuoi tag per spegnere la sveglia o sbloccare le app."
                        else
                            "Il chip NFC è disattivato. Accendilo per poter utilizzare i tag fisici come metodo di spegnimento.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )

                    if (!isNfcEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val nfcHelper = NfcHelper(context)
                                nfcHelper.tryEnableNfcOrOpenPrompt(context as android.app.Activity)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800),
                                contentColor = CyberBlack
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("⚡ Accendi Chip NFC Subito", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ============================================================
        // 4. PULSANTE FINALE: Salva & Continua
        // ============================================================
        item {
            Button(
                onClick = {
                    scope.launch {
                        TapOutApplication.settingsRepository.setOnboardingCompleted(true)
                        onContinue()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allCrucialGranted) NeonMint else NeonPink,
                    contentColor = CyberBlack
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (allCrucialGranted) "Configurazione Completata • Apri TapOut" else "Continua Comunque",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Opzione discreta per gestione e revoca dei permessi singoli
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { showRevokeDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Gestisci o revoca autorizzazioni singolarmente",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    )
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
private fun SystemPermissionChip(
    modifier: Modifier = Modifier,
    name: String,
    isGranted: Boolean
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isGranted) NeonMint.copy(alpha = 0.15f) else SurfaceElevated)
            .border(
                1.dp,
                if (isGranted) NeonMint.copy(alpha = 0.5f) else SurfaceBorder,
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isGranted) NeonMint else Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isGranted) NeonMint else Color(0xFF94A3B8),
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}
