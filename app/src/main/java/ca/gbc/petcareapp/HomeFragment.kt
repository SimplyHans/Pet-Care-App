package ca.gbc.petcareapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment(R.layout.home) {

    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var bookingRepository: BookingRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.get(requireContext())
        sessionManager = SessionManager(requireContext())
        bookingRepository = BookingRepository(requireContext())

        view.findViewById<Button>(R.id.homeTab)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        view.findViewById<Button>(R.id.bookTab)?.setOnClickListener {
            findNavController().navigate(R.id.bookCaregiverPickerFragment)
        }

        view.findViewById<Button>(R.id.petsTab)?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }

        view.findViewById<Button>(R.id.viewPetsBtn)?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }

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

        // Load pets and bookings
        loadPets(view)
        loadBookings(view)
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
}
