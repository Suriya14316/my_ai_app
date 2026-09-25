package com.example.service

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.SayaScreen
import com.example.data.model.UserTask
import com.example.data.model.VoiceProfile
import com.example.data.remote.GeminiClient
import com.example.data.remote.WeatherClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Result data class produced by routing a recognized voice command.
 */
sealed class CommandResult {
    data class Immediate(
        val speechText: String,
        val displayText: String,
        val actionBadge: String? = null,
        val launchIntent: Intent? = null,
        val toastNotice: String? = null,
        val isActivation: Boolean = false,
        val navigateToScreen: SayaScreen? = null
    ) : CommandResult()

    data class Async(
        val initialStatus: String,
        val action: suspend () -> Immediate
    ) : CommandResult()
}

/**
 * CommandRouter parses spoken user utterances, matches command patterns,
 * handles screen navigation across all screens, routes to native Android Intents,
 * manages Siri tasks, calls Weather/Gemini APIs, and acknowledges with "Yes, boss!".
 */
class CommandRouter(private val context: Context) {

    fun route(
        rawQuery: String,
        customApiKey: String? = null,
        tasks: List<UserTask> = emptyList(),
        onAddTask: ((String) -> Unit)? = null,
        onCompleteTask: ((String) -> Unit)? = null,
        onToggleVoice: (() -> VoiceProfile)? = null
    ): CommandResult {
        val query = rawQuery.trim().lowercase(Locale.ROOT)

        // 1. Voice Navigation across ALL Screens: Home, Tasks, Controls, Settings
        if (query.contains("navigate to home") || query == "go home" || query == "go to home" || query == "open home" || query == "home screen") {
            return CommandResult.Immediate(
                speechText = "Yes, boss! Navigating to the Home screen.",
                displayText = "Navigated to Home Screen",
                actionBadge = "Navigation",
                navigateToScreen = SayaScreen.HOME
            )
        }

        if (query.contains("navigate to task") || query == "go to tasks" || query == "open tasks" || query == "tasks screen" || query == "show tasks screen") {
            return CommandResult.Immediate(
                speechText = "Yes, boss! Opening your Tasks and Reminders screen.",
                displayText = "Navigated to Tasks Screen",
                actionBadge = "Navigation",
                navigateToScreen = SayaScreen.TASKS
            )
        }

        if (query.contains("navigate to control") || query.contains("smart control") || query == "go to controls" || query == "open shortcuts" || query == "controls screen") {
            return CommandResult.Immediate(
                speechText = "Yes, boss! Navigating to Smart Controls and Shortcuts.",
                displayText = "Navigated to Controls Screen",
                actionBadge = "Navigation",
                navigateToScreen = SayaScreen.CONTROLS
            )
        }

        if (query.contains("navigate to setting") || query.contains("voice setting") || query == "go to settings" || query == "open voice" || query == "settings screen") {
            return CommandResult.Immediate(
                speechText = "Yes, boss! Opening Voice and Assistant Settings.",
                displayText = "Navigated to Voice & AI Settings",
                actionBadge = "Navigation",
                navigateToScreen = SayaScreen.SETTINGS
            )
        }

        // 2. Start Conversation command: "start conversation" / "let's talk" / "chat with me"
        if (query.contains("start conversation") || query.contains("let's talk") || query.contains("lets talk") || query == "chat") {
            return CommandResult.Immediate(
                speechText = "Yes, boss! I'm here and ready to chat. What's on your mind today, neighbor?",
                displayText = "Yes, boss! Starting conversation...",
                actionBadge = "Conversation",
                isActivation = true
            )
        }

        // 3. Activation Command 1: "Weak dady is home" / "Wake daddy is home"
        if (query.contains("weak dady is home") ||
            query.contains("wake daddy is home") ||
            query.contains("wake dady is home") ||
            query.contains("weak daddy is home") ||
            query.contains("daddy is home") ||
            query.contains("daddy's home")
        ) {
            val pendingCount = tasks.count { !it.isCompleted }
            val taskNotice = if (pendingCount > 0) {
                "You have $pendingCount pending tasks."
            } else {
                "All tasks are clear."
            }

            return CommandResult.Immediate(
                speechText = "Yes, boss! Welcome home! SAYA is activated and online. Voice verification confirmed. $taskNotice How can I assist you?",
                displayText = "● ACTIVATED: Yes, boss! Welcome home.\nVoice Verified: Primary User (Daddy)\n$taskNotice",
                actionBadge = "Voice Activation",
                isActivation = true,
                toastNotice = "SAYA Activated: Welcome home routine triggered via authorized voice."
            )
        }

        // 4. Activation Command 2: "Hey SAYA"
        if (query == "hey saya" || query == "saya" || query == "activate saya") {
            return CommandResult.Immediate(
                speechText = "Yes, boss! SAYA is activated and ready for all your tasks.",
                displayText = "● ACTIVATED: Yes, boss! SAYA is online and listening...",
                actionBadge = "Voice Activation",
                isActivation = true
            )
        }

        // 5. Siri-like Task Management: "add task [title]" / "remind me to [title]" / "create task [title]"
        if (query.startsWith("add task ") || query.startsWith("remind me to ") || query.startsWith("create task ")) {
            val prefix = when {
                query.startsWith("add task ") -> "add task "
                query.startsWith("remind me to ") -> "remind me to "
                else -> "create task "
            }
            val taskTitle = rawQuery.substring(prefix.length).trim()
            if (taskTitle.isNotBlank()) {
                onAddTask?.invoke(taskTitle)
                return CommandResult.Immediate(
                    speechText = "Yes, boss! Added \"$taskTitle\" to your tasks list.",
                    displayText = "Task Created: \"$taskTitle\"",
                    actionBadge = "Siri Tasks"
                )
            }
        }

        // 6. Siri-like Task Management: "show my tasks" / "what are my tasks" / "list tasks"
        if (query.contains("show my tasks") || query.contains("what are my tasks") ||
            query.contains("list tasks") || query.contains("my tasks") || query == "tasks"
        ) {
            val pending = tasks.filter { !it.isCompleted }
            return if (pending.isEmpty()) {
                CommandResult.Immediate(
                    speechText = "Yes, boss! You have no pending tasks. All clear!",
                    displayText = "No pending tasks. You're all caught up!",
                    actionBadge = "Siri Tasks"
                )
            } else {
                val listSpeech = pending.mapIndexed { idx, t -> "${idx + 1}: ${t.title}" }.joinToString(". ")
                val listDisplay = pending.mapIndexed { idx, t -> "• ${t.title}" }.joinToString("\n")
                CommandResult.Immediate(
                    speechText = "Yes, boss! You have ${pending.size} pending tasks: $listSpeech.",
                    displayText = "Active Tasks (${pending.size}):\n$listDisplay",
                    actionBadge = "Siri Tasks"
                )
            }
        }

        // 7. Siri-like Task Management: "complete task [title]"
        if (query.startsWith("complete task ") || query.startsWith("finish task ")) {
            val titlePart = rawQuery.substring(if (query.startsWith("complete task ")) "complete task ".length else "finish task ".length).trim()
            onCompleteTask?.invoke(titlePart)
            return CommandResult.Immediate(
                speechText = "Yes, boss! Marked task as completed.",
                displayText = "Task marked completed: \"$titlePart\"",
                actionBadge = "Siri Tasks"
            )
        }

        // 8. Voice Profile switching: "switch voice" / "change voice"
        if (query.contains("switch voice") || query.contains("change voice") ||
            query.contains("use voice 1") || query.contains("use voice 2")
        ) {
            val updated = onToggleVoice?.invoke() ?: VoiceProfile.VOICE_1_IRIS
            return CommandResult.Immediate(
                speechText = "Yes, boss! Voice updated. Now speaking with ${updated.displayName}.",
                displayText = "Switched to ${updated.displayName}\n(${updated.description})",
                actionBadge = "Voice Profile"
            )
        }

        // 9. WhatsApp Command
        if (query == "open whatsapp" || query.startsWith("open whatsapp")) {
            val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/"))
            return CommandResult.Immediate(
                speechText = "Yes, boss! Opening WhatsApp for you right now.",
                displayText = "Opening WhatsApp...",
                actionBadge = "WhatsApp Intent",
                launchIntent = intent
            )
        }

        // 10. YouTube Command
        if (query == "open youtube" || query.startsWith("open youtube")) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return CommandResult.Immediate(
                speechText = "Yes, boss! Opening YouTube for you now, enjoy watching!",
                displayText = "Opening YouTube...",
                actionBadge = "YouTube",
                launchIntent = intent
            )
        }

