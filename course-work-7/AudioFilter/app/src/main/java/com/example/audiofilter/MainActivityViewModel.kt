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
                _processedAudio.value = audioData.copyOf() // Начальное состояние без обработки
            }
        }
    }

    fun applyLowPassFilter() {
        _processedAudio.value = AudioUtils.lowPassFilter(_processedAudio.value!!, sampleRate, 1000f)
    }

    fun applyHighPassFilter() {
        _processedAudio.value = AudioUtils.highPassFilter(_processedAudio.value!!, sampleRate, 200f)
    }

    fun saveProcessedAudio() {
        val uri = saveToMediaStore(context, _processedAudio.value!!, sampleRate)
        _processedFileUri.value = uri
    }
}