package com.airdox.voicecloning.audio

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.*

class AudioProcessor(private val context: Context) {
    
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BYTES_PER_SAMPLE = 2
    }
    
    suspend fun enhanceAudio(inputFile: File): File = withContext(Dispatchers.IO) {
        val outputFile = File(inputFile.parent, "enhanced_${inputFile.name}")
        
        try {
            val audioData = readWaveFile(inputFile)
            
            // Apply audio enhancements
            val enhancedData = audioData
                .let { applyNoiseReduction(it) }
                .let { normalizeAudio(it) }
                .let { applyEqualization(it) }
            
            writeWaveFile(outputFile, enhancedData)
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            inputFile // Return original file if enhancement fails
        }
    }
    
    private fun readWaveFile(file: File): ShortArray {
        return try {
            FileInputStream(file).use { fis ->
                // Skip WAV header (44 bytes)
                fis.skip(44)
                
                val dataSize = (file.length() - 44).toInt()
                val samples = dataSize / BYTES_PER_SAMPLE
                val audioData = ShortArray(samples)
                
                val buffer = ByteArray(BYTES_PER_SAMPLE)
                for (i in 0 until samples) {
                    if (fis.read(buffer) == BYTES_PER_SAMPLE) {
                        audioData[i] = ((buffer[1].toInt() shl 8) or (buffer[0].toInt() and 0xFF)).toShort()
                    }
                }
                
                audioData
            }
        } catch (e: Exception) {
            e.printStackTrace()
            shortArrayOf()
        }
    }
    
    private fun writeWaveFile(file: File, audioData: ShortArray) {
        try {
            FileOutputStream(file).use { fos ->
                val dataSize = audioData.size * BYTES_PER_SAMPLE
                val fileSize = dataSize + 36
                
                // Write WAV header
                writeWaveHeader(fos, fileSize, dataSize, SAMPLE_RATE, 1, 16)
                
                // Write audio data
                val buffer = ByteArray(BYTES_PER_SAMPLE)
                for (sample in audioData) {
                    buffer[0] = (sample.toInt() and 0xFF).toByte()
                    buffer[1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                    fos.write(buffer)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun writeWaveHeader(
        fos: FileOutputStream,
        fileSize: Int,
        dataSize: Int,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) {
        val header = ByteArray(44)
        
        // RIFF header
        "RIFF".toByteArray().copyInto(header, 0)
        intToBytes(fileSize).copyInto(header, 4)
        "WAVE".toByteArray().copyInto(header, 8)
        
        // Format chunk
        "fmt ".toByteArray().copyInto(header, 12)
        intToBytes(16).copyInto(header, 16) // Chunk size
        shortToBytes(1).copyInto(header, 20) // Audio format (PCM)
        shortToBytes(channels).copyInto(header, 22)
        intToBytes(sampleRate).copyInto(header, 24)
        intToBytes(sampleRate * channels * bitsPerSample / 8).copyInto(header, 28) // Byte rate
        shortToBytes(channels * bitsPerSample / 8).copyInto(header, 32) // Block align
        shortToBytes(bitsPerSample).copyInto(header, 34)
        
        // Data chunk
        "data".toByteArray().copyInto(header, 36)
        intToBytes(dataSize).copyInto(header, 40)
        
        fos.write(header)
    }
    
    private fun intToBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }
    
    private fun shortToBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte()
        )
    }
    
    private fun applyNoiseReduction(audioData: ShortArray): ShortArray {
        if (audioData.isEmpty()) return audioData
        
        val windowSize = 256
        val result = audioData.copyOf()
        
        // Simple noise gate
        val threshold = calculateNoiseThreshold(audioData)
        
        for (i in result.indices) {
            if (abs(result[i].toInt()) < threshold) {
                result[i] = (result[i] * 0.3).toInt().toShort()
            }
        }
        
        return result
    }
    
    private fun calculateNoiseThreshold(audioData: ShortArray): Int {
        val sortedData = audioData.map { abs(it.toInt()) }.sorted()
        val percentile10 = sortedData[(sortedData.size * 0.1).toInt()]
        return percentile10
    }
    
    private fun normalizeAudio(audioData: ShortArray): ShortArray {
        if (audioData.isEmpty()) return audioData
        
        val maxValue = audioData.maxOf { abs(it.toInt()) }
        if (maxValue == 0) return audioData
        
        val normalizedData = ShortArray(audioData.size)
        val scaleFactor = Short.MAX_VALUE.toDouble() * 0.9 / maxValue
        
        for (i in audioData.indices) {
            val normalized = (audioData[i] * scaleFactor).toInt()
            normalizedData[i] = normalized.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        
        return normalizedData
    }
    
    private fun applyEqualization(audioData: ShortArray): ShortArray {
        if (audioData.isEmpty()) return audioData
        
        // Simple high-pass filter to remove low-frequency noise
        val result = ShortArray(audioData.size)
        val alpha = 0.95f
        
        result[0] = audioData[0]
        for (i in 1 until audioData.size) {
            result[i] = (alpha * (result[i-1] + audioData[i] - audioData[i-1])).toInt().toShort()
        }
        
        return result
    }
    
    fun analyzeAudioQuality(file: File): AudioQualityMetrics {
        return try {
            val audioData = readWaveFile(file)
            
            val rms = calculateRMS(audioData)
            val snr = calculateSNR(audioData)
            val dynamicRange = calculateDynamicRange(audioData)
            val spectralCentroid = calculateSpectralCentroid(audioData)
            
            AudioQualityMetrics(
                rms = rms,
                signalToNoiseRatio = snr,
                dynamicRange = dynamicRange,
                spectralCentroid = spectralCentroid,
                duration = file.length() / (SAMPLE_RATE * BYTES_PER_SAMPLE).toDouble()
            )
        } catch (e: Exception) {
            AudioQualityMetrics()
        }
    }
    
    private fun calculateRMS(audioData: ShortArray): Double {
        val sum = audioData.map { (it.toDouble() / Short.MAX_VALUE).pow(2) }.sum()
        return sqrt(sum / audioData.size)
    }
    
    private fun calculateSNR(audioData: ShortArray): Double {
        val signal = audioData.map { abs(it.toInt()) }.average()
        val noise = calculateNoiseThreshold(audioData).toDouble()
        return if (noise > 0) 20 * log10(signal / noise) else 0.0
    }
    
    private fun calculateDynamicRange(audioData: ShortArray): Double {
        val max = audioData.maxOf { abs(it.toInt()) }
        val min = audioData.filter { it != 0.toShort() }.minOfOrNull { abs(it.toInt()) } ?: 1
        return 20 * log10(max.toDouble() / min)
    }
    
    private fun calculateSpectralCentroid(audioData: ShortArray): Double {
        // Simplified spectral centroid calculation
        return audioData.mapIndexed { index, value -> 
            index * abs(value.toInt()) 
        }.sum().toDouble() / audioData.map { abs(it.toInt()) }.sum()
    }
}

data class AudioQualityMetrics(
    val rms: Double = 0.0,
    val signalToNoiseRatio: Double = 0.0,
    val dynamicRange: Double = 0.0,
    val spectralCentroid: Double = 0.0,
    val duration: Double = 0.0
) {
    fun getQualityScore(): Int {
        val rmsScore = (rms * 100).coerceIn(0.0, 100.0)
        val snrScore = (signalToNoiseRatio / 60 * 100).coerceIn(0.0, 100.0)
        val drScore = (dynamicRange / 96 * 100).coerceIn(0.0, 100.0)
        
        return ((rmsScore + snrScore + drScore) / 3).toInt()
    }
}