package com.example.wearos_watch

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter


class FileMaker(private val context: Context) {
    private val TAG = "main_file"

    private var writer: BufferedWriter? = null

    private val path: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path

    private val channelClient by lazy { Wearable.getChannelClient(context) }

    private val DATA_CHANNEL_PATH = "/vibauth_data"
    private val name = "watch_record"

    private var transcriptionNodeId: String? = null

    fun start() {
        val dir = File(path)
        if (dir.exists()) {
            val file = File("$path/$name.txt")
            if (file.exists()) file.delete()
            writer = BufferedWriter(FileWriter(file, false))
        } else if (File(path).mkdir()) {
            writer = BufferedWriter(FileWriter(File("$path/$name.txt"), false))
        } else {
            Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
        }

        writer?.write("start\n")
    }

    fun end() {
        writer?.write("end\n")
        writer?.close()
        writer = null

        requestTranscription(Uri.fromFile(File("$path/$name.txt")))
    }

    fun addData(content: String) {
        writer?.write(content)
    }

    private fun requestTranscription(uri: Uri) {
        transcriptionNodeId?.let { nodeId ->
            channelClient.openChannel(nodeId, DATA_CHANNEL_PATH).addOnSuccessListener {
                channelClient.sendFile(it, uri)
                    .addOnSuccessListener {
                        Log.d("$TAG-result", "success")
                    }.addOnFailureListener {
                        Log.d("$TAG-result", "fail")
                    }.addOnCanceledListener {
                        Log.d("$TAG-result", "cancel")
                    }.addOnCompleteListener {
                        Log.d("$TAG-result", "complete")
                    }
            }
        }
    }

    fun setNode(nodeId: String) {
        transcriptionNodeId = nodeId
        Log.d("$TAG-node", nodeId)
    }
}
