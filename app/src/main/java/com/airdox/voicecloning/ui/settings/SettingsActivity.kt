package com.airdox.voicecloning.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.airdox.voicecloning.R
import com.airdox.voicecloning.model.VoiceCloningModel

class SettingsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)
        
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    class SettingsFragment : PreferenceFragmentCompat() {
        
        private lateinit var voiceModel: VoiceCloningModel
        
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            
            voiceModel = VoiceCloningModel(requireContext())
            
            setupPreferences()
        }
        
        private fun setupPreferences() {
            // Model Management
            findPreference<Preference>("model_info")?.setOnPreferenceClickListener {
                showModelInfo()
                true
            }
            
            findPreference<Preference>("delete_model")?.setOnPreferenceClickListener {
                showDeleteModelDialog()
                true
            }
            
            // Audio Quality Settings
            findPreference<Preference>("sample_rate")?.setOnPreferenceChangeListener { _, newValue ->
                val sampleRate = newValue as String
                Toast.makeText(context, "Sample-Rate auf $sampleRate Hz gesetzt", Toast.LENGTH_SHORT).show()
                true
            }
            
            findPreference<Preference>("bit_rate")?.setOnPreferenceChangeListener { _, newValue ->
                val bitRate = newValue as String
                Toast.makeText(context, "Bit-Rate auf $bitRate kbps gesetzt", Toast.LENGTH_SHORT).show()
                true
            }
            
            // Model Quality Settings
            findPreference<Preference>("model_quality")?.setOnPreferenceChangeListener { _, newValue ->
                val quality = newValue as String
                Toast.makeText(context, "Modell-Qualität auf $quality gesetzt", Toast.LENGTH_SHORT).show()
                true
            }
            
            // Processing Power
            findPreference<Preference>("processing_power")?.setOnPreferenceChangeListener { _, newValue ->
                val power = newValue as String
                Toast.makeText(context, "Verarbeitungsleistung auf $power gesetzt", Toast.LENGTH_SHORT).show()
                true
            }
            
            // About
            findPreference<Preference>("app_version")?.summary = "Version 1.0.0"
            
            findPreference<Preference>("about")?.setOnPreferenceClickListener {
                showAboutDialog()
                true
            }
        }
        
        private fun showModelInfo() {
            val modelInfo = voiceModel.getModelInfo()
            
            val message = if (modelInfo != null) {
                """
                    Modell-Version: ${modelInfo.version}
                    Sprache: ${modelInfo.language.uppercase()}
                    Audiodateien: ${modelInfo.audioFilesCount}
                    Qualität: ${modelInfo.quality}%
                    Erstellt: ${java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.GERMAN).format(java.util.Date(modelInfo.created))}
                """.trimIndent()
            } else {
                "Kein trainiertes Modell gefunden."
            }
            
            AlertDialog.Builder(requireContext())
                .setTitle("Modell-Information")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
        
        private fun showDeleteModelDialog() {
            if (!voiceModel.isModelAvailable()) {
                Toast.makeText(context, "Kein Modell zum Löschen gefunden", Toast.LENGTH_SHORT).show()
                return
            }
            
            AlertDialog.Builder(requireContext())
                .setTitle("Modell löschen")
                .setMessage("Sind Sie sicher, dass Sie das trainierte Stimmmodell löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden.")
                .setPositiveButton("Löschen") { _, _ ->
                    voiceModel.deleteModel()
                    Toast.makeText(context, "Modell erfolgreich gelöscht", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Abbrechen", null)
                .show()
        }
        
        private fun showAboutDialog() {
            val message = """
                VoiceCloning Pro
                
                Eine professionelle App zur Stimmklonierung mit modernster KI-Technologie.
                
                Features:
                • Hochwertige Audioaufnahme
                • Professionelle Audio-Verbesserung
                • Schnelles Training mit wenigen Samples
                • Deutsche Sprachunterstützung
                • Sichere lokale Verarbeitung
                
                Entwickelt mit ❤️ für beste Benutzerfreundlichkeit.
            """.trimIndent()
            
            AlertDialog.Builder(requireContext())
                .setTitle("Über VoiceCloning Pro")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }
}