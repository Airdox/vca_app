package com.airdox.voicecloning.ui.tts

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.airdox.voicecloning.R
import com.airdox.voicecloning.model.VoiceCloningModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.io.File

class TTSActivity : AppCompatActivity() {
    
    private lateinit var voiceModel: VoiceCloningModel
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioFile: File? = null
    
    private lateinit var textInputLayout: TextInputLayout
    private lateinit var editText: TextInputEditText
    private lateinit var btnGenerate: MaterialButton
    private lateinit var btnPlay: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnShare: MaterialButton
    
    private var isGenerating = false
    private var isPlaying = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tts)
        
        setupUI()
        initializeComponents()
        checkModelAvailability()
    }
    
    private fun setupUI() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.tts_title)
        
        textInputLayout = findViewById(R.id.text_input_layout)
        editText = findViewById(R.id.edit_text_input)
        btnGenerate = findViewById(R.id.btn_generate)
        btnPlay = findViewById(R.id.btn_play)
        btnSave = findViewById(R.id.btn_save)
        btnShare = findViewById(R.id.btn_share)
        
        btnGenerate.setOnClickListener { generateSpeech() }
        btnPlay.setOnClickListener { togglePlayback() }
        btnSave.setOnClickListener { saveAudio() }
        btnShare.setOnClickListener { shareAudio() }
        
        updateUI()
    }
    
    private fun initializeComponents() {
        voiceModel = VoiceCloningModel(this)
    }
    
    private fun checkModelAvailability() {
        if (!voiceModel.isModelAvailable()) {
            Toast.makeText(
                this, 
                "Kein trainiertes Stimmmodell gefunden. Bitte trainieren Sie zunächst ein Modell.",
                Toast.LENGTH_LONG
            ).show()
            
            btnGenerate.isEnabled = false
            btnGenerate.text = "Modell nicht verfügbar"
        }
    }
    
    private fun generateSpeech() {
        val text = editText.text?.toString()?.trim()
        
        if (text.isNullOrBlank()) {
            textInputLayout.error = "Bitte geben Sie einen Text ein"
            return
        }
        
        if (text.length > 500) {
            textInputLayout.error = "Text ist zu lang (max. 500 Zeichen)"
            return
        }
        
        textInputLayout.error = null
        isGenerating = true
        updateUI()
        
        lifecycleScope.launch {
            try {
                val audioFile = voiceModel.generateSpeech(text)
                
                if (audioFile != null && audioFile.exists()) {
                    currentAudioFile = audioFile
                    Toast.makeText(this@TTSActivity, "Sprache erfolgreich generiert!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@TTSActivity, "Fehler bei der Sprachgenerierung", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@TTSActivity, "Fehler: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isGenerating = false
                updateUI()
            }
        }
    }
    
    private fun togglePlayback() {
        if (isPlaying) {
            stopPlayback()
        } else {
            currentAudioFile?.let { file ->
                startPlayback(file)
            }
        }
    }
    
    private fun startPlayback(file: File) {
        try {
            stopPlayback() // Stop any current playback
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    isPlaying = false
                    updateUI()
                }
                start()
            }
            
            isPlaying = true
            updateUI()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Fehler bei der Wiedergabe: ${e.message}", Toast.LENGTH_SHORT).show()
            isPlaying = false
            updateUI()
        }
    }
    
    private fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        isPlaying = false
        updateUI()
    }
    
    private fun saveAudio() {
        currentAudioFile?.let { file ->
            try {
                val downloadsDir = File(getExternalFilesDir(null), "downloads")
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                
                val savedFile = File(downloadsDir, "tts_${System.currentTimeMillis()}.wav")
                file.copyTo(savedFile, overwrite = true)
                
                Toast.makeText(this, "Audio gespeichert: ${savedFile.name}", Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                Toast.makeText(this, "Fehler beim Speichern: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun shareAudio() {
        currentAudioFile?.let { file ->
            try {
                val uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                )
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Generierte Sprache mit VoiceCloning Pro")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                startActivity(Intent.createChooser(shareIntent, "Audio teilen"))
                
            } catch (e: Exception) {
                Toast.makeText(this, "Fehler beim Teilen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateUI() {
        btnGenerate.isEnabled = !isGenerating && voiceModel.isModelAvailable()
        btnGenerate.text = when {
            isGenerating -> "Generiere..."
            !voiceModel.isModelAvailable() -> "Modell nicht verfügbar"
            else -> getString(R.string.tts_generate)
        }
        
        val hasAudio = currentAudioFile != null
        btnPlay.isEnabled = hasAudio && !isGenerating
        btnSave.isEnabled = hasAudio && !isGenerating
        btnShare.isEnabled = hasAudio && !isGenerating
        
        btnPlay.text = if (isPlaying) "Stop" else getString(R.string.tts_play)
        
        editText.isEnabled = !isGenerating
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}