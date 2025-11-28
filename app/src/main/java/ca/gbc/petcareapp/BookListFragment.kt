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
import ca.gbc.petcareapp.data.BookingRepository
import ca.gbc.petcareapp.data.Booking as DbBooking
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class BookListFragment : Fragment(R.layout.bk_list) {

    private lateinit var bookingRepository: BookingRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookingRepository = BookingRepository(requireContext())

        // Update header title
        val header = view.findViewById<View>(R.id.header)
        val title = header.findViewById<TextView>(R.id.title)
        title.text = "Appointments"

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
        highlightNav(bookTab)

        // Bottom navigation click listeners
        bookTab.setOnClickListener { highlightNav(bookTab) }

        petsTab.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
            highlightNav(petsTab)
        }

        homeTab.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
            highlightNav(homeTab)
        }

        val addBook = view.findViewById<MaterialCardView>(R.id.button_BkApoint)
        addBook.setOnClickListener {
            findNavController().navigate(R.id.bkServiceTypeFragment)
        }

        view.findViewById<View>(R.id.settingsBtn)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        view.findViewById<View>(R.id.notisBtn)?.setOnClickListener {
            findNavController().navigate(R.id.notisFragment)
        }

        // Load and display bookings
        loadBookings(view)
    }

    private fun loadBookings(view: View) {
        lifecycleScope.launch {
            val bookings = bookingRepository.getAll().first()
            
            val bookingsContainer = view.findViewById<LinearLayout>(R.id.bookingsContainer)
            bookingsContainer.removeAllViews()
            
            // Sort bookings by date (newest first)
            val sortedBookings = bookings.sortedByDescending { it.startTimeUtc }
            
            if (sortedBookings.isEmpty()) {
                val emptyText = TextView(requireContext()).apply {
                    text = "No appointments yet.\nTap + to book an appointment."
                    textSize = 16f
                    setPadding(32, 64, 32, 64)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                }
                bookingsContainer.addView(emptyText)
            } else {
                sortedBookings.forEach { booking ->
                    val cardView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.appointment_card, bookingsContainer, false)
                    
                    val titleText = cardView.findViewById<TextView>(R.id.notisTitle)
                    val timeText = cardView.findViewById<TextView>(R.id.time)
                    val dayText = cardView.findViewById<TextView>(R.id.day)
                    val serviceTypeBtn = cardView.findViewById<Button>(R.id.tagType)
                    val descText = cardView.findViewById<TextView>(R.id.desc)
                    
                    titleText.text = booking.caregiverName
                    
                    // Format time and date
                    val instant = booking.startTimeUtc
                    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
                    val localDate = zonedDateTime.toLocalDate()
                    val localTime = zonedDateTime.toLocalTime()
                    
                    timeText.text = localTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
                    dayText.text = localDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault()))
                    serviceTypeBtn.text = booking.serviceType
                    
                    // Set description
                    val isPast = instant.isBefore(Instant.now())
                    descText.text = if (isPast) "Completed" else "Upcoming"
                    
                    // Set layout params
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 16)
                    }
                    cardView.layoutParams = layoutParams
                    
                    bookingsContainer.addView(cardView)
                }
            }
        }
    }
}
