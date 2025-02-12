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
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.audiofilter.ui.theme.AudioFilterTheme
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
            AudioFilterTheme {
                AudioFilterScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioFilterScreen(viewModel: MainActivityViewModel) {
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

                Text(
                    text = "AudioFilter",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Выберете аудиофайл перед началом работы",
                    color = Color.Gray,
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        filePickerLauncher.launch("audio/*")
                    }
                ) {
                    Text("Выбрать аудиофайл")
                }

                selectedFileName?.let {

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.saveProcessedAudio()
                        }
                    ) {
                        Text("Сохранить результат")
                    }

                    processedFileUri?.let { uri ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Файл сохранён")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LowPassFilterView(viewModel = viewModel)

                    Spacer(modifier = Modifier.height(16.dp))

                    HighPassFilterView(viewModel = viewModel)

                    Spacer(modifier = Modifier.height(16.dp))

                    BandPassFilterView(viewModel = viewModel)

                    Spacer(modifier = Modifier.height(16.dp))

                    KalmanFilterView(viewModel = viewModel)

                    Spacer(modifier = Modifier.height(16.dp))

                    GaussianFilterView(viewModel = viewModel)

                    Spacer(modifier = Modifier.height(16.dp))

                    MedianFilterView(viewModel = viewModel)

                    Spacer(modifier = Modifier.height(16.dp))

                    SpectralSubtractionView(viewModel = viewModel)

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
fun LowPassFilterView(viewModel: MainActivityViewModel) {
    var cutoffFreq by remember { mutableStateOf(TextFieldValue("")) }

    FilterView(
        title = "Низкочастотный фильтр",
        apply = {
            viewModel.applyLowPassFilter(cutoffFreq.text.toFloat())
        }) {
        ParamFieldView(
            name = "cutoffFreq",
            value = cutoffFreq,
            onValueChange = { newValue -> cutoffFreq = newValue })
    }
}

@Composable
fun HighPassFilterView(viewModel: MainActivityViewModel) {
    var cutoffFreq by remember { mutableStateOf(TextFieldValue("")) }

    FilterView(
        title = "Высокочастотный фильтр",
        apply = {
            viewModel.applyHighPassFilter(cutoffFreq.text.toFloat())
        }) {
        ParamFieldView(
            name = "cutoffFreq",
            value = cutoffFreq,
            onValueChange = { newValue -> cutoffFreq = newValue })
    }
}

@Composable
fun BandPassFilterView(viewModel: MainActivityViewModel) {
    var lowCutoffFreq by remember { mutableStateOf(TextFieldValue("")) }
    var highCutoffFreq by remember { mutableStateOf(TextFieldValue("")) }

    FilterView(
        title = "Полосовой фильтр",
        apply = {
            viewModel.applyBandPassFilter(
                lowCutoffFreq.text.toFloat(),
                highCutoffFreq.text.toFloat()
            )
        }) {
        ParamFieldView(
            name = "lowCutoffFreq",
            value = lowCutoffFreq,
            onValueChange = { newValue -> lowCutoffFreq = newValue }
        )
        ParamFieldView(
            name = "highCutoffFreq",
            value = highCutoffFreq,
            onValueChange = { newValue -> highCutoffFreq = newValue }
        )
    }
}

@Composable
fun KalmanFilterView(viewModel: MainActivityViewModel) {
    var processNoiseCov by remember { mutableStateOf(TextFieldValue("")) }
    var measurementNoiseCov by remember { mutableStateOf(TextFieldValue("")) }

    FilterView(
        title = "Метод Калмана",
        apply = {
            viewModel.applyKalmanFilter(
                processNoiseCov.text.toFloat(),
                measurementNoiseCov.text.toFloat()
            )
        }) {
        ParamFieldView(
            name = "processNoiseCov",
            value = processNoiseCov,
            onValueChange = { newValue -> processNoiseCov = newValue }
        )
        ParamFieldView(
            name = "measurementNoiseCov",
            value = measurementNoiseCov,
            onValueChange = { newValue -> measurementNoiseCov = newValue }
        )
    }
}

@Composable
fun GaussianFilterView(viewModel: MainActivityViewModel) {
    var kernelSize by remember { mutableStateOf(TextFieldValue("")) }
    var sigma by remember { mutableStateOf(TextFieldValue("")) }

    FilterView(
        title = "Гауссов фильтр",
        apply = {
            viewModel.applyGaussianFilter(kernelSize.text.toInt(), sigma.text.toDouble())
        }) {
        ParamFieldView(
            name = "kernelSize",
            value = kernelSize,
            onValueChange = { newValue -> kernelSize = newValue }
        )
        ParamFieldView(
            name = "sigma",
            value = sigma,
            onValueChange = { newValue -> sigma = newValue }
        )
    }
}

@Composable
fun MedianFilterView(viewModel: MainActivityViewModel) {
    var windowSize by remember { mutableStateOf(TextFieldValue("")) }

    FilterView(
        title = "Медианный фильтр",
        apply = {
            viewModel.applyMedianFilter(windowSize.text.toInt())
        }) {
        ParamFieldView(
            name = "windowSize",
            value = windowSize,
            onValueChange = { newValue -> windowSize = newValue }
        )
    }
}

@Composable
fun SpectralSubtractionView(viewModel: MainActivityViewModel) {
    var noiseStartMs by remember { mutableStateOf(TextFieldValue("")) }
    var noiseEndMs by remember { mutableStateOf(TextFieldValue("")) }
    var alpha by remember { mutableStateOf(TextFieldValue("")) }

    FilterView(
        title = "Метод шумоподавления с использованием спектрального вычитания",
        apply = {
            viewModel.applySpectralSubtraction(noiseStartMs.text.toInt(), noiseEndMs.text.toInt(), alpha.text.toFloat())
        }) {
        ParamFieldView(
            name = "noiseStartMs",
            value = noiseStartMs,
            onValueChange = { newValue -> noiseStartMs = newValue }
        )
        ParamFieldView(
            name = "noiseEndMs",
            value = noiseEndMs,
            onValueChange = { newValue -> noiseEndMs = newValue }
        )
        ParamFieldView(
            name = "alpha",
            value = alpha,
            onValueChange = { newValue -> alpha = newValue }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParamFieldView(
    name: String,
    value: TextFieldValue,
    onValueChange: (newValue: TextFieldValue) -> Unit
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(0.5f),
        value = value,
        onValueChange = { newValue -> onValueChange(newValue) },
        label = { Text(name) },
        singleLine = true
    )
}

@Composable
fun FilterView(title: String, apply: () -> Unit, Body: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Body()
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                apply()
            }
        ) {
            Text("Применить")
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