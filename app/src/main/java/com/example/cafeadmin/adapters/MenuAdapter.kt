package com.example.cafeadmin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cafeadmin.databinding.ItemMenuBinding
import com.example.cafeadmin.models.Food

class MenuAdapter(
    private val list: List<Food>,
    private val onDelete: (Food) -> Unit
) : RecyclerView.Adapter<MenuAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(food: Food) {
            binding.tvName.text = food.name
            binding.tvPrice.text = food.price
            Glide.with(binding.root.context)
                .load(food.imageUrl)
                .into(binding.imgFood)

            // delete tugmasi
            binding.btnDelete.setOnClickListener {
                onDelete(food)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        return FoodViewHolder(ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}
