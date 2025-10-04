package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class AddPetTypeFragment : Fragment(R.layout.add_pet_type) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.button_backPetType2)?.setOnClickListener {
            findNavController().navigate(R.id.action_addPetType_to_addPetDetails)
        }

        view.findViewById<MaterialButton>(R.id.backButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_addPetType_to_petList)
        }
    }
}
