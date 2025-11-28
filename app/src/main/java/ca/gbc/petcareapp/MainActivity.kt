package ca.gbc.petcareapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import ca.gbc.petcareapp.auth.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        
        // Initialize dark mode from session
        lifecycleScope.launch {
            val session = sessionManager.sessionFlow.first()
            AppCompatDelegate.setDefaultNightMode(
                if (session.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        
        setContentView(R.layout.activity_main)
    }
}
