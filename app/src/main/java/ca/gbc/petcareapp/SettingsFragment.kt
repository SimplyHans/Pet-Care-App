package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.session.SessionManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.activity_settings) {

    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize session manager and database
        sessionManager = SessionManager(requireContext())
        db = AppDatabase.get(requireContext())

        // Setup navbar buttons
        setupNavbar(view)
        
        // Update profile name based on logged-in user
        updateProfileName(view)
        
        // Update pet count
        updatePetCount(view)
        
        // Setup role switching button
        setupRoleSwitching(view)
    }

    private fun setupNavbar(view: View) {
        view.findViewById<Button>(R.id.homeTab)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        view.findViewById<Button>(R.id.bookTab)?.setOnClickListener {
            findNavController().navigate(R.id.bookCaregiverPickerFragment)
        }

        view.findViewById<Button>(R.id.petsTab)?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }

        view.findViewById<View>(R.id.settingsBtn)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        view.findViewById<View>(R.id.notisBtn)?.setOnClickListener {
            findNavController().navigate(R.id.notisFragment)
        }

        view.findViewById<Button>(R.id.btn_logout)?.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun updateProfileName(view: View) {
        val profileNameTextView = view.findViewById<TextView>(R.id.profile_name)
        
        // Observe session state and update profile name
        lifecycleScope.launch {
            sessionManager.sessionFlow.collect { sessionState ->
                if (sessionState.isLoggedIn && sessionState.fullName.isNotEmpty()) {
                    profileNameTextView.text = sessionState.fullName
                } else {
                    profileNameTextView.text = "Guest User"
                }
            }
        }
    }

    private fun updatePetCount(view: View) {
        val amoPetsTextView = view.findViewById<TextView>(R.id.amo_pets)
        
        lifecycleScope.launch {
            val session = sessionManager.sessionFlow.first()
            if (session.isLoggedIn) {
                val pets = db.petDao().getPetsForUser(session.userId)
                val petCount = pets.size
                amoPetsTextView.text = if (petCount == 1) "1 pet" else "$petCount pets"
            } else {
                amoPetsTextView.text = "0 pets"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh pet count when returning to settings
        view?.let { updatePetCount(it) }
    }

    private fun setupRoleSwitching(view: View) {
        val switchButton = view.findViewById<MaterialButton>(R.id.btn_switch_business)
        val businessPitchTextView = view.findViewById<TextView>(R.id.business_pitch)

        // Observe session state and update button text
        lifecycleScope.launch {
            sessionManager.sessionFlow.collect { sessionState ->
                if (sessionState.isLoggedIn) {
                    if (sessionState.currentRole == "consumer") {
                        switchButton.text = "Switch to Business"
                        businessPitchTextView.text = "Own a business that provides services for pets? Switch to the business perspective of the app."
                    } else {
                        switchButton.text = "Switch to Consumer"
                        businessPitchTextView.text = "Need reliable pet care? Switch to Consumer mode and find trusted caregivers for your pets!"
                    }
                } else {
                    businessPitchTextView.text = "Log in to access full features and switch between roles."
                }
            }
        }
        
        // Handle button click to switch roles
        switchButton.setOnClickListener {
            lifecycleScope.launch {
                sessionManager.switchRole()
            }
        }
    }
}
