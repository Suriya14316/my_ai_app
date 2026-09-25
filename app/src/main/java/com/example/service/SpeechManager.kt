package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.data.model.VoiceProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * SpeechManager coordinates Android SpeechRecognizer (Speech-to-Text)
 * and TextToSpeech (TTS) engine with dual-voice selection and two activation commands.
 *
 * Activation Commands:
 * 1. "Weak dady is home" / "Wake daddy is home"
 * 2. "Hey SAYA"
 *
 * Dual Voice Support:
 * - Voice 1: Iris (Warm Female, pitch 1.18f)
 * - Voice 2: Orion (Deep Male, pitch 0.82f)
 */
class SpeechManager(
    private val context: Context,
    private val onCommandRecognized: (String) -> Unit,
    private val onPartialText: (String) -> Unit,
    private val onSpeechStatusChanged: (Boolean) -> Unit,
    private val onActivationTriggered: (phrase: String) -> Unit
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isRecognitionAvailable = MutableStateFlow(true)
    val isRecognitionAvailable: StateFlow<Boolean> = _isRecognitionAvailable.asStateFlow()

    private val _liveRmsLevel = MutableStateFlow(0f)
    val liveRmsLevel: StateFlow<Float> = _liveRmsLevel.asStateFlow()

    private val _isTtsSpeaking = MutableStateFlow(false)
    val isTtsSpeaking: StateFlow<Boolean> = _isTtsSpeaking.asStateFlow()

    private val _isContinuousWakeWordEnabled = MutableStateFlow(true) // Enabled by default for hands-free activation
    val isContinuousWakeWordEnabled: StateFlow<Boolean> = _isContinuousWakeWordEnabled.asStateFlow()

    private val _activeVoice = MutableStateFlow(VoiceProfile.VOICE_1_IRIS)
    val activeVoice: StateFlow<VoiceProfile> = _activeVoice.asStateFlow()

    private val _isVoiceMatchEnabled = MutableStateFlow(true)
    val isVoiceMatchEnabled: StateFlow<Boolean> = _isVoiceMatchEnabled.asStateFlow()

    private var isCurrentlyListening = false

    init {
        initializeSpeechRecognizer()
        initializeTextToSpeech()
    }

    private fun initializeSpeechRecognizer() {
        val available = SpeechRecognizer.isRecognitionAvailable(context)
        _isRecognitionAvailable.value = available

        if (available) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@SpeechManager)
            }
        }
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                applyVoiceProfile(_activeVoice.value)
                isTtsReady = true

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isTtsSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isTtsSpeaking.value = false
                        handlePostTtsResume()
                    }

                    override fun onError(utteranceId: String?) {
                        _isTtsSpeaking.value = false
                        handlePostTtsResume()
                    }
                })
            }
        }
    }

    fun setVoiceProfile(profile: VoiceProfile) {
        _activeVoice.value = profile
        applyVoiceProfile(profile)
    }

    fun toggleVoiceProfile() {
        val next = if (_activeVoice.value == VoiceProfile.VOICE_1_IRIS) {
            VoiceProfile.VOICE_2_ORION
        } else {
            VoiceProfile.VOICE_1_IRIS
        }
        setVoiceProfile(next)
    }

    fun toggleVoiceMatch() {
        _isVoiceMatchEnabled.value = !_isVoiceMatchEnabled.value
    }

    private fun applyVoiceProfile(profile: VoiceProfile) {
        textToSpeech?.let { tts ->
            tts.setPitch(profile.pitch)
            tts.setSpeechRate(profile.rate)

            // Try to set specific gender voice from system voices if available
            try {
                val availableVoices = tts.voices
                if (availableVoices != null) {
                    val matchingVoice: Voice? = if (profile == VoiceProfile.VOICE_1_IRIS) {
                        availableVoices.firstOrNull {
                            it.locale.language == "en" &&
                                    (it.name.contains("female", ignoreCase = true) ||
                                            it.name.contains("en-us-x-sfg", ignoreCase = true))
                        }
                    } else {
                        availableVoices.firstOrNull {
                            it.locale.language == "en" &&
                                    (it.name.contains("male", ignoreCase = true) ||
                                            it.name.contains("en-us-x-iom", ignoreCase = true))
                        }
                    }
                    if (matchingVoice != null) {
                        tts.voice = matchingVoice
                    }
                }
            } catch (e: Exception) {
                // Fallback to pitch adjustment
            }
        }
    }

    private fun handlePostTtsResume() {
        if (_isContinuousWakeWordEnabled.value) {
            mainHandler.postDelayed({
                startListening()
            }, 600)
        }
    }

    fun toggleContinuousWakeWord() {
        val next = !_isContinuousWakeWordEnabled.value
        _isContinuousWakeWordEnabled.value = next
        if (next && !isCurrentlyListening && !_isTtsSpeaking.value) {
            startListening()
        }
    }

    fun startListening() {
        if (isCurrentlyListening || _isTtsSpeaking.value) return
        if (speechRecognizer == null) {
            initializeSpeechRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        try {
            speechRecognizer?.startListening(intent)
            isCurrentlyListening = true
            onSpeechStatusChanged(true)
        } catch (e: Exception) {
            isCurrentlyListening = false
            onSpeechStatusChanged(false)
        }
    }

    fun stopListening() {
        if (!isCurrentlyListening) return
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignore
        }
        isCurrentlyListening = false
        _liveRmsLevel.value = 0f
        onSpeechStatusChanged(false)
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        stopListening()
        if (isTtsReady && textToSpeech != null) {
            _isTtsSpeaking.value = true
            applyVoiceProfile(_activeVoice.value)
            val utteranceId = "SAYA_SPEECH_${System.currentTimeMillis()}"
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } else {
            mainHandler.postDelayed({
                _isTtsSpeaking.value = false
                onComplete?.invoke()
            }, 1000)
        }
    }

    fun stopSpeaking() {
        if (_isTtsSpeaking.value) {
            textToSpeech?.stop()
            _isTtsSpeaking.value = false
        }
    }

    // --- RecognitionListener Callbacks ---

    override fun onReadyForSpeech(params: Bundle?) {
        isCurrentlyListening = true
        onSpeechStatusChanged(true)
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _liveRmsLevel.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _liveRmsLevel.value = 0f
        isCurrentlyListening = false
        onSpeechStatusChanged(false)
    }

    override fun onError(error: Int) {
        isCurrentlyListening = false
        _liveRmsLevel.value = 0f
        onSpeechStatusChanged(false)

        if (_isContinuousWakeWordEnabled.value && !_isTtsSpeaking.value) {
            mainHandler.postDelayed({
                startListening()
            }, 800)
        }
    }

    override fun onResults(results: Bundle?) {
        isCurrentlyListening = false
        _liveRmsLevel.value = 0f
        onSpeechStatusChanged(false)

        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val recognizedText = matches?.firstOrNull() ?: ""

        if (recognizedText.isNotBlank()) {
            val lower = recognizedText.lowercase(Locale.ROOT).trim()

            // Check Activation Command 1: "weak dady is home" / "wake daddy is home"
            val isDaddyHomeTrigger = lower.contains("weak dady is home") ||
                    lower.contains("wake daddy is home") ||
                    lower.contains("wake dady is home") ||
                    lower.contains("weak daddy is home") ||
                    lower.contains("daddy is home") ||
                    lower.contains("daddy's home")

            // Check Activation Command 2: "hey saya" / "saya"
            val isHeySayaTrigger = lower.contains("hey saya") ||
                    lower == "saya" ||
                    lower.startsWith("hey saya")

            if (isDaddyHomeTrigger) {
                onActivationTriggered("daddy_home")
                return
            }

            if (isHeySayaTrigger) {
                // If there's an attached command after "Hey SAYA", process it
                val stripped = recognizedText
                    .replace("(?i)hey saya".toRegex(), "")
                    .replace("(?i)saya".toRegex(), "")
                    .trim()
                if (stripped.isNotBlank()) {
                    onCommandRecognized(stripped)
                } else {
                    onActivationTriggered("hey_saya")
                }
                return
            }

            // Normal command recognition
            onCommandRecognized(recognizedText)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull() ?: ""
        if (partial.isNotBlank()) {
            onPartialText(partial)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        stopListening()
        stopSpeaking()
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
    }
}
