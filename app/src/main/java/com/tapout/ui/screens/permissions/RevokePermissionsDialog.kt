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
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.tapout.ui.theme.CyberBlack
import com.tapout.ui.theme.NeonMint
import com.tapout.ui.theme.SurfaceBorder
import com.tapout.ui.theme.SurfaceDark
import com.tapout.ui.theme.SurfaceElevated
import com.tapout.ui.theme.TextMuted
import com.tapout.ui.theme.TextWhite
import kotlinx.coroutines.delay

enum class RevokablePermissionType(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    CAMERA(
        title = "Fotocamera",
        description = "Utilizzata in sola RAM per la Dance Mission e il riconoscimento dei Sorrisi/Baci.",
        icon = Icons.Default.CameraAlt
    ),
    NOTIFICATIONS(
        title = "Notifiche",
        description = "Consente di ricevere gli avvisi della sveglia e le notifiche di stato in background.",
        icon = Icons.Default.Notifications
    ),
    EXACT_ALARM(
        title = "Sveglie Esatte",
        description = "Garantisce precisione al secondo per AlarmManager evitando che la sveglia ritardi.",
        icon = Icons.Default.Schedule
    ),
    BATTERY_OPTIMIZATION(
        title = "Ottimizzazione Batteria",
        description = "Impedisce al risparmio energetico Doze di bloccare la riproduzione della sveglia.",
        icon = Icons.Default.BatteryAlert
    ),
    OVERLAY(
        title = "Mostra sopra altre app",
        description = "Permette alla schermata della sveglia di comparire sopra il lockscreen.",
        icon = Icons.Default.Layers
    ),
    ACCESSIBILITY(
        title = "Servizio Accessibilità",
        description = "Permette a TapOut di rilevare e bloccare le applicazioni vietate.",
        icon = Icons.Default.Accessibility
    )
}

@Composable
fun RevokePermissionsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var hasCamera by remember { mutableStateOf(false) }
    var hasNotifications by remember { mutableStateOf(false) }
    var hasExactAlarm by remember { mutableStateOf(false) }
    var isBatteryIgnored by remember { mutableStateOf(false) }
    var hasOverlay by remember { mutableStateOf(false) }
    var hasAccessibility by remember { mutableStateOf(false) }

    fun refreshState() {
        hasCamera = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        hasExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.canScheduleExactAlarms()
        } else {
            true
        }

        hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isBatteryIgnored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        hasAccessibility = enabledServices.contains(context.packageName)
    }

    LaunchedEffect(Unit) {
        while (true) {
            refreshState()
            delay(1200)
        }
    }

    fun isPermissionActive(type: RevokablePermissionType): Boolean {
        return when (type) {
            RevokablePermissionType.CAMERA -> hasCamera
            RevokablePermissionType.NOTIFICATIONS -> hasNotifications
            RevokablePermissionType.EXACT_ALARM -> hasExactAlarm
            RevokablePermissionType.BATTERY_OPTIMIZATION -> isBatteryIgnored
            RevokablePermissionType.OVERLAY -> hasOverlay
            RevokablePermissionType.ACCESSIBILITY -> hasAccessibility
        }
    }

    fun revokePermission(type: RevokablePermissionType) {
        when (type) {
            RevokablePermissionType.CAMERA -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    try {
                        context.revokeSelfPermissionOnKill(Manifest.permission.CAMERA)
                    } catch (_: Exception) {}
                }
                Toast.makeText(context, "Disattiva il permesso Fotocamera nelle impostazioni", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
            RevokablePermissionType.NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    try {
                        context.revokeSelfPermissionOnKill(Manifest.permission.POST_NOTIFICATIONS)
                    } catch (_: Exception) {}
                    Toast.makeText(context, "Disattiva le notifiche per TapOut", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Disattiva le notifiche nelle impostazioni", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
            RevokablePermissionType.EXACT_ALARM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Toast.makeText(context, "Disattiva l'interruttore Sveglie Esatte", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Non revocabile su questa versione Android", Toast.LENGTH_SHORT).show()
                }
            }
            RevokablePermissionType.BATTERY_OPTIMIZATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Toast.makeText(context, "Imposta ottimizzazione batteria su 'Ottimizza'", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                }
            }
            RevokablePermissionType.OVERLAY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Toast.makeText(context, "Disattiva l'autorizzazione di sovrapposizione", Toast.LENGTH_SHORT).show()
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            }
            RevokablePermissionType.ACCESSIBILITY -> {
                Toast.makeText(context, "Disattiva il servizio TapOut Blocker", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(CyberBlack)
                .border(1.5.dp, SurfaceBorder, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Dialog
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFE53935).copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color(0xFFFF8A80),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Gestione & Revoca Permessi",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 17.sp
                                )
                            )
                            Text(
                                text = "DISATTIVAZIONE SINGOLA NELLE IMPOSTAZIONI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFF8A80),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiudi",
                            tint = Color(0xFFCBD5E1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Tocca 'Disattiva' sul singolo permesso per aprire la relativa schermata di sistema dove puoi revocare l'autorizzazione.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lista delle singole autorizzazioni - Schede verticali pulite e spaziose
                RevokablePermissionType.values().forEach { permType ->
                    val isActive = isPermissionActive(permType)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .border(
                                1.dp,
                                if (isActive) Color(0xFFE53935).copy(alpha = 0.35f) else SurfaceBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Riga 1: Icona + Titolo + Badge Stato
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
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isActive) NeonMint.copy(alpha = 0.15f)
                                                else SurfaceElevated
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = permType.icon,
                                            contentDescription = null,
                                            tint = if (isActive) NeonMint else Color(0xFF94A3B8),
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = permType.title,
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
                                        .background(
                                            if (isActive) NeonMint.copy(alpha = 0.15f)
                                            else SurfaceElevated
                                        )
                                        .border(
                                            1.dp,
                                            if (isActive) NeonMint.copy(alpha = 0.4f) else SurfaceBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isActive) "🟢 ATTIVO" else "⚪ NON ATTIVO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isActive) NeonMint else Color(0xFF94A3B8),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Riga 2: Descrizione dettagliata ad alta leggibilità
                            Text(
                                text = permType.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Riga 3: Pulsante di azione ben dimensionato
                            if (isActive) {
                                Button(
                                    onClick = { revokePermission(permType) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE53935).copy(alpha = 0.18f),
                                        contentColor = Color(0xFFFF8A80)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color(0xFFE53935).copy(alpha = 0.6f)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = "🔴 Disattiva nelle Impostazioni ↗",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Permesso già disattivato",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pulsante Chiudi a fondo dialogo
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = TextWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Text(
                        text = "Chiudi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
