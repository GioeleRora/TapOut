package com.tapout.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapout.TapOutApplication
import com.tapout.ui.screens.blocker.AppBlockerScreen
import com.tapout.ui.screens.dashboard.AlarmDashboardScreen
import com.tapout.ui.screens.permissions.PermissionsWizardScreen
import com.tapout.ui.screens.settings.SettingsScreen
import com.tapout.ui.theme.CyberBlack
import com.tapout.ui.theme.ElectricPurple
import com.tapout.ui.theme.NeonPink
import com.tapout.ui.theme.SurfaceBorder
import com.tapout.ui.theme.SurfaceDark
import com.tapout.ui.theme.TapOutTheme
import com.tapout.ui.theme.TextMuted
import com.tapout.ui.theme.TextWhite

enum class MainTab(val title: String, val icon: ImageVector) {
    ALARMS("Sveglie", Icons.Default.NotificationsActive),
    BLOCKER("Blocco App", Icons.Default.Shield),
    SETTINGS("Impostazioni", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TapOutTheme {
                val isOnboardingCompleted by TapOutApplication.settingsRepository.isOnboardingCompleted
                    .collectAsState(initial = false)

                var showingWizardManually by remember { mutableStateOf(false) }

                if (!isOnboardingCompleted || showingWizardManually) {
                    PermissionsWizardScreen(
                        onContinue = {
                            showingWizardManually = false
                        }
                    )
                } else {
                    MainAppScaffold(
                        onOpenPermissionsWizard = {
                            showingWizardManually = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    onOpenPermissionsWizard: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.ALARMS) }

    Scaffold(
        containerColor = CyberBlack,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
            ) {
                NavigationBar(
                    containerColor = SurfaceDark,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    MainTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        val accentColor = when (tab) {
                            MainTab.ALARMS -> NeonPink
                            MainTab.BLOCKER -> ElectricPurple
                            MainTab.SETTINGS -> TextWhite
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyberBlack,
                                selectedTextColor = accentColor,
                                indicatorColor = accentColor,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                MainTab.ALARMS -> AlarmDashboardScreen()
                MainTab.BLOCKER -> AppBlockerScreen()
                MainTab.SETTINGS -> SettingsScreen(onOpenPermissionsWizard = onOpenPermissionsWizard)
            }
        }
    }
}
