package com.tapout.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.tapout.TapOutApplication
import com.tapout.data.local.entity.BlockScheduleEntity
import com.tapout.ui.screens.alarmactive.AlarmActivity
import com.tapout.ui.screens.blocker.BlockerOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class AppBlockerAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var blockedPackagesCache = setOf<String>()
    private var activeSchedulesCache = listOf<BlockScheduleEntity>()
    private var immediateBlockUntilTimestamp = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        info.notificationTimeout = 20
        serviceInfo = info

        // Sincronizza cache locale per verifiche ultra-rapide a latenza zero
        observeRules()
    }

    private fun observeRules() {
        scope.launch {
            TapOutApplication.database.blockedAppDao().getAllBlockedApps().collect { list ->
                blockedPackagesCache = list.filter { it.isBlocked }.map { it.packageName }.toSet()
            }
        }
        scope.launch {
            TapOutApplication.database.blockScheduleDao().getAllSchedules().collect { schedules ->
                activeSchedulesCache = schedules.filter { it.isEnabled }
            }
        }
        scope.launch {
            TapOutApplication.settingsRepository.immediateBlockUntil.collect { until ->
                immediateBlockUntilTimestamp = until
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // 1. SCUDO HARDCORE: Se la sveglia sta suonando, sigilla completamente il dispositivo
        if (AlarmService.isAlarmRinging) {
            if (packageName == applicationContext.packageName) {
                return
            }

            // Consenti chiamate di emergenza
            if (packageName.contains("dialer") || packageName.contains("telecom") || packageName.contains("emergency")) {
                return
            }

            // Se l'utente tenta di aprire la tendina notifiche
            if (packageName == "com.android.systemui") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
                }
                performGlobalAction(GLOBAL_ACTION_BACK)
            }

            // Riporta istantaneamente l'allarme in primo piano
            val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(alarmIntent)
            return
        }

        // 2. BLOCCO APP PROGRAMMATO / IMMEDIATO
        if (packageName == applicationContext.packageName) {
            return
        }

        // Verifica se l'app è nella blacklist
        if (blockedPackagesCache.contains(packageName)) {
            val isBlockedNow = isCurrentlyBlocked()
            if (isBlockedNow) {
                val overlayIntent = Intent(this, BlockerOverlayActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(BlockerOverlayActivity.EXTRA_BLOCKED_PACKAGE, packageName)
                }
                startActivity(overlayIntent)
            }
        }
    }

    private fun isCurrentlyBlocked(): Boolean {
        val now = System.currentTimeMillis()
        if (now < immediateBlockUntilTimestamp) {
            return true
        }

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentDay = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        for (schedule in activeSchedulesCache) {
            val activeDays = schedule.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
            if (activeDays.contains(currentDay)) {
                val startMinutes = schedule.startHour * 60 + schedule.startMinute
                val endMinutes = schedule.endHour * 60 + schedule.endMinute
                val currentMinutes = currentHour * 60 + currentMinute

                if (startMinutes <= endMinutes) {
                    if (currentMinutes in startMinutes..endMinutes) return true
                } else {
                    // Fascia a cavallo della mezzanotte (es. 23:00 - 07:00)
                    if (currentMinutes >= startMinutes || currentMinutes <= endMinutes) return true
                }
            }
        }

        return false
    }

    override fun onInterrupt() {}
}
