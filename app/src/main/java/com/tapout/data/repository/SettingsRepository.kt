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
import com.tapout.TapOutApplication
import com.tapout.data.local.entity.NfcTagEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Migrazione trasparente di eventuali tag esistenti nel DataStore verso Room
        scope.launch {
            try {
                val db = TapOutApplication.database
                val count = db.nfcTagDao().getTagCount()
                if (count == 0) {
                    val prefs = context.tapoutDataStore.data.first()
                    val oldUid = prefs[PreferencesKeys.REGISTERED_NFC_UID]
                    val oldName = prefs[PreferencesKeys.NFC_TAG_NAME] ?: "Tag Sveglia Principale"
                    if (!oldUid.isNullOrBlank()) {
                        db.nfcTagDao().insertTag(
                            NfcTagEntity(uid = oldUid, name = oldName)
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    val allNfcTags: Flow<List<NfcTagEntity>> = TapOutApplication.database.nfcTagDao().getAllTags()

    val registeredNfcUid: Flow<String?> = allNfcTags.map { tags ->
        tags.firstOrNull()?.uid
    }

    val nfcTagName: Flow<String> = allNfcTags.map { tags ->
        tags.firstOrNull()?.name ?: "Tag Sveglia Principale"
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
        val existing = TapOutApplication.database.nfcTagDao().getTagByUid(uid)
        if (existing != null) {
            TapOutApplication.database.nfcTagDao().insertTag(
                existing.copy(name = name)
            )
        } else {
            TapOutApplication.database.nfcTagDao().insertTag(
                NfcTagEntity(uid = uid, name = name)
            )
        }
        // Aggiorna anche DataStore per retrocompatibilità
        context.tapoutDataStore.edit { preferences ->
            preferences[PreferencesKeys.REGISTERED_NFC_UID] = uid
            preferences[PreferencesKeys.NFC_TAG_NAME] = name
        }
    }

    suspend fun deleteNfcTag(tagId: Long) {
        TapOutApplication.database.nfcTagDao().deleteTagById(tagId)
        val remaining = TapOutApplication.database.nfcTagDao().getAllTagsList()
        context.tapoutDataStore.edit { preferences ->
            if (remaining.isNotEmpty()) {
                preferences[PreferencesKeys.REGISTERED_NFC_UID] = remaining.first().uid
                preferences[PreferencesKeys.NFC_TAG_NAME] = remaining.first().name
            } else {
                preferences.remove(PreferencesKeys.REGISTERED_NFC_UID)
                preferences.remove(PreferencesKeys.NFC_TAG_NAME)
            }
        }
    }

    suspend fun clearAllNfcTags() {
        val all = TapOutApplication.database.nfcTagDao().getAllTagsList()
        for (tag in all) {
            TapOutApplication.database.nfcTagDao().deleteTag(tag)
        }
        context.tapoutDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.REGISTERED_NFC_UID)
            preferences.remove(PreferencesKeys.NFC_TAG_NAME)
        }
    }

    suspend fun getAllRegisteredUids(): List<String> {
        val tags = TapOutApplication.database.nfcTagDao().getAllTagsList()
        return if (tags.isNotEmpty()) {
            tags.map { it.uid }
        } else {
            val legacy = context.tapoutDataStore.data.first()[PreferencesKeys.REGISTERED_NFC_UID]
            if (!legacy.isNullOrBlank()) listOf(legacy) else emptyList()
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
