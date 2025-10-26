package ca.gbc.petcareapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime

data class Booking(
    val serviceType: ServiceType? = null,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val caregiverId: String? = null
) {
    val isComplete: Boolean
        get() = serviceType != null && date != null && time != null && caregiverId != null

    fun asLocalDateTime(): LocalDateTime? =
        if (date != null && time != null) LocalDateTime.of(date, time) else null
}

// Service types
enum class ServiceType { WALKING, GROOMING, VET_VISIT, BOARDING }

// ViewModel
class BookingViewModel : ViewModel() {

    // Current booking
    private val _booking = MutableStateFlow(Booking())
    val booking: StateFlow<Booking> get() = _booking

    // Notifications list
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

    fun setCaregiver(id: String) {
        _booking.value = _booking.value.copy(caregiverId = id)
    }

    fun reset() {
        _booking.value = Booking()
    }

    fun finalizeBooking() {
        val b = _booking.value
        if (!b.isComplete) return

        val message = "Appointment booked with ${b.caregiverId} on ${b.date} at ${b.time}"
        _notifications.value = listOf(message) + _notifications.value // newest first
    }
}
