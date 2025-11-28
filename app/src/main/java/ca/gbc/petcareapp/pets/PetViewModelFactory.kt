package ca.gbc.petcareapp.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ca.gbc.petcareapp.auth.data.AppDatabase

class PetViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetViewModel::class.java)) {
            return PetViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
