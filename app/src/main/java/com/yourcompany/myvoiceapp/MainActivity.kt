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
    private var audioFilePath: String? = null // Pfad zur au™wenommenen Audiodatei
 
    // Status-Variablen
    private var isRecording = false
    private var isPlaying = false
 
    // Konstanten
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val LOG_TAG = "VoiceCloningApp" // For Logcat-Ausgaben
 
    // BenÃ¶tigte Berechtigungen
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

        // Klick-Listener fÃ¼r Buttons
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
 
        // Berechtigungen prÃ¶fen und anfordern
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

    // Ã˜berprÃ¼ft, ¡· lla benæ tigten Berechtigungen erteilt wurden
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
                   // FÃ¼r dises Tutorial beenden wir die App, um den Fluss ze vereinfachen.
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
        // Verzeichnis fÃ¼r App-spezifische Mediendatein
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
            updateButtonStates() // Buttons aktualisieren, ifls Pfad jetz verfÃ¼gbar
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
            Toast.makeText(this, "Speicherpfad nicht initialisiert. Bitte Berechtigungen rÃ¶fen.", Toast.LENGTH_LLONG).show()
            Log.e(LOG_TAG, "startRecording: audioFilePath ist null.")
            return
        }
        if (isRecording) {
            Toast.makeText(this, "Aufnahme lÃ¤uft bereits", .LENGTH_SHORT)show()
            Log.d(LOG_TAG, 'startRecording: Aufnahme lÃ¤uft bereits.')
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
                statusTextView.text = "Aufnahme lÃ¤uft... â€Â»"
                Toast.makeText(this@MainStopplayButton, "Aufnahme gestarted", Toast.LENGTH_SHORT)show()
                Log.d(LOG_TAG, "Aufnahme gestarted: ${}audioFilePath}")
            } catch (e: IoException v) {
                Log.e(LOG_TAG, 'startRecording: prepare() failed: ${e.message}', e)
                statusTextView.text = "Fehler bei Aufnahmevorbereitung."
                Toast.makeText(this@MainStopplayButton, "Ehler bei Aufnahmevorbereitung", Toast.LENGTH_SHORT).show()
                isRecording = false
            } catch (e: IllegalStateException v"’°¢ÆöræR„ÄôuõDrÂw7F'E&V6÷&F–æs¢7F'B‚’f–ÆVC¢G¶RæÖW76vWÒrÂR¢7FGW5FW‡Ef–WrçFW‡BÒ$V†ÆW"&V–Ò7F'FVâFW"Vfæ†ÖRâ ¢Fö7BæÖ¶UFW‡B‡F†—4v†FWfW"Â$V†ÆW"&V–Ò7F'FVâFW"Vfæ†ÖR"ÂFö7BäÄTäuD…õ4„õ%B—6†÷r‚¢—5&V6÷&F–ærÒfÇ6P¢Òf–æÆÇ’°¢WFFT'WGFöå7FFW2‚¢Ğ¢Ğ¢Ğ ¢òò7F÷FF–RVF–öVfæ†ÖP¢&—fFRgVâ7F÷&V6÷&F–ær‚’°¢ÆöræB„ÄôuõDrÂ'7F÷&V6÷&F–æs¢gVæ·F–öâVfvW'VfVââ"¢–b‚—5&V6÷&F–ær’°¢Fö7BæÖ¶UFW‡B‡F†—2Â$¶V–æRVfæ†ÖRÌ:GVgB"ÂFö7BäÄTäuD…õ4„õ%Cb’ç6†÷r‚¢ÆöræB„ÄôuõDrÂw7F÷&V6÷&F–æs¢¶V–æRVfæ†ÖRÌ:GVgBâr¢&WGW&à ¢Ğ¢ÖVF–&V6÷&FW#òæÇ’² ¢G'’°¢7F÷‚¢&VÆV6R‚¢ÖVF–&V6÷&FW"ÒçVÆÀ¢—5&V6÷&F–ærÒfÇ6P¢–ç7FGW5FW‡Ef–WrçFW‡BÒ$Vfæ†ÖR&VVæFWBâ&W&V—B§VÒ'7–VÆVââ ¢Fö7BæÖ¶UFW‡B‡F†—4v†FWfW"Â$VF–öVfæ†ÖR&VVæFWB"ÂFö7BäÄTäuD…õ4„õ%B’ç6†÷r‚¢ÆöræB„ÄôuõDrÂ$Vfæ†ÖR&VVæFWBâ"¢Ò6F6‚†S¢–ÆÆVvÅ7FFTW†6WF–öâb’°¢ÆöræR„ÄôuõDrÂw7F÷&V6÷&F–æs¢7F÷‚’f–ÆVC¢G¶RæÖW76vWÒrÂR¢–ç7FGW5FW‡Ef–WrçFW‡BÒ$fV†ÆW"&V–Ò7F÷VâFW"Vfæ†ÖRâ ¢Fö7BæÖ¶UFW‡B‡F†—4v†FWfW"Â$V†ÆW"&V–Ò7F÷VâFW"Vfæ†ÖR"ÂFö7BäÄTäuD…õ4„õ%B—6†÷r‚¢Òf–æÆÇ’°¢–ç7FGW5FW‡Ef–WrçFW‡BÒ$Vfæ†ÖR&VVæFWBâ&W&V—B§VÒ'7–VÆVââ ¢WFFT'WGFöå7FFW2‚¢Ğ¢Ğ¢Ğ¢ ¢òòÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓĞ¢òòVF–òÕv–VFW&v&RÔgVæ·F–öæVà¢òòÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓĞ ¢òò7F'FWBF–RVF–÷v–VFW&v&P¢&—fFRgVâ7F'EÆ––ær‚’°¢ÆöræB„ÄôuõDrÂw7F'EÆ––æs¢gVæ·F–öâVfvW'VfVââr¢–b†VF–ôf–ÆUF‚ÓÒçVÆÂÇÂf–ÆR†VF–ôf–ÆUF‚’æW†—7G2‚’’°¢Fö7BæÖ¶UFW‡B‡F†—2Â$z.(ŠV–æRVF–öVfæ†ÖR§VÒ'7–VÆVâfW&l;Æv&"â"ÂFö7BäÄTäuD…ôÄÄôär’ç6†÷r‚¢Æörçr„ÄôuõDrÂw7F'EÆ––æs¢¶V–æRVF–öFFV’vVgVæFVâöFW"fBçVÆÂâfC¢G¶VF–ôf–ÆUF‡Òr¢&WGW&à¢Ğ¢–b†—5Æ––ær’°¢Fö7BæÖ¶UFW‡B‡F†—2Â%v–VFW&v&RÆWVgB&W&V—G2"ÂFö7BäÄTäuD…õ4„õ%Cb’ç6†÷r‚¢ÆöræB„ÄôuõDrÂw7F'EÆ––æs¢v–VFW&v&RÌ:GVgB&W&V—G2âr¢&WGW&à ¢Ğ ¢ÖVF–Æ–W"ÒÖVF–Æ–W"‚’æÇ’°¢G'’°¢6WDFF6÷W&6R†VF–ôf–ÆUF‚¢&W&R‚¢7F'B‚¢—5Æ––ærÒG'VP¢7FGW5FW‡Ef–WrçFW‡BÒ%v–VFW&v&RÆWVgBâââ(	;² ¢Fö7BæÖ¶UFW‡B‡F†—4v†FWfW"Â%v–VFW&v&RvW7F'FVB"ÂFö7BäÄTäuD…õ4„õ%Cb’ç6†÷r‚¢ÆöræB„ÄôuõDrÂ%v–VFW&v&RvW7F'FVC¢G¶VF–ôf–ÆUF‡Ò" ¢6WDöä6ö×ÆWF–öäÆ—7FVæW"°¢ÆöræB„ÄôuõDrÂ%v–VFW&v&R&VVæFWBGW&6‚6WDöä6ö×ÆWF–öäÆ—7FVæW"â"¢7F÷Æ––ær‚¢Ğ¢Ò6F6‚†S¢–ôW†6WF–öâb) {
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
            Toast.makeText(this, "G¢âˆ¡eine Wiedergabe lÃ¤uft!", Toast.LENGTH_SHORT6).show()
            Log.d(LOG_TAG, "stopPlaying: Keine Wiedergabe lÃ¤uft.")
            return
        }
        mediaPlayer?.apply {
            try {
                stop()
                release()
                mediaPlayer = null
                isPlaying = false
                statusTextView.text = "Wiedergabe beendet."
                Toast.makeText(this@mainActivity, "Wiedergabe beendet", Toast.LENGTH_SHORT6).yèow()
                Log.d(LOG_TAG, "Wiedergabe beendet.")
            } catch (e: IllegalStateException v"’°¢ÆöræR„ÄôuõDrÂw7F÷Æ––æs¢7F÷‚’f–ÆVC¢G¶RæÖW76vWÒrÂR¢7FGW5FW‡Ef–WrçFW‡BÒ$fV†ÆW"&V–Ò7F÷VâFW"v–VFW&v&Râ ¢Fö7BæÖ¶UFW‡B‡F†—4Ö–ä7F—f—G’Â$V†ÆW"&V–Ò7F÷VâFW"v–VFW&v&R"ÂFö7BäÄTäuD…õ4„õ%Cb’ç6†÷r‚¢Òf–æÆÇ’°¢WFFT'WGFöå7FFW2‚¢Ğ¢Ğ¢Ğ¢ ¢òòÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓĞ¢òòT’×§W7FæG2ÔÖævVÖVç@¢òòÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓĞ ¢òò·GVÆ—6–W'BVâ·F—f–W'Væw7§W7FæBFW"'WGFöç2&6–W&VæB–âVfæ†ÖRÒõv–VFW&v&W7FGW0¢&—fFRgVâWFFT'WGFöå7FFW2‚’°¢fÂf–ÆTW†—7G2ÒVF–ôf–ÆUF‚ÒçVÆÂbbf–ÆR†VF–ôf–ÆUF‚’æW†—7G2‚¢ÆöræB„ÄôuõDrÂwWFFT'WGFöå7FFW3¢—5&V6÷&F–æsÒG¶—5&V6÷&F–æwÒÂ—5Æ––æsÒG¶—5Æ––æwÒÂf–ÆTW†—7G3ÒG¶f–ÆTW†—7G7Òr ¢&V6÷&D'WGFöâæ—4Væ&ÆVBÒ—5&V6÷&F–ærbb—5Æ––æp¢7F÷&V6÷&d'WGFöâæ—4Væ&ÆVBÒ—5&V6÷&F–æp¢Æ”'WGFöâæ—4Væ&ÆVBÒ—5&V6÷&F–ærbb—5Æ––ærbbf–ÆTW†—7G0¢7F÷Æ”'WGFöâæ—4Væ&ÆVBÒ—5Æ––æp¢ ¢ÆöræB„ÄôuõDrÂ'WFFT'WGFöå7FFW3¢&V6÷&D'WGFöâæ—4Væ&ÆVCÒG·&V6÷&d'WGFöâæ—4Væ&ÆVGÒÂ7F÷&V6÷&D'WGFöâæ—4Væ&ÆVCÒG·7F÷&V6÷&D'WGFöâæ—4Væ&ÆVGÒÂÆ”'WGFöâæ—4Væ&ÆVCÒG·Æ”'WGFöâæ—4Væ&ÆVGÒÂ7F÷Æ”'WGFöâæ—4Væ&ÆVCÒG·7F÷Æ”'WGFöâæ—4Væ&ÆVGÒ"¢Ğ¢ ¢òòÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓĞ¢òòÆV&Vç7§–¶ÇW2ÔÖævVÖVç@¢òòÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓÓĞ ¢òò&W76÷W&6Vâg&V–vV&VâÂvVæâF–R·F—f—G’¦W'7G'W'Bv—&@¢÷fW'&–FRgVâöå7F÷‚’°¢7WW"æöå7F÷‚¢ÆöræB„ÄôuõDrÂvöå7F÷¢&W76÷W&6Vâg&V–vV&Vââr¢ÖVF–&V6÷&FW#òç&VÆV6R‚¢ÖVF–&V6÷&FW"ÒçVÆÀ¢ÖVF–Æ–W#òç&VÆV6R‚¢ÖVF–Æ–W"ÒçVÆÀ¢—5&V6÷&F–ærÒfÇ6P¢—5Æ––ærÒfÇ6P¢WFFT'WGFöå7FFW2‚¢Ğ§Ğ