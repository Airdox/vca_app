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

    // Benötigte Berechtigungen
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE // Für Android < 29
        // Manifest.permission.READ_EXTERNAL_STORAGE // Für Android < 29, nur wenn du Dateien außerhalb des App-spezifischen Speichers lesen willst
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

        // NEU: Berechtigungen direkt beim Start der App anfordern
        if (!checkPermissions()) { // Wenn Berechtigungen NICHT erteilt sind
            requestPermissions() // Fordere sie an
        } else {
            // Optional: Zeige eine Toast-Nachricht, wenn Berechtigungen bereits erteilt sind
            Toast.makeText(this, "Alle Berechtigungen bereits erteilt.", Toast.LENGTH_SHORT).show()
        }

        // Button-Listener setzen
        recordButton.setOnClickListener {
            // NEU: Nur noch checken, aber nicht mehr extra anfordern, da dies bei App-Start geschieht
            if (checkPermissions()) {
                startRecording()
            } else {
                Toast.makeText(this, "Mikrofonberechtigung nicht erteilt. Bitte erteilen Sie diese in den App-Einstellungen.", Toast.LENGTH_LONG).show()
                // Optional: Hier könnten Sie den Benutzer zu den App-Einstellungen leiten.
                // val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                // val uri = Uri.fromParts("package", packageName, null)
                // intent.data = uri
                // startActivity(intent)
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
        // Überprüft, ob ALLE benötigten Berechtigungen erteilt sind.
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
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

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            // Überprüfe, ob alle angeforderten Berechtigungen erteilt wurden.
            // grantResults.isNotEmpty() stellt sicher, dass eine Antwort vorhanden ist.
            // grantResults.all { it == PackageManager.PERMISSION_GRANTED } prüft, ob alle erteilt wurden.
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Alle Berechtigungen erteilt!", Toast.LENGTH_SHORT).show()
                // Hier könntest du direkt die Aufnahmefunktion starten,
                // falls der Klick auf den Aufnahme-Button dies ausgelöst hat
                // (wird aber durch den Button-Listener selbst gehandhabt).
            } else {
                Toast.makeText(this, "Berechtigungen verweigert. App kann nicht alle Funktionen nutzen.", Toast.LENGTH_LONG).show()
                // Optional: Zeige eine dauerhaftere Meldung oder deaktiviere die Funktionalität.
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
            // Erstellt eine Datei im App-spezifischen Speicher (benötigt keine WRITE_EXTERNAL_STORAGE ab Android 10).
            val audioFile = File(getExternalFilesDir(null), "voice_recording.3gp")
            audioFilePath = audioFile.absolutePath
            Log.d(LOG_TAG, "startRecording: Audiodatei wird gespeichert unter: $audioFilePath")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC) // Audioquelle: Mikrofon
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // Ausgabeformat
                setOutputFile(audioFilePath) // Pfad zur Ausgabedatei
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // Audio-Encoder
                prepare() // MediaRecorder vorbereiten
                start() // Aufnahme starten
            }

            isRecording = true
            updateButtonStates() // Button-Zustände aktualisieren
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
                stop() // Aufnahme stoppen
                release() // Ressourcen freigeben
            }
            mediaRecorder = null
            isRecording = false
            updateButtonStates()
            statusTextView.text = "Aufnahme gestoppt"
            Toast.makeText(this, "Aufnahme beendet", Toast.LENGTH_SHORT).show()
        } catch (e: RuntimeException) { // Fängt IllegalStateException und andere ab
            Log.e(LOG_TAG, "stopRecording: stop() failed", e)
            statusTextView.text = "Fehler beim Stoppen"
            Toast.makeText(this, "Fehler beim Stoppen der Aufnahme", Toast.LENGTH_SHORT).show()
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
            return
        }

        if (isPlaying) {
            Toast.makeText(this, "Wiedergabe läuft bereits", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFilePath) // Pfad zur Audiodatei setzen
                prepare() // MediaPlayer vorbereiten
                start() // Wiedergabe starten
                // Listener für das Ende der Wiedergabe
                setOnCompletionListener {
                    stopPlaying() // Wiedergabe stoppen, wenn die Datei beendet ist
                }
            }

            isPlaying = true
            updateButtonStates() // Button-Zustände aktualisieren
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
            if (isPlaying) { // Überprüfe erneut innerhalb des apply-Blocks
                stop() // Wiedergabe stoppen
            }
            release() // Ressourcen freigeben
        }
        mediaPlayer = null
        isPlaying = false
        updateButtonStates()
        statusTextView.text = "Wiedergabe gestoppt"
        // Optional: Toast anzeigen, dass die Wiedergabe beendet wurde.
        // Toast.makeText(this, "Wiedergabe beendet", Toast.LENGTH_SHORT).show()
    }

    // Button-Zustände aktualisieren (Aktivieren/Deaktivieren)
    private fun updateButtonStates() {
        recordButton.isEnabled = !isRecording && !isPlaying // Aufnahme-Button ist nur aktiv, wenn weder aufgenommen noch abgespielt wird
        stopRecordButton.isEnabled = isRecording // Stop-Aufnahme-Button ist nur aktiv, wenn aufgenommen wird
        playButton.isEnabled = !isRecording && !isPlaying && audioFilePath != null && File(audioFilePath!!).exists() // Wiedergabe-Button ist aktiv, wenn nichts läuft und eine Datei existiert
        stopPlayButton.isEnabled = isPlaying // Stop-Wiedergabe-Button ist nur aktiv, wenn abgespielt wird
    }

    // Lebenszyklus-Methoden zum Freigeben von Ressourcen
    override fun onStop() {
        super.onStop()
        // Stoppe Aufnahme und Wiedergabe, wenn die Aktivität gestoppt wird
        if (isRecording) {
            stopRecording()
        }
        if (isPlaying) {
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