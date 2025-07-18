package com.airdox.voicecloning.ui.recording

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.airdox.voicecloning.R
import com.airdox.voicecloning.audio.AudioProcessor
import com.airdox.voicecloning.audio.AudioRecordingManager
import com.airdox.voicecloning.audio.WaveformView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.io.File

class RecordingActivity : AppCompatActivity() {
    
    private lateinit var recordingManager: AudioRecordingManager
    private lateinit var audioProcessor: AudioProcessor
    private lateinit var waveformView: WaveformView
    
    private lateinit var btnRecord: FloatingActionButton
    private lateinit var btnPlay: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var btnUpload: MaterialButton
    private lateinit var btnEnhance: MaterialButton
    
    private var isRecording = false
    private var isPlaying = false
    private var currentAudioFile: File? = null
    
    private val recordingTexts = listOf(
        "Guten Tag, ich bin hier, um meine Stimme für das Training aufzunehmen.",
        "Die Qualität der Aufnahme ist sehr wichtig für ein gutes Ergebnis.",
        "Bitte sprechen Sie klar und deutlich in das Mikrofon.",
        "Verschiedene Sätze helfen dabei, ein besseres Stimmmodell zu erstellen.",
        "Machine Learning benötigt qualitativ hochwertige Audiodaten.",
        "Diese Technologie ermöglicht es, realistische Stimmen zu synthetisieren.",
        "Je mehr Aufnahmen Sie machen, desto besser wird das Ergebnis.",
        "Versuchen Sie, eine natürliche Sprechweise beizubehalten.",
        "Die Aufnahmen werden sicher auf Ihrem Gerät gespeichert.",
        "Vielen Dank für Ihre Teilnahme an diesem Trainingsprozess."
    )
    
    private var currentTextIndex = 0
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] == true &&
            permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
            initializeRecording()
        } else {
            Toast.makeText(this, getString(R.string.permission_audio_message), Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleUploadedFile(uri)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)
        
        setupUI()
        checkPermissions()
    }
    
    private fun setupUI() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.recording_title)
        
        // Initialize views
        waveformView = findViewById(R.id.waveform_view)
        btnRecord = findViewById(R.id.btn_record)
        btnPlay = findViewById(R.id.btn_play)
        btnSave = findViewById(R.id.btn_save)
        btnDelete = findViewById(R.id.btn_delete)
        btnUpload = findViewById(R.id.btn_upload)
        btnEnhance = findViewById(R.id.btn_enhance)
        
        // Set up click listeners
        btnRecord.setOnClickListener { toggleRecording() }
        btnPlay.setOnClickListener { togglePlayback() }
        btnSave.setOnClickListener { saveRecording() }
        btnDelete.setOnClickListener { deleteRecording() }
        btnUpload.setOnClickListener { uploadFile() }
        btnEnhance.setOnClickListener { enhanceAudio() }
        
        // Update UI state
        updateUI()
        updateRecordingText()
    }
    
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            initializeRecording()
        }
    }
    
    private fun initializeRecording() {
        recordingManager = AudioRecordingManager(this)
        audioProcessor = AudioProcessor(this)
        
        recordingManager.onRecordingUpdate = { amplitude ->
            waveformView.addAmplitude(amplitude)
        }
        
        recordingManager.onRecordingComplete = { file ->
            currentAudioFile = file
            updateUI()
            Toast.makeText(this, "Aufnahme gespeichert", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }
    
    private fun startRecording() {
        isRecording = true
        recordingManager.startRecording()
        updateUI()
        waveformView.startRecording()
    }
    
    private fun stopRecording() {
        isRecording = false
        recordingManager.stopRecording()
        updateUI()
        waveformView.stopRecording()
        
        // Move to next text
        currentTextIndex = (currentTextIndex + 1) % recordingTexts.size
        updateRecordingText()
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
        isPlaying = true
        recordingManager.playRecording(file) {
            isPlaying = false
            updateUI()
        }
        updateUI()
    }
    
    private fun stopPlayback() {
        isPlaying = false
        recordingManager.stopPlayback()
        updateUI()
    }
    
    private fun saveRecording() {
        currentAudioFile?.let { file ->
            lifecycleScope.launch {
                val success = recordingManager.saveToTrainingSet(file)
                if (success) {
                    Toast.makeText(this@RecordingActivity, "Aufnahme zum Training hinzugefügt", Toast.LENGTH_SHORT).show()
                    deleteRecording()
                } else {
                    Toast.makeText(this@RecordingActivity, "Fehler beim Speichern", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun deleteRecording() {
        currentAudioFile?.delete()
        currentAudioFile = null
        waveformView.clear()
        updateUI()
        Toast.makeText(this, "Aufnahme gelöscht", Toast.LENGTH_SHORT).show()
    }
    
    private fun uploadFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun handleUploadedFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                val file = recordingManager.importAudioFile(uri)
                if (file != null) {
                    currentAudioFile = file
                    waveformView.loadAudioFile(file)
                    updateUI()
                    Toast.makeText(this@RecordingActivity, "Datei erfolgreich geladen", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@RecordingActivity, "Fehler beim Laden der Datei", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecordingActivity, "Fehler: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun enhanceAudio() {
        currentAudioFile?.let { file ->
            lifecycleScope.launch {
                try {
                    val enhancedFile = audioProcessor.enhanceAudio(file)
                    currentAudioFile = enhancedFile
                    waveformView.loadAudioFile(enhancedFile)
                    updateUI()
                    Toast.makeText(this@RecordingActivity, "Audio verbessert", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@RecordingActivity, "Fehler bei der Verbesserung: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateRecordingText() {
        findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.text_recording_prompt).text = 
            recordingTexts[currentTextIndex]
    }
    
    private fun updateUI() {
        btnRecord.setImageResource(
            if (isRecording) R.drawable.ic_stop else R.drawable.ic_mic
        )
        
        btnPlay.isEnabled = currentAudioFile != null && !isRecording
        btnSave.isEnabled = currentAudioFile != null && !isRecording
        btnDelete.isEnabled = currentAudioFile != null && !isRecording
        btnEnhance.isEnabled = currentAudioFile != null && !isRecording
        
        btnPlay.text = if (isPlaying) "Stop" else getString(R.string.recording_play)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::recordingManager.isInitialized) {
            recordingManager.cleanup()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}