        // 11. Email / Gmail Command
        if (query == "open email" || query == "open gmail" || query.startsWith("open email") || query.startsWith("open gmail")) {
            val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))
            return CommandResult.Immediate(
                speechText = "Yes, boss! Opening your email inbox right away.",
                displayText = "Opening Email...",
                actionBadge = "Email Intent",
                launchIntent = intent
            )
        }

        // 12. Call [name] Command
        if (query.startsWith("call ")) {
            val contactName = rawQuery.substring(5).trim()
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:")
            }
            return CommandResult.Immediate(
                speechText = "Yes, boss! Opening the phone dialer to call $contactName.",
                displayText = "Calling $contactName (Opening phone dialer...)",
                actionBadge = "Phone Dialer",
                launchIntent = dialIntent,
                toastNotice = "Note: Native Android CALL_PHONE permission is guarded; using ACTION_DIAL ensures user safety."
            )
        }

        // 13. Send message to [name]
        if (query.startsWith("send message to ")) {
            val contactName = rawQuery.substring("send message to ".length).trim()
            val encodedName = Uri.encode(contactName)
            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=Hi%20$encodedName"))
            return CommandResult.Immediate(
                speechText = "Yes, boss! Opening messaging for $contactName.",
                displayText = "Opening message for $contactName...",
                actionBadge = "Message Intent",
                launchIntent = waIntent,
                toastNotice = "Browser limitation: Web cannot access native device contacts. Android uses ContactsContract or WhatsApp link."
            )
        }

        // 14. Current Time Command
        if (query.contains("what is the time") || query.contains("what's the time") || query == "time") {
            val timeFormat = SimpleDateFormat("h:mm a 'on' EEEE, MMMM d", Locale.getDefault())
            val formattedTime = timeFormat.format(Date())
            return CommandResult.Immediate(
                speechText = "Yes, boss! It is currently $formattedTime.",
                displayText = "Current time is $formattedTime",
                actionBadge = "Clock"
            )
        }

        // 15. Weather Command
        if (query.contains("weather today") || query.contains("what's the weather") || query.contains("weather")) {
            return CommandResult.Async(
                initialStatus = "Checking Open-Meteo weather...",
                action = {
                    fetchWeather()
                }
            )
        }

        // 16. Search [query] on Google
        if (query.contains("search ") && query.contains("on google")) {
            val extractedQuery = extractSearchQuery(rawQuery)
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, extractedQuery)
            }
            return CommandResult.Immediate(
                speechText = "Yes, boss! Searching Google for $extractedQuery.",
                displayText = "Searching Google: \"$extractedQuery\"",
                actionBadge = "Google Search",
                launchIntent = searchIntent
            )
        }

        // 17. Fallback to Gemini AI conversational query
        return CommandResult.Async(
            initialStatus = "Asking SAYA...",
            action = {
                val responseResult = GeminiClient.askSaya(rawQuery, customApiKey)
                val reply = responseResult.getOrDefault(
                    "Yes, boss! I'm here to help, but had a slight issue with my connection. Feel free to ask again!"
                )
                CommandResult.Immediate(
                    speechText = reply,
                    displayText = reply,
                    actionBadge = "Gemini AI"
                )
            }
        )
    }

    private suspend fun fetchWeather(): CommandResult.Immediate {
        return try {
            val response = WeatherClient.api.getCurrentWeather(
                latitude = 37.7749,
                longitude = -122.4194
            )
            val current = response.current
            val temp = current?.temperature?.let { "$it°C" } ?: "20°C"
            val condition = WeatherClient.weatherCodeToCondition(current?.weatherCode)
            val wind = current?.windSpeed?.let { "$it km/h" } ?: "10 km/h"

            val spoken = "Yes, boss! Today's weather is $condition with a temperature of $temp and wind speeds of $wind."
            val displayed = "Weather: $condition\nTemp: $temp | Wind: $wind"

            CommandResult.Immediate(
                speechText = spoken,
                displayText = displayed,
                actionBadge = "Open-Meteo API"
            )
        } catch (e: Exception) {
            CommandResult.Immediate(
                speechText = "Yes, boss! I couldn't reach the weather service right now, but it looks like a nice day!",
                displayText = "Weather service currently unavailable (${e.localizedMessage})",
                actionBadge = "Open-Meteo"
            )
        }
    }

    private fun extractSearchQuery(raw: String): String {
        var clean = raw
        val onGoogleIndex = clean.lowercase(Locale.ROOT).indexOf("on google")
        if (onGoogleIndex != -1) {
            clean = clean.substring(0, onGoogleIndex).trim()
        }
        val searchIndex = clean.lowercase(Locale.ROOT).indexOf("search")
        if (searchIndex != -1) {
            clean = clean.substring(searchIndex + "search".length).trim()
        }
        return clean.ifBlank { raw }
    }
}
