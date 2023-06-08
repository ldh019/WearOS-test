package com.example.wearos_watch

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.google.android.gms.wearable.Wearable
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class FileMaker(private val context: Context) {
    private var writer: BufferedWriter? = null

    private val filePath: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path

    fun start(input: String) {
        val directory = input.split("/")[0]
        val name = input.split("/")[1]
        val path = "$filePath/$directory"
        val dir = File(path)
        if (dir.exists()) {
            writer = BufferedWriter(FileWriter(File("$path/$name.txt"), false))
        } else if (File(path).mkdir()) {
            writer = BufferedWriter(FileWriter(File("$path/$name.txt"), false))
        } else {
            Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
        }

        writer?.write("start\n")
    }

    fun end(input: String) {
        writer?.write("end\n")
        writer?.close()
        writer = null
        getWatchData(input)
    }

    fun addData(content: String) {
        writer?.write(content)
    }

    fun getUri(input: String): Uri {
        val directory = input.split("/")[0]
        val name = input.split("/")[1]
        val path = "$filePath/$directory"
        return Uri.fromFile(File("$path/${name}_watch.txt"))
    }

    private fun getWatchData(input: String) {
        val directory = input.split("/")[0]
        val name = input.split("/")[1]

    }
}