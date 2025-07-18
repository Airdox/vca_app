package com.airdox.voicecloning.ui.training

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.airdox.voicecloning.R
import com.airdox.voicecloning.audio.AudioRecordingManager
import com.airdox.voicecloning.model.VoiceCloningModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class TrainingActivity : AppCompatActivity() {
    
    private lateinit var recordingManager: AudioRecordingManager
    private lateinit var voiceModel: VoiceCloningModel
    
    private lateinit var btnStartTraining: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var textStatus: TextView
    private lateinit var textAudioCount: TextView
    private lateinit var textProgress: TextView
    
    private var isTraining = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)
        
        setupUI()
        initializeComponents()
        updateAudioCount()
    }
    
    private fun setupUI() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.training_title)
        
        btnStartTraining = findViewById(R.id.btn_start_training)
        progressBar = findViewById(R.id.progress_training)
        textStatus = findViewById(R.id.text_training_status)
        textAudioCount = findViewById(R.id.text_audio_count)
        textProgress = findViewById(R.id.text_training_progress)
        
        btnStartTraining.setOnClickListener { startTraining() }
    }
    
    private fun initializeComponents() {
        recordingManager = AudioRecordingManager(this)
        voiceModel = VoiceCloningModel(this)
    }
    
    private fun updateAudioCount() {
        val audioFiles = recordingManager.getTrainingFiles()
        textAudioCount.text = getString(R.string.training_audio_count, audioFiles.size)
        
        btnStartTraining.isEnabled = audioFiles.isNotEmpty() && !isTraining
        
        if (audioFiles.isEmpty()) {
            textStatus.text = "Keine Audioaufnahmen gefunden. Bitte nehmen Sie zunächst einige Sprachsamples auf."
        } else {
            textStatus.text = "Bereit für Training"
        }
    }
    
    private fun startTraining() {
        if (isTraining) return
        
        val audioFiles = recordingManager.getTrainingFiles()
        if (audioFiles.isEmpty()) {
            Toast.makeText(this, "Keine Audiodateien für das Training gefunden", Toast.LENGTH_LONG).show()
            return
        }
        
        if (audioFiles.size < 3) {
            Toast.makeText(this, "Mindestens 3 Audioaufnahmen werden empfohlen", Toast.LENGTH_LONG).show()
        }
        
        isTraining = true
        updateUI()
        
        lifecycleScope.launch {
            try {
                voiceModel.trainModel(audioFiles) { progress, status ->
                    runOnUiThread {
                        updateTrainingProgress(progress, status)
                    }
                }
                
                runOnUiThread {
                    onTrainingComplete(true)
                }
                
            } catch (e: Exception) {
                runOnUiThread {
                    onTrainingComplete(false, e.message)
                }
            }
        }
    }
    
    private fun updateTrainingProgress(progress: Int, status: String) {
        progressBar.progress = progress
        textProgress.text = getString(R.string.training_progress, progress)
        textStatus.text = status
    }
    
    private fun onTrainingComplete(success: Boolean, error: String? = null) {
        isTraining = false
        
        if (success) {
            progressBar.progress = 100
            textStatus.text = getString(R.string.training_status_completed)
            textProgress.text = getString(R.string.training_progress, 100)
            Toast.makeText(this, "Training erfolgreich abgeschlossen!", Toast.LENGTH_LONG).show()
        } else {
            textStatus.text = "Training fehlgeschlagen: ${error ?: "Unbekannter Fehler"}"
            progressBar.progress = 0
            textProgress.text = ""
            Toast.makeText(this, "Training fehlgeschlagen", Toast.LENGTH_LONG).show()
        }
        
        updateUI()
    }
    
    private fun updateUI() {
        btnStartTraining.isEnabled = !isTraining && recordingManager.getTrainingFiles().isNotEmpty()
        btnStartTraining.text = if (isTraining) "Training läuft..." else getString(R.string.training_start)
        
        progressBar.isIndeterminate = false
        if (isTraining && progressBar.progress == 0) {
            progressBar.isIndeterminate = true
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateAudioCount()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}