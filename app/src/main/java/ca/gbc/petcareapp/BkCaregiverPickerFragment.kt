package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class BkCaregiverPickerFragment : Fragment(R.layout.bk_fragment_caregiver_picker) {

    private val bookingVM: BookingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ---- Header / Navbar ----
        view.findViewById<Button>(R.id.homeTab)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
        view.findViewById<Button>(R.id.bookTab)?.setOnClickListener {
            findNavController().navigate(R.id.bkServiceTypeFragment)
        }
        view.findViewById<Button>(R.id.petsTab)?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }
        view.findViewById<View>(R.id.settingsBtn)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
        view.findViewById<View>(R.id.notisBtn)?.setOnClickListener {
            findNavController().navigate(R.id.notisFragment)
        }

        // ---- Caregiver choices (3 fixed rows in your XML) ----
        val row1 = view.findViewById<View>(R.id.bk_item1)
        val row2 = view.findViewById<View>(R.id.bk_item2)
        val row3 = view.findViewById<View>(R.id.bk_item3)
        val rows = listOf(row1, row2, row3)

        val continueBtn = view.findViewById<MaterialButton>(R.id.bk_btn_continue)

        // simple visual highlight without new drawables
        fun markSelected(selected: View?) {
            rows.forEach { row ->
                val isSelected = row === selected
                row.isSelected = isSelected
                // subtle emphasis
                row.alpha = if (isSelected) 1f else 0.85f
                row.scaleX = if (isSelected) 1.02f else 1.0f
                row.scaleY = if (isSelected) 1.02f else 1.0f
                row.elevation = if (isSelected) 6f else 0f
            }
        }

        // restore prior selection if user navigates back
        when (bookingVM.booking.value.caregiverId) {
            "cg_1" -> markSelected(row1)
            "cg_2" -> markSelected(row2)
            "cg_3" -> markSelected(row3)
            else   -> markSelected(null)
        }
        continueBtn.isEnabled = bookingVM.booking.value.caregiverId != null

        row1.setOnClickListener {
            bookingVM.setCaregiver("cg_1")
            markSelected(row1)
            continueBtn.isEnabled = true
        }
        row2.setOnClickListener {
            bookingVM.setCaregiver("cg_2")
            markSelected(row2)
            continueBtn.isEnabled = true
        }
        row3.setOnClickListener {
            bookingVM.setCaregiver("cg_3")
            markSelected(row3)
            continueBtn.isEnabled = true
        }

        // replace ONLY this click listener
        continueBtn.setOnClickListener {
            // if you navigate by destination id:
            findNavController().navigate(R.id.bkDateTimeFragment)}
    }
}
