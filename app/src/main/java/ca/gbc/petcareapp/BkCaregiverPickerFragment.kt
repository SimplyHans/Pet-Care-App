package ca.gbc.petcareapp.feature.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.R
import ca.gbc.petcareapp.databinding.BkFragmentCaregiverPickerBinding

class BkCaregiverPickerFragment : Fragment() {

    private var _binding: BkFragmentCaregiverPickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BkFragmentCaregiverPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bkBtnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_bkCaregiverPicker_to_bkDateTime)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
