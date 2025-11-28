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
import ca.gbc.petcareapp.utils.NotificationBadgeHelper
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment(R.layout.home) {

    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase
    private lateinit var bookingRepository: BookingRepository
    
    // ViewModels for notifications
    private val notificationsVM: NotificationsViewModel by activityViewModels()
    private val bookingVM: BookingViewModel by activityViewModels()
    
    // Cache views to avoid repeated findViewById calls
    private var petsContainer: LinearLayout? = null
    private var appointmentsContainer: LinearLayout? = null
    private var profileNameTextView: TextView? = null
    private var lastPetsCount: Int = -1

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
        // Staff card - navigate to staff list (second card in second row)
        val staffCard = secondRow?.getChildAt(1) as? View
        staffCard?.setOnClickListener {
            findNavController().navigate(R.id.staffListFragment)
            highlightNav(bookTab)
        }

        // Cache views
        petsContainer = view.findViewById<LinearLayout>(R.id.petsContainer)
        appointmentsContainer = view.findViewById<LinearLayout>(R.id.appointmentsContainer)
        profileNameTextView = view.findViewById<TextView>(R.id.name_text)
        
        // Load pets and bookings
        loadPets(view)
        loadBookings(view)
        
        updateProfileName(view)
        
        // Update notification badge (reuse header from above)
        NotificationBadgeHelper.updateBadge(this, header, notificationsVM, bookingVM, bookingRepository)
    }

    override fun onResume() {
        super.onResume()
        // Only refresh if view is available and data might have changed
        view?.let { 
            // Check if we need to reload (only if coming back from add pet flow)
            if (lastPetsCount >= 0) {
                lifecycleScope.launch {
                    val session = sessionManager.sessionFlow.first()
                    val userId = session.userId
                    val currentPetsCount = withContext(Dispatchers.IO) {
                        db.petDao().getPetsForUser(userId).size
                    }
                    if (currentPetsCount != lastPetsCount) {
                        loadPets(it)
                    }
                }
            }
        }
    }

    private fun loadPets(view: View) {
        lifecycleScope.launch {
            val session = withContext(Dispatchers.IO) {
                sessionManager.sessionFlow.first()
            }
            val userId = session.userId
            val pets = withContext(Dispatchers.IO) {
                db.petDao().getPetsForUser(userId)
            }
            
            lastPetsCount = pets.size
            
            val container = petsContainer ?: view.findViewById<LinearLayout>(R.id.petsContainer)
            val addButton = view.findViewById<View>(R.id.addPetButton)
            
            // Remove all pet views except the "Add" button
            container.removeAllViews()
            if (addButton != null) {
                container.addView(addButton)
            }
            
            // Pre-cache colors to avoid repeated getColor calls
            val colorResIds = listOf(
                R.color.pet_color_1,
                R.color.pet_color_2,
                R.color.pet_color_3,
                R.color.pet_color_4
            )
            val colors = colorResIds.map { 
                ContextCompat.getColor(requireContext(), it) 
            }
            
            // Add pet views
            pets.forEachIndexed { index, pet ->
                val petView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_pet_home, container, false)
                
                val petButton = petView.findViewById<Button>(R.id.petButton)
                val petName = petView.findViewById<TextView>(R.id.petName)
                
                petName.text = pet.petName
                
                // Use cached colors
                val colorStateList = android.content.res.ColorStateList.valueOf(
                    colors[index % 4]
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
                
                container.addView(petView)
            }
        }
    }

    private fun loadBookings(view: View) {
        lifecycleScope.launch {
            val bookings = withContext(Dispatchers.IO) {
                bookingRepository.getAll().first()
            }
            
            val container = appointmentsContainer ?: view.findViewById<LinearLayout>(R.id.appointmentsContainer)
            container.removeAllViews()
            
            // Show upcoming bookings (filter by future dates)
            val now = Instant.now()
            val upcomingBookings = bookings
                .filter { it.startTimeUtc.isAfter(now) }
                .sortedBy { it.startTimeUtc }
                .take(5) // Show up to 5 upcoming bookings
            
            // Pre-create formatters to avoid repeated creation
            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            val dateFormatter = DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault())
            
            // Pre-calculate layout params
            val dp302 = (302 * resources.displayMetrics.density).toInt()
            val dp12 = (12 * resources.displayMetrics.density).toInt()
            
            if (upcomingBookings.isEmpty()) {
                val emptyText = TextView(requireContext()).apply {
                    text = "No upcoming appointments"
                    textSize = 14f
                    setPadding(32, 16, 32, 16)
                }
                container.addView(emptyText)
            } else {
                upcomingBookings.forEach { booking ->
                    val cardView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.appointment_card, container, false)
                    
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
                    
                    timeText.text = localTime.format(timeFormatter)
                    dayText.text = localDate.format(dateFormatter)
                    serviceTypeBtn.text = booking.serviceType
                    
                    // Set layout params for horizontal scrolling
                    val layoutParams = LinearLayout.LayoutParams(
                        dp302,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = dp12
                    }
                    cardView.layoutParams = layoutParams
                    
                    container.addView(cardView)
                }
            }
        }
    }

    private fun updateProfileName(view: View) {
        val textView = profileNameTextView ?: view.findViewById<TextView>(R.id.name_text)
        profileNameTextView = textView

        // Observe session state and update profile name (only once, not continuously)
        lifecycleScope.launch {
            val sessionState = sessionManager.sessionFlow.first()
            if (sessionState.isLoggedIn && sessionState.fullName.isNotEmpty()) {
                textView.text = sessionState.fullName
            } else {
                textView.text = "Guest User"
            }
        }
    }
}
