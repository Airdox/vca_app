package com.airdox.voicecloning

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.airdox.voicecloning.ui.recording.RecordingActivity
import com.airdox.voicecloning.ui.training.TrainingActivity
import com.airdox.voicecloning.ui.tts.TTSActivity
import com.airdox.voicecloning.ui.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupUI()
    }
    
    private fun setupUI() {
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = getString(R.string.dashboard_title)
        
        // Setup card click listeners
        findViewById<CardView>(R.id.card_recording).setOnClickListener {
            startActivity(Intent(this, RecordingActivity::class.java))
        }
        
        findViewById<CardView>(R.id.card_training).setOnClickListener {
            startActivity(Intent(this, TrainingActivity::class.java))
        }
        
        findViewById<CardView>(R.id.card_tts).setOnClickListener {
            startActivity(Intent(this, TTSActivity::class.java))
        }
        
        findViewById<CardView>(R.id.card_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}