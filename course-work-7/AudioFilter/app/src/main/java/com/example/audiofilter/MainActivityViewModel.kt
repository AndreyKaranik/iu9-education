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
    private var sampleRate: Int = 44100
    private var processedAudio: ShortArray? = null

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
                processedAudio = audioData.copyOf() // Начальное состояние без обработки
            }
        }
    }

    fun applyLowPassFilter() {
        processedAudio?.let {
            processedAudio = AudioUtils.lowPassFilter(it, sampleRate, 1000f)
        }
    }

    fun applyHighPassFilter() {
        processedAudio?.let {
            processedAudio = AudioUtils.highPassFilter(it, sampleRate, 200f)
        }
    }

    fun saveProcessedAudio() {
        processedAudio?.let {
            val uri = saveToMediaStore(context, it, sampleRate)
            _processedFileUri.value = uri
        }
    }
}