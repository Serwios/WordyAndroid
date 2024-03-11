package com.geekglasses.wordy.csv

import android.os.Environment
import java.io.File
import java.io.FileOutputStream

class CsvGenerator {
    companion object {
        fun generateCsv(): File? {
            return try {
                val fileName = "example.csv"
                val file = File(Environment.getExternalStorageDirectory(), fileName)
                val outputStream = FileOutputStream(file)
                val csvContent = "Name,Age,Email\nJohn,25,john@example.com\nDoe,30,doe@example.com"
                outputStream.write(csvContent.toByteArray())
                outputStream.close()
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}