package com.tapout.ui.screens.blocker

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.tapout.data.repository.BillingManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tapout.data.local.entity.BlockedAppEntity
import com.tapout.ui.components.AppIcon
import com.tapout.ui.theme.ElectricPurple
import com.tapout.ui.theme.SurfaceBorder
import com.tapout.ui.theme.SurfaceDark
import com.tapout.ui.theme.SurfaceElevated
import com.tapout.ui.theme.TextMuted
import com.tapout.ui.theme.TextWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String
)

@Composable
fun AppSelectionDialog(
    alreadyBlocked: Set<String>,
    isPremium: Boolean = false,
    alreadyBlockedCount: Int = 0,
    onDismiss: () -> Unit,
    onAppsSelected: (List<BlockedAppEntity>) -> Unit
) {
    val context = LocalContext.current
    val installedApps = remember { mutableStateListOf<InstalledAppItem>() }
    val selectedPackages = remember { mutableStateListOf<String>() }
    var searchQuery by remember { mutableStateOf("") }
    var showLimitReachedNotice by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolved = pm.queryIntentActivities(mainIntent, 0)
            val list = resolved.mapNotNull { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                if (pkg != context.packageName && !alreadyBlocked.contains(pkg)) {
                    val name = resolveInfo.loadLabel(pm).toString()
                    InstalledAppItem(pkg, name)
                } else null
            }.distinctBy { it.packageName }.sortedBy { it.appName }

            withContext(Dispatchers.Main) {
                installedApps.clear()
                installedApps.addAll(list)
            }
        }
    }

    val filteredApps = installedApps.filter {
        it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Seleziona App da Bloccare",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )

                Text(
                    text = if (isPremium) "👑 Piano PRO: Selezione illimitata attiva"
                    else "Piano Base: ${selectedPackages.size + alreadyBlockedCount}/${BillingManager.FREE_MAX_BLOCKED_APPS} app totali consentite",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isPremium) com.tapout.ui.theme.NeonMint else com.tapout.ui.theme.NeonPurpleBright,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (showLimitReachedNotice && !isPremium) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ Limite Base raggiunto (max ${BillingManager.FREE_MAX_BLOCKED_APPS} app)! Passa a PRO per bloccare app illimitate.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = androidx.compose.ui.graphics.Color(0xFFFF5252),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cerca app per nome...", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val isSelected = selectedPackages.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricPurple.copy(alpha = 0.2f) else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricPurple else SurfaceBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (isSelected) {
                                        selectedPackages.remove(app.packageName)
                                        showLimitReachedNotice = false
                                    } else {
                                        val totalSelected = selectedPackages.size + alreadyBlockedCount
                                        if (!isPremium && totalSelected >= BillingManager.FREE_MAX_BLOCKED_APPS) {
                                            showLimitReachedNotice = true
                                        } else {
                                            selectedPackages.add(app.packageName)
                                            showLimitReachedNotice = false
                                        }
                                    }
                                }
                                .padding(12.dp),
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
                                            fontWeight = FontWeight.Medium,
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

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(ElectricPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = TextWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            val selectedEntities = installedApps
                                .filter { selectedPackages.contains(it.packageName) }
                                .map {
                                    BlockedAppEntity(
                                        packageName = it.packageName,
                                        appName = it.appName,
                                        isBlocked = true
                                    )
                                }
                            onAppsSelected(selectedEntities)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricPurple,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = selectedPackages.isNotEmpty()
                    ) {
                        Text("Blocca (${selectedPackages.size})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
