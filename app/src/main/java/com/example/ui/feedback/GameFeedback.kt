package com.example.ui.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.R

/** Effets sonores du jeu. Les fichiers sont res/raw/snd_<pack>_<nom>.wav. */
enum class Sfx { MOVE, CAPTURE, CASTLE, CHECK, PROMOTE, WIN, LOSE, DRAW, ILLEGAL, START }

/** Familles de sons. */
enum class SoundPack(val id: String, val fr: String, val en: String) {
    WOOD("wood", "Bois", "Wood"),
    METAL("metal", "Métal", "Metal"),
    GLASS("glass", "Verre", "Glass"),
    FUTURE("future", "Futuriste", "Futuristic");

    companion object {
        fun fromId(id: String?) = values().firstOrNull { it.id == id } ?: WOOD
    }
}

/** Nature d'un coup, déduite de la différence entre deux positions. */
enum class MoveKind { QUIET, CAPTURE, CASTLE, CHECK, PROMOTION }

/**
 * Sons (SoundPool) et vibrations. Les deux sont activables séparément ; si le téléphone n'a pas
 * de vibreur ou si le son n'est pas chargé, l'appel est simplement ignoré.
 */
class GameFeedback(context: Context) {
    private val appContext = context.applicationContext
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val ids = HashMap<Sfx, Int>()
    private val ready = HashSet<Int>()

    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

    @Volatile var soundEnabled: Boolean = true
    @Volatile var hapticsEnabled: Boolean = true
    @Volatile var volume: Float = 0.85f
    private var pack: SoundPack? = null

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status -> if (status == 0) synchronized(ready) { ready.add(sampleId) } }
        setPack(SoundPack.WOOD)
    }

    /** Charge un autre pack de sons (remplace l'ancien). */
    fun setPack(newPack: SoundPack) {
        if (pack == newPack) return
        pack = newPack
        ids.values.forEach { runCatching { pool.unload(it) } }
        ids.clear()
        synchronized(ready) { ready.clear() }
        Sfx.values().forEach { sfx ->
            val res = appContext.resources.getIdentifier("snd_${newPack.id}_${sfx.name.lowercase()}", "raw", appContext.packageName)
            if (res != 0) runCatching { ids[sfx] = pool.load(appContext, res, 1) }
        }
    }

    fun play(sfx: Sfx, gain: Float = 1f) {
        if (!soundEnabled) return
        val id = ids[sfx] ?: return
        if (synchronized(ready) { id !in ready }) return
        val v = (volume * gain).coerceIn(0f, 1f)
        pool.play(id, v, v, 1, 0, 1f)
    }

    /** Son + vibration adaptés à un coup. */
    fun onMove(kind: MoveKind) {
        when (kind) {
            MoveKind.QUIET -> { play(Sfx.MOVE); buzz(longArrayOf(0, 10), intArrayOf(0, 70)) }
            MoveKind.CAPTURE -> { play(Sfx.CAPTURE); buzz(longArrayOf(0, 28), intArrayOf(0, 170)) }
            MoveKind.CASTLE -> { play(Sfx.CASTLE); buzz(longArrayOf(0, 18, 70, 22), intArrayOf(0, 120, 0, 150)) }
            MoveKind.CHECK -> { play(Sfx.CHECK); buzz(longArrayOf(0, 35, 60, 35, 60, 70), intArrayOf(0, 200, 0, 200, 0, 255)) }
            MoveKind.PROMOTION -> { play(Sfx.PROMOTE); buzz(longArrayOf(0, 15, 50, 25, 50, 40), intArrayOf(0, 90, 0, 150, 0, 220)) }
        }
    }

    fun onGameOver(sfx: Sfx) {
        play(sfx)
        buzz(longArrayOf(0, 120, 80, 200), intArrayOf(0, 200, 0, 255))
    }

    fun onIllegal() {
        play(Sfx.ILLEGAL, 0.7f)
        buzz(longArrayOf(0, 20, 40, 20), intArrayOf(0, 140, 0, 140))
    }

    private fun buzz(timings: LongArray, amplitudes: IntArray) {
        if (!hapticsEnabled) return
        val v = vibrator ?: return
        runCatching {
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = if (v.hasAmplitudeControl()) amplitudes else IntArray(amplitudes.size) { if (it % 2 == 0) 0 else VibrationEffect.DEFAULT_AMPLITUDE }
                v.vibrate(VibrationEffect.createWaveform(timings, amp, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(timings, -1)
            }
        }
    }

    fun release() {
        runCatching { pool.release() }
    }
}

val LocalGameFeedback = staticCompositionLocalOf<GameFeedback?> { null }
