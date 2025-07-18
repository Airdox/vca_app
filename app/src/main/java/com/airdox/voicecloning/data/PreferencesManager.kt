package com.airdox.voicecloning.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferencesManager(context: Context) {
    
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    companion object {
        private const val SAMPLE_RATE_KEY = "sample_rate"
        private const val BIT_RATE_KEY = "bit_rate"
        private const val MODEL_QUALITY_KEY = "model_quality"
        private const val PROCESSING_POWER_KEY = "processing_power"
        private const val FIRST_RUN_KEY = "first_run"
        private const val TRAINING_COUNT_KEY = "training_count"
    }
    
    fun getSampleRate(): Int = preferences.getString(SAMPLE_RATE_KEY, "44100")?.toInt() ?: 44100
    
    fun getBitRate(): Int = preferences.getString(BIT_RATE_KEY, "128")?.toInt() ?: 128
    
    fun getModelQuality(): String = preferences.getString(MODEL_QUALITY_KEY, "balanced") ?: "balanced"
    
    fun getProcessingPower(): String = preferences.getString(PROCESSING_POWER_KEY, "balanced") ?: "balanced"
    
    fun isFirstRun(): Boolean = preferences.getBoolean(FIRST_RUN_KEY, true)
    
    fun setFirstRunCompleted() {
        preferences.edit().putBoolean(FIRST_RUN_KEY, false).apply()
    }
    
    fun getTrainingCount(): Int = preferences.getInt(TRAINING_COUNT_KEY, 0)
    
    fun incrementTrainingCount() {
        val current = getTrainingCount()
        preferences.edit().putInt(TRAINING_COUNT_KEY, current + 1).apply()
    }
    
    fun getQualityMultiplier(): Float {
        return when (getModelQuality()) {
            "fast" -> 0.8f
            "balanced" -> 1.0f
            "high" -> 1.3f
            "perfect" -> 1.6f
            else -> 1.0f
        }
    }
    
    fun getProcessingThreads(): Int {
        return when (getProcessingPower()) {
            "eco" -> 1
            "balanced" -> 2
            "performance" -> 4
            "maximum" -> Runtime.getRuntime().availableProcessors()
            else -> 2
        }
    }
}