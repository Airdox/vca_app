package com.airdox.voicecloning.utils

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    
    fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$bytes Bytes"
        }
    }
    
    fun formatTimestamp(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN)
        return formatter.format(Date(timestamp))
    }
    
    fun getAudioFileDuration(file: File): Long {
        // Simplified duration calculation
        // In a real implementation, you would use MediaMetadataRetriever
        return (file.length() / 44100 / 2) * 1000 // Estimate for 44.1kHz 16-bit mono
    }
    
    fun getDirectorySize(directory: File): Long {
        var size = 0L
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }
    
    fun cleanupTempFiles(directory: File, maxAge: Long = 24 * 60 * 60 * 1000) {
        val now = System.currentTimeMillis()
        directory.listFiles()?.forEach { file ->
            if (now - file.lastModified() > maxAge) {
                file.delete()
            }
        }
    }
}