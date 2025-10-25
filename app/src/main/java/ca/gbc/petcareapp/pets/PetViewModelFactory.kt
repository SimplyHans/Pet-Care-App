package ca.gbc.petcareapp.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ca.gbc.petcareapp.auth.data.AppDatabase

class PetViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PetViewModel(db) as T
    }
}
