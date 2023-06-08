package com.example.wearos_watch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.wearos_watch.databinding.DialogfragmentSettingBinding

class SettingDialog(private val value: String, private val listener: OnInputListener) : DialogFragment() {
    interface OnInputListener {
        fun sendInput(input: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    private lateinit var binding: DialogfragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogfragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val monthItems = resources.getStringArray(R.array.array_month)
        val dayItems = resources.getStringArray(R.array.array_day)
        val gestureItems = resources.getStringArray(R.array.array_gesture)

        binding.spinnerMonth.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            monthItems
        )
        binding.spinnerDay.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            dayItems
        )
        binding.spinnerGesture.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            gestureItems
        )

        if (value != "") {
            val dirValues = value.split("/")[0].split("-")
            val fileValues = value.split("/")[1].split("-")
            val date = dirValues[0]
            binding.spinnerMonth.setSelection(monthItems.indexOf(date.substring(0, 2)))
            binding.spinnerDay.setSelection(dayItems.indexOf(date.substring(2, 4)))
            binding.tietUser.setText(dirValues[1])
            binding.tietDevice.setText(dirValues[2])
            binding.tietTable.setText(dirValues[3])
            if (dirValues.size == 5) binding.tietOption.setText(dirValues[4])

            binding.spinnerGesture.setSelection(gestureItems.indexOf(fileValues[0]))
            binding.tietNumber.setText(fileValues[1])
            binding.tietCount.setText(fileValues[2])
        }

        binding.button.setOnClickListener {
            val month = binding.spinnerMonth.selectedItem.toString()
            val day = binding.spinnerDay.selectedItem.toString()
            val gesture = binding.spinnerGesture.selectedItem.toString()
            val user = binding.tietUser.text.toString()
            val device = binding.tietDevice.text.toString()
            val table = binding.tietTable.text.toString()
            val option =
                if (binding.tietOption.text.toString() == "") "" else "-${binding.tietOption.text.toString()}"
            val number = binding.tietNumber.text.toString()
            val count = binding.tietCount.text.toString()

            val text = "$month$day-$user-$device-$table$option/$gesture-$number-$count"
            listener.sendInput(text)

            dismiss()
        }
    }
}