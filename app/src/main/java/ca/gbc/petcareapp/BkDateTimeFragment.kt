package ca.gbc.petcareapp.feature.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.R
import ca.gbc.petcareapp.databinding.BkFragmentDatetimeBinding

class BkDateTimeFragment : Fragment() {

    private var _binding: BkFragmentDatetimeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BkFragmentDatetimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bkBtnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_bkDateTime_to_bkServiceType)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
