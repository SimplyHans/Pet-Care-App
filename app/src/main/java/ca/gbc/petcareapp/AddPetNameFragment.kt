package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class AddPetNameFragment : Fragment(R.layout.add_pet_name) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.backButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_addPetName_to_addPetDetails)
        }

        view.findViewById<MaterialButton>(R.id.addPetButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_addPetName_to_petList)
        }
    }
}
