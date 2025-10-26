package ca.gbc.petcareapp.auth.session


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "session_prefs")


data class SessionState(
    val isLoggedIn: Boolean = false,
    val userId: Long = 0L,
    val fullName: String = "",
    val currentRole: String = "consumer"
)


class SessionManager(private val context: Context) {
    private object Keys {
        val LOGGED_IN = booleanPreferencesKey("logged_in")
        val USER_ID = longPreferencesKey("user_id")
        val FULL_NAME = stringPreferencesKey("full_name")
        val CURRENT_ROLE = stringPreferencesKey("current_role")
    }


    val sessionFlow: Flow<SessionState> = context.dataStore.data.map { prefs ->
        SessionState(
            isLoggedIn = prefs[Keys.LOGGED_IN] ?: false,
            userId = prefs[Keys.USER_ID] ?: 0L,
            fullName = prefs[Keys.FULL_NAME] ?: "",
            currentRole = prefs[Keys.CURRENT_ROLE] ?: "consumer"
        )
    }


    suspend fun setLoggedIn(userId: Long, fullName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOGGED_IN] = true
            prefs[Keys.USER_ID] = userId
            prefs[Keys.FULL_NAME] = fullName
            prefs[Keys.CURRENT_ROLE] = "consumer"
        }
    }


    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOGGED_IN] = false
            prefs[Keys.USER_ID] = 0L
            prefs[Keys.FULL_NAME] = ""
            prefs[Keys.CURRENT_ROLE] = "consumer"
        }
    }

    suspend fun switchRole() {
        context.dataStore.edit { prefs ->
            val currentRole = prefs[Keys.CURRENT_ROLE] ?: "consumer"
            val newRole = if (currentRole == "consumer") "business" else "consumer"
            prefs[Keys.CURRENT_ROLE] = newRole
        }
    }
}