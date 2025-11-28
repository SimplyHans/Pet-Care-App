package ca.gbc.petcareapp.data

data class CaregiverInfo(
    val id: Long,
    val name: String,
    val profession: String, // e.g., "Veterinarian", "Pet Groomer", "Dog Walker"
    val specialization: String?, // Service types they specialize in
    val rating: Double, // Average rating (0.0 - 5.0)
    val reviewCount: Int,
    val reviews: List<Review> = emptyList(),
    val isDummy: Boolean = false // Flag to identify dummy caregivers
)

data class Review(
    val reviewerName: String,
    val rating: Double,
    val comment: String,
    val date: String
)

