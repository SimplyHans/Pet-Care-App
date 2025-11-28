package ca.gbc.petcareapp

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.domain.UserRepository
import ca.gbc.petcareapp.auth.session.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import android.widget.Switch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment(R.layout.activity_settings) {

    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase
    private lateinit var userRepository: UserRepository
    private lateinit var prefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize
        sessionManager = SessionManager(requireContext())
        db = AppDatabase.get(requireContext())
        userRepository = UserRepository(db.userDao())
        prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Setup navbar (fix crash - use ImageButton)
        setupNavbar(view)
        
        // Update profile info
        updateProfileInfo(view)
        
        // Setup all settings buttons
        setupSettingsButtons(view)
    }

    private fun setupNavbar(view: View) {
        val homeTab = view.findViewById<ImageButton>(R.id.homeTab)
        val bookTab = view.findViewById<ImageButton>(R.id.bookTab)
        val petsTab = view.findViewById<ImageButton>(R.id.petsTab)
        val settingsBtn = view.findViewById<View>(R.id.settingsBtn)
        val notisBtn = view.findViewById<View>(R.id.notisBtn)

        homeTab?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        bookTab?.setOnClickListener {
            findNavController().navigate(R.id.bkServiceTypeFragment)
        }

        petsTab?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }

        settingsBtn?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        notisBtn?.setOnClickListener {
            findNavController().navigate(R.id.notisFragment)
        }
    }

    private fun updateProfileInfo(view: View) {
        val profileName = view.findViewById<TextView>(R.id.profile_name)
        val profileEmail = view.findViewById<TextView>(R.id.profile_email)
        val amoPets = view.findViewById<TextView>(R.id.amo_pets)
        
        lifecycleScope.launch {
            val session = withContext(Dispatchers.IO) {
                sessionManager.sessionFlow.first()
            }
            if (session.isLoggedIn) {
                val user = withContext(Dispatchers.IO) {
                    db.userDao().findById(session.userId)
                }
                val pets = withContext(Dispatchers.IO) {
                    db.petDao().getPetsForUser(session.userId)
                }
                
                profileName.text = user?.fullName ?: "Guest User"
                profileEmail.text = user?.email ?: ""
                amoPets.text = if (pets.size == 1) "1 pet" else "${pets.size} pets"
            } else {
                profileName.text = "Guest User"
                profileEmail.text = "Not logged in"
                amoPets.text = "0 pets"
            }
        }
    }

    private fun setupSettingsButtons(view: View) {
        // Edit Profile
        view.findViewById<MaterialCardView>(R.id.btn_edit_profile)?.setOnClickListener {
            showEditProfileDialog(view)
        }

        // Dark Mode Toggle
        val darkModeSwitch = view.findViewById<Switch>(R.id.switch_dark_mode)
        lifecycleScope.launch {
            // Only get initial state, don't continuously collect
            val sessionState = withContext(Dispatchers.IO) {
                sessionManager.sessionFlow.first()
            }
            darkModeSwitch.isChecked = sessionState.isDarkMode
        }
        
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                sessionManager.setDarkMode(isChecked)
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                Toast.makeText(requireContext(), if (isChecked) "Dark mode enabled" else "Dark mode disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Notifications Settings
        view.findViewById<MaterialCardView>(R.id.btn_notifications)?.setOnClickListener {
            Toast.makeText(requireContext(), "Notification settings coming soon", Toast.LENGTH_SHORT).show()
        }

        // Privacy Settings
        view.findViewById<MaterialCardView>(R.id.btn_privacy)?.setOnClickListener {
            Toast.makeText(requireContext(), "Privacy settings coming soon", Toast.LENGTH_SHORT).show()
        }

        // Switch Role
        val switchRoleCard = view.findViewById<MaterialCardView>(R.id.btn_switch_role)
        val currentRoleText = view.findViewById<TextView>(R.id.current_role_text)
        
        lifecycleScope.launch {
            sessionManager.sessionFlow.collect { sessionState ->
                if (sessionState.isLoggedIn) {
                    currentRoleText.text = if (sessionState.currentRole == "consumer") "Consumer" else "Business"
                    switchRoleCard?.setOnClickListener {
                        lifecycleScope.launch {
                            sessionManager.switchRole()
                            Toast.makeText(requireContext(), "Role switched", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    currentRoleText.text = "Not logged in"
                    switchRoleCard?.setOnClickListener {
                        Toast.makeText(requireContext(), "Please log in to switch roles", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // About
        view.findViewById<MaterialCardView>(R.id.btn_about)?.setOnClickListener {
            showAboutDialog()
        }

        // Delete Account
        view.findViewById<MaterialCardView>(R.id.btn_delete_account)?.setOnClickListener {
            showDeleteAccountDialog(view)
        }

        // Logout
        view.findViewById<MaterialButton>(R.id.btn_logout)?.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showEditProfileDialog(view: View) {
        lifecycleScope.launch {
            val session = sessionManager.sessionFlow.first()
            if (!session.isLoggedIn) {
                Toast.makeText(requireContext(), "Please log in to edit profile", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val user = db.userDao().findById(session.userId)
            if (user == null) {
                Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
            val nameInput = dialogView.findViewById<EditText>(R.id.edit_name)
            val emailInput = dialogView.findViewById<EditText>(R.id.edit_email)
            
            nameInput.setText(user.fullName)
            emailInput.setText(user.email)

            AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val newName = nameInput.text.toString().trim()
                    val newEmail = emailInput.text.toString().trim()

                    if (newName.isEmpty()) {
                        Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if (newEmail.isEmpty() || !newEmail.contains("@")) {
                        Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    lifecycleScope.launch {
                        try {
                            db.userDao().updateName(user.id, newName)
                            if (newEmail != user.email) {
                                // Check if email already exists
                                val existingUser = db.userDao().findByEmail(newEmail)
                                if (existingUser != null && existingUser.id != user.id) {
                                    Toast.makeText(requireContext(), "Email already in use", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                db.userDao().updateEmail(user.id, newEmail)
                            }
                            updateProfileInfo(view)
                            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showDeleteAccountDialog(view: View) {
        lifecycleScope.launch {
            val session = sessionManager.sessionFlow.first()
            if (!session.isLoggedIn) {
                Toast.makeText(requireContext(), "Please log in to delete account", Toast.LENGTH_SHORT).show()
                return@launch
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone. All your pets and bookings will be deleted.")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            val userId = session.userId
                            
                            // Delete user's pets
                            val pets = db.petDao().getPetsForUser(userId)
                            pets.forEach { pet ->
                                db.petDao().delete(pet)
                            }
                            
                            // Delete user account
                            db.userDao().deleteById(userId)
                            
                            // Logout
                            sessionManager.logout()
                            
                            Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.loginFragment)
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Failed to delete account: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                lifecycleScope.launch {
                    sessionManager.logout()
                    Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.loginFragment)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("About Pet Care App")
            .setMessage("Pet Care App v1.0\n\n" +
                    "Your one-stop solution for pet care services.\n\n" +
                    "Features:\n" +
                    "• Book pet care services\n" +
                    "• Manage your pets\n" +
                    "• Track appointments\n" +
                    "• Switch between consumer and business roles\n\n" +
                    "© 2025 Pet Care App")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        view?.let { updateProfileInfo(it) }
    }
}
