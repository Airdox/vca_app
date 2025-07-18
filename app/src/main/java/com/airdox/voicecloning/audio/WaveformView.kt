package com.airdox.voicecloning.audio

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.airdox.voicecloning.R
import kotlin.math.max
import kotlin.math.min
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val amplitudes = CopyOnWriteArrayList<Int>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    
    private var isRecording = false
    private var maxAmplitude = 0
    private val maxPoints = 200 // Maximum number of points to display
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        paint.apply {
            color = context.getColor(R.color.waveform_color)
            strokeWidth = 3f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        
        backgroundPaint.apply {
            color = context.getColor(R.color.waveform_background)
            style = Paint.Style.FILL
        }
        
        centerLinePaint.apply {
            color = Color.GRAY
            strokeWidth = 1f
            alpha = 128
        }
    }
    
    fun addAmplitude(amplitude: Int) {
        amplitudes.add(amplitude)
        maxAmplitude = max(maxAmplitude, amplitude)
        
        // Keep only the latest points
        while (amplitudes.size > maxPoints) {
            amplitudes.removeAt(0)
        }
        
        invalidate()
    }
    
    fun startRecording() {
        isRecording = true
        clear()
    }
    
    fun stopRecording() {
        isRecording = false
    }
    
    fun clear() {
        amplitudes.clear()
        maxAmplitude = 0
        invalidate()
    }
    
    fun loadAudioFile(file: File) {
        // Simulate loading audio file and generating waveform
        clear()
        
        // Generate sample waveform data
        val sampleData = generateSampleWaveform(100)
        amplitudes.addAll(sampleData)
        maxAmplitude = sampleData.maxOrNull() ?: 0
        
        invalidate()
    }
    
    private fun generateSampleWaveform(points: Int): List<Int> {
        return (0 until points).map {
            (Math.random() * 32767).toInt()
        }
    }
    
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        
        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2
        
        // Draw background
        canvas.drawRect(0f, 0f, width, height, backgroundPaint)
        
        // Draw center line
        canvas.drawLine(0f, centerY, width, centerY, centerLinePaint)
        
        if (amplitudes.isEmpty()) {
            // Draw placeholder text
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.GRAY
                textSize = 24f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                if (isRecording) "Aufnahme läuft..." else "Bereit für Aufnahme",
                width / 2,
                centerY + 10,
                textPaint
            )
            return
        }
        
        // Draw waveform
        if (amplitudes.size >= 2) {
            path.reset()
            
            val pointWidth = width / maxPoints
            val maxHeight = height / 2 - 20f // Leave some margin
            
            for (i in amplitudes.indices) {
                val x = i * pointWidth
                val normalizedAmplitude = if (maxAmplitude > 0) {
                    amplitudes[i].toFloat() / maxAmplitude
                } else {
                    0f
                }
                val y = centerY - (normalizedAmplitude * maxHeight)
                
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            canvas.drawPath(path, paint)
            
            // Mirror waveform below center line
            path.reset()
            for (i in amplitudes.indices) {
                val x = i * pointWidth
                val normalizedAmplitude = if (maxAmplitude > 0) {
                    amplitudes[i].toFloat() / maxAmplitude
                } else {
                    0f
                }
                val y = centerY + (normalizedAmplitude * maxHeight)
                
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            canvas.drawPath(path, paint)
        }
        
        // Draw recording indicator
        if (isRecording) {
            val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = context.getColor(R.color.recording_active)
                style = Paint.Style.FILL
            }
            canvas.drawCircle(width - 30, 30f, 10f, indicatorPaint)
        }
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minHeight = 150
        val height = max(minHeight, MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }
}