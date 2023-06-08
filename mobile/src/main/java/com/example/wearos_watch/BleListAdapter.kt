package com.example.wearos_watch

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wearos_watch.databinding.ItemBleListBinding

class BleListAdapter
    : ListAdapter<BluetoothDevice, BleListAdapter.BleViewHolder>(diffUtil) {
    private lateinit var itemClickListner: ItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleViewHolder {
        val binding = ItemBleListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BleViewHolder(binding, itemClickListner)
    }

    override fun onBindViewHolder(holder: BleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BleViewHolder(
        private val binding: ItemBleListBinding,
        private val listener: ItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(currentDevice: BluetoothDevice?) {
            binding.tvName.text = currentDevice?.name
            binding.tvAddress.text = currentDevice?.address
            binding.root.setOnClickListener {
                listener.onClick(it, currentDevice)
            }
        }
    }

    interface ItemClickListener {
        fun onClick(view: View, device: BluetoothDevice?)
    }

    fun setOnItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<BluetoothDevice>() {
            override fun areItemsTheSame(
                oldItem: BluetoothDevice,
                newItem: BluetoothDevice
            ): Boolean {
                return oldItem.address == newItem.address
            }

            override fun areContentsTheSame(
                oldItem: BluetoothDevice,
                newItem: BluetoothDevice
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}