package ca.gbc.petcareapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.gbc.petcareapp.auth.data.AppDatabase
import ca.gbc.petcareapp.auth.data.User
import ca.gbc.petcareapp.data.CaregiverInfo
import ca.gbc.petcareapp.data.Review
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StaffListFragment : Fragment(R.layout.staff_list) {

    private lateinit var db: AppDatabase
    private var caregiversLoaded: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.get(requireContext())

        // Update header title
        val header = view.findViewById<View>(R.id.header)
        val title = header.findViewById<TextView>(R.id.title)
        title.text = "Staff"

        // Setup navigation
        setupNavigation(view)

        // Load and display caregivers
        loadCaregivers(view)
    }

    private fun setupNavigation(view: View) {
        val homeTab = view.findViewById<ImageButton>(R.id.homeTab)
        val bookTab = view.findViewById<ImageButton>(R.id.bookTab)
        val petsTab = view.findViewById<ImageButton>(R.id.petsTab)

        homeTab?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        bookTab?.setOnClickListener {
            findNavController().navigate(R.id.bookListFragment)
        }

        petsTab?.setOnClickListener {
            findNavController().navigate(R.id.petListFragment)
        }

        view.findViewById<View>(R.id.settingsBtn)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        view.findViewById<View>(R.id.notisBtn)?.setOnClickListener {
            findNavController().navigate(R.id.notisFragment)
        }
    }

    private fun loadCaregivers(view: View) {
        if (caregiversLoaded) return // Avoid reloading if already loaded
        lifecycleScope.launch {
            // Get real business users from database (on IO thread)
            val businessUsers = withContext(Dispatchers.IO) {
                db.userDao().findByRole("business")
            }

            // Convert to CaregiverInfo with dummy ratings/reviews
            val realCaregivers = businessUsers.map { user ->
                createCaregiverInfo(user, false)
            }

            // Create dummy caregivers with ratings and reviews
            val dummyCaregivers = createDummyCaregivers()

            // Combine all caregivers
            val allCaregivers = realCaregivers + dummyCaregivers

            // Display them
            displayCaregivers(view, allCaregivers)
            caregiversLoaded = true
        }
    }

    private fun createCaregiverInfo(user: User, isDummy: Boolean): CaregiverInfo {
        // Determine profession based on specialization or default
        val profession = when {
            user.serviceSpecialization?.contains("WALKING", ignoreCase = true) == true -> "Dog Walker"
            user.serviceSpecialization?.contains("GROOMING", ignoreCase = true) == true -> "Pet Groomer"
            user.serviceSpecialization?.contains("VETERINARY", ignoreCase = true) == true -> "Veterinarian"
            user.serviceSpecialization?.contains("TRAINING", ignoreCase = true) == true -> "Pet Trainer"
            else -> "Pet Care Specialist"
        }

        // Generate random but consistent ratings for real users
        val rating = 3.5 + (user.id % 3) * 0.5 // 3.5, 4.0, or 4.5
        val reviewCount = 50 + (user.id % 100).toInt()

        // Create sample reviews
        val reviews = listOf(
            Review("Sarah M.", 5.0, "Excellent service! Very professional and caring.", "2 weeks ago"),
            Review("Mike T.", 4.0, "Great experience, would recommend.", "1 month ago"),
            Review("Emma L.", 5.0, "My pet loved it! Will book again.", "3 weeks ago")
        )

        return CaregiverInfo(
            id = user.id,
            name = user.fullName,
            profession = profession,
            specialization = user.serviceSpecialization,
            rating = rating,
            reviewCount = reviewCount,
            reviews = reviews,
            isDummy = isDummy
        )
    }

    private fun createDummyCaregivers(): List<CaregiverInfo> {
        return listOf(
            CaregiverInfo(
                id = -1,
                name = "Dr. Smith",
                profession = "Veterinarian",
                specialization = "VETERINARY,GROOMING",
                rating = 4.8,
                reviewCount = 120,
                reviews = listOf(
                    Review("John D.", 5.0, "Dr. Smith is amazing! Very knowledgeable and caring with my pets.", "1 week ago"),
                    Review("Lisa K.", 4.5, "Professional service, my dog felt comfortable throughout.", "2 weeks ago"),
                    Review("Tom R.", 5.0, "Best vet in town! Highly recommend.", "3 weeks ago")
                ),
                isDummy = true
            ),
            CaregiverInfo(
                id = -2,
                name = "Dr. Johnson",
                profession = "Pet Groomer",
                specialization = "GROOMING",
                rating = 4.6,
                reviewCount = 89,
                reviews = listOf(
                    Review("Maria S.", 5.0, "My cat looks beautiful after grooming! Very gentle.", "5 days ago"),
                    Review("David W.", 4.0, "Good service, reasonable prices.", "1 week ago"),
                    Review("Anna B.", 5.0, "Professional and friendly. My pet loves going there!", "2 weeks ago")
                ),
                isDummy = true
            ),
            CaregiverInfo(
                id = -3,
                name = "Dr. Williams",
                profession = "Dog Walker",
                specialization = "WALKING,TRAINING",
                rating = 4.9,
                reviewCount = 156,
                reviews = listOf(
                    Review("Chris P.", 5.0, "Reliable and trustworthy. My dog gets so excited to see them!", "3 days ago"),
                    Review("Jennifer L.", 5.0, "Excellent walker, very attentive to my dog's needs.", "1 week ago"),
                    Review("Robert H.", 4.5, "Great service, always on time.", "2 weeks ago")
                ),
                isDummy = true
            ),
            CaregiverInfo(
                id = -4,
                name = "Dr. Brown",
                profession = "Pet Trainer",
                specialization = "TRAINING",
                rating = 4.7,
                reviewCount = 95,
                reviews = listOf(
                    Review("Amanda T.", 5.0, "My dog learned so much! Great training techniques.", "1 week ago"),
                    Review("Kevin M.", 4.5, "Patient and effective trainer.", "2 weeks ago"),
                    Review("Rachel F.", 5.0, "Highly recommend for behavioral training!", "3 weeks ago")
                ),
                isDummy = true
            ),
            CaregiverInfo(
                id = -5,
                name = "Dr. Davis",
                profession = "Pet Care Specialist",
                specialization = "WALKING,GROOMING,VETERINARY",
                rating = 4.5,
                reviewCount = 67,
                reviews = listOf(
                    Review("Steven G.", 4.0, "Versatile caregiver, handles multiple services well.", "1 week ago"),
                    Review("Michelle Y.", 5.0, "Very professional and caring.", "2 weeks ago"),
                    Review("Daniel K.", 4.5, "Great all-around pet care provider.", "3 weeks ago")
                ),
                isDummy = true
            )
        )
    }

    private fun displayCaregivers(view: View, caregivers: List<CaregiverInfo>) {
        val container = view.findViewById<LinearLayout>(R.id.caregivers_container)
        container.removeAllViews()

        // Pre-cache colors to avoid repeated getColor calls
        val secondaryColor = ContextCompat.getColorStateList(requireContext(), R.color.secondary)
        val whiteColor = ContextCompat.getColor(requireContext(), android.R.color.white)

        caregivers.forEach { caregiver ->
            val caregiverView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_caregiver, container, false)

            // Set name and profession
            caregiverView.findViewById<TextView>(R.id.caregiver_name).text = caregiver.name
            caregiverView.findViewById<TextView>(R.id.caregiver_profession).text = caregiver.profession

            // Set rating
            caregiverView.findViewById<TextView>(R.id.rating_text).text = String.format("%.1f", caregiver.rating)
            caregiverView.findViewById<TextView>(R.id.review_count).text = "(${caregiver.reviewCount} reviews)"

            // Add specialization chips
            val specializationContainer = caregiverView.findViewById<LinearLayout>(R.id.specialization_container)
            specializationContainer.removeAllViews()
            
            if (caregiver.specialization != null && caregiver.specialization.isNotEmpty()) {
                val specializations = caregiver.specialization.split(",").map { it.trim() }
                specializations.forEach { spec ->
                    val chip = Chip(requireContext())
                    chip.text = spec.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                    chip.isClickable = false
                    chip.chipBackgroundColor = secondaryColor
                    chip.setTextColor(whiteColor)
                    chip.textSize = 11f
                    specializationContainer.addView(chip)
                }
            } else {
                val chip = Chip(requireContext())
                chip.text = "All Services"
                chip.isClickable = false
                chip.chipBackgroundColor = secondaryColor
                chip.setTextColor(whiteColor)
                chip.textSize = 11f
                specializationContainer.addView(chip)
            }

            // Show recent review if available
            val reviewPreview = caregiverView.findViewById<LinearLayout>(R.id.review_preview)
            if (caregiver.reviews.isNotEmpty()) {
                val recentReview = caregiver.reviews.first()
                caregiverView.findViewById<TextView>(R.id.review_text).text = "\"${recentReview.comment}\""
                caregiverView.findViewById<TextView>(R.id.reviewer_name).text = "- ${recentReview.reviewerName}"
                reviewPreview.visibility = View.VISIBLE
            } else {
                reviewPreview.visibility = View.GONE
            }

            container.addView(caregiverView)
        }
    }
}

