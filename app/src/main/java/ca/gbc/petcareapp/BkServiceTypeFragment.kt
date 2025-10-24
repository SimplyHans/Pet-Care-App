package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class BkServiceTypeFragment : Fragment(R.layout.bk_fragment_service_type) {

    // Shared VM across booking steps
    private val bookingVM: BookingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ----- Header / navbar you already had -----
        view.findViewById<Button>(R.id.homeTab)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
        view.findViewById<Button>(R.id.bookTab)?.setOnClickListener {
            // If you want Book tab to land on this screen, leave it as is.
            // Or jump straight to caregiver if that's your flow:
            findNavController().navigate(R.id.bookCaregiverPickerFragment)
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

        // ----- Booking UI wiring -----
        val btnContinue = view.findViewById<MaterialButton>(R.id.bk_btn_continue)

        val walking  = view.findViewById<MaterialButton>(R.id.bk_service_walking)
        val grooming = view.findViewById<MaterialButton>(R.id.bk_service_grooming)
        val vet      = view.findViewById<MaterialButton>(R.id.bk_service_vet)
        val boarding = view.findViewById<MaterialButton>(R.id.bk_service_boarding)

        fun select(type: ServiceType) {
            bookingVM.setServiceType(type)
            // simple visual selection if your MaterialButtons are checkable
            listOf(walking, grooming, vet, boarding).forEach { it?.isChecked = false }
            when (type) {
                ServiceType.WALKING   -> walking?.isChecked = true
                ServiceType.GROOMING  -> grooming?.isChecked = true
                ServiceType.VET_VISIT -> vet?.isChecked = true
                ServiceType.BOARDING  -> boarding?.isChecked = true
            }
            btnContinue?.isEnabled = true
        }

        walking?.setOnClickListener  { select(ServiceType.WALKING) }
        grooming?.setOnClickListener { select(ServiceType.GROOMING) }
        vet?.setOnClickListener      { select(ServiceType.VET_VISIT) }
        boarding?.setOnClickListener { select(ServiceType.BOARDING) }

        // Rehydrate selection if user came back
        btnContinue?.isEnabled = bookingVM.booking.value.serviceType != null

        // ---- Choose where Continue goes ----
        // Preferred flow: Service -> DateTime -> Caregiver
        btnContinue?.setOnClickListener {
            // If your nav graph ID is bkDateTimeFragment, use this:
            findNavController().navigate(R.id.bkDateTimeFragment)

            // If you truly want to skip date/time and go straight to caregiver, use:
            // findNavController().navigate(R.id.bookCaregiverPickerFragment)
        }
    }
}
