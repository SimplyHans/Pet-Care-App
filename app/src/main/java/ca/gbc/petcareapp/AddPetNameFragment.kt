package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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

        // ✅ Shared ViewModel (use requireActivity so it's same across fragments)
        val db = AppDatabase.get(view.context)
        val factory = PetViewModelFactory(db)
        viewModel = ViewModelProvider(requireActivity(), factory)[PetViewModel::class.java]

        val nameInput = view.findViewById<EditText>(R.id.nameInput)
        val nextBtn = view.findViewById<Button>(R.id.delPetBtn)
        val backBtn = view.findViewById<Button>(R.id.backBtn)

        nextBtn.setOnClickListener {
            viewModel.petName = nameInput.text.toString()
            findNavController().navigate(R.id.addPetDetailsFragment)
        }

        backBtn.setOnClickListener {
            findNavController().navigate(R.id.addPetTypeFragment)
        }
    }
}
