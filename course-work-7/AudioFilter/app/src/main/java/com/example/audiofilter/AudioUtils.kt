package com.example.audiofilter

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.min
import kotlin.math.pow

object AudioUtils {
    fun readWavFile(inputStream: InputStream): Pair<ShortArray, Int> {
        val byteArray = inputStream.readBytes()
        val sampleRate = ByteBuffer.wrap(byteArray.copyOfRange(24, 28)).order(ByteOrder.LITTLE_ENDIAN).int

        val audioDataStart = 44
        val audioBytes = byteArray.copyOfRange(audioDataStart, byteArray.size)
        val buffer = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()

        val audioSamples = ShortArray(buffer.limit()) { buffer[it] }
        return Pair(audioSamples, sampleRate)
    }

    fun lowPassFilter(input: ShortArray, sampleRate: Int, cutoffFreq: Float): ShortArray {
        val output = ShortArray(input.size)
        val rc = 1.0f / (cutoffFreq * 2.0f * Math.PI).toFloat()
        val dt = 1.0f / sampleRate
        val alpha = dt / (rc + dt)

        output[0] = input[0]
        for (i in 1 until input.size) {
            output[i] = ((alpha * input[i] + (1 - alpha) * output[i - 1]).toInt()).toShort()
        }
        return output
    }

    fun highPassFilter(input: ShortArray, sampleRate: Int, cutoffFreq: Float): ShortArray {
        val output = ShortArray(input.size)
        val rc = 1.0f / (cutoffFreq * 2.0f * Math.PI).toFloat()
        val dt = 1.0f / sampleRate
        val alpha = rc / (rc + dt)

        output[0] = input[0]
        for (i in 1 until input.size) {
            output[i] = ((alpha * (output[i - 1] + input[i] - input[i - 1])).toInt()).toShort()
        }
        return output
    }

    fun bandPassFilter(input: ShortArray, sampleRate: Int, lowCutoffFreq: Float, highCutoffFreq: Float): ShortArray {
        val output = ShortArray(input.size)

        val lowRC = 1.0f / (lowCutoffFreq * 2.0f * Math.PI).toFloat()
        val highRC = 1.0f / (highCutoffFreq * 2.0f * Math.PI).toFloat()

        val dt = 1.0f / sampleRate

        val lowAlpha = dt / (lowRC + dt)
        val highAlpha = highRC / (highRC + dt)

        var lowPrev = 0.0f
        var highPrev = 0.0f

        output[0] = input[0]

        for (i in 1 until input.size) {
            lowPrev = lowAlpha * input[i] + (1 - lowAlpha) * lowPrev
            highPrev = highAlpha * (highPrev + input[i] - input[i - 1])
            output[i] = (lowPrev - highPrev).toInt().toShort()
        }

        return output
    }

    fun kalmanFilter(input: ShortArray, sampleRate: Int, processNoiseCov: Float, measurementNoiseCov: Float): ShortArray {
        val n = input.size
        val filteredData = ShortArray(n)

        // Начальные значения
        var x = input[0].toFloat()  // Начальное предположение о состоянии (первый элемент)
        var P = 1.0f  // Начальная ковариация ошибки (доверие к начальному состоянию)
        val A = 1.0f  // Матрица состояния (здесь предполагаем, что аудио меняется линейно)
        val H = 1.0f  // Матрица наблюдений (тоже предполагаем линейность)

        // Параметры шума
        val Q = processNoiseCov  // Ковариация шума процесса
        val R = measurementNoiseCov  // Ковариация шума измерений

        for (i in 0 until n) {
            val z = input[i].toFloat()  // Текущее наблюдение

            // Прогнозирование
            val xMinus = A * x  // Прогнозируемое состояние
            var PMinus = A * P * A + Q  // Прогнозированная ковариация ошибки

            // Вычисление усиления Калмана
            val K = PMinus * H / (H * PMinus * H + R)

            // Обновление состояния
            x = xMinus + K * (z - H * xMinus)

            // Обновление ковариации
            P = (1 - K * H) * PMinus

            // Сохранение результата в массив
            filteredData[i] = x.toInt().toShort()  // Конвертируем в short перед сохранением
        }

        return filteredData
    }

    fun gaussianFilter(audioData: ShortArray, kernelSize: Int, sigma: Double): ShortArray {
        val kernel = generateGaussianKernel(kernelSize, sigma)

        val filteredData = ShortArray(audioData.size)

        for (i in audioData.indices) {
            var sum = 0.0
            var weightSum = 0.0
            for (j in -kernelSize / 2..kernelSize / 2) {
                val index = i + j
                if (index in audioData.indices) {
                    sum += audioData[index].toDouble() * kernel[j + kernelSize / 2]
                    weightSum += kernel[j + kernelSize / 2]
                }
            }
            filteredData[i] = (sum / weightSum).toInt().toShort()
        }

        return filteredData
    }

    fun generateGaussianKernel(size: Int, sigma: Double): DoubleArray {
        val kernel = DoubleArray(size)
        val mean = (size - 1) / 2.0
        var sum = 0.0
        for (i in 0 until size) {
            val x = i - mean
            kernel[i] = Math.exp(-0.5 * (x * x) / (sigma * sigma))
            sum += kernel[i]
        }
        for (i in 0 until size) {
            kernel[i] /= sum
        }
        return kernel
    }

