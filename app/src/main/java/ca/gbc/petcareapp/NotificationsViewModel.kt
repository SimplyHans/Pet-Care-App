package ca.gbc.petcareapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

data class NotificationItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val type: NotificationType = NotificationType.BOOKING_CONFIRMED
)

enum class NotificationType {
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    REMINDER,
    GENERAL
}

class NotificationsViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> get() = _notifications

    fun addBookingNotification(booking: Booking) {
        val notification = NotificationItem(
            title = "Appointment Booked",
            message = createBookingMessage(booking),
            type = NotificationType.BOOKING_CONFIRMED
        )
        // Add to the beginning of the list (newest first)
        _notifications.value = listOf(notification) + _notifications.value
    }

    fun addNotification(title: String, message: String, type: NotificationType = NotificationType.GENERAL) {
        val notification = NotificationItem(
            title = title,
            message = message,
            type = type
        )
        _notifications.value = listOf(notification) + _notifications.value
    }

    fun removeNotification(notificationId: String) {
        _notifications.value = _notifications.value.filter { it.id != notificationId }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    private fun createBookingMessage(booking: Booking): String {
        val caregiver = booking.caregiverId ?: "Your caregiver"
        val serviceType = booking.serviceType?.name?.replace("_", " ")?.lowercase() ?: "a service"
        val date = booking.date?.toString() ?: "a selected date"
        val time = booking.time?.toString() ?: "a selected time"
        
        return "$caregiver has accepted your $serviceType appointment for $time on $date."
    }
}
