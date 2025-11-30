package ca.gbc.petcareapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.pets.PetViewModel
import ca.gbc.petcareapp.pets.PetViewModelFactory

class AddPetNameFragment : Fragment(R.layout.add_pet_name) {

    private lateinit var viewModel: PetViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Shared ViewModel
        val db = AppDatabase.get(view.context)
        val factory = PetViewModelFactory(db)
        viewModel = ViewModelProvider(requireActivity(), factory)[PetViewModel::class.java]

        val nameInput = view.findViewById<EditText>(R.id.nameInput)
        val nextBtn = view.findViewById<Button>(R.id.addPetBtn)
        val backBtn = view.findViewById<Button>(R.id.backBtn)

        // Add Pet button
        nextBtn.setOnClickListener {
            val petName = nameInput.text.toString().trim()
            if (petName.isNotEmpty()) {
                viewModel.petName = petName
                findNavController().navigate(R.id.addPetDetailsFragment)
            } else {
                Toast.makeText(requireContext(), "Please enter a name for your pet.", Toast.LENGTH_SHORT).show()
            }
        }

        // Back button
        backBtn.setOnClickListener {
            findNavController().navigateUp() // <-- fixed navigation
        }

    }
}
