package ca.gbc.petcareapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.data.AppDatabase
import com.google.android.material.button.MaterialButton
import android.util.Log
import ca.gbc.petcareapp.pets.PetViewModel
import ca.gbc.petcareapp.pets.PetViewModelFactory

class AddPetTypeFragment : Fragment() {

    private lateinit var petTypeGroup: RadioGroup
    private lateinit var nextBtn: MaterialButton
    private lateinit var backBtn: MaterialButton
    private lateinit var viewModel: PetViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_pet_type, container, false)

        val db = AppDatabase.get(requireContext())
        val factory = PetViewModelFactory(db)
        viewModel = ViewModelProvider(requireActivity(), factory)[PetViewModel::class.java]

        petTypeGroup = view.findViewById(R.id.petTypeGroup)
        nextBtn = view.findViewById(R.id.nextBtn)
        backBtn = view.findViewById(R.id.backBtn)

        nextBtn.setOnClickListener {
            val selectedId = petTypeGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedButton: RadioButton = view.findViewById(selectedId)
                val selectedType = selectedButton.text.toString()
                viewModel.petType = selectedType

                Log.d("PetDebug", "Pet type selected: ${viewModel.petType}")

                findNavController().navigate(R.id.action_addPetTypeFragment_to_addPetNameFragment)
            }
        }

        backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }
}
