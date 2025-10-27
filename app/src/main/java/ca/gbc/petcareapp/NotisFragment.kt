package ca.gbc.petcareapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NotisFragment : Fragment(R.layout.notifications) {

    // Both viewmodels are used
    private val notificationsVM: NotificationsViewModel by activityViewModels()
    private val bookingVM: BookingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavbar(view)

        val container = view.findViewById<LinearLayout>(R.id.notisContainer)
        val inflater = LayoutInflater.from(requireContext())

        // Combine both flows: session (bookingVM) + other notifications (notificationsVM)
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                notificationsVM.notifications,   // emits List<NotificationItem(title, message, ...?)>
                bookingVM.notifications          // emits List<String> (messages created this session)
            ) { vmNotifs, sessionMsgs ->
                // Convert booking messages to the same shape used by notificationsVM
                val sessionAsItems = sessionMsgs.map { msg ->
                    // We only need title + message for rendering
                    SimpleItem(title = "Appointment Booked", message = msg)
                }
                val vmItems = vmNotifs.map { it -> SimpleItem(title = it.title, message = it.message) }

                // Merge them; put newest-session items first, then the VM ones
                sessionAsItems + vmItems
            }.collectLatest { items ->
                container.removeAllViews()

                if (items.isEmpty()) {
                    val emptyMsg = TextView(requireContext()).apply {
                        text = "No notifications yet."
                        textSize = 16f
                        setPadding(32, 64, 32, 64)
                        setTextColor(
                            ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
                        )
                    }
                    container.addView(emptyMsg)
                } else {
                    items.forEach { item ->
                        val card = inflater.inflate(R.layout.notis_reg_card, container, false)
                        card.findViewById<TextView>(R.id.notisTitle).text = item.title
                        card.findViewById<TextView>(R.id.desc).text  = item.message
                        container.addView(card)
                    }
                }
            }
        }
    }

    // Lightweight local model so we don't depend on your NotificationsItem class shape
    private data class SimpleItem(val title: String, val message: String)

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
