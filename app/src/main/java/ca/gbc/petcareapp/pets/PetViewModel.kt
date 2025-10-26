package ca.gbc.petcareapp.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gbc.petcareapp.auth.data.AppDatabase
import kotlinx.coroutines.launch

class PetViewModel(private val db: AppDatabase) : ViewModel() {

    var petType: String? = null
    var petName: String? = null
    var breed: String? = null
    var age: Int? = null
    var desc: String? = null
    var isEditing: Boolean = false
    var currentEditingPetId: Long? = null
    var currentUserId: Long? = null

    fun savePet(userId: Long) {
        viewModelScope.launch {
            val pet = Pet(
                userId = userId,
                petType = petType ?: "",
                petName = petName ?: "",
                breed = breed ?: "",
                age = age ?: 0,
                desc = desc
            )
            db.petDao().insert(pet)
        }
    }
    fun updatePet(petId: Long, userId: Long) {
        viewModelScope.launch {
            val updatedPet = Pet(
                id = petId, // keep same ID
                userId = userId,
                petType = petType ?: "",
                petName = petName ?: "",
                breed = breed ?: "",
                age = age ?: 0,
                desc = desc
            )
            db.petDao().update(updatedPet)
        }
    }


}
