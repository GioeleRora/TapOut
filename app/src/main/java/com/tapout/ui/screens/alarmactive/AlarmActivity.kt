package com.tapout.ui.screens.alarmactive

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
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
import androidx.lifecycle.lifecycleScope
import com.tapout.TapOutApplication
import com.tapout.alarm.AlarmScheduler
import com.tapout.nfc.NfcHelper
import com.tapout.service.AlarmService
import com.tapout.ui.screens.dance.DanceMissionScreen
import com.tapout.ui.screens.face.SmileKissMissionScreen
import com.tapout.ui.theme.TapOutTheme
import com.tapout.vision.DanceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AlarmRingState {
    RINGING,
    DANCE_MISSION,
    SMILE_KISS_MISSION,
    DISMISSED
}

class AlarmActivity : ComponentActivity() {

    private lateinit var nfcHelper: NfcHelper
    private var wakeLock: PowerManager.WakeLock? = null
    private var registeredUids: List<String> = emptyList()
    private var registeredTagNames: List<String> = emptyList()
    private var isTestMode by mutableStateOf(true)
    private var currentState by mutableStateOf(AlarmRingState.RINGING)
    private var scanFeedbackMessage by mutableStateOf<String?>(null)
    private var isScanError by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLockscreenFlags()
        hideSystemBars()

        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "TapOut:AlarmActivityWakeLock"
            )
            wakeLock?.acquire(10 * 60 * 1000L) // 10 minuti di guardia
        } catch (_: Exception) {}

        nfcHelper = NfcHelper(this)

        val alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Sveglia TapOut"
        val requireNfc = intent.getBooleanExtra(AlarmScheduler.EXTRA_REQUIRE_NFC, true)
        val requireDance = intent.getBooleanExtra(AlarmScheduler.EXTRA_REQUIRE_DANCE, true)
        val requireSmileKiss = intent.getBooleanExtra(AlarmScheduler.EXTRA_REQUIRE_SMILE_KISS, false)

        lifecycleScope.launch {
            TapOutApplication.settingsRepository.allNfcTags.collect { tags ->
                registeredUids = tags.map { it.uid }
                registeredTagNames = tags.map { it.name }
            }
        }

        lifecycleScope.launch {
            TapOutApplication.settingsRepository.isTestMode.collect { mode ->
                isTestMode = mode
            }
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
                            registeredTagNames = registeredTagNames,
                            scanFeedbackMessage = scanFeedbackMessage,
                            isScanError = isScanError,
                            onOpenNfcPanel = {
                                nfcHelper.tryEnableNfcOrOpenPrompt(this@AlarmActivity)
                            },
                            onSimulateNfcTap = {
                                stopAlarmAndFinish()
                            },
                            onStartDanceMission = {
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
                                val stopIntent = Intent(this@AlarmActivity, AlarmService::class.java).apply {
                                    action = AlarmService.ACTION_STOP_ALARM
                                }
                                startService(stopIntent)
                                currentState = AlarmRingState.DISMISSED
                                window.decorView.postDelayed({
                                    if (!isFinishing && !isDestroyed) {
                                        finishAndRemoveTask()
                                    }
                                }, 2000)
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

                    AlarmRingState.SMILE_KISS_MISSION -> {
                        SmileKissMissionScreen(
                            onMissionCompleted = {
                                val stopIntent = Intent(this@AlarmActivity, AlarmService::class.java).apply {
                                    action = AlarmService.ACTION_STOP_ALARM
                                }
                                startService(stopIntent)
                                currentState = AlarmRingState.DISMISSED
                                window.decorView.postDelayed({
                                    if (!isFinishing && !isDestroyed) {
                                        finishAndRemoveTask()
                                    }
                                }, 2000)
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

                    AlarmRingState.DISMISSED -> {
                        AlarmDismissedScreen(
                            onClose = {
                                finishAndRemoveTask()
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
        if (requireNfc && nfcHelper.isNfcSupported) {
            if (!nfcHelper.isNfcEnabled) {
                nfcHelper.tryEnableNfcOrOpenPrompt(this)
            }
            nfcHelper.enableReaderMode(this) { scannedUid ->
                processScannedUid(scannedUid)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        nfcHelper.disableReaderMode(this)
    }

    override fun onDestroy() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        super.onDestroy()
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
        if (currentState == AlarmRingState.DISMISSED) {
            // Già disattivata: assorbi silenziosamente i rimbalzi del chip NFC mentre l'utente allontana il tag
            return
        }

        lifecycleScope.launch {
            val uids = if (registeredUids.isEmpty()) {
                withContext(Dispatchers.IO) {
                    TapOutApplication.settingsRepository.getAllRegisteredUids()
                }
            } else {
                registeredUids
            }

            val isValid = NfcHelper.isTagValid(scannedUid, uids, isTestMode)

            NfcHelper.vibrateFeedback(this@AlarmActivity, isSuccess = isValid)

            if (isValid) {
                isScanError = false
                scanFeedbackMessage = "✅ Tag Riconosciuto! Sveglia Spenta"
                currentState = AlarmRingState.DISMISSED

                // Ferma subito suono e vibrazione inarrestabile
                val stopIntent = Intent(this@AlarmActivity, AlarmService::class.java).apply {
                    action = AlarmService.ACTION_STOP_ALARM
                }
                startService(stopIntent)

                // IMPORTANTE: NON chiudere l'activity dopo soli 400ms!
                // Mantenendo l'activity a schermo e il ReaderMode attivo per 2.5 secondi,
                // assorbiamo le letture ripetute mentre l'utente allontana la mano.
                // Questo impedisce categoricamente al sistema operativo Android di aprire "TagViewer (Empty tag)"!
                window.decorView.postDelayed({
                    if (!isFinishing && !isDestroyed) {
                        finishAndRemoveTask()
                    }
                }, 2500)
            } else {
                isScanError = true
                scanFeedbackMessage = "⚠️ Tag non valido ($scannedUid). Usa uno dei tag registrati!"
                window.decorView.postDelayed({
                    scanFeedbackMessage = null
                }, 4000)
            }
        }
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
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
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
