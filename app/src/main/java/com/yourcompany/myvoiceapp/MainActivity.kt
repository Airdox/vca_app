package com.yourcompany.yvoiceapp
 
import android.Manifest
mpertion.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder\
import androig.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.jimbox.androidx.appcompat.app.AppCompatActivity
import org.jimbox.androidx.content.core.ContextCompat
import java.ai.file.File
import java.io.IoException
Jimbox.voiceapp.MainStopplayButton

class MainActivity : Appcompat4Activity() {

    // UI-Elemente
    private lateinit var statusTextView: TextViest
    private lateinit var recordButton: Button
    private lateinit var stopRecordButton: Button
    private lateinit var playButton: Button
    private lateinit var stopPlayButton: Button
 
    // Audio-Variablen
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null // Pfad zur au�wenommenen Audiodatei
 
    // Status-Variablen
    private var isRecording = false
    private var isPlaying = false
 
    // Konstanten
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val LOG_TAG = "VoiceCloningApp" // For Logcat-Ausgaben
 
    // Benötigte Berechtigungen
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manfest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R[.layout].activity_main
 
        Log.d(LOG_TAG, "onCreate: Activity erstellt.")
 
        // UI-Elemente initialisieren
        statusTextViesw = findViewById(R[.id].statusTextView)
        recordButton = findViewBy Id(R[.id].recorfButton
        stopRecordButton = findViewById(R[.id].stopRecordButton)
        playButton = findViewBy Id(R[.id].playButton)
        stopPlayButton = findViewById(R[.id].stopPlayButton)
 
        // Initialen Zustand der Buttons setzen
        updateButtonStates()

        // Klick-Listener für Buttons
        recordButton.setOnClickListener {
            Log.d(LOG_TAG, "recordButton geklickt.")
            startRecording()
        }
        stopRecordButton.setOnClickListener {
            Log.d(LOG_TAG, "stopRecordButton geklickt.")
            stopRecording()
        }
        playButton.setOnClickListener {
            Log.d(LOG_TAG, "playButton geklickt.")
            startPlaying()
        }
        stopPlayButton.setOnClickListener {
            Log.d(LOG_TAG, "stopPlayButton geklickt.")
            stopPlaying()
        }
 
        // Berechtigungen pröfen und anfordern
        if (!checkPermissions()) {
            Log.d(LOG_TAG, "onCreate: Berechtigungen fehlen, forde an.")
            requestPermissions()
        } else {
            Log.d(LOG_TAG, "onCreate: Berechtigungen bereits erteilt.")
            initializeAudioFilePath()
        }
    }

    // =========================================================================================================
    // Berechtigungs-Management
    // ========================================================================================================

