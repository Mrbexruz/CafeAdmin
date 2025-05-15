package com.example.cafeadmin.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.cafeadmin.databinding.ItemTableBinding
import com.example.cafeadmin.models.Table
class TableAdapter(
    private val tableList: MutableList<Table>,
    private val onDelete: (Table) -> Unit,
    private val onTableClick: (Table) -> Unit
) : RecyclerView.Adapter<TableAdapter.TableViewHolder>() {

    private val busyTableIds = mutableSetOf<Int>()

    inner class TableViewHolder(val binding: ItemTableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(table: Table) {
            binding.tvTableNumber.text = table.number

            // Rangni o'zgartirish
            val cardView = binding.root as CardView
            val color = if (busyTableIds.contains(table.id)) {
                Color.parseColor("#E5FF67") // Yashil rang - band
            } else {
                Color.WHITE // Oq rang - bo'sh
            }
            cardView.setCardBackgroundColor(color)

            binding.root.setOnClickListener { onTableClick(table) }
            binding.btnDelete.setOnClickListener { onDelete(table) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val binding = ItemTableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        holder.bind(tableList[position])
    }

    override fun getItemCount() = tableList.size

    fun addTable(table: Table) {
        tableList.add(table)
        notifyItemInserted(tableList.size - 1)
    }

    fun removeTable(table: Table) {
        val index = tableList.indexOfFirst { it.id == table.id }
        if (index != -1) {
            tableList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun updateBusyTables(newBusyTableIds: Set<Int>) {
        busyTableIds.clear()
        busyTableIds.addAll(newBusyTableIds)
        notifyDataSetChanged()
    }
}