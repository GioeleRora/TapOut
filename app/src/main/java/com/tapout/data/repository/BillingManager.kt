package com.tapout.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SubscriptionPlan {
    FREE,
    PREMIUM
}

class BillingManager(private val settingsRepository: SettingsRepository) {

    private val scope = CoroutineScope(Dispatchers.IO)

    val currentPlan: StateFlow<SubscriptionPlan> = settingsRepository.isPremiumSimulationActive
        .map { isSimulationActive ->
            if (isSimulationActive) SubscriptionPlan.PREMIUM else SubscriptionPlan.FREE
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = SubscriptionPlan.FREE
        )

    fun isPremium(): Boolean {
        return currentPlan.value == SubscriptionPlan.PREMIUM
    }

    fun canBlockMoreApps(currentBlockedCount: Int): Boolean {
        return isPremium() || currentBlockedCount < FREE_MAX_BLOCKED_APPS
    }

    fun canUseMultipleNfcTags(): Boolean {
        return isPremium()
    }

    fun canUseCustomThemes(): Boolean {
        return isPremium()
    }

    fun toggleSimulatedPremium(enable: Boolean) {
        scope.launch {
            settingsRepository.setPremiumSimulation(enable)
        }
    }

    companion object {
        const val FREE_MAX_BLOCKED_APPS = 2
    }
}
