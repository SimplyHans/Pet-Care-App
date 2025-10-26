package ca.gbc.petcareapp.pets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ca.gbc.petcareapp.R

class PetAdapter(
    private var pets: List<Pet>,
    private val onClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.card_pet_item)
        val nameText: TextView = itemView.findViewById(R.id.text_pet_name)
        val breedText: TextView = itemView.findViewById(R.id.text_pet_breed)
        val ageText: TextView = itemView.findViewById(R.id.text_pet_age)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]
        holder.nameText.text = pet.petName
        holder.breedText.text = pet.breed
        holder.ageText.text = "Age: ${pet.age}"

        val colors = listOf(
            holder.itemView.context.getColor(R.color.pet_color_1),
            holder.itemView.context.getColor(R.color.pet_color_2),
            holder.itemView.context.getColor(R.color.pet_color_3),
            holder.itemView.context.getColor(R.color.pet_color_4)
        )
        holder.cardView.setCardBackgroundColor(colors[position % 4])

        holder.itemView.setOnClickListener { onClick(pet) }
    }

    override fun getItemCount(): Int = pets.size

    fun updatePets(newPets: List<Pet>) {
        pets = newPets
        notifyDataSetChanged()
    }
}
