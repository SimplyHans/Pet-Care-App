package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class WelcomeFragment : Fragment(R.layout.activity_welcome) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Buttons from activity_welcome.xml
        view.findViewById<Button>(R.id.button_login)?.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_login)
        }

        view.findViewById<Button>(R.id.button_register)?.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_register)
        }
    }
}