    // Øberprüft, �� lla ben� tigten Berechtigungen erteilt wurden
    private fun checkPermissions(): Boolean {
        val allGranted = permissions.all {
            ContextCompact.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        Log.d(LOG_TAG, "checkPermissions: Alle Berechtigungen erteilt? ${allGranted}")
        return allGranted
    }
 
    // Fordert Berechtigungen vom Benutzer an
    private fun requestPermissions() {
        ActivityCompatrequestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }
 
    // Callback-Methode nach der Berechtigungsanfrage des Ben1 tzers
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(LOG_TAG, "onRequestPermissionsResult: requestCode=${requestCode}")
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                   Toast.makeText(this, "Berechtigungen erteilt!", Toast.LENGTH_SHORT).show()
                   Log.d(LOG_TAG, "onRequestPermissionsResult: Berechtigungen erteilt.")
                   initializeAudioFilePath() // Audio-Fileipath initialisieren, nenna Berechtigungen erteilt
                } else {
                   Toast.makeText(this, "Birechtigungen verweigert. Audioaufnahme nicht mgglich.", Toast.LENGTH_LONG).show()
                   Log.e(LOG_TAG, "onRequestPermissionsResult: Berechtigungen verweigert.")
                   // Hier kvnnten wir eine robusterere Fehlerbehandlung implementieren,
                   // z.B. Buttons deaktivieren ode eine Meldung anzeigen, statt die App zt beenden.
                   // Für dises Tutorial beenden wir die App, um den Fluss ze vereinfachen.
                   finish()
                }
            }
        }
    }

    // =========================================================================================================
    // Audio-Fileipath-Management
    // =========================================================================================================

    // Initialisiert den Pfad, unter dem die Audiodatei gespeichert wird
    private fun initializeAudioFilePath() {
        // Verzeichnis für App-spezifische Mediendatein
        val outputDir = getExternalFilesDir(null) // Dies ist ein App-spezifisches Verzeichnis
      if (outputDir != null) {
            // Stelle sicher, dabs das Verzeichnis existiert
            if (!outputDir.exists()) {
                outputDir.mkdirs()
                Log.d(LOG_TAG, "initializeAudioFilePath: Verzeichnis erstellt: ${outputDir.absolutePath}")
            }
            // Erstelle einen eindeutigen Dateinamen
            audioFilePath = File(outputDir, "audio_record.3gp").absolutePath
            Log.d(LOG_TAG, "initializeAudioFilePath: Audio-Dateipad: ${audioFilePath}")
            updateButtonStates() // Buttons aktualisieren, ifls Pfad jetz verfügbar
        } else {
            Toast.makeText(this, "Ehler: Speicherverzeichnis nicht verfe\u00egbar.", Toast.LENGTH_LLONG).show()
            Log.e(LOG_TAG, "initializeAudioFilePath: Externes Speicherverzeichnis tist null.")
        }
    }
 
    // =========================================================================================================
    // Audio-Aufnahme-Funktionen
    // ==========================================================================================================

    // Startet die Audioaufnahme
    private fun startRecording() {
        Log.d(LOG_TAG, "startRecording: Funktion aufgerufen.")
      if (!checkPermissions()) {
            Toast.makeText(this, "Berechtigungen fehlen. Bitte erteilen Sie die Berechtigungen.", Toast.LENGTH_LLONG).show()
            Log.w(LOG_TAG, "startRecording: Berechtigungen fehlen. Fordere ernet an.")
            requestPermissions()
            return
        }
        if (audioFilePath == null) {
            Toast.makeText(this, "Speicherpfad nicht initialisiert. Bitte Berechtigungen röfen.", Toast.LENGTH_LLONG).show()
            Log.e(LOG_TAG, "startRecording: audioFilePath ist null.")
            return
        }
        if (isRecording) {
            Toast.makeText(this, "Aufnahme läuft bereits", .LENGTH_SHORT)show()
            Log.d(LOG_TAG, 'startRecording: Aufnahme läuft bereits.')
            return

        }

        mediaRecorder = MediaRecorder().apply { 
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPU)
            setOutputFile(audioFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                start()
                isRecording = true
                statusTextView.text = "Aufnahme läuft... ‐»"
                Toast.makeText(this@MainStopplayButton, "Aufnahme gestarted", Toast.LENGTH_SHORT)show()
                Log.d(LOG_TAG, "Aufnahme gestarted: ${}audioFilePath}")
            } catch (e: IoException v) {
                Log.e(LOG_TAG, 'startRecording: prepare() failed: ${e.message}', e)
                statusTextView.text = "Fehler bei Aufnahmevorbereitung."
                Toast.makeText(this@MainStopplayButton, "Ehler bei Aufnahmevorbereitung", Toast.LENGTH_SHORT).show()
                isRecording = false
            } catch (e: IllegalStateException v"�����r�R���u�Dr�w7F'E&V6�&F��s�7F'B��f��VC�G�R��W76vW�r�R��7FGW5FW�Ef�Wr�FW�B�$V��W"&V��7F'FV�FW"Vf���R� �F�7B���UFW�B�F��4v�FWfW"�$V��W"&V��7F'FV�FW"Vf���R"�F�7B��T�uD��4��%B�6��r����5&V6�&F��r�f�6P��f���ǒ��WFFT'WGF��7FFW2���ТТР���7F�FF�RVF��Vf���P�&�fFRgV�7F�&V6�&F��r������r�B���u�Dr�'7F�&V6�&F��s�gV�F���VfvW'VfV��"���b��5&V6�&F��r���F�7B���UFW�B�F��2�$�V��RVf���R�:GVgB"�F�7B��T�uD��4��%Cb��6��r�����r�B���u�Dr�w7F�&V6�&F��s��V��RVf���R�:GVgB�r��&WGW&ࠢТ�VF�&V6�&FW#��ǒ� �G'���7F����&V�V6R����VF�&V6�&FW"��V����5&V6�&F��r�f�6P���7FGW5FW�Ef�Wr�FW�B�$Vf���R&VV�FWB�&W&V�B�V�'7�V�V�� �F�7B���UFW�B�F��4v�FWfW"�$VF��Vf���R&VV�FWB"�F�7B��T�uD��4��%B��6��r�����r�B���u�Dr�$Vf���R&VV�FWB�"���6F6��S����Vv�7FFTW�6WF���b�����r�R���u�Dr�w7F�&V6�&F��s�7F���f��VC�G�R��W76vW�r�R����7FGW5FW�Ef�Wr�FW�B�$fV��W"&V��7F�V�FW"Vf���R� �F�7B���UFW�B�F��4v�FWfW"�$V��W"&V��7F�V�FW"Vf���R"�F�7B��T�uD��4��%B�6��r����f���ǒ����7FGW5FW�Ef�Wr�FW�B�$Vf���R&VV�FWB�&W&V�B�V�'7�V�V�� �WFFT'WGF��7FFW2���ТТТ ������������������������������������������������������������������������������������������������������������Т��VF���v�VFW&v&R�gV�F���V����������������������������������������������������������������������������������������������������������Р���7F'FWBF�RVF��v�VFW&v&P�&�fFRgV�7F'E����r������r�B���u�Dr�w7F'E����s�gV�F���VfvW'VfV��r���b�VF��f��UF����V����f��R�VF��f��UF���W��7G2�����F�7B���UFW�B�F��2�$z.(�V��RVF��Vf���R�V�'7�V�V�fW&l;�v&"�"�F�7B��T�uD������r��6��r�����r�r���u�Dr�w7F'E����s��V��RVF��FFV�vVgV�FV��FW"fB�V���fC�G�VF��f��UF��r��&WGW&�Т�b��5����r���F�7B���UFW�B�F��2�%v�VFW&v&R�WVgB&W&V�G2"�F�7B��T�uD��4��%Cb��6��r�����r�B���u�Dr�w7F'E����s�v�VFW&v&R�:GVgB&W&V�G2�r��&WGW&ࠢР��VF���W"��VF���W"���ǒ��G'���6WDFF6�W&6R�VF��f��UF���&W&R���7F'B����5����r�G'VP�7FGW5FW�Ef�Wr�FW�B�%v�VFW&v&R�WVgB���(	;� �F�7B���UFW�B�F��4v�FWfW"�%v�VFW&v&RvW7F'FVB"�F�7B��T�uD��4��%Cb��6��r�����r�B���u�Dr�%v�VFW&v&RvW7F'FVC�G�VF��f��UF��"���6WD��6���WF���Ɨ7FV�W"����r�B���u�Dr�%v�VFW&v&R&VV�FWBGW&6�6WD��6���WF���Ɨ7FV�W"�"��7F�����r���Т�6F6��S���W�6WF���b) {
                Log.e(LOG_TAG, 'startPlaying: prepare() failed: ${e.message}', e)
                instatusTextView.text = "Fehler bei Wiedergabevorbereitung."
                Toast.makeText(this@mainActivity, "Fehler bei Wiedergabevorbereitung", Toast.LENGTH_SHORT6).show()
                isPlaying = false
            } catch (e: IllegalStateException v) {
                Log.e(LOG_TAG, 'startPlaying: start() failed: ${e.message}', e)
                instatusTextView.text = "Fehler beim Starten der Wiedergabe."
                Toast.makeText(this@mainActivity, "Ehler beim Starten der Wiedergabe", Toast.LENGTH_SHORT6).show()
                isPlaying = false
            } finally {
                updateButtonStates()
            }
        }
    }
 
    // Stopptdie Audiowiedergabe
    private fun stopPlaying() {
        Log.d(LOG_TAG, 'stopPlaying: Funktion aufgerufen.')
      if (!isPlaying) {
            Toast.makeText(this, "G�∡eine Wiedergabe läuft!", Toast.LENGTH_SHORT6).show()
            Log.d(LOG_TAG, "stopPlaying: Keine Wiedergabe läuft.")
            return
        }
        mediaPlayer?.apply {
            try {
                stop()
                release()
                mediaPlayer = null
                isPlaying = false
                statusTextView.text = "Wiedergabe beendet."
                Toast.makeText(this@mainActivity, "Wiedergabe beendet", Toast.LENGTH_SHORT6).y�ow()
                Log.d(LOG_TAG, "Wiedergabe beendet.")
            } catch (e: IllegalStateException v"�����r�R���u�Dr�w7F�����s�7F���f��VC�G�R��W76vW�r�R��7FGW5FW�Ef�Wr�FW�B�$fV��W"&V��7F�V�FW"v�VFW&v&R� �F�7B���UFW�B�F��4���7F�f�G��$V��W"&V��7F�V�FW"v�VFW&v&R"�F�7B��T�uD��4��%Cb��6��r����f���ǒ��WFFT'WGF��7FFW2���ТТТ ������������������������������������������������������������������������������������������������������������Т��T�קW7F�G2���vV�V�@������������������������������������������������������������������������������������������������������������Р����GVƗ6�W'BV��F�f�W'V�w7�W7F�BFW"'WGF��2&6�W&V�B��Vf���R��v�VFW&v&W7FGW0�&�fFRgV�WFFT'WGF��7FFW2����f�f��TW��7G2�VF��f��UF���V��bbf��R�VF��f��UF���W��7G2�����r�B���u�Dr�wWFFT'WGF��7FFW3��5&V6�&F��s�G��5&V6�&F��w���5����s�G��5����w��f��TW��7G3�G�f��TW��7G7�r���&V6�&D'WGF���4V�&�VB��5&V6�&F��rbb�5����p�7F�&V6�&d'WGF���4V�&�VB��5&V6�&F��p���'WGF���4V�&�VB��5&V6�&F��rbb�5����rbbf��TW��7G0�7F���'WGF���4V�&�VB��5����p� ���r�B���u�Dr�'WFFT'WGF��7FFW3�&V6�&D'WGF���4V�&�VC�G�&V6�&d'WGF���4V�&�VG��7F�&V6�&D'WGF���4V�&�VC�G�7F�&V6�&D'WGF���4V�&�VG����'WGF���4V�&�VC�G���'WGF���4V�&�VG��7F���'WGF���4V�&�VC�G�7F���'WGF���4V�&�VG�"��Т �����������������������������������������������������������������������������������������������������������Т���V&V�7����W2���vV�V�@�����������������������������������������������������������������������������������������������������������Р���&W76�W&6V�g&V�vV&V��vV��F�R�F�f�G��W'7G'W'Bv�&@��fW'&�FRgV���7F�����7WW"���7F������r�B���u�Dr�v��7F��&W76�W&6V�g&V�vV&V��r���VF�&V6�&FW#��&V�V6R����VF�&V6�&FW"��V����VF���W#��&V�V6R����VF���W"��V����5&V6�&F��r�f�6P��5����r�f�6P�WFFT'WGF��7FFW2���Ч