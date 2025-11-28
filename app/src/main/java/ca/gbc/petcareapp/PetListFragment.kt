package ca.gbc.petcareapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.session.SessionManager
import ca.gbc.petcareapp.pets.PetAdapter
import ca.gbc.petcareapp.utils.NotificationBadgeHelper
import ca.gbc.petcareapp.data.BookingRepository
import androidx.fragment.app.activityViewModels
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PetListFragment : Fragment(R.layout.pet_list) {

    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase
    private lateinit var petAdapter: PetAdapter
    private lateinit var bookingRepository: BookingRepository
    private var lastPetsCount: Int = -1
    
    // ViewModels for notifications
    private val notificationsVM: NotificationsViewModel by activityViewModels()
    private val bookingVM: BookingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.get(requireContext())
        sessionManager = SessionManager(requireContext())
        bookingRepository = BookingRepository(requireContext())

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

        // Update header title
        val header = view.findViewById<View>(R.id.header)
        val title = header.findViewById<TextView>(R.id.title)
        title.text = "Pets"

        // Bottom nav buttons
        val homeTab = view.findViewById<ImageButton>(R.id.homeTab)
        val bookTab = view.findViewById<ImageButton>(R.id.bookTab)
        val petsTab = view.findViewById<ImageButton>(R.id.petsTab)

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.bright_orange)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.dark_main)

        // Function to update bottom nav colors
        fun highlightNav(selected: ImageButton) {
            homeTab.imageTintList = ColorStateList.valueOf(if (selected == homeTab) selectedColor else unselectedColor)
            bookTab.imageTintList = ColorStateList.valueOf(if (selected == bookTab) selectedColor else unselectedColor)
            petsTab.imageTintList = ColorStateList.valueOf(if (selected == petsTab) selectedColor else unselectedColor)
        }

        // Set initial selection
        highlightNav(petsTab)

        // Bottom navigation click listeners
        petsTab.setOnClickListener { highlightNav(petsTab) }

        bookTab.setOnClickListener {
            findNavController().navigate(R.id.bookListFragment)
            highlightNav(bookTab)
        }

        homeTab.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
            highlightNav(homeTab)
        }

        val addPetCard = view.findViewById<MaterialCardView>(R.id.button_AddPet)
        addPetCard.setOnClickListener {
            findNavController().navigate(R.id.addPetTypeFragment)
        }

        view.findViewById<View>(R.id.settingsBtn)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        view.findViewById<View>(R.id.notisBtn)?.setOnClickListener {
            findNavController().navigate(R.id.notisFragment)
        }
        
        // Update notification badge
        NotificationBadgeHelper.updateBadge(this, header, notificationsVM, bookingVM, bookingRepository)
    }

    override fun onResume() {
        super.onResume()
        // Only refresh if data might have changed
        lifecycleScope.launch {
            val session = withContext(Dispatchers.IO) {
                sessionManager.sessionFlow.first()
            }
            val userId = session.userId
            val currentPetsCount = withContext(Dispatchers.IO) {
                db.petDao().getPetsForUser(userId).size
            }
            if (currentPetsCount != lastPetsCount) {
                loadUserPets()
            }
        }
    }

    private fun loadUserPets() {
        lifecycleScope.launch {
            val session = withContext(Dispatchers.IO) {
                sessionManager.sessionFlow.first()
            }
            val userId = session.userId
            val pets = withContext(Dispatchers.IO) {
                db.petDao().getPetsForUser(userId)
            }
            lastPetsCount = pets.size
            petAdapter.updatePets(pets)
        }
    }
}
