package ca.gbc.petcareapp.pets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ca.gbc.petcareapp.R

class PetAdapter(
    private var pets: List<Pet>,
    private val onClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    // Cache colors to avoid repeated getColor calls
    private var cachedColors: List<Int>? = null

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.card_pet_item)
        val nameText: TextView = itemView.findViewById(R.id.text_pet_name)
        val breedText: TextView = itemView.findViewById(R.id.text_pet_breed)
        val ageText: TextView = itemView.findViewById(R.id.text_pet_age)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet, parent, false)
        
        // Cache colors on first creation
        if (cachedColors == null) {
            cachedColors = listOf(
                ContextCompat.getColor(parent.context, R.color.pet_color_1),
                ContextCompat.getColor(parent.context, R.color.pet_color_2),
                ContextCompat.getColor(parent.context, R.color.pet_color_3),
                ContextCompat.getColor(parent.context, R.color.pet_color_4)
            )
        }
        
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]
        holder.nameText.text = pet.petName
        holder.breedText.text = pet.breed
        holder.ageText.text = "Age: ${pet.age}"

        // Use cached colors
        val colors = cachedColors ?: listOf(
            ContextCompat.getColor(holder.itemView.context, R.color.pet_color_1),
            ContextCompat.getColor(holder.itemView.context, R.color.pet_color_2),
            ContextCompat.getColor(holder.itemView.context, R.color.pet_color_3),
            ContextCompat.getColor(holder.itemView.context, R.color.pet_color_4)
        )
        holder.cardView.setCardBackgroundColor(colors[position % 4])

        holder.itemView.setOnClickListener { onClick(pet) }
    }

    override fun getItemCount(): Int = pets.size

    fun updatePets(newPets: List<Pet>) {
        val diffCallback = PetDiffCallback(pets, newPets)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        pets = newPets
        diffResult.dispatchUpdatesTo(this)
    }
    
    private class PetDiffCallback(
        private val oldList: List<Pet>,
        private val newList: List<Pet>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
