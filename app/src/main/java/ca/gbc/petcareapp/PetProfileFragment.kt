package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.session.SessionManager
import ca.gbc.petcareapp.pets.PetViewModel
import ca.gbc.petcareapp.pets.PetViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PetProfileFragment : Fragment(R.layout.pet_profile) {

    private var petId: Long = 0
    private var petName: String? = null
    private var breed: String? = null
    private var age: Int = 0
    private var description: String? = null
    private var petType: String? = null

    private lateinit var db: AppDatabase
    private lateinit var viewModel: PetViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.get(requireContext())
        val factory = PetViewModelFactory(db)
        viewModel = ViewModelProvider(requireActivity(), factory)[PetViewModel::class.java]

        // Get arguments from navigation
        arguments?.let {
            petId = it.getLong("petId")
            petName = it.getString("petName")
            breed = it.getString("breed")
            age = it.getInt("age")
            description = it.getString("description")
            petType = it.getString("petType")
        }

        val nameView = view.findViewById<TextView>(R.id.PetName)
        val breedInput = view.findViewById<TextInputEditText>(R.id.breedInput)
        val ageInput = view.findViewById<TextInputEditText>(R.id.ageInput)
        val descInput = view.findViewById<TextInputEditText>(R.id.nameInput)

        val editBtn = view.findViewById<MaterialButton>(R.id.editBtn)
        val delBtn = view.findViewById<MaterialButton>(R.id.savePetBtn)

        nameView.text = petName
        breedInput.setText(breed)
        ageInput.setText(age.toString())
        descInput.setText(description)

        val sessionManager = SessionManager(requireContext())

        // ✅ EDIT BUTTON → start edit flow (same as Add Pet)
        editBtn.setOnClickListener {
            lifecycleScope.launch {
                val session = sessionManager.sessionFlow.first()
                val userId = session.userId

                // Store data in ViewModel
                viewModel.petType = petType
                viewModel.petName = petName
                viewModel.breed = breed
                viewModel.age = age
                viewModel.desc = description

                // Set a temporary property to know we’re editing this pet
                viewModel.currentEditingPetId = petId
                viewModel.currentUserId = userId

                // Navigate to AddPetTypeFragment (reuse add flow)
                findNavController().navigate(R.id.addPetTypeFragment)
            }
        }

        // ✅ DELETE button
        delBtn.setOnClickListener {
            deletePetAndGoBack()
        }
    }

    private fun deletePetAndGoBack() {
        val sessionManager = SessionManager(requireContext())
        lifecycleScope.launch {
            val session = sessionManager.sessionFlow.first()
            val userId = session.userId

            val pet = db.petDao().getPetsForUser(userId).firstOrNull { it.id == petId }
            pet?.let { db.petDao().delete(it) }

            findNavController().navigateUp()
        }
    }
}
