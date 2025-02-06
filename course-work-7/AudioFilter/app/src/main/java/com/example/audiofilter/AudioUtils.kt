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

object AudioUtils {
    fun readWavFile(inputStream: InputStream): Pair<ShortArray, Int> {
        val byteArray = inputStream.readBytes()
        val sampleRate = ByteBuffer.wrap(byteArray.copyOfRange(24, 28)).order(ByteOrder.LITTLE_ENDIAN).int

        val audioDataStart = 44 // WAV-формат начинается с 44 байта заголовка
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

    fun createWavHeader(dataSize: Int, sampleRate: Int): ByteArray {
        val numChannels = 1
        val bitsPerSample = 16
        val bytesPerSample = bitsPerSample / 8
        val byteRate = sampleRate * numChannels * bytesPerSample
        val blockAlign = numChannels * bytesPerSample
        val totalDataLen = dataSize * bytesPerSample + 36

        return ByteBuffer.allocate(44).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put("RIFF".toByteArray()) // "RIFF" chunk descriptor
            putInt(totalDataLen) // Total file size - 8 bytes
            put("WAVE".toByteArray()) // "WAVE" format
            put("fmt ".toByteArray()) // "fmt " sub-chunk
            putInt(16) // Sub-chunk size (16 for PCM)
            putShort(1) // Audio format (1 = PCM)
            putShort(numChannels.toShort()) // Number of channels
            putInt(sampleRate) // Sample rate (правильная частота)
            putInt(byteRate) // Byte rate (sampleRate * numChannels * bytesPerSample)
            putShort(blockAlign.toShort()) // Block align
            putShort(bitsPerSample.toShort()) // Bits per sample (16 bits)
            put("data".toByteArray()) // "data" sub-chunk
            putInt(dataSize * bytesPerSample) // Data chunk size
        }.array()
    }



}