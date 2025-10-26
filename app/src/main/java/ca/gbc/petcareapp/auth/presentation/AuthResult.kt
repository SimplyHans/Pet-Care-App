package ca.gbc.petcareapp.auth.presentation


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.domain.UserRepository
import ca.gbc.petcareapp.auth.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


sealed class AuthResult {
    data class Success(val message: String = "Success") : AuthResult()
    data class Error(val message: String) : AuthResult()
    data object Idle : AuthResult()
    data object Loading : AuthResult()
}


class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = UserRepository(AppDatabase.get(app).userDao())
    private val session = SessionManager(app)


    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authState: StateFlow<AuthResult> = _authState


    fun register(fullName: String, email: String, password: String) {
        _authState.value = AuthResult.Loading
        viewModelScope.launch {
            val result = repo.register(fullName, email, password)
            _authState.value = result.fold(
                onSuccess = { 
                    session.setLoggedIn(it.id, it.fullName)
                    AuthResult.Success("Account created. Welcome, ${it.fullName}!") 
                },
                onFailure = { AuthResult.Error(it.message ?: "Registration failed.") }
            )
        }
    }


    fun login(email: String, password: String) {
        _authState.value = AuthResult.Loading
        viewModelScope.launch {
            val result = repo.login(email, password)
            _authState.value = result.fold(
                onSuccess = {
                    viewModelScope.launch { session.setLoggedIn(it.id, it.fullName) }
                    AuthResult.Success("Welcome back, ${it.fullName}!")
                },
                onFailure = { AuthResult.Error(it.message ?: "Login failed.") }
            )
        }
    }
}