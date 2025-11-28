package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.Button
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

class BkDateTimeFragment : Fragment(R.layout.bk_fragment_datetime) {

    private val bookingVM: BookingViewModel by activityViewModels()
    private var selDate: LocalDate? = null
    private var selTime: LocalTime? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // ---- Booking logic ----
        val dayInput  = view.findViewById<EditText>(R.id.bk_input_day)
        val timeInput = view.findViewById<EditText>(R.id.bk_input_time)
        val btnContinue = view.findViewById<MaterialButton>(R.id.bk_btn_continue)

        // Restore previously chosen values if user goes back
        selDate = bookingVM.booking.value.date
        selTime = bookingVM.booking.value.time
        dayInput.setText(selDate?.toString() ?: "")
        timeInput.setText(selTime?.toString() ?: "")
        btnContinue.isEnabled = selDate != null && selTime != null

        // --- Date Picker ---
        dayInput.setOnClickListener {
            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(startOfTodayMillis()))
                .build()

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setCalendarConstraints(constraints)
                .setSelection(startOfTodayMillis())
                .build()

            datePicker.addOnPositiveButtonClickListener { millis ->
                val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                selDate = picked
                bookingVM.setDate(picked)
                dayInput.setText(picked.toString())
                btnContinue.isEnabled = selDate != null && selTime != null
            }

            datePicker.show(parentFragmentManager, "datePicker")
        }

        // --- Time Picker ---
        timeInput.setOnClickListener {
            val now = LocalTime.now()
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(now.hour)
                .setMinute(now.minute)
                .setTitleText("Select time")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val t = LocalTime.of(timePicker.hour, timePicker.minute)

                // prevent selecting a past time if it's today
                if (selDate == LocalDate.now() && t.isBefore(LocalTime.now())) {
                    timeInput.error = "Please choose a future time"
                    return@addOnPositiveButtonClickListener
                }

                selTime = t
                bookingVM.setTime(t)
                timeInput.setText(t.toString())
                btnContinue.isEnabled = selDate != null && selTime != null
            }

            timePicker.show(parentFragmentManager, "timePicker")
        }

        // --- Continue Button ---
        btnContinue.setOnClickListener {
            val booking = bookingVM.booking.value
            if (booking.isComplete) {
                // Finalize the booking (saves to database, schedules notification)
                bookingVM.finalizeBooking(requireContext())
                
                // Navigate to Home
                findNavController().navigate(R.id.homeFragment)
                
                // Reset the booking for next appointment
                bookingVM.reset()
            }
        }

    }

    private fun startOfTodayMillis(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
