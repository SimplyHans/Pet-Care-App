package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class BkCaregiverPickerFragment : Fragment(R.layout.bk_fragment_caregiver_picker) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.homeTab)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        view.findViewById<Button>(R.id.bookTab)?.setOnClickListener {
            findNavController().navigate(R.id.bookCaregiverPickerFragment)
        }

        view.findViewById<Button>(R.id.petsTab)?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }

        view.findViewById<Button>(R.id.bk_btn_continue)?.setOnClickListener {
            findNavController().navigate(R.id.bkDateTimeFragment)
        }

        view.findViewById<View>(R.id.settingsBtn)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        view.findViewById<View>(R.id.notisBtn)?.setOnClickListener {
            findNavController().navigate(R.id.notisFragment)
        }

    }

}
