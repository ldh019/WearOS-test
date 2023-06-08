package com.example.wearos_watch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(), CapabilityClient.OnCapabilityChangedListener,
    MessageClient.OnMessageReceivedListener {
    private val TAG = "MAIN-VIEWMODEL"

    val resultFlow = MutableStateFlow("")

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: ${p0.name}")
    }

    override fun onMessageReceived(p0: MessageEvent) {
        viewModelScope.launch {
            resultFlow.emit(p0.path)
        }
    }
}