package com.tapout.ui.screens.blocker

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tapout.data.local.entity.BlockScheduleEntity
import com.tapout.ui.screens.dashboard.TimePod
import com.tapout.ui.theme.CyberBlack
import com.tapout.ui.theme.ElectricPurple
import com.tapout.ui.theme.SurfaceBorder
import com.tapout.ui.theme.SurfaceDark
import com.tapout.ui.theme.SurfaceElevated
import com.tapout.ui.theme.TextMuted
import com.tapout.ui.theme.TextWhite

@Composable
fun BlockScheduleDialog(
    onDismiss: () -> Unit,
    onSave: (BlockScheduleEntity) -> Unit
) {
    var startHour by remember { mutableIntStateOf(23) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(7) }
    var endMinute by remember { mutableIntStateOf(30) }

    fun computeSmartScheduleLabel(startH: Int, endH: Int): String {
        val isMorning = startH in 5..12 && endH in 6..13 && endH > startH
        val isAfternoon = startH in 13..20 && endH in 14..21 && endH > startH
        val isNight = (startH >= 21 || startH < 5) && (endH in 1..7 || endH < startH)

        return when {
            isMorning -> "Focus Mattutino"
            isAfternoon -> "Focus Pomeridiano"
            isNight -> "Focus Notturno"
            else -> "" // Mischiato: non scrivere niente
        }
    }

    var isManuallyEdited by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf(computeSmartScheduleLabel(23, 7)) }
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) }

    fun updateAutoLabel(newStartH: Int, newEndH: Int) {
        if (!isManuallyEdited) {
            label = computeSmartScheduleLabel(newStartH, newEndH)
        }
    }

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
                    text = "Nuova Fascia Oraria",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = {
                        label = it
                        isManuallyEdited = true
                    },
                    label = { Text("Nome fascia (opzionale)") },
                    placeholder = { Text("es. Focus Mattutino", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Orario di Inizio",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimePod(
                        value = startHour,
                        onIncrement = {
                            startHour = (startHour + 1) % 24
                            updateAutoLabel(startHour, endHour)
                        },
                        onDecrement = {
                            startHour = if (startHour - 1 < 0) 23 else startHour - 1
                            updateAutoLabel(startHour, endHour)
                        }
                    )
                    Text(":", style = MaterialTheme.typography.headlineMedium.copy(color = ElectricPurple), modifier = Modifier.padding(horizontal = 8.dp))
                    TimePod(value = startMinute, onIncrement = { startMinute = (startMinute + 5) % 60 }, onDecrement = { startMinute = if (startMinute - 5 < 0) 55 else startMinute - 5 })
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Orario di Fine",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimePod(
                        value = endHour,
                        onIncrement = {
                            endHour = (endHour + 1) % 24
                            updateAutoLabel(startHour, endHour)
                        },
                        onDecrement = {
                            endHour = if (endHour - 1 < 0) 23 else endHour - 1
                            updateAutoLabel(startHour, endHour)
                        }
                    )
                    Text(":", style = MaterialTheme.typography.headlineMedium.copy(color = ElectricPurple), modifier = Modifier.padding(horizontal = 8.dp))
                    TimePod(value = endMinute, onIncrement = { endMinute = (endMinute + 5) % 60 }, onDecrement = { endMinute = if (endMinute - 5 < 0) 55 else endMinute - 5 })
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Giorni
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf(1 to "L", 2 to "M", 3 to "M", 4 to "G", 5 to "V", 6 to "S", 7 to "D")
                    for ((dNum, dLabel) in days) {
                        val isSelected = selectedDays.contains(dNum)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ElectricPurple else SurfaceElevated)
                                .border(1.dp, if (isSelected) ElectricPurple else SurfaceBorder, CircleShape)
                                .clickable {
                                    selectedDays = if (isSelected) selectedDays - dNum else selectedDays + dNum
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TextWhite else TextMuted
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                            val daysString = selectedDays.sorted().joinToString(",")
                            onSave(
                                BlockScheduleEntity(
                                    startHour = startHour,
                                    startMinute = startMinute,
                                    endHour = endHour,
                                    endMinute = endMinute,
                                    daysOfWeek = daysString,
                                    label = label.trim(),
                                    isEnabled = true
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricPurple,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Salva Fascia", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
