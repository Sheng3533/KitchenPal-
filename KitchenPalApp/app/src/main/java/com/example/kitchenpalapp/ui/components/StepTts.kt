package com.example.kitchenpalapp.ui.components

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.kitchenpalapp.data.CookStep
import java.util.Locale

/**
 * 离线中文语音朗读（Android 原生 TextToSpeech，无需联网/权限）。
 * 在菜谱步骤区用于「朗读全部步骤」与「单步朗读」。
 */
class StepTtsState {
    private var tts: TextToSpeech? = null
    private var appContext: Context? = null
    var ready by mutableStateOf(false)
        private set
    var speaking by mutableStateOf(false)
        private set

    fun init(context: Context) {
        if (tts != null) return
        appContext = context.applicationContext
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale.CHINESE
                val avail = tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_MISSING_DATA
                if (avail >= TextToSpeech.LANG_AVAILABLE) {
                    tts?.language = locale
                }
                tts?.setSpeechRate(0.95f) // 略慢，适合做菜时听清
                tts?.setPitch(1.0f)
                ready = true
            }
        }
    }

    fun speakStep(step: CookStep, index: Int) {
        if (!ensureReady()) return
        stop()
        val text = buildStepText(step, index)
        setDoneListener("step_$index")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "step_$index")
        speaking = true
    }

    fun speakAll(steps: List<CookStep>) {
        if (!ensureReady() || steps.isEmpty()) return
        stop()
        steps.forEachIndexed { i, step ->
            val text = buildStepText(step, i)
            val uttId = "all_$i"
            if (i == steps.lastIndex) setDoneListener(uttId)
            tts?.speak(
                text,
                if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                null,
                uttId
            )
        }
        speaking = true
    }

    /** 进入/切换步骤时自动朗读，未就绪（无中文语音包）则静默跳过，不弹提示。 */
    fun autoSpeakStep(step: CookStep, index: Int) {
        if (!ready) return
        tts?.stop()
        val text = buildStepText(step, index)
        setDoneListener("auto_$index")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "auto_$index")
        speaking = true
    }

    fun stop() {
        tts?.stop()
        speaking = false
    }

    fun shutdown() {
        tts?.setOnUtteranceProgressListener(null)
        tts?.stop()
        tts?.shutdown()
        tts = null
        speaking = false
        ready = false
    }

    private fun ensureReady(): Boolean {
        if (ready) return true
        appContext?.let {
            Toast.makeText(it, "未安装中文语音包，无法朗读", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun setDoneListener(doneId: String) {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                if (utteranceId == doneId) speaking = false
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (utteranceId == doneId) speaking = false
            }
        })
    }

    private fun buildStepText(step: CookStep, index: Int): String = buildString {
        append("第${index + 1}步，")
        if (step.title.isNotBlank()) append(step.title).append("。")
        if (step.description.isNotBlank()) append(step.description)
        if (!step.heat.isNullOrBlank()) append("火候：${step.heat}。")
        if (step.durationSeconds > 0) append("约${step.durationSeconds}秒。")
        if (!step.tip.isNullOrBlank()) append("小贴士：${step.tip}。")
    }
}

@Composable
fun rememberStepTts(): StepTtsState {
    val context = LocalContext.current
    val state = remember { StepTtsState() }
    DisposableEffect(context) {
        state.init(context)
        onDispose { state.shutdown() }
    }
    return state
}
