package com.example.wearos_watch

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wearos_watch.databinding.ActivityMainBinding
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val TAG = "Main-Activity"

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val sensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val accelerometer: Sensor by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    private val gyroscope: Sensor by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) }

    private val maker by lazy { FileMaker(applicationContext) }

    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val channelClient by lazy { Wearable.getChannelClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resultFlow.collectLatest {
                    it?.let {
                        binding.tvTitle.text = it.data.toString(Charsets.UTF_8)
                        when (it.path) {
                            START_RECORD_PATH -> {
                                maker.start()
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    accelerometer,
                                    SensorManager.SENSOR_DELAY_FASTEST
                                ).also { result ->
                                    Log.d("$TAG-regi", "accelerometer: $result")
                                }
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    gyroscope,
                                    SensorManager.SENSOR_DELAY_FASTEST
                                ).also { result ->
                                    Log.d("$TAG-regi", "gyroscope: $result")
                                }
                            }
                            STOP_RECORD_PATH -> {
                                maker.end()
                                sensorManager.unregisterListener(this@MainActivity, accelerometer)
                                    .also { result ->
                                        Log.d("$TAG-unregi", "accelerometer: $result")
                                    }
                                sensorManager.unregisterListener(this@MainActivity, gyroscope)
                                    .also { result ->
                                        Log.d("$TAG-unregi", "gyroscope: $result")
                                    }
                            }
                            DATA_CHANNEL_PATH -> {
                                maker.setNode(it.data.toString(Charsets.UTF_8))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        messageClient.addListener(viewModel)
        channelClient.registerChannelCallback(viewModel.channelCallback)
        capabilityClient.addListener(
            viewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_REACHABLE
        )
    }

    override fun onStop() {
        super.onStop()

        capabilityClient.removeListener(viewModel)
        messageClient.removeListener(viewModel)
        channelClient.unregisterChannelCallback(viewModel.channelCallback)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val content = when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                "AW ${event.timestamp} ${event.values[0]} ${event.values[1]} ${event.values[2]}\n"
            }

            Sensor.TYPE_GYROSCOPE -> {
                "GW ${event.timestamp} ${event.values[0]} ${event.values[1]} ${event.values[2]}\n"
            }

            else -> return
        }
        binding.tvContents.text = content
        maker.addData(content)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    companion object {
        private val START_ACTIVITY_PATH = "/start_activity"
        private val START_RECORD_PATH = "/start_record"
        private val STOP_RECORD_PATH = "/stop_record"
        private val DATA_CHANNEL_PATH = "/vibauth_data"
    }
}
