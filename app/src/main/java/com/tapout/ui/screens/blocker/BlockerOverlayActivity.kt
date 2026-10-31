package com.tapout.ui.screens.blocker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.tapout.nfc.NfcHelper
import com.tapout.ui.screens.dance.DanceMissionScreen
import com.tapout.ui.theme.CyberBlack
import com.tapout.ui.theme.ElectricPurple
import com.tapout.ui.theme.NeonPink
import com.tapout.ui.theme.NeonPurpleBright
import com.tapout.ui.theme.SurfaceBorder
import com.tapout.ui.theme.SurfaceDark
import com.tapout.ui.theme.SurfaceElevated
import com.tapout.ui.theme.TapOutTheme
import com.tapout.ui.theme.TextMuted
import com.tapout.ui.theme.TextWhite
import com.tapout.vision.DanceDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.withContext

class BlockerOverlayActivity : ComponentActivity() {

    private lateinit var nfcHelper: NfcHelper
    private var registeredUids: List<String> = emptyList()
    private var isTestMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcHelper = NfcHelper(this)

        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: "App Bloccata"

        lifecycleScope.launch {
            TapOutApplication.settingsRepository.allNfcTags.collect { tags ->
                registeredUids = tags.map { it.uid }
            }
        }

        lifecycleScope.launch {
            TapOutApplication.settingsRepository.isTestMode.collect { mode ->
                isTestMode = mode
            }
        }

        setContent {
            TapOutTheme {
                var isDanceMissionActive by remember { mutableStateOf(false) }

                BackHandler {
                    // Tasto Indietro reindirizza alla Home di Android per impedire il bypass
                    goToHomeScreen()
                }

                if (isDanceMissionActive) {
                    DanceMissionScreen(
                        targetSeconds = DanceDetector.DANCE_REQUIRED_SECONDS,
                        onMissionCompleted = {
                            unlockAndExit()
                        },
                        onCancel = {
                            isDanceMissionActive = false
                        }
                    )
                } else {
                    BlockerOverlayContent(
                        blockedPackage = blockedPackage,
                        isTestMode = isTestMode,
                        onSimulateNfcTap = {
                            unlockAndExit()
                        },
                        onStartDanceMission = {
                            isDanceMissionActive = true
                        },
                        onGoHome = {
                            goToHomeScreen()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcHelper.enableReaderMode(this) { scannedUid ->
            processScannedUid(scannedUid)
        }
        nfcHelper.enableForegroundDispatch(this)
    }

    override fun onPause() {
        super.onPause()
        nfcHelper.disableReaderMode(this)
        nfcHelper.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val uid = NfcHelper.extractUidFromIntent(intent)
        if (uid != null) {
            processScannedUid(uid)
        }
    }

    private fun processScannedUid(scannedUid: String) {
        lifecycleScope.launch {
            val uids = if (registeredUids.isEmpty()) {
                withContext(Dispatchers.IO) {
                    TapOutApplication.settingsRepository.getAllRegisteredUids()
                }
            } else {
                registeredUids
            }
            val isValid = NfcHelper.isTagValid(scannedUid, uids, isTestMode)
            NfcHelper.vibrateFeedback(this@BlockerOverlayActivity, isSuccess = isValid)
            if (isValid) {
                unlockAndExit()
            }
        }
    }

    private fun unlockAndExit() {
        // Disattiva il blocco immediato se attivo
        CoroutineScope(Dispatchers.IO).launch {
            TapOutApplication.settingsRepository.clearImmediateBlock()
        }
        finishAndRemoveTask()
    }

    private fun goToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
    }
}

@Composable
fun BlockerOverlayContent(
    blockedPackage: String,
    isTestMode: Boolean,
    onSimulateNfcTap: () -> Unit,
    onStartDanceMission: () -> Unit,
    onGoHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(ElectricPurple.copy(alpha = 0.3f), NeonPink.copy(alpha = 0.2f))
                        )
                    )
                    .border(2.dp, ElectricPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = ElectricPurple,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "APPLICAZIONE BLOCCATA",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Questa app ($blockedPackage) è inclusa nella tua fascia di protezione TapOut.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Card Sblocco NFC
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = null,
                        tint = NeonPurpleBright,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Avvicina il Tag NFC per Sbloccare",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                    Text(
                        text = "Scansiona il tag fisico autorizzato sul retro",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )

                    if (isTestMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onSimulateNfcTap,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceElevated,
                                contentColor = NeonPurpleBright
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Simula Tap NFC (Modalità Test)", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pulsante Dance Mission alternativa (30s)
            Button(
                onClick = onStartDanceMission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPink,
                    contentColor = CyberBlack
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Balla 30 Secondi per Sbloccare",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = TextMuted)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Torna alla Home", color = TextMuted)
            }
        }
    }
}
