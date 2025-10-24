package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.presentation.AuthResult
import ca.gbc.petcareapp.auth.presentation.AuthViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest

class LoginFragment : Fragment(R.layout.activity_login) {
    private val vm: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use your EXACT IDs from the XML
        val backBtn  = view.findViewById<ImageButton>(R.id.button_back)
        val emailEt  = view.findViewById<EditText>(R.id.input_email)
        val passEt   = view.findViewById<EditText>(R.id.input_password)
        val loginBtn = view.findViewById<MaterialButton>(R.id.button_login_login)

        backBtn?.setOnClickListener { findNavController().navigate(R.id.welcomeFragment) }

        loginBtn?.setOnClickListener {
            val email = emailEt.text?.toString()?.trim().orEmpty()
            val pass  = passEt.text?.toString().orEmpty()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Email & password required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginBtn.isEnabled = false
            vm.login(email, pass)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.authState.collectLatest { state ->
                when (state) {
                    is AuthResult.Success -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.homeFragment)
                        loginBtn?.isEnabled = true
                    }
                    is AuthResult.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        loginBtn?.isEnabled = true
                    }
                    AuthResult.Loading -> loginBtn?.isEnabled = false
                    AuthResult.Idle -> loginBtn?.isEnabled = true
                }
            }
        }
    }
}
