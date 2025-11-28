package ca.gbc.petcareapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.data.BookingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class NotisFragment : Fragment(R.layout.notifications) {

    // Both viewmodels are used
    private val notificationsVM: NotificationsViewModel by activityViewModels()
    private val bookingVM: BookingViewModel by activityViewModels()
    
    // Database and repository
    private lateinit var bookingRepository: BookingRepository
    private lateinit var db: AppDatabase
    
    // Cache views and inflater
    private var container: LinearLayout? = null
    private var inflater: LayoutInflater? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database
        db = AppDatabase.get(requireContext())
        bookingRepository = BookingRepository(requireContext())

        setupNavbar(view)

        container = view.findViewById<LinearLayout>(R.id.notisContainer)
        inflater = LayoutInflater.from(requireContext())

        // Update header title
        val header = view.findViewById<View>(R.id.header)
        val title = header?.findViewById<TextView>(R.id.title)
        title?.text = "Notifications"

        // Load and display notifications including booking reminders
        loadNotifications()
        
        // Update notification badge (should be hidden when viewing notifications)
        val badge = header?.findViewById<View>(R.id.notisBadge)
        val counter = header?.findViewById<TextView>(R.id.notisCounter)
        badge?.visibility = View.GONE
        counter?.visibility = View.GONE
    }
    
    private fun loadNotifications() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    notificationsVM.notifications,
                    bookingVM.notifications,
                    bookingRepository.getAll()
                ) { vmNotifs, sessionMsgs, bookings ->
                    // Generate booking reminders for ALL upcoming bookings
                    val bookingReminders = generateBookingReminders(bookings)
                    
                    // Convert booking messages to the same shape used by notificationsVM
                    val sessionAsItems = sessionMsgs.map { msg ->
                        SimpleItem(title = "Appointment Booked", message = msg)
                    }
                    val vmItems = vmNotifs.map { it -> SimpleItem(title = it.title, message = it.message) }

                    // Combine all notifications: booking reminders first (most urgent), then session items, then VM items
                    var allItems = bookingReminders + sessionAsItems + vmItems
                    
                    // If we have bookings but no reminders (shouldn't happen, but just in case), add confirmations
                    if (bookings.isNotEmpty() && bookingReminders.isEmpty() && allItems.isEmpty()) {
                        // Add confirmation notifications for all bookings
                        bookings.filter { it.startTimeUtc.isAfter(Instant.now()) }.forEach { booking ->
                            val localDateTime = booking.startTimeUtc.atZone(ZoneId.systemDefault())
                            val bookingDate = localDateTime.toLocalDate()
                            val bookingTime = localDateTime.toLocalTime()
                            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
                            val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                            val timeStr = bookingTime.format(timeFormatter)
                            val dateStr = bookingDate.format(dateFormatter)
                            val serviceType = booking.serviceType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                            
                            allItems = allItems + SimpleItem(
                                title = "Appointment Confirmed",
                                message = "Your $serviceType appointment with ${booking.caregiverName} is scheduled for $dateStr at $timeStr."
                            )
                        }
                    }
                    
                    allItems
                }.collectLatest { items ->
                    val containerView = container ?: return@collectLatest
                    val inflaterView = inflater ?: return@collectLatest
                    
                    containerView.removeAllViews()

                    if (items.isEmpty()) {
                        val emptyMsg = TextView(requireContext()).apply {
                            text = "No notifications yet."
                            textSize = 16f
                            setPadding(32, 64, 32, 64)
                            setTextColor(
                                ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
                            )
                        }
                        containerView.addView(emptyMsg)
                    } else {
                        items.forEach { item ->
                            val card = inflaterView.inflate(R.layout.notis_reg_card, containerView, false)
                            card.findViewById<TextView>(R.id.notisTitle)?.text = item.title
                            card.findViewById<TextView>(R.id.desc)?.text = item.message
                            containerView.addView(card)
                        }
                    }
                }
            }
        }
    }
    
    private suspend fun generateBookingReminders(bookings: List<ca.gbc.petcareapp.data.Booking>): List<SimpleItem> {
        return withContext(Dispatchers.IO) {
            val now = Instant.now()
            val reminders = mutableListOf<SimpleItem>()
            
            // Get all future bookings
            val futureBookings = bookings.filter { it.startTimeUtc.isAfter(now) }
            
            futureBookings
                .sortedBy { it.startTimeUtc } // Sort by date (soonest first)
                .forEach { booking ->
                    val hoursUntil = ChronoUnit.HOURS.between(now, booking.startTimeUtc)
                    val daysUntil = ChronoUnit.DAYS.between(now, booking.startTimeUtc)
                    
                    val localDateTime = booking.startTimeUtc.atZone(ZoneId.systemDefault())
                    val bookingDate = localDateTime.toLocalDate()
                    val bookingTime = localDateTime.toLocalTime()
                    val today = LocalDate.now(ZoneId.systemDefault())
                    val tomorrow = today.plusDays(1)
                    
                    // Format time in 12-hour format with AM/PM
                    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
                    val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                    val timeStr = bookingTime.format(timeFormatter)
                    val dateStr = bookingDate.format(dateFormatter)
                    val serviceType = booking.serviceType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                    
                    // Always add a reminder for every upcoming booking
                    val reminder = when {
                        hoursUntil < 24 && bookingDate == today -> {
                            // Less than 24 hours and today
                            SimpleItem(
                                title = "Appointment Today!",
                                message = "Your $serviceType appointment with ${booking.caregiverName} is today at $timeStr. Don't forget!"
                            )
                        }
                        hoursUntil < 24 && bookingDate == tomorrow -> {
                            // Less than 24 hours but tomorrow
                            SimpleItem(
                                title = "Appointment Tomorrow",
                                message = "Your $serviceType appointment with ${booking.caregiverName} is tomorrow at $timeStr. Get ready!"
                            )
                        }
                        hoursUntil < 48 -> {
                            // Less than 48 hours
                            SimpleItem(
                                title = "Appointment Coming Soon",
                                message = "Your $serviceType appointment with ${booking.caregiverName} is in ${hoursUntil.toInt()} hours ($dateStr at $timeStr)."
                            )
                        }
                        daysUntil <= 7 -> {
                            // Within a week
                            SimpleItem(
                                title = "Upcoming Appointment",
                                message = "You have a $serviceType appointment with ${booking.caregiverName} on $dateStr at $timeStr."
                            )
                        }
                        else -> {
                            // More than a week away - still show it
                            SimpleItem(
                                title = "Upcoming Appointment",
                                message = "You have a $serviceType appointment with ${booking.caregiverName} on $dateStr at $timeStr."
                            )
                        }
                    }
                    reminders.add(reminder)
                }
            
            // Also add confirmation notifications for all bookings (so each booking shows up twice: confirmation + reminder)
            futureBookings.forEach { booking ->
                val localDateTime = booking.startTimeUtc.atZone(ZoneId.systemDefault())
                val bookingDate = localDateTime.toLocalDate()
                val bookingTime = localDateTime.toLocalTime()
                val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
                val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
                val timeStr = bookingTime.format(timeFormatter)
                val dateStr = bookingDate.format(dateFormatter)
                val serviceType = booking.serviceType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                
                reminders.add(
                    SimpleItem(
                        title = "Appointment Confirmed",
                        message = "Your $serviceType appointment with ${booking.caregiverName} has been confirmed for $dateStr at $timeStr."
                    )
                )
            }
            
            reminders
        }
    }

    // Lightweight local model so we don't depend on your NotificationsItem class shape
    private data class SimpleItem(val title: String, val message: String)

    private fun setupNavbar(view: View) {
        // Fix: Use ImageButton instead of Button
        view.findViewById<ImageButton>(R.id.homeTab)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
        view.findViewById<ImageButton>(R.id.bookTab)?.setOnClickListener {
            findNavController().navigate(R.id.bookListFragment)
        }
        view.findViewById<ImageButton>(R.id.petsTab)?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }
        view.findViewById<View>(R.id.settingsBtn)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
        view.findViewById<View>(R.id.notisBtn)?.setOnClickListener {
            findNavController().navigate(R.id.notisFragment)
        }
    }
}
