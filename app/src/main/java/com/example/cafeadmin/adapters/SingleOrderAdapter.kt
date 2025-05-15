package com.example.cafeadmin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cafeadmin.databinding.ItemSingleOrderBinding
import com.example.cafeadmin.models.Order

class SingleOrderAdapter : RecyclerView.Adapter<SingleOrderAdapter.SingleOrderViewHolder>() {

    private var itemList: List<Order> = emptyList()

    inner class SingleOrderViewHolder(private val binding: ItemSingleOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Order) {
            binding.tvName.text = item.name
            binding.tvPrice.text = "${item.price} so'm"
            binding.count.text = "Soni: ${item.count}"

            Glide.with(binding.imgFood.context)
                .load(item.imageUrl)
                .into(binding.imgFood)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleOrderViewHolder {
        val binding = ItemSingleOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SingleOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SingleOrderViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size

    fun submitList(newItems: List<Order>) {
        itemList = newItems
        notifyDataSetChanged()
    }
}
