package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

class BkDateTimeFragment : Fragment(R.layout.bk_fragment_datetime) {

    private val bookingVM: BookingViewModel by activityViewModels()
    private val notificationsVM: NotificationsViewModel by activityViewModels()
    private var selDate: LocalDate? = null
    private var selTime: LocalTime? = null
    
    // Cache these to avoid repeated calculations
    private val startOfTodayMillis: Long by lazy {
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    private val dateConstraints: CalendarConstraints by lazy {
        CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(startOfTodayMillis))
            .build()
    }
    
    // Date and time formatters
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    
    // Cache current time to avoid repeated calls
    private val currentTime: LocalTime by lazy { LocalTime.now() }
    private val currentDate: LocalDate by lazy { LocalDate.now() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ---- Booking logic ----
        val dayInput  = view.findViewById<EditText>(R.id.bk_input_day)
        val timeInput = view.findViewById<EditText>(R.id.bk_input_time)
        val btnContinue = view.findViewById<MaterialButton>(R.id.bk_btn_continue)

        // Make EditTexts non-focusable to prevent keyboard from showing
        dayInput.isFocusable = false
        dayInput.isClickable = true
        timeInput.isFocusable = false
        timeInput.isClickable = true

        // Restore previously chosen values if user goes back
        selDate = bookingVM.booking.value.date
        selTime = bookingVM.booking.value.time
        updateDateDisplay(dayInput, selDate)
        updateTimeDisplay(timeInput, selTime)
        btnContinue.isEnabled = selDate != null && selTime != null

        // --- Date Picker ---
        dayInput.setOnClickListener {
            showDatePicker(dayInput, btnContinue)
        }

        // --- Time Picker ---
        timeInput.setOnClickListener {
            showTimePicker(timeInput, btnContinue)
        }

        // --- Continue Button ---
        btnContinue.setOnClickListener {
            val booking = bookingVM.booking.value
            if (booking.isComplete) {
                // Finalize the booking (saves to database, schedules notification)
                bookingVM.finalizeBooking(requireContext(), notificationsVM)
                
                // Navigate to Home
                findNavController().navigate(R.id.homeFragment)
                
                // Reset the booking for next appointment
                bookingVM.reset()
            }
        }
    }

    private fun showDatePicker(dayInput: EditText, btnContinue: MaterialButton) {
        // Create picker with cached constraints for better performance
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setCalendarConstraints(dateConstraints)
            .setSelection(selDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: startOfTodayMillis)
            .build()
        
        datePicker.addOnPositiveButtonClickListener { millis ->
            val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            selDate = picked
            bookingVM.setDate(picked)
            updateDateDisplay(dayInput, picked)
            btnContinue.isEnabled = selDate != null && selTime != null
        }

        datePicker.show(parentFragmentManager, "datePicker")
    }

    private fun showTimePicker(timeInput: EditText, btnContinue: MaterialButton) {
        val initialHour = selTime?.hour ?: currentTime.hour
        val initialMinute = selTime?.minute ?: currentTime.minute
        
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(initialHour)
            .setMinute(initialMinute)
            .setTitleText("Select time")
            .build()
        
        timePicker.addOnPositiveButtonClickListener {
            val t = LocalTime.of(timePicker.hour, timePicker.minute)

            // prevent selecting a past time if it's today
            if (selDate == currentDate && t.isBefore(currentTime)) {
                timeInput.error = "Please choose a future time"
                return@addOnPositiveButtonClickListener
            }

            timeInput.error = null
            selTime = t
            bookingVM.setTime(t)
            updateTimeDisplay(timeInput, t)
            btnContinue.isEnabled = selDate != null && selTime != null
        }

        timePicker.show(parentFragmentManager, "timePicker")
    }

    private fun updateDateDisplay(dayInput: EditText, date: LocalDate?) {
        dayInput.setText(date?.format(dateFormatter) ?: "")
    }

    private fun updateTimeDisplay(timeInput: EditText, time: LocalTime?) {
        timeInput.setText(time?.format(timeFormatter) ?: "")
    }
}
