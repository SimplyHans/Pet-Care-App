package ca.gbc.petcareapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.navigation.fragment.findNavController

class AddPetDetailsFragment : Fragment(R.layout.add_pet_details) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.homeTab)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        view.findViewById<Button>(R.id.bookTab)?.setOnClickListener {
            findNavController().navigate(R.id.bookCaregiverPickerFragment)
        }

        view.findViewById<Button>(R.id.petsTab)?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }

        view.findViewById<Button>(R.id.addPetBtn)?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }

        view.findViewById<Button>(R.id.skipBtn)?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }

        view.findViewById<Button>(R.id.backBtn)?.setOnClickListener {
            findNavController().navigate(R.id.addPetNameFragment)
        }

        view.findViewById<View>(R.id.settingsBtn)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        view.findViewById<View>(R.id.notisBtn)?.setOnClickListener {
            findNavController().navigate(R.id.notisFragment)
        }
    }

}
