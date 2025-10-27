package ca.gbc.petcareapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Instant
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import ca.gbc.petcareapp.notifications.BookingReminderWorker

// NEW: Room repository + entity (aliased to avoid name clash)
import ca.gbc.petcareapp.data.BookingRepository
import ca.gbc.petcareapp.data.Booking as DbBooking

// UI model for current draft booking
data class Booking(
    val serviceType: ServiceType? = null,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val caregiverId: String? = null,
    val caregiverName: String? = null // optional if you have it
) {
    val isComplete: Boolean
        get() = serviceType != null && date != null && time != null && caregiverId != null

    fun asLocalDateTime(): LocalDateTime? =
        if (date != null && time != null) LocalDateTime.of(date, time) else null
}

enum class ServiceType { WALKING, GROOMING, VET_VISIT, BOARDING }

class BookingViewModel : ViewModel() {

    // Current booking selections
    private val _booking = MutableStateFlow(Booking())
    val booking: StateFlow<Booking> get() = _booking

    // Simple in-app notification feed (strings)
    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications: StateFlow<List<String>> get() = _notifications.asStateFlow()

    fun setServiceType(type: ServiceType) {
        _booking.value = _booking.value.copy(serviceType = type)
    }

    fun setDate(date: LocalDate) {
        _booking.value = _booking.value.copy(date = date)
    }

    fun setTime(time: LocalTime) {
        _booking.value = _booking.value.copy(time = time)
    }

    fun setCaregiver(id: String, name: String? = null) {
        _booking.value = _booking.value.copy(caregiverId = id, caregiverName = name ?: id)
    }

    fun reset() {
        _booking.value = Booking()
    }

    /**
     * Finalizes the booking:
     * 1) Adds an item to the in-app notifications list
     * 2) Schedules a system notification via WorkManager for the chosen date/time
     * 3) Persists the booking to Room via BookingRepository
     *
     * Pass an Activity/Fragment context (e.g., requireContext()).
     */
    fun finalizeBooking(context: Context) {
        val b = _booking.value
        if (!b.isComplete) return

        val whenLdt = b.asLocalDateTime() ?: return
        val startInstant: Instant = whenLdt.atZone(ZoneId.systemDefault()).toInstant()
        val whenMillis = startInstant.toEpochMilli()

        // 1) Update in-app feed immediately
        val message = "Appointment booked with ${b.caregiverId} on ${b.date} at ${b.time} (${b.serviceType})"
        _notifications.value = listOf(message) + _notifications.value

        // 2) Schedule the system notification
        val title = "Booking: ${b.serviceType}"
        val text  = "Caregiver ${b.caregiverName ?: b.caregiverId} at ${b.time} on ${b.date}"
        scheduleNotification(context, title, text, whenMillis)

        // 3) Persist to Room (Repository)
        viewModelScope.launch {
            val repo = BookingRepository(context)
            val entity = DbBooking(
                caregiverId   = b.caregiverId!!,
                caregiverName = b.caregiverName ?: b.caregiverId!!,
                serviceType   = b.serviceType!!.name,   // store as String
                notes         = null,                   // hook up if you add notes
                startTimeUtc  = startInstant           // Instant (Converters handle it)
                // createdAtUtc uses default in entity
            )
            repo.save(entity)
        }

        // Optional: reset()
    }

    private fun scheduleNotification(
        context: Context,
        title: String,
        text: String,
        triggerAtMillis: Long
    ) {
        val delayMs = (triggerAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)

        val data = Data.Builder()
            .putString("title", title)
            .putString("text", text)
            .build()

        val req = OneTimeWorkRequestBuilder<BookingReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueue(req)
    }
}
