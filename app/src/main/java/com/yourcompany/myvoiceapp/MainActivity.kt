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
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200 // Eindeutiger Request-Code für Berechtigungsanfragen
    private val LOG_TAG = "VoiceCloningApp"

    // Benötigte Berechtigungen (Nur RECORD_AUDIO ist für das Mikrofon erforderlich)
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO
        // Manifest.permission.WRITE_EXTERNAL_STORAGE // Dies wird hier NICHT benötigt, da getExternalFilesDir verwendet wird
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

        // NEU: Berechtigungen direkt beim Start der App anfordern
        if (!checkPermissions()) { // Wenn Berechtigungen NICHT erteilt sind
            Log.d(LOG_TAG, "onCreate: Berechtigungen fehlen, fordern an.")
            requestPermissions() // Fordere sie an
        } else {
            // Optional: Zeige eine Toast-Nachricht, wenn Berechtigungen bereits erteilt sind
            Toast.makeText(this, "Alle Berechtigungen bereits erteilt.", Toast.LENGTH_SHORT).show()
            Log.d(LOG_TAG, "onCreate: Alle Berechtigungen bereits erteilt.")
        }

        // Initialen Zustand der Buttons setzen (vor den Listenern, da diese den Zustand ändern)
        updateButtonStates()

        // Button-Listener setzen
        recordButton.setOnClickListener {
            if (checkPermissions()) { // Überprüfe die Berechtigungen vor dem Start der Aufnahme
                Log.d(LOG_TAG, "recordButton: Berechtigungen vorhanden, starte Aufnahme.")
                startRecording()
            } else {
                Toast.makeText(this, "Mikrofonberechtigung nicht erteilt. Bitte erteilen Sie diese, um aufzunehmen.", Toast.LENGTH_LONG).show()
                Log.w(LOG_TAG, "recordButton: Mikrofonberechtigung fehlt beim Klick auf Aufnahme-Button.")
                // Optional: Benutzer zu den App-Einstellungen leiten
                // val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                // val uri = Uri.fromParts("package", packageName, null)
                // intent.data = uri
                // startActivity(intent)
            }
        }

        stopRecordButton.setOnClickListener {
            Log.d(LOG_TAG, "stopRecordButton: Stoppe Aufnahme.")
            stopRecording()
        }

        playButton.setOnClickListener {
            Log.d(LOG_TAG, "playButton: Starte Wiedergabe.")
            startPlaying()
        }

        stopPlayButton.setOnClickListener {
            Log.d(LOG_TAG, "stopPlayButton: Stoppe Wiedergabe.")
            stopPlaying()
        }
    }

    // Überprüfung der Berechtigungen
    private fun checkPermissions(): Boolean {
        // Überprüft, ob ALLE benötigten Berechtigungen erteilt sind.
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        Log.d(LOG_TAG, "checkPermissions: Alle Berechtigungen erteilt? $allGranted")
        return allGranted
    }

    // Berechtigungen anfordern
    private fun requestPermissions() {
        Log.d(LOG_TAG, "requestPermissions: Berechtigungen werden angefordert.")
        // Fordert die Berechtigungen vom Benutzer an. Der Dialog wird vom System angezeigt.
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    // Callback-Methode, die aufgerufen wird, nachdem der Benutzer auf den Berechtigungs-Dialog reagiert hat.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(LOG_TAG, "onRequestPermissionsResult: Empfangen für RequestCode: $requestCode")

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Alle Berechtigungen erteilt!", Toast.LENGTH_SHORT).show()
                Log.d(LOG_TAG, "onRequestPermissionsResult: Berechtigungen erteilt.")
            } else {
                Toast.makeText(this, "Berechtigungen verweigert. App kann nicht alle Funktionen nutzen.", Toast.LENGTH_LONG).show()
                Log.w(LOG_TAG, "onRequestPermissionsResult: Berechtigungen verweigert.")
            }
        }
    }

    // Aufnahme starten
    private fun startRecording() {
        if (isRecording) {
            Toast.makeText(this, "Aufnahme läuft bereits!", Toast.LENGTH_SHORT).show()
            return
        }

        // Stelle sicher, dass die Berechtigung VOR der Aufnahme geprüft wird
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Mikrofonberechtigung nicht erteilt.", Toast.LENGTH_LONG).show()
            Log.e(LOG_TAG, "startRecording: Versuch, Aufnahme ohne Berechtigung zu starten.")
            return
        }

        try {
            // Erstellt eine Datei im App-spezifischen Speicher.
            // Dies benötigt KEINE WRITE_EXTERNAL_STORAGE Berechtigung auf Android 10 (API 29) und höher.
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
            Log.d(LOG_TAG, "startRecording: Aufnahme erfolgreich gestartet.")

        } catch (e: IOException) {
            Log.e(LOG_TAG, "startRecording: prepare() failed", e)
            statusTextView.text = "Fehler bei der Aufnahme: " + e.localizedMessage
            Toast.makeText(this, "Aufnahme fehlgeschlagen: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            isRecording = false
            updateButtonStates()
        } catch (e: IllegalStateException) {
            Log.e(LOG_TAG, "startRecording: start() failed, state error", e)
            statusTextView.text = "Fehler beim Start der Aufnahme: " + e.localizedMessage
            Toast.makeText(this, "Aufnahmestart fehlgeschlagen: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            isRecording = false
            updateButtonStates()
        }
    }

    // Aufnahme stoppen
    private fun stopRecording() {
        if (!isRecording) {
            Log.d(LOG_TAG, "stopRecording: Keine Aufnahme läuft.")
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
            Log.d(LOG_TAG, "stopRecording: Aufnahme erfolgreich gestoppt.")
        } catch (e: RuntimeException) { // Fängt IllegalStateException und andere ab, die beim Stoppen auftreten können
            Log.e(LOG_TAG, "stopRecording: stop() failed or release() failed", e)
            statusTextView.text = "Fehler beim Stoppen: " + e.localizedMessage
            Toast.makeText(this, "Fehler beim Stoppen der Aufnahme: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            // Zustand zurücksetzen bei Fehler
            mediaRecorder = null
            isRecording = false
            updateButtonStates()
        }
    }

    // Wiedergabe starten
    private fun startPlaying() {
        // Überprüfe, ob eine Audiodatei zum Abspielen vorhanden ist
        if (audioFilePath == null || !File(audioFilePath!!).exists()) {
            Toast.makeText(this, "Keine Aufnahme zum Abspielen vorhanden", Toast.LENGTH_SHORT).show()
            Log.w(LOG_TAG, "startPlaying: Kein audioFilePath oder Datei existiert nicht.")
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
                    Log.d(LOG_TAG, "startPlaying: Wiedergabe beendet.")
                    stopPlaying()
                }
            }

            isPlaying = true
            updateButtonStates()
            statusTextView.text = "Wiedergabe läuft..."
            Toast.makeText(this, "Wiedergabe gestartet", Toast.LENGTH_SHORT).show()
            Log.d(LOG_TAG, "startPlaying: Wiedergabe erfolgreich gestartet.")

        } catch (e: IOException) {
            Log.e(LOG_TAG, "startPlaying: prepare() failed", e)
            statusTextView.text = "Fehler bei der Wiedergabe: " + e.localizedMessage
            Toast.makeText(this, "Wiedergabe fehlgeschlagen: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            isPlaying = false
            updateButtonStates()
        } catch (e: IllegalStateException) {
            Log.e(LOG_TAG, "startPlaying: start() failed, state error", e)
            statusTextView.text = "Fehler beim Start der Wiedergabe: " + e.localizedMessage
            Toast.makeText(this, "Wiedergabestart fehlgeschlagen: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            isPlaying = false
            updateButtonStates()
        }
    }

    // Wiedergabe stoppen
    private fun stopPlaying() {
        if (!isPlaying) {
            Log.d(LOG_TAG, "stopPlaying: Keine Wiedergabe läuft.")
            return
        }

        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        isPlaying = false
        updateButtonStates()
        statusTextView.text = "Wiedergabe gestoppt"
        Toast.makeText(this, "Wiedergabe beendet", Toast.LENGTH_SHORT).show()
        Log.d(LOG_TAG, "stopPlaying: Wiedergabe erfolgreich gestoppt.")
    }

    // Button-Zustände aktualisieren (Aktivieren/Deaktivieren)
    private fun updateButtonStates() {
        recordButton.isEnabled = !isRecording && !isPlaying // Aufnahme-Button ist nur aktiv, wenn weder aufgenommen noch abgespielt wird
        stopRecordButton.isEnabled = isRecording // Stop-Aufnahme-Button ist nur aktiv, wenn aufgenommen wird
        playButton.isEnabled = !isRecording && !isPlaying && audioFilePath != null && File(audioFilePath!!).exists() // Wiedergabe-Button ist aktiv, wenn nichts läuft und eine Datei existiert
        stopPlayButton.isEnabled = isPlaying // Stop-Wiedergabe-Button ist nur aktiv, wenn abgespielt wird
        Log.d(LOG_TAG, "updateButtonStates: isRecording=$isRecording, isPlaying=$isPlaying, audioFileExists=${audioFilePath != null && File(audioFilePath!!).exists()}")
    }

    // Lebenszyklus-Methoden zum Freigeben von Ressourcen
    override fun onStop() {
        super.onStop()
        // Stoppe Aufnahme und Wiedergabe, wenn die Aktivität gestoppt wird
        if (isRecording) {
            Log.d(LOG_TAG, "onStop: Stoppe Aufnahme.")
            stopRecording()
        }
        if (isPlaying) {
            Log.d(LOG_TAG, "onStop: Stoppe Wiedergabe.")
            stopPlaying()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Gib MediaRecorder und MediaPlayer Ressourcen frei, wenn die Aktivität zerstört wird
        mediaRecorder?.release()
        mediaPlayer?.release()
        Log.d(LOG_TAG, "onDestroy: Activity wird zerstört und Ressourcen freigegeben.")
    }
}