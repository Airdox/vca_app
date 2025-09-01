
    try {
        // Systematische Dateibenennung: recording_0000.m4a, recording_0001.m4a, ...
        val fileName = String.format(Locale.ROOT, "recording_%04d.m4a", recordingCounter)
        val audioFile = File(getExternalFilesDir(null), fileName) // Speichern im App-spezifischen Verzeichnis
        audioFilePath = audioFile.absolutePath
        recordingCounter++ // Zähler für die nächste Aufnahme erhöhen

        isRecording = true
        updateButtonStates()
        binding.statusTextView.text = getString(R.string.status_recording)
        Toast.makeText(this, R.string.toast_recording_started, Toast.LENGTH_SHORT).show()
        Log.d(LOG_TAG, "startRecording: Aufnahme erfolgreich gestartet.")

    } catch (e: IOException) {
        Log.e(LOG_TAG, "startRecording: prepare() failed", e)
        binding.statusTextView.text = getString(R.string.error_recording_failed, e.localizedMessage)
        Toast.makeText(this, getString(R.string.error_recording_failed, e.localizedMessage), Toast.LENGTH_LONG).show()
        isRecording = false
        updateButtonStates()
    } catch (e: IllegalStateException) {
        Log.e(LOG_TAG, "startRecording: start() failed, state error", e)
        binding.statusTextView.text = getString(R.string.error_start_recording_failed_status, e.localizedMessage)
        Toast.makeText(this, getString(R.string.error_start_recording_failed_status, e.localizedMessage), Toast.LENGTH_LONG).show()
        isRecording = false
        updateButtonStates()
            mediaRecorder = null
            isRecording = false
            updateButtonStates()
            binding.statusTextView.text = getString(R.string.status_recording_stopped)
            Toast.makeText(this, R.string.toast_recording_stopped, Toast.LENGTH_SHORT).show()
            Log.d(LOG_TAG, "stopRecording: Aufnahme erfolgreich gestoppt.")
        } catch (e: RuntimeException) {
            Log.e(LOG_TAG, "stopRecording: stop() failed or release() failed", e)
            binding.statusTextView.text = getString(R.string.error_stop_recording_failed_status, e.localizedMessage)
            Toast.makeText(this, getString(R.string.error_stop_recording_failed_status, e.localizedMessage), Toast.LENGTH_LONG).show()
            mediaRecorder = null
            isRecording = false

                isPlaying = true
                updateButtonStates()
                binding.statusTextView.text = getString(R.string.status_playing)
                Toast.makeText(this, R.string.toast_playback_started, Toast.LENGTH_SHORT).show()
                Log.d(LOG_TAG, "startPlaying: Wiedergabe erfolgreich gestartet.")

            } catch (e: IOException) {
                Log.e(LOG_TAG, "startPlaying: prepare() failed", e)
                binding.statusTextView.text = getString(R.string.error_playback_failed_status, e.localizedMessage)
                Toast.makeText(this, getString(R.string.error_playback_failed_status, e.localizedMessage), Toast.LENGTH_LONG).show()
                isPlaying = false
                updateButtonStates()
            } catch (e: IllegalStateException) {
                Log.e(LOG_TAG, "startPlaying: start() failed, state error", e)
                binding.statusTextView.text = getString(R.string.error_start_playback_failed_status, e.localizedMessage)
                Toast.makeText(this, getString(R.string.error_start_playback_failed_status, e.localizedMessage), Toast.LENGTH_LONG).show()
                isPlaying = false
                updateButtonStates()
    mediaPlayer = null
    isPlaying = false
    updateButtonStates()
    binding.statusTextView.text = getString(R.string.status_playback_stopped)
    Toast.makeText(this, R.string.toast_playback_stopped, Toast.LENGTH_SHORT).show()
    Log.d(LOG_TAG, "stopPlaying: Wiedergabe erfolgreich gestoppt.")
}
