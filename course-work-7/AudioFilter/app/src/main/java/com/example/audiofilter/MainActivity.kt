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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val scrollState = rememberScrollState()

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
                    Button(onClick = { viewModel.applyBandPassFilter() }) {
                        Text("Применить полосовой фильтр")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.applyKalmanFilter() }) {
                        Text("Применить фильтр Калмана")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.applyGaussianFilter() }) {
                        Text("Применить Гауссов фильтр")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.applyMedianFilter() }) {
                        Text("Применить медианный фильтр")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.applySpectralSubtraction(200, 500, 1.0f) }) {
                        Text("Применить метод подавления шума")
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
                Spacer(modifier = Modifier.size(16.dp))
                processedAudio?.let {
                    AudioWaveformView(
                        audioData = it,
                        sampleRate = viewModel.sampleRate,
                        scrollState = scrollState
                    )
                }
            }
        }
    }
}

@Composable
fun AudioWaveformView(audioData: ShortArray, sampleRate: Int, scrollState: ScrollState) {

    var scale by remember { mutableStateOf(1f) }

    Text(
        text = "Масштаб отображения:",
        fontSize = 16.sp,
    )
    Slider(
        value = scale,
        onValueChange = { newScale ->
            scale = newScale
        },
        valueRange = 0.2f..2.0f,
        steps = 20,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text("Мин: 0.2")
        Text("Текущее: ${"%.2f".format(scale)}")
        Text("Макс: 2.0")
    }

    val v = audioData.size / sampleRate

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .height(200.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(LocalDensity.current) { ((v * 1000) * scale).toDp() })
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
                    val normalizedSample =
                        sample.toFloat() / Short.MAX_VALUE.toFloat() * amplitude
                    drawLine(
                        color = Color.Blue,
                        start = androidx.compose.ui.geometry.Offset(
                            i.toFloat(),
                            centerY - normalizedSample
                        ),
                        end = androidx.compose.ui.geometry.Offset(
                            i.toFloat(),
                            centerY + normalizedSample
                        ),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}