package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class PetListFragment : Fragment(R.layout.pet_list) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.button_AddPet)?.setOnClickListener {
            findNavController().navigate(R.id.action_petList_to_addPetType)
        }

        val petButtons = listOf(
            R.id.button_pet01,
            R.id.button_pet02,
            R.id.button_pet03,
            R.id.button_pet04
        )

        petButtons.forEach { id ->
            view.findViewById<MaterialButton>(id)?.setOnClickListener {
                findNavController().navigate(R.id.action_petList_to_petProfile)
            }
        }
    }
}
