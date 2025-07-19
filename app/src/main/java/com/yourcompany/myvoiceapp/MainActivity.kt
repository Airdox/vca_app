package com.yourcompany.myvoiceapp

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // UI-Elemente
    private lateinit var statusTextView: TextView
    private lateinit var recordButton: Button
    private lateinit var stopRecordButton: Button
    private lateinit var playButton: Button
    private lateinit var stopPlayButton: Button

    // Audio-Variablen
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null

    // Status-Variablen
    private var isRecording = false
    private var isPlaying = false

    // Konstanten
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val LOG_TAG = "VoiceCloningApp"

    // Benötigte Berechtigungen
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(LOG_TAG, "onCreate: Activity erstellt.")

        // UI-Elemente initialisieren
        statusTextView = findViewById(R.id.statusTextView)
        recordButton = findViewById(R.id.recordButton)
        stopRecordButton = findViewById(R.id.stopRecordButton)
        playButton = findViewById(R.id.playButton)
        stopPlayButton = findViewById(R.id.stopPlayButton)

        // Initialen Zustand der Buttons setzen
        updateButtonStates()

        // Button-Listener setzen
        recordButton.setOnClickListener {
            if (checkPermissions()) {
                startRecording()
            } else {
                requestPermissions()
            }
        }

        stopRecordButton.setOnClickListener {
            stopRecording()
        }

        playButton.setOnClickListener {
            startPlaying()
        }

        stopPlayButton.setOnClickListener {
            stopPlaying()
        }
    }

    // Überprüfung der Berechtigungen
    private fun checkPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Berechtigungen anfordern
    private fun requestPermissions() {
        Log.d(LOG_TAG, "requestPermissions: Berechtigungen werden angefordert.")
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Berechtigungen erteilt!", Toast.LENGTH_SHORT).show()
                // Man könnte hier direkt die Aufnahme starten, wenn der Nutzer das beabsichtigt hat
                // startRecording() 
            } else {
                Toast.makeText(this, "Berechtigungen verweigert. App kann nicht alle Funktionen nutzen.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Aufnahme starten
    private fun startRecording() {
        if (isRecording) {
            Toast.makeText(this, "Aufnahme läuft bereits!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val audioFile = File(getExternalFilesDir(null), "voice_recording.3gp")
            audioFilePath = audioFile.absolutePath
            Log.d(LOG_TAG, "startRecording: Audiodatei wird gespeichert unter: $audioFilePath")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }

            isRecording = true
            updateButtonStates()
            statusTextView.text = "Aufnahme läuft..."
            Toast.makeText(this, "Aufnahme gestartet", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            Log.e(LOG_TAG, "startRecording: prepare() failed", e)
            statusTextView.text = "Fehler bei der Aufnahme"
            Toast.makeText(this, "Aufnahme fehlgeschlagen", Toast.LENGTH_SHORT).show()
            isRecording = false
            updateButtonStates()
        } catch (e: IllegalStateException) {
            Log.e(LOG_TAG, "startRecording: start() failed", e)
            statusTextView.text = "Fehler beim Start der Aufnahme"
            Toast.makeText(this, "Aufnahmestart fehlgeschlagen", Toast.LENGTH_SHORT).show()
            isRecording = false
            updateButtonStates()
        }
    }

    // Aufnahme stoppen
    private fun stopRecording() {
        if (!isRecording) {
            return
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            updateButtonStates()
            statusTextView.text = "Aufnahme gestoppt"
            Toast.makeText(this, "Aufnahme beendet", Toast.LENGTH_SHORT).show()
        } catch (e: RuntimeException) { // Catches IllegalStateException and others
            Log.e(LOG_TAG, "stopRecording: stop() failed", e)
            statusTextView.text = "Fehler beim Stoppen"
            Toast.makeText(this, "Fehler beim Stoppen der Aufnahme", Toast.LENGTH_SHORT).show()
            // Reset state
            mediaRecorder = null
            isRecording = false
            updateButtonStates()
        }
    }

    // Wiedergabe starten
    private fun startPlaying() {
        if (audioFilePath == null || !File(audioFilePath!!).exists()) {
            Toast.makeText(this, "Keine Aufnahme zum Abspielen vorhanden", Toast.LENGTH_SHORT).show()
            return
        }

        if (isPlaying) {
            Toast.makeText(this, "Wiedergabe läuft bereits", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFilePath)
                prepare()
                start()
                setOnCompletionListener {
                    stopPlaying()
                }
            }

            isPlaying = true
            updateButtonStates()
            statusTextView.text = "Wiedergabe läuft..."
            Toast.makeText(this, "Wiedergabe gestartet", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            Log.e(LOG_TAG, "startPlaying: prepare() failed", e)
            statusTextView.text = "Fehler bei der Wiedergabe"
            Toast.makeText(this, "Wiedergabe fehlgeschlagen", Toast.LENGTH_SHORT).show()
            isPlaying = false
            updateButtonStates()
        }
    }

    // Wiedergabe stoppen
    private fun stopPlaying() {
        if (!isPlaying) {
            return
        }

        mediaPlayer?.apply {
            if (isPlaying) { // Check again inside apply block
                stop()
            }
            release()
        }
        mediaPlayer = null
        isPlaying = false
        updateButtonStates()
        statusTextView.text = "Wiedergabe gestoppt"
        // Optional: Toast anzeigen, dass die Wiedergabe beendet wurde.
        // Toast.makeText(this, "Wiedergabe beendet", Toast.LENGTH_SHORT).show()
    }

    // Button-Zustände aktualisieren
    private fun updateButtonStates() {
        recordButton.isEnabled = !isRecording && !isPlaying
        stopRecordButton.isEnabled = isRecording
        playButton.isEnabled = !isRecording && !isPlaying && audioFilePath != null && File(audioFilePath!!).exists()
        stopPlayButton.isEnabled = isPlaying
    }

    override fun onStop() {
        super.onStop()
        if (isRecording) {
            stopRecording()
        }
        if (isPlaying) {
            stopPlaying()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaPlayer?.release()
        Log.d(LOG_TAG, "onDestroy: Activity wird zerstört und Ressourcen freigegeben.")
    }
}