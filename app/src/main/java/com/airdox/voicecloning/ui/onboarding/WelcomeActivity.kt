package com.airdox.voicecloning.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airdox.voicecloning.MainActivity
import com.airdox.voicecloning.R
import com.airdox.voicecloning.data.PreferencesManager
import com.google.android.material.button.MaterialButton

class WelcomeActivity : AppCompatActivity() {
    
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        
        preferencesManager = PreferencesManager(this)
        
        // Check if this is the first run
        if (!preferencesManager.isFirstRun()) {
            startMainActivity()
            return
        }
        
        setupUI()
    }
    
    private fun setupUI() {
        findViewById<MaterialButton>(R.id.btn_get_started).setOnClickListener {
            preferencesManager.setFirstRunCompleted()
            startMainActivity()
        }
        
        findViewById<MaterialButton>(R.id.btn_learn_more).setOnClickListener {
            // Show app features or help
            showFeatures()
        }
    }
    
    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    private fun showFeatures() {
        // Could implement a features showcase or help dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("VoiceCloning Pro Features")
            .setMessage("""
                🎤 Hochwertige Audioaufnahme
                🔧 Professionelle Audio-Verbesserung
                🧠 KI-basierte Stimmklonierung
                🗣️ Text-zu-Sprache mit Ihrer Stimme
                🎛️ Anpassbare Qualitätseinstellungen
                🔒 Lokale Verarbeitung für Datenschutz
            """.trimIndent())
            .setPositiveButton("Verstanden") { _, _ ->
                preferencesManager.setFirstRunCompleted()
                startMainActivity()
            }
            .show()
    }
}