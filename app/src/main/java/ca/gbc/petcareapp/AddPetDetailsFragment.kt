package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.session.SessionManager
import ca.gbc.petcareapp.pets.PetViewModel
import ca.gbc.petcareapp.pets.PetViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddPetDetailsFragment : Fragment(R.layout.add_pet_details) {

    private lateinit var breedInput: TextInputEditText
    private lateinit var ageInput: EditText
    private lateinit var descInput: EditText
    private lateinit var saveBtn: MaterialButton
    private lateinit var backBtn: MaterialButton
    private lateinit var viewModel: PetViewModel
    private lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.get(requireContext())
        val factory = PetViewModelFactory(db)
        viewModel = ViewModelProvider(requireActivity(), factory)[PetViewModel::class.java]
        sessionManager = SessionManager(requireContext())

        breedInput = view.findViewById(R.id.breedInput)
        ageInput = view.findViewById(R.id.ageInput)
        descInput = view.findViewById(R.id.descInput)
        saveBtn = view.findViewById(R.id.savePetBtn)
        backBtn = view.findViewById(R.id.backBtn)

        saveBtn.setOnClickListener {
            val breed = breedInput.text.toString().trim()
            val ageText = ageInput.text.toString().trim()
            val desc = descInput.text.toString().trim()

            // Validate inputs
            if (breed.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter the breed.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (ageText.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter the age.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageText.toIntOrNull()
            if (age == null) {
                Toast.makeText(requireContext(), "Age must be a valid number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.breed = breed
            viewModel.age = age
            viewModel.desc = desc

            lifecycleScope.launch {
                val session = sessionManager.sessionFlow.first()
                val userId = session.userId
                val petId = viewModel.currentEditingPetId

                if (petId != null) {
                    viewModel.updatePet(petId, userId)
                } else {
                    viewModel.savePet(userId)
                }

                viewModel.isEditing = false
                viewModel.currentEditingPetId = null
                viewModel.currentUserId = null

                findNavController().navigate(R.id.petListFragment)
            }
        }

        backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        // navbar
        view.findViewById<View>(R.id.homeTab)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
        view.findViewById<View>(R.id.bookTab)?.setOnClickListener {
            findNavController().navigate(R.id.bookCaregiverPickerFragment)
        }
        view.findViewById<View>(R.id.petsTab)?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }
        view.findViewById<View>(R.id.settingsBtn)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
        view.findViewById<View>(R.id.notisBtn)?.setOnClickListener {
            findNavController().navigate(R.id.notisFragment)
        }
    }
}
