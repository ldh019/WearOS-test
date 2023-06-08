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

class MainViewModel: ViewModel(), CapabilityClient.OnCapabilityChangedListener, MessageClient.OnMessageReceivedListener {
    private val TAG = "MAIN-VIEWMODEL"

    val resultFlow = MutableStateFlow<MessageEvent?>(null)

    val channelCallback = object: ChannelClient.ChannelCallback() {
        override fun onChannelOpened(channel: ChannelClient.Channel) {
            super.onChannelOpened(channel)
            Log.d(TAG, "onChannelOpened: $channel")
        }

        override fun onChannelClosed(channel: ChannelClient.Channel, closeReason: Int, appSpecificErrorCode: Int) {
            super.onChannelClosed(channel, closeReason, appSpecificErrorCode)
            Log.d(TAG, "onChannelClosed: $channel")
        }

        override fun onInputClosed(channel: ChannelClient.Channel, closeReason: Int, appSpecificErrorCode: Int) {
            super.onInputClosed(channel, closeReason, appSpecificErrorCode)
            Log.d(TAG, "onInputClosed: $channel")
        }

        override fun onOutputClosed(channel: ChannelClient.Channel, closeReason: Int, appSpecificErrorCode: Int) {
            super.onOutputClosed(channel, closeReason, appSpecificErrorCode)
            Log.d(TAG, "onOutputClosed: $channel")
        }
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: ${p0.name}")
    }

    override fun onMessageReceived(p0: MessageEvent) {
        viewModelScope.launch {
            resultFlow.emit(p0)
            Log.d(TAG, "onMessageReceived: ${p0.path}")
        }
    }
}