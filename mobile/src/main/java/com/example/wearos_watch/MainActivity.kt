package com.example.wearos_watch

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wearos_watch.databinding.ActivityMainBinding
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.ChannelClient.Channel
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val TAG = "MAIN-ACTIVITY"

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val adapter = BleListAdapter()
    private val phoneAdapter = BleDataAdapter()
    private val watchAdapter = BleDataAdapter()

    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val channelClient by lazy { Wearable.getChannelClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val sensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val accelerometer by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    private val gyroscope by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) }

    private val maker by lazy { FileMaker(applicationContext) }

    private val gestureArray: List<String> by lazy {
        resources.getStringArray(R.array.array_gesture).toList()
    }

    private var watchNode: Node? = null

    private val channelCallback = object : ChannelClient.ChannelCallback() {
        override fun onChannelOpened(channel: Channel) {
            super.onChannelOpened(channel)
            Log.d(TAG, "onChannelOpened: $channel")
            val uri = maker.getUri(binding.tvFileName.text.toString())
            channelClient.receiveFile(channel, uri, false).addOnSuccessListener {
                binding.tvFileName.text = getDataName(binding.tvFileName.text.toString())
            }
        }

        override fun onChannelClosed(
            channel: Channel,
            closeReason: Int,
            appSpecificErrorCode: Int
        ) {
            super.onChannelClosed(channel, closeReason, appSpecificErrorCode)
            Log.d(TAG, "onChannelClosed: $channel")
        }

        override fun onInputClosed(
            channel: Channel,
            closeReason: Int,
            appSpecificErrorCode: Int
        ) {
            super.onInputClosed(channel, closeReason, appSpecificErrorCode)
            Log.d(TAG, "onInputClosed: $channel")
        }

        override fun onOutputClosed(
            channel: Channel,
            closeReason: Int,
            appSpecificErrorCode: Int
        ) {
            super.onOutputClosed(channel, closeReason, appSpecificErrorCode)
            Log.d(TAG, "onOutputClosed: $channel")
        }
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = viewModel

        binding.rvBleList.adapter = adapter
        binding.rvBleList.layoutManager = LinearLayoutManager(this@MainActivity)

        binding.rvPhoneData.adapter = phoneAdapter
        binding.rvPhoneData.layoutManager = LinearLayoutManager(this@MainActivity)

        binding.rvWatchData.adapter = watchAdapter
        binding.rvWatchData.layoutManager = LinearLayoutManager(this@MainActivity)

        binding.buttonConnect.setOnClickListener {
            connectWithWatch()
        }

        setupCollect()
        setupToolbars()
        setupButton()
    }

    override fun onResume() {
        super.onResume()

        capabilityClient.addListener(
            viewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_ALL
        )
        messageClient.addListener(viewModel)
        channelClient.registerChannelCallback(channelCallback)
    }

    override fun onStop() {
        super.onStop()
        capabilityClient.removeListener(viewModel)
        messageClient.removeListener(viewModel)
        channelClient.unregisterChannelCallback(channelCallback)
    }

    private fun setupCollect() {
        lifecycleScope.launch {
            viewModel.resultFlow.collectLatest {
                when (it) {
                    START_ACTIVITY_PATH -> binding.tvBleStatus.text = "Connected"
                    START_RECORD_PATH -> binding.tvBleStatus.text = "Start"
                    STOP_RECORD_PATH -> binding.tvBleStatus.text = "Stop"
                }
            }
        }
    }

    private fun setupToolbars() {
        binding.toolbar.inflateMenu(R.menu.menu_main)

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    SettingDialog(
                        binding.tvFileName.text.toString(),
                        object : SettingDialog.OnInputListener {
                            override fun sendInput(input: String) {
                                binding.tvFileName.text = input
                            }
                        }).show(supportFragmentManager, null)
                    true
                }

                else -> false
            }
        }
    }

    private fun setupButton() {
        binding.buttonRecord.setBackgroundColor(Color.GREEN)

        binding.buttonRecord.setOnClickListener {
            if (binding.buttonRecord.text == "START") {
                if (binding.tvFileName.text != "") {
                    sensorManager.registerListener(
                        this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_FASTEST
                    )
                    sensorManager.registerListener(
                        this,
                        gyroscope,
                        SensorManager.SENSOR_DELAY_FASTEST
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@MainActivity, "waiting", Toast.LENGTH_SHORT).show()
                        delay(2000)
                        sendRecordMessage("start")
                        Toast.makeText(this@MainActivity, "start", Toast.LENGTH_SHORT).show()
                        maker.start(binding.tvFileName.text.toString())
                    }

                    binding.buttonRecord.text = "STOP"
                    binding.buttonRecord.setBackgroundColor(Color.RED)
                } else {
                    Toast.makeText(this, "Please set file name", Toast.LENGTH_SHORT).show()
                }
            } else {
                sendRecordMessage("stop")
                sensorManager.unregisterListener(this, accelerometer)
                sensorManager.unregisterListener(this, gyroscope)
                maker.end(binding.tvFileName.text.toString())

                binding.buttonRecord.text = "START"
                binding.buttonRecord.setBackgroundColor(Color.GREEN)
            }
        }

        binding.buttonDisconnect.setOnClickListener {
            setConnected(false)
            watchNode = null
        }
    }

    private fun getDataName(prev: String): String {
        val dirPath = prev.split("/")[0]
        val fileValues = prev.split("/")[1].split("-") // clap-15-1

        return if (fileValues[0] == gestureArray.last()) {
            val count = if (fileValues[2] == "1") 2 else 1
            "$dirPath/${gestureArray.first()}-${fileValues[1]}-$count"
        } else {
            val idx = gestureArray.indexOf(fileValues[0]) + 1
            "$dirPath/${gestureArray[idx]}-${fileValues[1]}-${fileValues[2]}"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent) {
        val content = when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                "A ${event.timestamp} ${event.values[0]} ${event.values[1]} ${event.values[2]}\n"
            }

            Sensor.TYPE_GYROSCOPE -> {
                "G ${event.timestamp} ${event.values[0]} ${event.values[1]} ${event.values[2]}\n"
            }

            else -> return
        }
        phoneAdapter.addItem(content)
        binding.rvPhoneData.scrollToPosition(phoneAdapter.itemCount - 1)

        writeData(content)
    }

    private fun writeData(content: String) {
        maker.addData(content)
    }

    private fun setConnected(enable: Boolean) {
        if (enable) {
            binding.tvBleStatus.text = "Connected"
            binding.buttonConnect.isEnabled = false
            binding.buttonDisconnect.isEnabled = true
            binding.buttonRecord.isEnabled = true

        } else {
            binding.tvBleStatus.text = "Disconnected"
            binding.buttonConnect.isEnabled = true
            binding.buttonDisconnect.isEnabled = false
            binding.buttonRecord.isEnabled = false
        }
    }

    private fun connectWithWatch() {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                Log.d(TAG, "connectWithWatch")
                val myNode = Wearable.getNodeClient(this@MainActivity).localNode.await().id
                Log.d(TAG, "my node id: $myNode")

                val nodes = capabilityClient
                    .getCapability(CAPABILITY_NAME, CapabilityClient.FILTER_ALL)
                    .await().nodes

                watchNode = pickBestNodeId(nodes)
                Log.d(TAG, "watch node id: ${watchNode?.id}")


                watchNode?.let { node ->
                    messageClient.sendMessage(node.id, START_ACTIVITY_PATH, byteArrayOf())
                        .addOnSuccessListener {
                            setConnected(true)
                        }.addOnFailureListener {
                            setConnected(false)
                        }

                    messageClient.sendMessage(node.id, DATA_CHANNEL_PATH, myNode.toByteArray())
                    Log.d(TAG, "transcription node id: ${node.id}")
                }
            } catch (cancellationException: CancellationException) {
                Log.d(TAG, "Capability request canceled.")
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.d(TAG, throwable.message ?: "Unknown message.")
                setConnected(false)
            }
        }
    }

    private fun sendRecordMessage(mode: String) {
        lifecycleScope.launch {
            try {
                watchNode?.let { node ->
                    val path = when (mode) {
                        "start" -> START_RECORD_PATH
                        "stop" -> STOP_RECORD_PATH
                        else -> ""
                    }

                    messageClient.sendMessage(node.id, path, byteArrayOf())
                        .addOnSuccessListener {
                            Log.d(TAG, "message sent")
                        }.addOnFailureListener {
                            Log.d(TAG, "message failed")
                        }
                    Log.d(TAG, "transcription node id: ${node.id}")
                }
            } catch (exception: Exception) {
                Log.d(TAG, "Starting activity failed: $exception")
            }
        }
    }

    private fun pickBestNodeId(nodes: Set<Node>): Node? {
        // Find a nearby node or pick one arbitrarily
        return nodes.firstOrNull { it.isNearby } ?: nodes.firstOrNull()
    }

    companion object {
        private const val CAPABILITY_NAME = "vibauth"
        private val START_ACTIVITY_PATH = "/start_activity"
        private val START_RECORD_PATH = "/start_record"
        private val STOP_RECORD_PATH = "/stop_record"
        private val DATA_CHANNEL_PATH = "/vibauth_data"
    }
}
