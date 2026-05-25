package com.example.simon_intermediate

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.sin

// Gestisce la generazione e la riproduzione di un suono sinusoidale (nota musicale)  utilizzando AudioTrack in modalità statica
class SimonSound(private val frequenza: Double, private val durataMs: Int = 500) {
    private val tagSimonSound = this::class.simpleName
    private var audioTrack: AudioTrack? = null
    private lateinit var buffer: ShortArray

    init {
        prepare()
    }

    // Prepara l'AudioTrack per la riproduzione. Se è già inizializzato, non fa nulla
    fun prepare() {
        if (audioTrack == null) {
            buffer = generatePCMSineWave(frequenza, durataMs)
            audioTrack = createAudioTrack(buffer)
            Log.d(tagSimonSound, "Sound prepared for frequency $frequenza")
        }
    }

    // Riproduce il suono dall'inizio. Se il suono è già in riproduzione, viene riavvolto e riavviato immediatamente
    fun play() {
        try {
            prepare()
            audioTrack?.let {
                it.pause()
                it.reloadStaticData()
                it.play()
            }
        } catch (e: Exception) {
            Log.e(tagSimonSound, "Error playing sound", e)
        }
    }

    // Libera le risorse hardware associate all'AudioTrack
    fun release() {
        audioTrack?.let {
            it.release()
            Log.d(tagSimonSound, "Sound released for frequency $frequenza")
        }
        audioTrack = null
    }

    private fun generatePCMSineWave(frequenza: Double, durataMs: Int): ShortArray {
        val sampleRate = 44100
        val numeroCampioni = (durataMs * sampleRate) / 1000
        val buffer = ShortArray(numeroCampioni)

        // Durata del fade in/out in millisecondi per rendere il suono più "dolce"
        val fadeMs = 40
        val campioniFade = (fadeMs * sampleRate) / 1000

        // Volume al 70% per evitare distorsioni (clipping) su alcuni speaker
        val volumeMax = 0.7

        repeat(numeroCampioni) { i ->
            val tempo = i.toDouble() / sampleRate
            val angolo = 2.0 * PI * frequenza * tempo
            val sinusoide = sin(angolo)

            // Calcolo l'inviluppo di ampiezza (Gain)
            val gain = when {
                i < campioniFade -> {
                    // Fase di Fade-in (Attacco)
                    i.toDouble() / campioniFade
                }

                i >= numeroCampioni - campioniFade -> {
                    // Fase di Fade-out (Rilascio) - assicuro che l'ultimo campione sia 0
                    val campioniMancanti = (numeroCampioni - 1) - i
                    campioniMancanti.toDouble() / campioniFade
                }

                else -> {
                    // Sustain (Volume massimo)
                    1.0
                }
            }

            buffer[i] = (sinusoide * Short.MAX_VALUE * volumeMax * gain).toInt().toShort()
        }
        return buffer
    }

    // https://developer.android.com/reference/android/media/AudioTrack.Builder
    private fun createAudioTrack(bufferPcm: ShortArray): AudioTrack {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(44100)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferPcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(bufferPcm, 0, bufferPcm.size)
        return audioTrack
    }
}