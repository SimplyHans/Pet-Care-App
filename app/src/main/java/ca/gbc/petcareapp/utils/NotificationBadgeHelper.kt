package ca.gbc.petcareapp.utils

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ca.gbc.petcareapp.NotificationsViewModel
import ca.gbc.petcareapp.BookingViewModel
import ca.gbc.petcareapp.R
import ca.gbc.petcareapp.data.BookingRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

object NotificationBadgeHelper {
    
    /**
     * Updates the notification badge in the header based on notification count
     */
    fun updateBadge(
        fragment: Fragment,
        headerView: View?,
        notificationsVM: NotificationsViewModel,
        bookingVM: BookingViewModel,
        bookingRepository: BookingRepository
    ) {
        val badge = headerView?.findViewById<View>(R.id.notisBadge)
        val counter = headerView?.findViewById<TextView>(R.id.notisCounter)
        
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            combine(
                notificationsVM.notifications,
                bookingVM.notifications,
                bookingRepository.getAll()
            ) { vmNotifs, sessionMsgs, bookings ->
                // Count notifications
                var count = vmNotifs.size + sessionMsgs.size
                
                // Add count for upcoming bookings (within 7 days)
                val now = Instant.now()
                val upcomingBookings = bookings.filter { booking ->
                    booking.startTimeUtc.isAfter(now) && 
                    ChronoUnit.DAYS.between(now, booking.startTimeUtc) <= 7
                }
                count += upcomingBookings.size
                
                count
            }.collect { count ->
                if (count > 0) {
                    badge?.visibility = if (count <= 9) View.VISIBLE else View.GONE
                    counter?.visibility = if (count > 9) View.VISIBLE else View.GONE
                    if (count > 9) {
                        counter?.text = if (count > 99) "99+" else count.toString()
                    }
                } else {
                    badge?.visibility = View.GONE
                    counter?.visibility = View.GONE
                }
            }
        }
    }
}

