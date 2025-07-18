package com.airdox.voicecloning.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtils {
    
    fun hasRecordAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, 
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, 
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasAllPermissions(context: Context): Boolean {
        return hasRecordAudioPermission(context) && hasStoragePermission(context)
    }
}