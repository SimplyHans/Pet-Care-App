package ca.gbc.petcareapp

import android.content.res.ColorStateList
import android.graphics.Color
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
import android.widget.ImageButton
import ca.gbc.petcareapp.pets.PetViewModel
import ca.gbc.petcareapp.pets.PetViewModelFactory
import androidx.core.content.ContextCompat

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

        // Radio button list for styling
        val radioButtons = listOf(
            view.findViewById<RadioButton>(R.id.petType),
            view.findViewById<RadioButton>(R.id.petType2),
            view.findViewById<RadioButton>(R.id.petType3),
            view.findViewById<RadioButton>(R.id.petType4)
        )

        radioButtons.forEach { button ->
            button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E69C77"))
            button.setTextColor(Color.BLACK)
        }

        petTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            radioButtons.forEach { button ->
                if (button.id == checkedId) {
                    button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#b07070"))
                    button.setTextColor(Color.WHITE)
                } else {
                    button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E69C77"))
                    button.setTextColor(Color.BLACK)
                }
            }
        }

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
            findNavController().navigate(R.id.petListFragment)
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

        // Initially highlight Pets tab
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

        return view
    }
}
