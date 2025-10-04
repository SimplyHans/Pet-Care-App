package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class LoginFragment : Fragment(R.layout.activity_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button navigates to WelcomeFragment
        val backBtn = view.findViewById<ImageButton>(R.id.button_back)
        backBtn?.setOnClickListener {
            findNavController().navigate(R.id.welcomeFragment)
        }

        // Login button navigates to WelcomeFragment
        val loginBtn = view.findViewById<MaterialButton>(R.id.button_login_login)
        loginBtn?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
    }
}
