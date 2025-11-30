package ca.gbc.petcareapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
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
        val backBtn = view.findViewById<MaterialButton>(R.id.backBtn)

        // Populate fields
        nameView.text = petName
        breedInput.setText(breed)
        ageInput.setText(age.toString())
        descInput.setText(description)

        val sessionManager = SessionManager(requireContext())

        // EDIT BUTTON → start edit flow
        editBtn.setOnClickListener {
            lifecycleScope.launch {
                val session = sessionManager.sessionFlow.first()
                val userId = session.userId

                viewModel.petType = petType
                viewModel.petName = petName
                viewModel.breed = breed
                viewModel.age = age
                viewModel.desc = description
                viewModel.currentEditingPetId = petId
                viewModel.currentUserId = userId

                findNavController().navigate(R.id.addPetTypeFragment)
            }
        }

        // DELETE BUTTON → remove pet and go back
        delBtn.setOnClickListener {
            lifecycleScope.launch {
                val session = sessionManager.sessionFlow.first()
                val userId = session.userId

                val pet = db.petDao().getPetsForUser(userId).firstOrNull { it.id == petId }
                pet?.let { db.petDao().delete(it) }

                findNavController().navigate(R.id.action_petProfileFragment_to_petListFragment)
            }
        }

        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_petProfileFragment_to_petListFragment)
        }

        val homeTab = view.findViewById<ImageButton>(R.id.homeTab)
        val bookTab = view.findViewById<ImageButton>(R.id.bookTab)
        val petsTab = view.findViewById<ImageButton>(R.id.petsTab)

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.bright_orange)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.dark_main)

        fun highlightNav(selected: ImageButton) {
            homeTab.imageTintList =
                ColorStateList.valueOf(if (selected == homeTab) selectedColor else unselectedColor)
            bookTab.imageTintList =
                ColorStateList.valueOf(if (selected == bookTab) selectedColor else unselectedColor)
            petsTab.imageTintList =
                ColorStateList.valueOf(if (selected == petsTab) selectedColor else unselectedColor)
        }

        // Initially highlight the Pets tab
        highlightNav(petsTab)

        homeTab.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
            highlightNav(homeTab)
        }
        bookTab.setOnClickListener {
            findNavController().navigate(R.id.bookListFragment)
            highlightNav(bookTab)
        }
        petsTab.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
            highlightNav(petsTab)
        }

        view.findViewById<View>(R.id.settingsBtn)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
        view.findViewById<View>(R.id.notisBtn)?.setOnClickListener {
            findNavController().navigate(R.id.notisFragment)
        }
    }
}