    fun medianFilter(audioData: ShortArray, windowSize: Int): ShortArray {
        val filteredData = ShortArray(audioData.size)

        val halfWindow = windowSize / 2

        for (i in audioData.indices) {
            val window = mutableListOf<Short>()
            for (j in -halfWindow..halfWindow) {
                val index = i + j
                if (index in audioData.indices) {
                    window.add(audioData[index])
                }
            }
            window.sort()
            filteredData[i] = window[window.size / 2]
        }

        return filteredData
    }

    fun extractNoiseProfile(inputSignal: ShortArray, sampleRate: Int, startMs: Int, endMs: Int): ShortArray {
        val startIndex = (startMs * sampleRate) / 1000
        val endIndex = (endMs * sampleRate) / 1000
        return inputSignal.copyOfRange(startIndex, endIndex)
    }

    fun spectralSubtraction(audioSignal: ShortArray, sampleRate: Int, startMs: Int, endMs: Int, alpha: Float, windowSize: Int = 4096, overlap: Int = 2048): ShortArray {

        val noiseProfile: ShortArray = extractNoiseProfile(audioSignal, sampleRate, startMs, endMs)

        if (windowSize != 2048 && windowSize != 4096 && windowSize != 8192) {
            throw IllegalArgumentException("Window size must be 2048, 4096, or 8192 samples.")
        }

        val transformer = FastFourierTransformer(DftNormalization.STANDARD)

        val outputSignal = mutableListOf<Short>()

        val noiseMagnitudeWindows = mutableListOf<List<Double>>()

        var startIndex = 0
        while (startIndex + windowSize <= noiseProfile.size) {
            val noiseWindow = noiseProfile.sliceArray(startIndex until startIndex + windowSize)
            val noiseSpectrum = transformer.transform(noiseWindow.map { it.toDouble() }.toDoubleArray(), TransformType.FORWARD)
            val noiseMagnitude = noiseSpectrum.map { it.abs() }
            noiseMagnitudeWindows.add(noiseMagnitude)
            startIndex += windowSize
        }

        val avgNoiseMagnitudeWindow = List(windowSize) {
                index -> noiseMagnitudeWindows.map { it[index] }.average()
        }

        startIndex = 0
        while (startIndex + windowSize <= audioSignal.size) {
            val audioSignalWindow = audioSignal.sliceArray(startIndex until startIndex + windowSize)
            val audioSignalSpectrum = transformer.transform(audioSignalWindow.map { it.toDouble() }.toDoubleArray(), TransformType.FORWARD)
            val cleanedSpectrum = audioSignalSpectrum.mapIndexed { index, value ->
                val magnitude = value.abs()
                val newMagnitude = (magnitude - alpha * avgNoiseMagnitudeWindow[index]).coerceAtLeast(0.0)
                val phase = value.argument
                Complex(newMagnitude * cos(phase), newMagnitude * sin(phase))
            }

            val cleanedSignal = transformer.transform(cleanedSpectrum.toTypedArray(), TransformType.INVERSE)
            val cleanedSignalReal = cleanedSignal.map { it.real.toInt().toShort() }
            outputSignal.addAll(cleanedSignalReal)

            startIndex += windowSize
        }

        return outputSignal.toShortArray()
    }


    fun saveToMediaStore(context: Context, outputSamples: ShortArray, sampleRate: Int): Uri? {
        val fileName = "filtered_audio_${System.currentTimeMillis()}.wav"
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
            put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
            put(MediaStore.Audio.Media.IS_PENDING, 1) // Файл помечен как временный
        }

        val resolver = context.contentResolver
        val audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

        audioUri?.let { uri ->
            resolver.openOutputStream(uri)?.use { outputStream ->
                Log.d("AudioSave", "Saving WAV with sampleRate: $sampleRate, dataSize: ${outputSamples.size}")
                writeWavFile(outputStream, outputSamples, sampleRate)
            }
            contentValues.clear()
            contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
            MediaScannerConnection.scanFile(context, arrayOf(uri.toString()), null, null)
        }

        return audioUri
    }

    fun writeWavFile(outputStream: OutputStream, samples: ShortArray, sampleRate: Int) {
        val byteBuffer = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { byteBuffer.putShort(it) }

        val header = createWavHeader(samples.size, sampleRate)
        outputStream.write(header)
        outputStream.write(byteBuffer.array())
    }

    private fun createWavHeader(dataSize: Int, sampleRate: Int): ByteArray {
        val numChannels = 1
        val bitsPerSample = 16
        val bytesPerSample = bitsPerSample / 8
        val byteRate = sampleRate * numChannels * bytesPerSample
        val blockAlign = numChannels * bytesPerSample
        val totalDataLen = dataSize * bytesPerSample + 36

        return ByteBuffer.allocate(44).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put("RIFF".toByteArray())
            putInt(totalDataLen)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16)
            putShort(1)
            putShort(numChannels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign.toShort())
            putShort(bitsPerSample.toShort())
            put("data".toByteArray())
            putInt(dataSize * bytesPerSample)
        }.array()
    }



}