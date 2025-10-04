package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class PetProfileFragment : Fragment(R.layout.pet_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<MaterialButton>(R.id.backButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_petList)
        }
    }
}
