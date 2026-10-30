package com.tapout.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapout.TapOutApplication
import com.tapout.alarm.AlarmScheduler
import com.tapout.data.local.entity.AlarmEntity
import com.tapout.data.repository.SubscriptionPlan
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
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AlarmDashboardScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val alarmScheduler = remember { AlarmScheduler(context) }
    val alarms by TapOutApplication.database.alarmDao().getAllAlarms().collectAsState(initial = emptyList())
    val plan by TapOutApplication.billingManager.currentPlan.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header TapOut Neon Pink
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tap",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                        )
                        Text(
                            text = "Out",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = NeonPink
                            )
                        )
                    }
                    Text(
                        text = "SVEGLIA INARRESTABILE & DANCE MISSION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonPinkBright,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (plan == SubscriptionPlan.PREMIUM) NeonMint.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(1.dp, if (plan == SubscriptionPlan.PREMIUM) NeonMint else SurfaceBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (plan == SubscriptionPlan.PREMIUM) "👑 PRO" else "BASE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (plan == SubscriptionPlan.PREMIUM) NeonMint else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonPink.copy(alpha = 0.2f), ElectricPurple.copy(alpha = 0.2f))
                                )
                            )
                            .border(1.dp, NeonPink.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = NeonPink,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = TextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nessuna sveglia attiva",
                            style = MaterialTheme.typography.titleMedium.copy(color = TextMuted)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tocca il pulsante '+' per creare la tua prima sveglia",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted.copy(alpha = 0.7f))
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onToggle = { isEnabled ->
                                scope.launch {
                                    val updated = alarm.copy(isEnabled = isEnabled)
                                    TapOutApplication.database.alarmDao().updateAlarm(updated)
                                    alarmScheduler.scheduleAlarm(updated)
                                }
                            },
                            onClick = {
                                editingAlarm = alarm
                                showEditDialog = true
                            },
                            onDelete = {
                                scope.launch {
                                    alarmScheduler.cancelAlarm(alarm.id)
                                    TapOutApplication.database.alarmDao().deleteAlarm(alarm)
                                }
                            }
                        )
                    }
                }
            }
        }

        // FAB per aggiungere una sveglia
        FloatingActionButton(
            onClick = {
                editingAlarm = null
                showEditDialog = true
            },
            containerColor = NeonPink,
            contentColor = CyberBlack,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .size(60.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Nuova Sveglia",
                modifier = Modifier.size(32.dp)
            )
        }
    }

    if (showEditDialog) {
        AlarmEditDialog(
            alarmToEdit = editingAlarm,
            onDismiss = { showEditDialog = false },
            onSave = { savedAlarm ->
                scope.launch {
                    val id = if (savedAlarm.id == 0L) {
                        TapOutApplication.database.alarmDao().insertAlarm(savedAlarm)
                    } else {
                        TapOutApplication.database.alarmDao().updateAlarm(savedAlarm)
                        savedAlarm.id
                    }
                    val finalAlarm = savedAlarm.copy(id = id)
                    alarmScheduler.scheduleAlarm(finalAlarm)
                    showEditDialog = false
                }
            }
        )
    }
}

@Composable
fun AlarmCard(
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)
    val daysText = formatDaysOfWeek(alarm.daysOfWeek)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(
                1.dp,
                if (alarm.isEnabled) NeonPink.copy(alpha = 0.4f) else SurfaceBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = if (alarm.isEnabled) TextWhite else TextMuted
                        )
                    )
                    val showLabel = alarm.label.isNotBlank() &&
                            alarm.label != "Sveglia TapOut" &&
                            alarm.label != "Sveglia"
                    if (showLabel) {
                        Text(
                            text = alarm.label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (alarm.isEnabled) NeonPinkBright else TextMuted
                            )
                        )
                    }
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberBlack,
                        checkedTrackColor = NeonPink,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = daysText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (alarm.isEnabled) TextWhite.copy(alpha = 0.85f) else TextMuted
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (alarm.requireNfc) {
                        MissionBadge(icon = Icons.Default.Nfc, text = "NFC", color = ElectricPurple)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (alarm.requireDance) {
                        MissionBadge(icon = Icons.Default.DirectionsRun, text = "DANCE 30s", color = NeonPink)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (alarm.requireSmileKiss) {
                        MissionBadge(icon = Icons.Default.Favorite, text = "5+5 VISUAL", color = NeonPinkBright)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Elimina",
                            tint = TextMuted.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MissionBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 10.sp
                )
            )
        }
    }
}

private fun formatDaysOfWeek(days: String): String {
    if (days.isBlank()) return "Una volta sola"
    val dayList = days.split(",").mapNotNull { it.trim().toIntOrNull() }
    if (dayList.size == 7) return "Tutti i giorni"
    if (dayList == listOf(1, 2, 3, 4, 5)) return "Lun - Ven"
    if (dayList == listOf(6, 7)) return "Fine settimana"

    val names = mapOf(1 to "Lun", 2 to "Mar", 3 to "Mer", 4 to "Gio", 5 to "Ven", 6 to "Sab", 7 to "Dom")
    return dayList.mapNotNull { names[it] }.joinToString(", ")
}
