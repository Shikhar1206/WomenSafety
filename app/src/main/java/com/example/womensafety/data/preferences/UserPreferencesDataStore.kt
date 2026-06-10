package com.example.womensafety.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.womensafety.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPreferences(
    val shakeEnabled: Boolean = true,
    val shakeSensitivity: Float = 0.6f,
    val safeWordEnabled: Boolean = false,
    val safeWord: String = "help",
    val batterySosEnabled: Boolean = true,
    val autoCaptureEnabled: Boolean = false,
    val fakeCallName: String = "Mom",
    val fakeCallNumber: String = "+911234567890",
    val calendarUnlockEnabled: Boolean = true,
    val calendarUnlockDay: Int = 13
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val SHAKE_ENABLED = booleanPreferencesKey(Constants.PREFS_SHAKE_ENABLED)
    private val SHAKE_SENSITIVITY = floatPreferencesKey(Constants.PREFS_SHAKE_SENSITIVITY)
    private val SAFE_WORD_ENABLED = booleanPreferencesKey(Constants.PREFS_SAFE_WORD_ENABLED)
    private val SAFE_WORD = stringPreferencesKey(Constants.PREFS_SAFE_WORD)
    private val BATTERY_SOS_ENABLED = booleanPreferencesKey(Constants.PREFS_BATTERY_SOS_ENABLED)
    private val AUTO_CAPTURE_ENABLED = booleanPreferencesKey(Constants.PREFS_AUTO_CAPTURE_ENABLED)
    private val FAKE_CALL_NAME = stringPreferencesKey(Constants.PREFS_FAKE_CALL_NAME)
    private val FAKE_CALL_NUMBER = stringPreferencesKey(Constants.PREFS_FAKE_CALL_NUMBER)
    private val CALENDAR_UNLOCK_ENABLED = booleanPreferencesKey(Constants.PREFS_CALENDAR_UNLOCK_ENABLED)
    private val CALENDAR_UNLOCK_DAY = intPreferencesKey(Constants.PREFS_CALENDAR_UNLOCK_DAY)

    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .catch { e ->
            Timber.e(e, "Error reading preferences")
            emit(emptyPreferences())
        }
        .map { prefs ->
            UserPreferences(
                shakeEnabled = prefs[SHAKE_ENABLED] ?: true,
                shakeSensitivity = prefs[SHAKE_SENSITIVITY] ?: 0.6f,
                safeWordEnabled = prefs[SAFE_WORD_ENABLED] ?: false,
                safeWord = prefs[SAFE_WORD] ?: "help",
                batterySosEnabled = prefs[BATTERY_SOS_ENABLED] ?: true,
                autoCaptureEnabled = prefs[AUTO_CAPTURE_ENABLED] ?: false,
                fakeCallName = prefs[FAKE_CALL_NAME] ?: "Mom",
                fakeCallNumber = prefs[FAKE_CALL_NUMBER] ?: "+911234567890",
                calendarUnlockEnabled = prefs[CALENDAR_UNLOCK_ENABLED] ?: true,
                calendarUnlockDay = prefs[CALENDAR_UNLOCK_DAY] ?: 13
            )
        }

    suspend fun setShakeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SHAKE_ENABLED] = enabled }
    }

    suspend fun setShakeSensitivity(sensitivity: Float) {
        context.dataStore.edit { it[SHAKE_SENSITIVITY] = sensitivity }
    }

    suspend fun setSafeWordEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SAFE_WORD_ENABLED] = enabled }
    }

    suspend fun setSafeWord(word: String) {
        context.dataStore.edit { it[SAFE_WORD] = word }
    }

    suspend fun setBatterySosEnabled(enabled: Boolean) {
        context.dataStore.edit { it[BATTERY_SOS_ENABLED] = enabled }
    }

    suspend fun setAutoCaptureEnabled(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_CAPTURE_ENABLED] = enabled }
    }

    suspend fun setFakeCallName(name: String) {
        context.dataStore.edit { it[FAKE_CALL_NAME] = name }
    }

    suspend fun setFakeCallNumber(number: String) {
        context.dataStore.edit { it[FAKE_CALL_NUMBER] = number }
    }

    suspend fun setCalendarUnlockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[CALENDAR_UNLOCK_ENABLED] = enabled }
    }

    suspend fun setCalendarUnlockDay(day: Int) {
        context.dataStore.edit { it[CALENDAR_UNLOCK_DAY] = day }
    }
}
