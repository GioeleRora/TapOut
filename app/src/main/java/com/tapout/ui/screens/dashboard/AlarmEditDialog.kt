package com.tapout.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tapout.data.local.entity.AlarmEntity
import com.tapout.ui.theme.CyberBlack
import com.tapout.ui.theme.ElectricPurple
import com.tapout.ui.theme.NeonPink
import com.tapout.ui.theme.NeonPinkBright
import com.tapout.ui.theme.SurfaceBorder
import com.tapout.ui.theme.SurfaceDark
import com.tapout.ui.theme.SurfaceElevated
import com.tapout.ui.theme.TextMuted
import com.tapout.ui.theme.TextWhite
import java.util.Locale

@Composable
fun AlarmEditDialog(
    alarmToEdit: AlarmEntity?,
    onDismiss: () -> Unit,
    onSave: (AlarmEntity) -> Unit
) {
    var hour by remember { mutableIntStateOf(alarmToEdit?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(alarmToEdit?.minute ?: 0) }
    var label by remember {
        mutableStateOf(
            if (alarmToEdit?.label == "Sveglia TapOut" || alarmToEdit?.label == "Sveglia") ""
            else alarmToEdit?.label ?: ""
        )
    }
    var requireNfc by remember { mutableStateOf(alarmToEdit?.requireNfc ?: true) }
    var requireDance by remember { mutableStateOf(alarmToEdit?.requireDance ?: false) }
    var requireSmileKiss by remember { mutableStateOf(alarmToEdit?.requireSmileKiss ?: false) }

    val initialDays = remember {
        alarmToEdit?.daysOfWeek?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.toSet() ?: setOf(1, 2, 3, 4, 5)
    }
    var selectedDays by remember { mutableStateOf(initialDays) }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (alarmToEdit == null) "Nuova Sveglia" else "Modifica Sveglia",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Time Picker a due pod (Ore : Minuti)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimePod(
                        value = hour,
                        onIncrement = { hour = (hour + 1) % 24 },
                        onDecrement = { hour = if (hour - 1 < 0) 23 else hour - 1 }
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonPink
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    TimePod(
                        value = minute,
                        onIncrement = { minute = (minute + 1) % 60 },
                        onDecrement = { minute = if (minute - 1 < 0) 59 else minute - 1 }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Selettore Giorni Settimana
                Text(
                    text = "Ripeti nei giorni",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextMuted),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf(1 to "L", 2 to "M", 3 to "M", 4 to "G", 5 to "V", 6 to "S", 7 to "D")
                    for ((dayNum, dayLabel) in days) {
                        val isSelected = selectedDays.contains(dayNum)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonPink else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonPink else SurfaceBorder,
                                    CircleShape
                                )
                                .clickable {
                                    selectedDays = if (isSelected) {
                                        selectedDays - dayNum
                                    } else {
                                        selectedDays + dayNum
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CyberBlack else TextWhite
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Etichetta
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Nome sveglia") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedLabelColor = NeonPink,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Intestazione Sezione Metodi di Spegnimento
                Text(
                    text = "METODI PER SPEGNERE LA SVEGLIA",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = NeonPinkBright,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Seleziona uno o più metodi validi per disattivare l'allarme:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Toggles per Metodi di Spegnimento Sveglia
                MissionSwitchRow(
                    icon = Icons.Default.Nfc,
                    iconTint = ElectricPurple,
                    title = "Tag NFC Fisico",
                    subtitle = "Spegni toccando il tag NFC registrato",
                    checked = requireNfc,
                    onCheckedChange = { requireNfc = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                MissionSwitchRow(
                    icon = Icons.Default.DirectionsRun,
                    iconTint = NeonPink,
                    title = "Dance Mission (30s)",
                    subtitle = "Balla 30 secondi davanti alla fotocamera",
                    checked = requireDance,
                    onCheckedChange = { requireDance = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                MissionSwitchRow(
                    icon = Icons.Default.SentimentVerySatisfied,
                    iconTint = ElectricPurple,
                    title = "Sorrisi & Baci (5+5)",
                    subtitle = "Fai 5 sorrisi e manda 5 baci alla fotocamera",
                    checked = requireSmileKiss,
                    onCheckedChange = { requireSmileKiss = it }
                )

                val hasAtLeastOneMethod = requireNfc || requireDance || requireSmileKiss

                if (!hasAtLeastOneMethod) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "⚠️ Seleziona almeno un metodo per spegnere la sveglia!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = androidx.compose.ui.graphics.Color(0xFFFF5252),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pulsanti Annulla / Salva
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annulla", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (!hasAtLeastOneMethod) return@Button
                            val daysString = selectedDays.sorted().joinToString(",")
                            val result = AlarmEntity(
                                id = alarmToEdit?.id ?: 0L,
                                hour = hour,
                                minute = minute,
                                label = label.trim(),
                                daysOfWeek = daysString,
                                isEnabled = true,
                                vibrate = true, // Sempre attiva e garantita
                                soundUri = alarmToEdit?.soundUri ?: "",
                                requireNfc = requireNfc,
                                requireDance = requireDance,
                                requireSmileKiss = requireSmileKiss
                            )
                            onSave(result)
                        },
                        enabled = hasAtLeastOneMethod,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPink,
                            contentColor = CyberBlack,
                            disabledContainerColor = SurfaceElevated,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Salva Sveglia", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TimePod(
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onIncrement, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Aumenta",
                tint = NeonPink
            )
        }
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceElevated)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format(Locale.getDefault(), "%02d", value),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 36.sp
                )
            )
        }
        IconButton(onClick = onDecrement, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Diminuisci",
                tint = NeonPink
            )
        }
    }
}

@Composable
fun MissionSwitchRow(
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
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceElevated)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
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
                uncheckedTrackColor = SurfaceDark
            )
        )
    }
}
