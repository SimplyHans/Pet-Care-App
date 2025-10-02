package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddPetNameFragment : Fragment(R.layout.add_pet_name) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<MaterialButton>(R.id.backButton)
        val addPetButton = view.findViewById<MaterialButton>(R.id.addPetButton)
        val nameInput = view.findViewById<TextInputEditText>(R.id.nameInput)

        // Go back to previous page
        backButton?.setOnClickListener {
            findNavController().popBackStack()
        }

        // Add Pet and go back to Pet List
        addPetButton?.setOnClickListener {
            val petName = nameInput?.text.toString().trim()
            if (petName.isNotEmpty()) {
                // TODO: Save pet name to database or ViewModel here

                // Navigate back to Pet List Fragment
                findNavController().navigate(R.id.action_addPetNameFragment_to_petListFragment)
            } else {
                nameInput?.error = "Please enter a pet name"
            }
        }
    }
}
