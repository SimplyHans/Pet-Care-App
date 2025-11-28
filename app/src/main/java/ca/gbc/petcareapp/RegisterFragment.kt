package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.presentation.AuthResult
import ca.gbc.petcareapp.auth.presentation.AuthViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.activity_register) {
    private val vm: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backBtn     = view.findViewById<ImageButton>(R.id.button_back)
        val fullNameEt  = view.findViewById<EditText>(R.id.editTextTextEmailAddress3) // Name
        val emailEt     = view.findViewById<EditText>(R.id.editTextTextEmailAddress2) // Email
        val passwordEt  = view.findViewById<EditText>(R.id.editTextTextPassword)
        val confirmEt   = view.findViewById<EditText>(R.id.editTextTextPassword2)
        val registerBtn = view.findViewById<MaterialButton>(R.id.button_login)        // "Register" button

        backBtn?.setOnClickListener { findNavController().navigate(R.id.welcomeFragment) }

        registerBtn?.setOnClickListener {
            val fullName = fullNameEt.text?.toString()?.trim().orEmpty()
            val email    = emailEt.text?.toString()?.trim().orEmpty()
            val pass     = passwordEt.text?.toString().orEmpty()
            val confirm  = confirmEt.text?.toString().orEmpty()

            when {
                fullName.isEmpty() -> {
                    Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show(); return@setOnClickListener
                }
                email.isEmpty() || !email.contains("@") -> {
                    Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show(); return@setOnClickListener
                }
                pass.length < 6 -> {
                    Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show(); return@setOnClickListener
                }
                pass != confirm -> {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show(); return@setOnClickListener
                }
            }

            registerBtn.isEnabled = false
            vm.register(fullName, email, pass)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.authState.collectLatest { state ->
                    when (state) {
                        is AuthResult.Success -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.homeFragment) // destination ID
                            registerBtn?.isEnabled = true
                        }
                        is AuthResult.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            registerBtn?.isEnabled = true
                        }
                        AuthResult.Loading -> registerBtn?.isEnabled = false
                        AuthResult.Idle -> registerBtn?.isEnabled = true
                    }
                }
            }
        }
    }
}
