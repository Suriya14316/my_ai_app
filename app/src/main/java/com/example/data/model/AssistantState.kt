package com.example.data.model

/**
 * Represents the conversational activity state of the SAYA assistant.
 * Controls the animated orb visualizer, status banner, and audio loop.
 */
enum class AssistantState(val statusLabel: String) {
    IDLE("Say 'Hey SAYA' or 'Weak dady is home'"),
    ACTIVATED("SAYA Activated & Ready!"),
    LISTENING("SAYA is listening..."),
    THINKING("Thinking..."),
    SPEAKING("SAYA is speaking..."),
    ERROR("Need your attention")
}
