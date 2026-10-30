package com.tapout.ui.screens.alarmactive

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tapout.TapOutApplication
import com.tapout.alarm.AlarmScheduler
import com.tapout.nfc.NfcHelper
import com.tapout.service.AlarmService
import com.tapout.ui.screens.dance.DanceMissionScreen
import com.tapout.ui.screens.face.SmileKissMissionScreen
import com.tapout.ui.theme.TapOutTheme
import com.tapout.vision.DanceDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AlarmRingState {
    RINGING,
    DANCE_MISSION,
    SMILE_KISS_MISSION
}

class AlarmActivity : ComponentActivity() {

    private lateinit var nfcHelper: NfcHelper
    private var registeredUid: String? = null
    private var isTestMode = true
    private var currentState by mutableStateOf(AlarmRingState.RINGING)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLockscreenFlags()
        hideSystemBars()

        nfcHelper = NfcHelper(this)

        val alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Sveglia TapOut"
        val requireNfc = intent.getBooleanExtra(AlarmScheduler.EXTRA_REQUIRE_NFC, true)
        val requireDance = intent.getBooleanExtra(AlarmScheduler.EXTRA_REQUIRE_DANCE, true)
        val requireSmileKiss = intent.getBooleanExtra(AlarmScheduler.EXTRA_REQUIRE_SMILE_KISS, false)

        CoroutineScope(Dispatchers.IO).launch {
            registeredUid = TapOutApplication.settingsRepository.registeredNfcUid.first()
            isTestMode = TapOutApplication.settingsRepository.isTestMode.first()
        }

        setContent {
            TapOutTheme {
                BackHandler(enabled = true) {
                    // Tasto Indietro categoricamente disabilitato
                }

                when (currentState) {
                    AlarmRingState.RINGING -> {
                        AlarmActiveScreen(
                            alarmLabel = alarmLabel,
                            requireNfc = requireNfc,
                            requireDance = requireDance,
                            requireSmileKiss = requireSmileKiss,
                            isTestMode = isTestMode,
                            isNfcHardwareEnabled = nfcHelper.isNfcEnabled,
                            onOpenNfcPanel = {
                                nfcHelper.tryEnableNfcOrOpenPrompt(this@AlarmActivity)
                            },
                            onSimulateNfcTap = {
                                stopAlarmAndFinish()
                            },
                            onStartDanceMission = {
                                // Attenua il volume della sveglia mentre l'utente balla
                                val duckIntent = Intent(this@AlarmActivity, AlarmService::class.java).apply {
                                    action = AlarmService.ACTION_DUCK_AUDIO
                                }
                                startService(duckIntent)
                                currentState = AlarmRingState.DANCE_MISSION
                            },
                            onStartSmileKissMission = {
                                val duckIntent = Intent(this@AlarmActivity, AlarmService::class.java).apply {
                                    action = AlarmService.ACTION_DUCK_AUDIO
                                }
                                startService(duckIntent)
                                currentState = AlarmRingState.SMILE_KISS_MISSION
                            }
                        )
                    }

                    AlarmRingState.DANCE_MISSION -> {
                        DanceMissionScreen(
                            targetSeconds = DanceDetector.DANCE_REQUIRED_SECONDS,
                            onMissionCompleted = {
                                stopAlarmAndFinish()
                            },
                            onCancel = {
                                // Ripristina volume al 100%
                                val unduckIntent = Intent(this@AlarmActivity, AlarmService::class.java).apply {
                                    action = AlarmService.ACTION_UNDUCK_AUDIO
                                }
                                startService(unduckIntent)
                                currentState = AlarmRingState.RINGING
                            }
                        )
                    }

                    AlarmRingState.SMILE_KISS_MISSION -> {
                        SmileKissMissionScreen(
                            onMissionCompleted = {
                                stopAlarmAndFinish()
                            },
                            onCancel = {
                                val unduckIntent = Intent(this@AlarmActivity, AlarmService::class.java).apply {
                                    action = AlarmService.ACTION_UNDUCK_AUDIO
                                }
                                startService(unduckIntent)
                                currentState = AlarmRingState.RINGING
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        val requireNfc = intent.getBooleanExtra(AlarmScheduler.EXTRA_REQUIRE_NFC, true)
        if (requireNfc) {
            if (!nfcHelper.isNfcEnabled) {
                nfcHelper.tryEnableNfcOrOpenPrompt(this)
            }
            nfcHelper.enableReaderMode(this) { scannedUid ->
                if (NfcHelper.isTagValid(scannedUid, registeredUid, isTestMode)) {
                    stopAlarmAndFinish()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        nfcHelper.disableReaderMode(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            collapseStatusBarPanels()
        } else {
            hideSystemBars()
        }
    }

    private fun collapseStatusBarPanels() {
        try {
            @Suppress("WrongConstant")
            val statusBarService = getSystemService("statusbar")
            val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
            val collapseMethod = statusBarManagerClass.getMethod("collapsePanels")
            collapseMethod.invoke(statusBarService)
        } catch (_: Exception) {}
    }

    private fun setupLockscreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun hideSystemBars() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun stopAlarmAndFinish() {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP_ALARM
        }
        startService(stopIntent)
        finishAndRemoveTask()
    }
}
