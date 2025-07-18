package com.airdox.voicecloning.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecordingManager(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null
    
    var onRecordingUpdate: ((Int) -> Unit)? = null
    var onRecordingComplete: ((File) -> Unit)? = null
    
    private val recordingsDir by lazy {
        File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recordings").apply {
            if (!exists()) mkdirs()
        }
    }
    
    private val trainingDir by lazy {
        File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "training").apply {
            if (!exists()) mkdirs()
        }
    }
    
    fun startRecording(): Boolean {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            currentRecordingFile = File(recordingsDir, "recording_$timeStamp.wav")
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(currentRecordingFile!!.absolutePath)
                
                prepare()
                start()
            }
            
            // Start amplitude monitoring
            startAmplitudeMonitoring()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            currentRecordingFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    onRecordingComplete?.invoke(file)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun startAmplitudeMonitoring() {
        // Simulate amplitude monitoring
        Thread {
            while (mediaRecorder != null) {
                try {
                    val amplitude = mediaRecorder?.maxAmplitude ?: 0
                    onRecordingUpdate?.invoke(amplitude)
                    Thread.sleep(100)
                } catch (e: Exception) {
                    break
                }
            }
        }.start()
    }
    
    fun playRecording(file: File, onComplete: () -> Unit) {
        try {
            stopPlayback() // Stop any current playback
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    onComplete()
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete()
        }
    }
    
    fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }
    
    suspend fun saveToTrainingSet(file: File): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val trainingFile = File(trainingDir, "training_$timeStamp.wav")
            
            file.copyTo(trainingFile, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun importAudioFile(uri: Uri): File? = withContext(Dispatchers.IO) {
        return@withContext try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val importedFile = File(recordingsDir, "imported_$timeStamp.wav")
            
            inputStream?.use { input ->
                FileOutputStream(importedFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Validate the file
            if (importedFile.exists() && importedFile.length() > 0 && isValidAudioFile(importedFile)) {
                importedFile
            } else {
                importedFile.delete()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun isValidAudioFile(file: File): Boolean {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            duration != null && duration.toLongOrNull() != null
        } catch (e: Exception) {
            false
        }
    }
    
    fun getTrainingFiles(): List<File> {
        return trainingDir.listFiles()?.filter { it.isFile && it.extension in listOf("wav", "mp3", "m4a") } 
            ?: emptyList()
    }
    
    fun cleanup() {
        stopPlayback()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}