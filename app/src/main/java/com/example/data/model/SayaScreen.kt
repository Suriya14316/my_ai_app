package com.example.data.model

/**
 * Screen destinations supported in the SAYA voice assistant.
 * Users can switch between screens using the bottom bar or voice navigation commands:
 * e.g. "navigate to home", "navigate to tasks", "navigate to controls", "navigate to settings".
 */
enum class SayaScreen(val id: String, val title: String) {
    HOME("home", "Home"),
    TASKS("tasks", "Tasks"),
    CONTROLS("controls", "Controls"),
    SETTINGS("settings", "Voice & AI")
}
