package ca.gbc.petcareapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.data.User
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BkCaregiverPickerFragment : Fragment(R.layout.bk_fragment_caregiver_picker) {

    private val bookingVM: BookingViewModel by activityViewModels()
    private lateinit var db: AppDatabase
    private var businessUsers: List<User> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.get(requireContext())

        // ---- Caregiver choices (3 fixed rows in your XML) ----
        val row1 = view.findViewById<View>(R.id.bk_item1)
        val row2 = view.findViewById<View>(R.id.bk_item2)
        val row3 = view.findViewById<View>(R.id.bk_item3)
        val rows = listOf(row1, row2, row3)
        
        val name1 = view.findViewById<TextView>(R.id.bk_item1_name)
        val name2 = view.findViewById<TextView>(R.id.bk_item2_name)
        val name3 = view.findViewById<TextView>(R.id.bk_item3_name)
        val names = listOf(name1, name2, name3)
        
        val rating1 = view.findViewById<TextView>(R.id.bk_item1_rating)
        val rating2 = view.findViewById<TextView>(R.id.bk_item2_rating)
        val rating3 = view.findViewById<TextView>(R.id.bk_item3_rating)
        val ratings = listOf(rating1, rating2, rating3)

        val continueBtn = view.findViewById<MaterialButton>(R.id.bk_btn_continue)

        // Load business users from database
        loadBusinessUsers(view, rows, names, ratings, continueBtn)

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
        val currentCaregiverId = bookingVM.booking.value.caregiverId
        val selectedIndex = businessUsers.indexOfFirst { it.id.toString() == currentCaregiverId }
        if (selectedIndex >= 0 && selectedIndex < rows.size) {
            markSelected(rows[selectedIndex])
        } else {
            markSelected(null)
        }
        continueBtn.isEnabled = currentCaregiverId != null

        row1.setOnClickListener {
            if (businessUsers.isNotEmpty()) {
                val user = businessUsers[0]
                bookingVM.setCaregiver(user.id.toString(), user.fullName)
                markSelected(row1)
                continueBtn.isEnabled = true
            }
        }
        row2.setOnClickListener {
            if (businessUsers.size > 1) {
                val user = businessUsers[1]
                bookingVM.setCaregiver(user.id.toString(), user.fullName)
                markSelected(row2)
                continueBtn.isEnabled = true
            }
        }
        row3.setOnClickListener {
            if (businessUsers.size > 2) {
                val user = businessUsers[2]
                bookingVM.setCaregiver(user.id.toString(), user.fullName)
                markSelected(row3)
                continueBtn.isEnabled = true
            }
        }

        // replace ONLY this click listener
        continueBtn.setOnClickListener {
            // if you navigate by destination id:
            findNavController().navigate(R.id.bkDateTimeFragment)}
    }

    private fun loadBusinessUsers(
        view: View,
        rows: List<View>,
        names: List<TextView>,
        ratings: List<TextView>,
        continueBtn: MaterialButton
    ) {
        lifecycleScope.launch {
            val allBusinessUsers = withContext(Dispatchers.IO) {
                db.userDao().findByRole("business")
            }
            val selectedServiceType = bookingVM.booking.value.serviceType
            
            // Filter caregivers by service specialization
            businessUsers = if (selectedServiceType != null) {
                val serviceTypeName = selectedServiceType.name
                allBusinessUsers.filter { user ->
                    // If user has no specialization, they can do all services
                    // Otherwise, check if their specialization includes the selected service
                    user.serviceSpecialization == null || 
                    user.serviceSpecialization.isEmpty() ||
                    user.serviceSpecialization.contains(serviceTypeName, ignoreCase = true)
                }
            } else {
                // If no service type selected, show all business users
                allBusinessUsers
            }
            
            // If no filtered users, show all business users as fallback
            if (businessUsers.isEmpty()) {
                businessUsers = allBusinessUsers
            }
            
            // Add dummy caregivers if we have fewer than 3
            val dummyCaregivers = listOf(
                User(id = -1, fullName = "Dr. Smith", email = "", passwordHash = "", salt = "", role = "business", serviceSpecialization = "VETERINARY,GROOMING"),
                User(id = -2, fullName = "Dr. Johnson", email = "", passwordHash = "", salt = "", role = "business", serviceSpecialization = "GROOMING"),
                User(id = -3, fullName = "Dr. Williams", email = "", passwordHash = "", salt = "", role = "business", serviceSpecialization = "WALKING,TRAINING")
            )
            
            // Combine real and dummy caregivers to always show 3 options
            val displayUsers = (businessUsers + dummyCaregivers).take(3)
            
            // Update UI with caregivers (real + dummy)
            displayUsers.forEachIndexed { index, user ->
                if (index < rows.size) {
                    names[index].text = user.fullName
                    
                    // Calculate rating for this caregiver
                    val rating = if (user.id < 0) {
                        // Dummy caregivers have predefined ratings
                        when (user.id) {
                            -1L -> 4.8  // Dr. Smith
                            -2L -> 4.6  // Dr. Johnson
                            -3L -> 4.9  // Dr. Williams
                            else -> 4.5
                        }
                    } else {
                        // Real users: generate consistent rating based on ID
                        3.5 + (user.id % 3) * 0.5 // 3.5, 4.0, or 4.5
                    }
                    
                    ratings[index].text = String.format("%.1f", rating)
                    rows[index].visibility = View.VISIBLE
                }
            }
            
            // Hide rows that don't have users (shouldn't happen now, but just in case)
            for (i in displayUsers.size until rows.size) {
                rows[i].visibility = View.GONE
            }
            
            // Update businessUsers list to include dummies for click handlers
            businessUsers = displayUsers
            
            // Restore selection after loading
            val currentCaregiverId = bookingVM.booking.value.caregiverId
            val selectedIndex = businessUsers.indexOfFirst { it.id.toString() == currentCaregiverId }
            if (selectedIndex >= 0 && selectedIndex < rows.size) {
                rows[selectedIndex].isSelected = true
                rows[selectedIndex].alpha = 1f
                rows[selectedIndex].scaleX = 1.02f
                rows[selectedIndex].scaleY = 1.02f
                rows[selectedIndex].elevation = 6f
            }
            continueBtn.isEnabled = currentCaregiverId != null
        }
    }
}
