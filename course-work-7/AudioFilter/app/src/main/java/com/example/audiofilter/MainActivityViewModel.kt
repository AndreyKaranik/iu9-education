package com.example.audiofilter

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audiofilter.AudioUtils.saveToMediaStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel(private val context: Context) : ViewModel() {
    private var originalAudio: ShortArray? = null
    var sampleRate: Int = 44100

    private val _processedAudio = MutableStateFlow<ShortArray?>(null)
    val processedAudio: StateFlow<ShortArray?> = _processedAudio

    private val _selectedFileName = MutableStateFlow<String?>(null)
    val selectedFileName: StateFlow<String?> = _selectedFileName

    private val _processedFileUri = MutableStateFlow<Uri?>(null)
    val processedFileUri: StateFlow<Uri?> = _processedFileUri

    fun loadAudioFile(uri: Uri) {
        viewModelScope.launch {
            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                val (audioData, rate) = AudioUtils.readWavFile(inputStream)
                originalAudio = audioData
                sampleRate = rate
                _selectedFileName.value = uri.lastPathSegment
                _processedAudio.value = audioData.copyOf()
            }
        }
    }

    fun saveProcessedAudio() {
        val uri = saveToMediaStore(context, _processedAudio.value!!, sampleRate)
        _processedFileUri.value = uri
    }

    fun applyLowPassFilter() {
        _processedAudio.value = AudioUtils.lowPassFilter(_processedAudio.value!!, sampleRate, 700f)
    }

    fun applyHighPassFilter() {
        _processedAudio.value = AudioUtils.highPassFilter(_processedAudio.value!!, sampleRate, 700f)
    }

    fun applyBandPassFilter() {
        _processedAudio.value =
            AudioUtils.bandPassFilter(_processedAudio.value!!, sampleRate, 1400f, 700f)
    }

    fun applyKalmanFilter() {
        _processedAudio.value =
            AudioUtils.kalmanFilter(_processedAudio.value!!, sampleRate, 0.2f, 2.0f)
    }

    fun applyGaussianFilter() {
        _processedAudio.value = AudioUtils.gaussianFilter(_processedAudio.value!!, 51, 1.0)
    }

    fun applyMedianFilter() {
        _processedAudio.value = AudioUtils.medianFilter(_processedAudio.value!!, 20)
    }

    fun applySpectralSubtraction(noiseStartMs: Int, noiseEndMs: Int, alpha: Float) {
        _processedAudio.value = AudioUtils.spectralSubtraction(
            _processedAudio.value!!,
            sampleRate,
            noiseStartMs,
            noiseEndMs,
            alpha = alpha.toDouble()
        )
    }


}