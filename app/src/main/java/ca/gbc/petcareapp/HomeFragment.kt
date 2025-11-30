package ca.gbc.petcareapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.session.SessionManager
import ca.gbc.petcareapp.pets.PetAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.home) {

    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase
    private lateinit var petAdapter: PetAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ---------------------- NAV BAR + HEADER ----------------------
        val header = view.findViewById<View>(R.id.header)
        val title = header.findViewById<TextView>(R.id.title)
        title.text = "Home"

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

        highlightNav(homeTab)

        homeTab.setOnClickListener { highlightNav(homeTab) }
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

        view.findViewById<View>(R.id.card_add_pet)?.setOnClickListener {
            findNavController().navigate(R.id.addPetTypeFragment)
        }

        view.findViewById<View>(R.id.card_view_pets)?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }

        sessionManager = SessionManager(requireContext())
        db = AppDatabase.get(requireContext())

        updateProfileName(view)

        setupPetRecycler(view)
        loadPetsIntoHome()
    }

    private fun updateProfileName(view: View) {
        val profileNameTextView = view.findViewById<TextView>(R.id.name_text)

        lifecycleScope.launch {
            sessionManager.sessionFlow.collect { sessionState ->
                profileNameTextView.text =
                    if (sessionState.isLoggedIn && sessionState.fullName.isNotEmpty())
                        sessionState.fullName
                    else
                        "Guest User"
            }
        }
    }

    private fun setupPetRecycler(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_home_pets)

        recycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        petAdapter = PetAdapter(emptyList()) { pet ->
            val bundle = Bundle().apply {
                putLong("petId", pet.id)
                putString("petName", pet.petName)
                putString("breed", pet.breed)
                putInt("age", pet.age)
                putString("description", pet.desc ?: "")
            }
            findNavController().navigate(R.id.petProfileFragment, bundle)
        }

        recycler.adapter = petAdapter
    }

    private fun loadPetsIntoHome() {
        lifecycleScope.launch {
            val session = sessionManager.sessionFlow.first()

            if (!session.isLoggedIn) return@launch

            val pets = db.petDao().getPetsForUser(session.userId)

            petAdapter.updatePets(pets)
        }
    }
}
