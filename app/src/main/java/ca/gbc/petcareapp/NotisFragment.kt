package ca.gbc.petcareapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class NotisFragment : Fragment(R.layout.notifications) {

    private val notificationsVM: NotificationsViewModel by activityViewModels()
    private val bookingVM: BookingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavbar(view)
        val container = view.findViewById<LinearLayout>(R.id.notisContainer)

        // Observe the list of notifications
        viewLifecycleOwner.lifecycleScope.launch {
            notificationsVM.notifications.collectLatest { notifications ->
                container.removeAllViews() // Clear previous list

                if (notifications.isEmpty()) {
                    // Show empty message if no notifications exist
                    val emptyMsg = TextView(requireContext()).apply {
                        text = "No notifications yet."
                        textSize = 16f
                        setPadding(32, 64, 32, 64)
                        setTextColor(resources.getColor(android.R.color.darker_gray, null))
                    }
                    container.addView(emptyMsg)
                } else {
                    // Inflate a new card for each notification (newest first)
                    notifications.forEach { notification ->
                        val cardView = LayoutInflater.from(requireContext())
                            .inflate(R.layout.notis_reg_card, container, false)

                        val titleView = cardView.findViewById<TextView>(R.id.notisTitle)
                        val descView = cardView.findViewById<TextView>(R.id.desc)

                        titleView.text = notification.title
                        descView.text = notification.message

                        // Add to container (newest notifications appear at the top)
                        container.addView(cardView)
                    }
                }
            }
        }
    }

    private fun setupNavbar(view: View) {
        view.findViewById<Button>(R.id.homeTab)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        view.findViewById<Button>(R.id.bookTab)?.setOnClickListener {
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
    }
}
