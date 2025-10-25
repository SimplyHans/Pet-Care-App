package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.session.SessionManager
import ca.gbc.petcareapp.pets.PetAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PetListFragment : Fragment(R.layout.pet_list) {

    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase
    private lateinit var petAdapter: PetAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.get(requireContext())
        sessionManager = SessionManager(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_pets)
        petAdapter = PetAdapter(emptyList()) { pet ->
            // Navigate to PetProfileFragment with pet details
            val bundle = Bundle().apply {
                putLong("petId", pet.id) // <-- use putLong if pet.id is Long
                putString("petName", pet.petName)
                putString("breed", pet.breed)
                putInt("age", pet.age)
                putString("description", pet.desc ?: "")
            }
            findNavController().navigate(R.id.petProfileFragment, bundle)
        }
        recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = petAdapter
        }

        loadUserPets()

        // Navigation buttons
        view.findViewById<Button>(R.id.homeTab)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
        view.findViewById<Button>(R.id.bookTab)?.setOnClickListener {
            findNavController().navigate(R.id.bookCaregiverPickerFragment)
        }
        view.findViewById<Button>(R.id.petsTab)?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }
        view.findViewById<Button>(R.id.button_AddPet)?.setOnClickListener {
            findNavController().navigate(R.id.addPetTypeFragment)
        }
    }

    private fun loadUserPets() {
        lifecycleScope.launch {
            val session = sessionManager.sessionFlow.first()
            val userId = session.userId
            val pets = db.petDao().getPetsForUser(userId)
            petAdapter.updatePets(pets)
        }
    }
}
