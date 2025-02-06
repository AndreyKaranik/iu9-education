package com.example.audiofilter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.exp

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = MainActivityViewModel(this)
        setContent {
            AudioProcessorScreen(viewModel)
        }
    }
}

@Composable
fun AudioProcessorScreen(viewModel: MainActivityViewModel) {
    val context = LocalContext.current
    val selectedFileName by viewModel.selectedFileName.collectAsState()
    val processedFileUri by viewModel.processedFileUri.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { viewModel.loadAudioFile(it) } }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        val processedAudio by viewModel.processedAudio.collectAsState()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Button(onClick = { filePickerLauncher.launch("audio/*") }) {
                    Text("Выбрать аудиофайл")
                }

                selectedFileName?.let {

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.applyLowPassFilter() }) {
                        Text("Применить НЧ-фильтр")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.applyHighPassFilter() }) {
                        Text("Применить ВЧ-фильтр")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.saveProcessedAudio() }) {
                        Text("Сохранить результат")
                    }

                    processedFileUri?.let { uri ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Файл сохранён")
                    }
                }
            }

            item {
                processedAudio?.let {
                    Text("Аудио график")
                    AudioWaveformView(
                        audioData = it
                    )
                }
            }
        }
    }
}

@Composable
fun AudioWaveformView(audioData: ShortArray) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val amplitude = height / 4

        val step = audioData.size / width.toInt()
        for (i in 0 until width.toInt()) {
            val sampleIndex = i * step
            if (sampleIndex < audioData.size) {
                val sample = audioData[sampleIndex]
                val normalizedSample = sample.toFloat() / Short.MAX_VALUE.toFloat() * amplitude
                drawLine(
                    color = Color.Blue,
                    start = androidx.compose.ui.geometry.Offset(i.toFloat(), centerY - normalizedSample),
                    end = androidx.compose.ui.geometry.Offset(i.toFloat(), centerY + normalizedSample),
                    strokeWidth = 1f
                )
            }
        }
    }
}
