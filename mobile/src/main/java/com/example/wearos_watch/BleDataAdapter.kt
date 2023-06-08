package com.example.wearos_watch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wearos_watch.databinding.ItemDataListBinding

class BleDataAdapter : RecyclerView.Adapter<BleDataAdapter.DataViewHolder>() {
    class DataViewHolder(private val binding: ItemDataListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(content: String) {
            binding.tvContent.text = content
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val binding =
            ItemDataListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    private val itemList: MutableList<String> = mutableListOf()

    fun addItem(item: String) {
        itemList.add(item)
        notifyItemInserted(itemList.size - 1)
    }

    fun removeItem(position: Int) {
        itemList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun getItem(position: Int): String {
        return itemList[position]
    }
}