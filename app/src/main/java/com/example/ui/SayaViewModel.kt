package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AssistantState
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.SayaScreen
import com.example.data.model.UserTask
import com.example.data.model.VoiceProfile
import com.example.service.CommandResult
import com.example.service.CommandRouter
import com.example.service.SpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SayaViewModel(application: Application) : AndroidViewModel(application) {

    private val commandRouter = CommandRouter(application)

    private val _currentScreen = MutableStateFlow(SayaScreen.HOME)
    val currentScreen: StateFlow<SayaScreen> = _currentScreen.asStateFlow()

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.SAYA,
                text = "Yes, boss! SAYA is online. Say \"Weak dady is home\" or \"Hey SAYA\" to activate, or tell me to navigate to any screen.",
                actionBadge = "Welcome"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _toastEvent = MutableStateFlow<String?>(null)
    val toastEvent: StateFlow<String?> = _toastEvent.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Siri-like Tasks & Reminders
    private val _tasks = MutableStateFlow<List<UserTask>>(
        listOf(
            UserTask(title = "Review tomorrow morning schedule"),
            UserTask(title = "Check home security & smart locks"),
            UserTask(title = "Call Mom this evening")
        )
    )
    val tasks: StateFlow<List<UserTask>> = _tasks.asStateFlow()

    val speechManager: SpeechManager = SpeechManager(
        context = application,
        onCommandRecognized = { query ->
            _liveTranscript.value = ""
            processCommand(query)
        },
        onPartialText = { partial ->
            _liveTranscript.value = partial
        },
        onSpeechStatusChanged = { isListening ->
            if (isListening) {
                if (_assistantState.value != AssistantState.ACTIVATED) {
                    _assistantState.value = AssistantState.LISTENING
                }
            } else if (_assistantState.value == AssistantState.LISTENING) {
                _assistantState.value = AssistantState.IDLE
            }
        },
        onActivationTriggered = { phrase ->
            _liveTranscript.value = ""
            _assistantState.value = AssistantState.ACTIVATED
            if (phrase == "daddy_home") {
                processCommand("weak dady is home")
            } else {
                processCommand("hey saya")
            }
        }
    )

    val rmsVolume: StateFlow<Float> = speechManager.liveRmsLevel
    val isContinuousWakeWordEnabled: StateFlow<Boolean> = speechManager.isContinuousWakeWordEnabled
    val activeVoice: StateFlow<VoiceProfile> = speechManager.activeVoice
    val isVoiceMatchEnabled: StateFlow<Boolean> = speechManager.isVoiceMatchEnabled

    fun navigateTo(screen: SayaScreen) {
        _currentScreen.value = screen
    }

    fun toggleListening() {
        if (_assistantState.value == AssistantState.LISTENING) {
            speechManager.stopListening()
            _assistantState.value = AssistantState.IDLE
        } else if (_assistantState.value == AssistantState.SPEAKING) {
            speechManager.stopSpeaking()
            _assistantState.value = AssistantState.IDLE
        } else {
            speechManager.startListening()
        }
    }

    fun toggleContinuousWakeWord() {
        speechManager.toggleContinuousWakeWord()
    }

    fun setVoiceProfile(voice: VoiceProfile) {
        speechManager.setVoiceProfile(voice)
    }

    fun toggleVoiceProfile() {
        speechManager.toggleVoiceProfile()
    }

    fun toggleVoiceMatch() {
        speechManager.toggleVoiceMatch()
    }

    fun replaySpeech(text: String) {
        _assistantState.value = AssistantState.SPEAKING
        speechManager.speak(text) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun saveCustomApiKey(key: String) {
        _customApiKey.value = key.trim()
    }

    fun clearToast() {
        _toastEvent.value = null
    }

    // Task Management
    fun addTask(title: String) {
        if (title.isBlank()) return
        val newTask = UserTask(title = title.trim())
        _tasks.value = _tasks.value + newTask
        _toastEvent.value = "Added task: \"${newTask.title}\""
    }

    fun toggleTask(id: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) task.copy(isCompleted = !task.isCompleted) else task
        }
    }

    fun deleteTask(id: String) {
        _tasks.value = _tasks.value.filter { it.id != id }
    }

    fun completeTaskByTitle(titleSubstring: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.title.contains(titleSubstring, ignoreCase = true)) {
                task.copy(isCompleted = true)
            } else {
                task
            }
        }
    }

    fun processCommand(query: String, onLaunchIntent: ((Intent) -> Unit)? = null) {
        if (query.isBlank()) return

        // 1. Add user message bubble
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = query
        )
        _messages.value = _messages.value + userMsg

        // 2. Set Assistant to THINKING
        _assistantState.value = AssistantState.THINKING

        // 3. Route Command
        val result = commandRouter.route(
            rawQuery = query,
            customApiKey = _customApiKey.value,
            tasks = _tasks.value,
            onAddTask = { title -> addTask(title) },
            onCompleteTask = { title -> completeTaskByTitle(title) },
            onToggleVoice = {
                toggleVoiceProfile()
                speechManager.activeVoice.value
            }
        )

        when (result) {
            is CommandResult.Immediate -> {
                handleImmediateResult(result, onLaunchIntent)
            }
            is CommandResult.Async -> {
                viewModelScope.launch {
                    val asyncResult = result.action()
                    handleImmediateResult(asyncResult, onLaunchIntent)
                }
            }
        }
    }

    private fun handleImmediateResult(
        result: CommandResult.Immediate,
        onLaunchIntent: ((Intent) -> Unit)?
    ) {
        // Handle voice navigation
        result.navigateToScreen?.let { target ->
            _currentScreen.value = target
        }

        // Show optional educational toast/snackbar
        result.toastNotice?.let { notice ->
            _toastEvent.value = notice
        }

        // Add SAYA message bubble
        val sayaMsg = ChatMessage(
            sender = MessageSender.SAYA,
            text = result.displayText,
            actionBadge = result.actionBadge
        )
        _messages.value = _messages.value + sayaMsg

        // Speak the response
        _assistantState.value = if (result.isActivation) AssistantState.ACTIVATED else AssistantState.SPEAKING
        speechManager.speak(result.speechText) {
            _assistantState.value = AssistantState.IDLE
        }

        // Launch Intent if present
        result.launchIntent?.let { intent ->
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                getApplication<Application>().startActivity(intent)
            } catch (e: Exception) {
                _toastEvent.value = "App not installed on device. Falling back smoothly."
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
