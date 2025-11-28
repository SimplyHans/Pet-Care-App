package ca.gbc.petcareapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.session.SessionManager
import ca.gbc.petcareapp.data.BookingRepository
import ca.gbc.petcareapp.data.Booking as DbBooking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment(R.layout.home) {

    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase
    private lateinit var bookingRepository: BookingRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize session manager and database
        sessionManager = SessionManager(requireContext())
        db = AppDatabase.get(requireContext())
        bookingRepository = BookingRepository(requireContext())

        // Update header title
        val header = view.findViewById<View>(R.id.header)
        val title = header.findViewById<TextView>(R.id.title)
        title.text = "Home"

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
        highlightNav(homeTab)

        // Bottom navigation click listeners
        homeTab.setOnClickListener { highlightNav(homeTab) }

        bookTab.setOnClickListener {
            findNavController().navigate(R.id.bookListFragment)
            highlightNav(bookTab)
        }

        petsTab.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
            highlightNav(petsTab)
        }

        // Other buttons
        view.findViewById<View>(R.id.settingsBtn)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        view.findViewById<View>(R.id.notisBtn)?.setOnClickListener {
            findNavController().navigate(R.id.notisFragment)
        }

        // Add pet button click handler
        view.findViewById<View>(R.id.addPetButton)?.setOnClickListener {
            findNavController().navigate(R.id.addPetTypeFragment)
        }

        // Quick action cards click handlers
        val quickActionsContainer = view.findViewById<LinearLayout>(R.id.quickActionsContainer)
        // Book card - navigate to book list (first card in first row)
        val firstRow = quickActionsContainer.getChildAt(0) as? ViewGroup
        val bookCard = firstRow?.getChildAt(0) as? View
        bookCard?.setOnClickListener {
            findNavController().navigate(R.id.bookListFragment)
            highlightNav(bookTab)
        }
        // Add Pet card - navigate to add pet (second card in first row)
        val addPetCard = firstRow?.getChildAt(1) as? View
        addPetCard?.setOnClickListener {
            findNavController().navigate(R.id.addPetTypeFragment)
        }
        // View Pets card - navigate to pet list (first card in second row)
        val secondRow = quickActionsContainer.getChildAt(1) as? ViewGroup
        val viewPetsCard = secondRow?.getChildAt(0) as? View
        viewPetsCard?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
            highlightNav(petsTab)
        }
        // Staff card - navigate to booking flow (second card in second row)
        val staffCard = secondRow?.getChildAt(1) as? View
        staffCard?.setOnClickListener {
            findNavController().navigate(R.id.bkServiceTypeFragment)
            highlightNav(bookTab)
        }

        // Load pets and bookings
        loadPets(view)
        loadBookings(view)
        
        updateProfileName(view)
    }

    private fun loadPets(view: View) {
        lifecycleScope.launch {
            val session = sessionManager.sessionFlow.first()
            val userId = session.userId
            val pets = db.petDao().getPetsForUser(userId)
            
            val petsContainer = view.findViewById<LinearLayout>(R.id.petsContainer)
            // Remove all pet views except the "Add" button
            val addButton = view.findViewById<View>(R.id.addPetButton)
            petsContainer.removeAllViews()
            if (addButton != null) {
                petsContainer.addView(addButton)
            }
            
            // Add pet views
            pets.forEach { pet ->
                val petView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_pet_home, petsContainer, false)
                
                val petButton = petView.findViewById<Button>(R.id.petButton)
                val petName = petView.findViewById<TextView>(R.id.petName)
                
                petName.text = pet.petName
                
                // Set different colors for pets
                val colorResIds = listOf(
                    R.color.pet_color_1,
                    R.color.pet_color_2,
                    R.color.pet_color_3,
                    R.color.pet_color_4
                )
                val colorStateList = android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(colorResIds[pets.indexOf(pet) % 4])
                )
                petButton.backgroundTintList = colorStateList
                
                petView.setOnClickListener {
                    val bundle = Bundle().apply {
                        putLong("petId", pet.id)
                        putString("petName", pet.petName)
                        putString("breed", pet.breed)
                        putInt("age", pet.age)
                        putString("description", pet.desc ?: "")
                    }
                    findNavController().navigate(R.id.petProfileFragment, bundle)
                }
                
                petsContainer.addView(petView)
            }
        }
    }

    private fun loadBookings(view: View) {
        lifecycleScope.launch {
            val bookings = bookingRepository.getAll().first()
            
            val appointmentsContainer = view.findViewById<LinearLayout>(R.id.appointmentsContainer)
            appointmentsContainer.removeAllViews()
            
            // Show upcoming bookings (filter by future dates)
            val now = Instant.now()
            val upcomingBookings = bookings
                .filter { it.startTimeUtc.isAfter(now) }
                .sortedBy { it.startTimeUtc }
                .take(5) // Show up to 5 upcoming bookings
            
            if (upcomingBookings.isEmpty()) {
                val emptyText = TextView(requireContext()).apply {
                    text = "No upcoming appointments"
                    textSize = 14f
                    setPadding(32, 16, 32, 16)
                }
                appointmentsContainer.addView(emptyText)
            } else {
                upcomingBookings.forEach { booking ->
                    val cardView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.appointment_card, appointmentsContainer, false)
                    
                    val titleText = cardView.findViewById<TextView>(R.id.notisTitle)
                    val timeText = cardView.findViewById<TextView>(R.id.time)
                    val dayText = cardView.findViewById<TextView>(R.id.day)
                    val serviceTypeBtn = cardView.findViewById<Button>(R.id.tagType)
                    
                    titleText.text = booking.caregiverName
                    
                    // Format time and date
                    val instant = booking.startTimeUtc
                    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
                    val localDate = zonedDateTime.toLocalDate()
                    val localTime = zonedDateTime.toLocalTime()
                    
                    timeText.text = localTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
                    dayText.text = localDate.format(DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault()))
                    serviceTypeBtn.text = booking.serviceType
                    
                    // Set layout params for horizontal scrolling
                    val dp302 = (302 * resources.displayMetrics.density).toInt()
                    val dp12 = (12 * resources.displayMetrics.density).toInt()
                    val layoutParams = LinearLayout.LayoutParams(
                        dp302,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = dp12
                    }
                    cardView.layoutParams = layoutParams
                    
                    appointmentsContainer.addView(cardView)
                }
            }
        }
    }

    private fun updateProfileName(view: View) {
        val profileNameTextView = view.findViewById<TextView>(R.id.name_text)

        // Observe session state and update profile name
        lifecycleScope.launch {
            sessionManager.sessionFlow.collect { sessionState ->
                if (sessionState.isLoggedIn && sessionState.fullName.isNotEmpty()) {
                    profileNameTextView.text = sessionState.fullName
                } else {
                    profileNameTextView.text = "Guest User"
                }
            }
        }
    }
}
