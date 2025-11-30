package ca.gbc.petcareapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.session.SessionManager
import ca.gbc.petcareapp.data.BookingRepository
import ca.gbc.petcareapp.pets.PetAdapter
import ca.gbc.petcareapp.utils.NotificationBadgeHelper
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
    private lateinit var petAdapter: PetAdapter

    // ViewModels
    private val notificationsVM: NotificationsViewModel by activityViewModels()
    private val bookingVM: BookingViewModel by activityViewModels()

    // Cached containers
    private var appointmentsContainer: LinearLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        db = AppDatabase.get(requireContext())
        bookingRepository = BookingRepository(requireContext())

        // ---------------------- HEADER ----------------------
        val header = view.findViewById<View>(R.id.header)
        val title = header.findViewById<TextView>(R.id.title)
        title.text = "Home"

        // ---------------------- NAV BAR ----------------------
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

        // ---------------------- QUICK ACTION CARDS ----------------------
        view.findViewById<View>(R.id.card_add_pet)?.setOnClickListener {
            findNavController().navigate(R.id.addPetTypeFragment)
        }

        view.findViewById<View>(R.id.card_book)?.setOnClickListener {
            findNavController().navigate(R.id.bkServiceTypeFragment)
        }

        view.findViewById<View>(R.id.card_staff)?.setOnClickListener {
            findNavController().navigate(R.id.staffListFragment)
        }

        // ---------------------- PROFILE NAME ----------------------
        updateProfileName(view)

        // ---------------------- PETS RECYCLER ----------------------
        setupPetRecycler(view)
        loadPetsIntoHome()

        // ---------------------- APPOINTMENTS ----------------------
        appointmentsContainer = view.findViewById(R.id.appointmentsContainer)
        loadBookings()

        // ---------------------- NOTIFICATION BADGE ----------------------
        NotificationBadgeHelper.updateBadge(this, header, notificationsVM, bookingVM, bookingRepository)
    }

    // ---------------------- PROFILE NAME ----------------------
    private fun updateProfileName(view: View) {
        val nameText = view.findViewById<TextView>(R.id.name_text)

        lifecycleScope.launch {
            sessionManager.sessionFlow.collect { session ->
                nameText.text =
                    if (session.isLoggedIn && session.fullName.isNotEmpty())
                        session.fullName
                    else "Guest User"
            }
        }
    }

    // ---------------------- PET RECYCLER ----------------------
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

            val pets = withContext(Dispatchers.IO) {
                db.petDao().getPetsForUser(session.userId)
            }

            petAdapter.updatePets(pets)
        }
    }

    // ---------------------- BOOKINGS ----------------------
    private fun loadBookings() {
        lifecycleScope.launch {
            val container = appointmentsContainer ?: return@launch

            val bookings = withContext(Dispatchers.IO) {
                bookingRepository.getAll().first()
            }

            container.removeAllViews()

            val now = Instant.now()
            val upcoming = bookings
                .filter { it.startTimeUtc.isAfter(now) }
                .sortedBy { it.startTimeUtc }
                .take(5)

            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            val dateFormatter = DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault())

            if (upcoming.isEmpty()) {
                val emptyText = TextView(requireContext()).apply {
                    text = "No upcoming appointments"
                    textSize = 14f
                    setPadding(32, 16, 32, 16)
                }
                container.addView(emptyText)
                return@launch
            }

            upcoming.forEach { booking ->
                val card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.appointment_card, container, false)

                val titleText = card.findViewById<TextView>(R.id.notisTitle)
                val timeText = card.findViewById<TextView>(R.id.time)
                val dayText = card.findViewById<TextView>(R.id.day)
                val serviceTypeBtn = card.findViewById<Button>(R.id.tagType)

                val zoned = booking.startTimeUtc.atZone(ZoneId.systemDefault())
                timeText.text = zoned.toLocalTime().format(timeFormatter)
                dayText.text = zoned.toLocalDate().format(dateFormatter)
                titleText.text = booking.caregiverName
                serviceTypeBtn.text = booking.serviceType

                container.addView(card)
            }
        }
    }
}
