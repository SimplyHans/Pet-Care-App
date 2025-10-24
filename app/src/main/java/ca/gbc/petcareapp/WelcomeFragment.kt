package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class WelcomeFragment : Fragment(R.layout.activity_welcome) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnRegister = view.findViewById<Button>(R.id.button_register)
        val btnLogin    = view.findViewById<Button>(R.id.button_login)


        btnRegister?.setOnClickListener { findNavController().navigate(R.id.registerFragment) }
        btnLogin?.setOnClickListener { findNavController().navigate(R.id.loginFragment) }
    }
}
