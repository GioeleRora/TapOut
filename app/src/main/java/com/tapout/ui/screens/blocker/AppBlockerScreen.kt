package com.tapout.ui.screens.blocker

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.tapout.data.repository.BillingManager
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapout.TapOutApplication
import com.tapout.data.local.entity.BlockedAppEntity
import com.tapout.data.local.entity.BlockScheduleEntity
import com.tapout.data.repository.SubscriptionPlan
import com.tapout.ui.components.AppIcon
import com.tapout.ui.theme.CyberBlack
import com.tapout.ui.theme.ElectricPurple
import com.tapout.ui.theme.NeonMint
import com.tapout.ui.theme.NeonPurpleBright
import com.tapout.ui.theme.SurfaceBorder
import com.tapout.ui.theme.SurfaceDark
import com.tapout.ui.theme.SurfaceElevated
import com.tapout.ui.theme.TextMuted
import com.tapout.ui.theme.TextWhite
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AppBlockerScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val blockedApps by TapOutApplication.database.blockedAppDao().getAllBlockedApps().collectAsState(initial = emptyList())
    val schedules by TapOutApplication.database.blockScheduleDao().getAllSchedules().collectAsState(initial = emptyList())
    val immediateBlockUntil by TapOutApplication.settingsRepository.immediateBlockUntil.collectAsState(initial = 0L)
    val plan by TapOutApplication.billingManager.currentPlan.collectAsState()

    var showAppSelectionDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showProUpgradeDialog by remember { mutableStateOf(false) }

    val isImmediateActive = immediateBlockUntil > System.currentTimeMillis()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Header Blocco App
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "App Blocker",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                    )
                    Text(
                        text = "PROTEZIONE FISICA ANTI-DISTRAZIONE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonPurpleBright,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ElectricPurple.copy(alpha = 0.2f))
                        .border(1.dp, ElectricPurple.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ElectricPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sezione Blocco Immediato (Sessione Focus Rapida)
            ImmediateBlockCard(
                isImmediateActive = isImmediateActive,
                untilTimestamp = immediateBlockUntil,
                onStart = { durationMinutes ->
                    scope.launch {
                        val until = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
                        TapOutApplication.settingsRepository.setImmediateBlock(until)
                    }
                },
                onStop = {
                    scope.launch {
                        TapOutApplication.settingsRepository.clearImmediateBlock()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sezione Fasce Orarie Programmate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fasce Orarie Settimanali",
                    style = MaterialTheme.typography.titleLarge.copy(color = TextWhite)
                )

                IconButton(
                    onClick = { showScheduleDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aggiungi Fascia",
                        tint = ElectricPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (schedules.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nessuna fascia oraria programmata",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                }
            }
        } else {
            items(schedules, key = { it.id }) { schedule ->
                ScheduleCard(
                    schedule = schedule,
                    onToggle = { isEnabled ->
                        scope.launch {
                            val updated = schedule.copy(isEnabled = isEnabled)
                            TapOutApplication.database.blockScheduleDao().updateSchedule(updated)
                        }
                    },
                    onDelete = {
                        scope.launch {
                            TapOutApplication.database.blockScheduleDao().deleteSchedule(schedule)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Sezione App da Bloccare
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Applicazioni Bloccate",
                        style = MaterialTheme.typography.titleLarge.copy(color = TextWhite),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (plan == SubscriptionPlan.PREMIUM) NeonMint.copy(alpha = 0.2f)
                                else ElectricPurple.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (plan == SubscriptionPlan.PREMIUM) "${blockedApps.size} (Illimitate)"
                            else "${blockedApps.size} / ${BillingManager.FREE_MAX_BLOCKED_APPS} Free",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (plan == SubscriptionPlan.PREMIUM) NeonMint else NeonPurpleBright,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (plan == SubscriptionPlan.FREE && blockedApps.size >= BillingManager.FREE_MAX_BLOCKED_APPS) {
                            showProUpgradeDialog = true
                        } else {
                            showAppSelectionDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricPurple,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aggiungi", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (blockedApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nessuna applicazione selezionata per il blocco",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                }
            }
        } else {
            items(blockedApps, key = { it.packageName }) { app ->
                BlockedAppRow(
                    app = app,
                    onToggle = { isBlocked ->
                        scope.launch {
                            TapOutApplication.database.blockedAppDao().updateBlockedStatus(app.packageName, isBlocked)
                        }
                    },
                    onDelete = {
                        scope.launch {
                            TapOutApplication.database.blockedAppDao().delete(app)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showAppSelectionDialog) {
        val isPremium = plan == SubscriptionPlan.PREMIUM
        AppSelectionDialog(
            alreadyBlocked = blockedApps.map { it.packageName }.toSet(),
            isPremium = isPremium,
            alreadyBlockedCount = blockedApps.size,
            onDismiss = { showAppSelectionDialog = false },
            onAppsSelected = { newApps ->
                scope.launch {
                    val allowedApps = if (isPremium) newApps else newApps.take((BillingManager.FREE_MAX_BLOCKED_APPS - blockedApps.size).coerceAtLeast(0))
                    if (allowedApps.isNotEmpty()) {
                        TapOutApplication.database.blockedAppDao().insertAll(allowedApps)
                    }
                    showAppSelectionDialog = false
                }
            }
        )
    }

    if (showScheduleDialog) {
        BlockScheduleDialog(
            onDismiss = { showScheduleDialog = false },
            onSave = { schedule ->
                scope.launch {
                    TapOutApplication.database.blockScheduleDao().insertSchedule(schedule)
                    showScheduleDialog = false
                }
            }
        )
    }

    if (showProUpgradeDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showProUpgradeDialog = false }) {
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
                        text = "Limite Piano Base (${BillingManager.FREE_MAX_BLOCKED_APPS}/${BillingManager.FREE_MAX_BLOCKED_APPS})",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Nel piano Base gratuito puoi bloccare fino a ${BillingManager.FREE_MAX_BLOCKED_APPS} applicazioni contemporaneamente.\n\nCon TapOut PRO sblocchi la protezione illimitata su tutte le tue app e le feature avanzate!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            TapOutApplication.billingManager.toggleSimulatedPremium(true)
                            showProUpgradeDialog = false
                            showAppSelectionDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonMint,
                            contentColor = CyberBlack
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Attiva TapOut PRO (Simula Subito ⭐)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { showProUpgradeDialog = false }) {
                        Text("Rimani su Piano Base", color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun ImmediateBlockCard(
    isImmediateActive: Boolean,
    untilTimestamp: Long,
    onStart: (Int) -> Unit,
    onStop: () -> Unit
) {
    val remainingMillis = untilTimestamp - System.currentTimeMillis()
    val remainingMinutes = if (remainingMillis > 0) (remainingMillis / 60000).toInt() + 1 else 0

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
                1.dp,
                if (isImmediateActive) ElectricPurple else SurfaceBorder,
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
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isImmediateActive) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = if (isImmediateActive) ElectricPurple else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Blocco Immediato (Focus)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = if (isImmediateActive) "Attivo per ancora $remainingMinutes min" else "Blocca subito le app per concentrarti",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isImmediateActive) NeonPurpleBright else TextMuted
                            ),
                            maxLines = 1
                        )
                    }
                }

                if (isImmediateActive) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onStop,
                        modifier = Modifier.height(38.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricPurple,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(19.dp)
                    ) {
                        Text(
                            text = "Sblocca",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            if (!isImmediateActive) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickBlockButton(label = "30 Min", onClick = { onStart(30) }, modifier = Modifier.weight(1f))
                    QuickBlockButton(label = "1 Ora", onClick = { onStart(60) }, modifier = Modifier.weight(1f))
                    QuickBlockButton(label = "2 Ore", onClick = { onStart(120) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun QuickBlockButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        )
    }
}

@Composable
fun ScheduleCard(
    schedule: BlockScheduleEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatted = String.format(
        Locale.getDefault(),
        "%02d:%02d - %02d:%02d",
        schedule.startHour,
        schedule.startMinute,
        schedule.endHour,
        schedule.endMinute
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(
                1.dp,
                if (schedule.isEnabled) ElectricPurple.copy(alpha = 0.4f) else SurfaceBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (schedule.isEnabled) TextWhite else TextMuted
                    )
                )
                if (schedule.label.isNotBlank()) {
                    Text(
                        text = schedule.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (schedule.isEnabled) NeonPurpleBright else TextMuted
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberBlack,
                        checkedTrackColor = ElectricPurple,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceElevated
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Elimina",
                        tint = TextMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BlockedAppRow(
    app: BlockedAppEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            AppIcon(
                packageName = app.packageName,
                appName = app.appName,
                size = 38.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    ),
                    maxLines = 1
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = app.isBlocked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyberBlack,
                    checkedTrackColor = ElectricPurple,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = SurfaceElevated
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Elimina",
                    tint = TextMuted.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
