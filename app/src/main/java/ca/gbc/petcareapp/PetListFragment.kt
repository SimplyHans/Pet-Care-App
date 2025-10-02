package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class PetListFragment : Fragment(R.layout.pet_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addPetButton = view.findViewById<MaterialButton>(R.id.button_AddPet)
        addPetButton.setOnClickListener {
            // Navigate to AddPetTypeFragment
            findNavController().navigate(R.id.action_petListFragment_to_addPetTypeFragment)
        }
    }
}
