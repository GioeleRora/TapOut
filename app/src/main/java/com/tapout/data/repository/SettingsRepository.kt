package com.tapout.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.tapoutDataStore: DataStore<Preferences> by preferencesDataStore(name = "tapout_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val REGISTERED_NFC_UID = stringPreferencesKey("registered_nfc_uid")
        val NFC_TAG_NAME = stringPreferencesKey("nfc_tag_name")
        val IS_TEST_MODE = booleanPreferencesKey("is_test_mode")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val IS_PREMIUM_SIMULATION_ACTIVE = booleanPreferencesKey("is_premium_simulation_active")
        val IMMEDIATE_BLOCK_UNTIL_TIMESTAMP = longPreferencesKey("immediate_block_until_timestamp")
    }

    val registeredNfcUid: Flow<String?> = context.tapoutDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.REGISTERED_NFC_UID]
        }

    val nfcTagName: Flow<String> = context.tapoutDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.NFC_TAG_NAME] ?: "Tag Sveglia Principale"
        }

    val isTestMode: Flow<Boolean> = context.tapoutDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_TEST_MODE] ?: true // Default attivo per test immediato
        }

    val isOnboardingCompleted: Flow<Boolean> = context.tapoutDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] ?: false
        }

    val isPremiumSimulationActive: Flow<Boolean> = context.tapoutDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_PREMIUM_SIMULATION_ACTIVE] ?: false
        }

    val immediateBlockUntil: Flow<Long> = context.tapoutDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.IMMEDIATE_BLOCK_UNTIL_TIMESTAMP] ?: 0L
        }

    suspend fun saveNfcTag(uid: String, name: String) {
        context.tapoutDataStore.edit { preferences ->
            preferences[PreferencesKeys.REGISTERED_NFC_UID] = uid
            preferences[PreferencesKeys.NFC_TAG_NAME] = name
        }
    }

    suspend fun clearNfcTag() {
        context.tapoutDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.REGISTERED_NFC_UID)
            preferences.remove(PreferencesKeys.NFC_TAG_NAME)
        }
    }

    suspend fun setTestMode(enabled: Boolean) {
        context.tapoutDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_TEST_MODE] = enabled
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.tapoutDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setPremiumSimulation(active: Boolean) {
        context.tapoutDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PREMIUM_SIMULATION_ACTIVE] = active
        }
    }

    suspend fun setImmediateBlock(untilTimestamp: Long) {
        context.tapoutDataStore.edit { preferences ->
            preferences[PreferencesKeys.IMMEDIATE_BLOCK_UNTIL_TIMESTAMP] = untilTimestamp
        }
    }

    suspend fun clearImmediateBlock() {
        context.tapoutDataStore.edit { preferences ->
            preferences[PreferencesKeys.IMMEDIATE_BLOCK_UNTIL_TIMESTAMP] = 0L
        }
    }
}
