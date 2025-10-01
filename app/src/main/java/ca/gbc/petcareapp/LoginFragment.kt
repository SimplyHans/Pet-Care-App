package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment(R.layout.activity_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backBtn = view.findViewById<ImageButton>(R.id.button_back)
        backBtn?.setOnClickListener {

            findNavController().navigate(R.id.welcomeFragment)
        }
    }
}
