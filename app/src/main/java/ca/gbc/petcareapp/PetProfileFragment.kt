package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.session.SessionManager
import ca.gbc.petcareapp.auth.session.SessionState
import ca.gbc.petcareapp.pets.Pet
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class PetProfileFragment : Fragment(R.layout.pet_profile) {

    private var petId: Long = 0
    private var petName: String? = null
    private var breed: String? = null
    private var age: Int = 0
    private var description: String? = null

    private lateinit var db: AppDatabase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.get(requireContext())

        // Get arguments passed to this fragment
        arguments?.let {
            petId = it.getLong("petId")
            petName = it.getString("petName")
            breed = it.getString("breed")
            age = it.getInt("age")
            description = it.getString("description")
        }

        // Set data to views
        view.findViewById<TextView>(R.id.PetName).text = petName
        view.findViewById<TextInputEditText>(R.id.breedInput).setText(breed)
        view.findViewById<TextInputEditText>(R.id.ageInput).setText(age.toString())
        view.findViewById<TextInputEditText>(R.id.nameInput).setText(description)

        // Delete button click
        view.findViewById<MaterialButton>(R.id.delPetBtn).setOnClickListener {
            deletePetAndGoBack()
        }
    }

    private fun deletePetAndGoBack() {
        val sessionManager = SessionManager(requireContext())
        lifecycleScope.launch {
            // Get the first value from the session Flow
            val session = sessionManager.sessionFlow.first()
            val userId = session.userId

            // Get the pet for this user
            val pet: Pet? = db.petDao().getPetsForUser(userId)
                .firstOrNull { it.id == petId }

            pet?.let {
                db.petDao().delete(it)
            }

            // Navigate back
            findNavController().navigateUp()
        }
    }

}
