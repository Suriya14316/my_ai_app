package com.example.data.model

/**
 * Defines the two distinct SAYA voice profiles:
 * - IRIS: Warm, melodic Siri-inspired female voice
 * - ORION: Deep, calm, smooth Siri-inspired male voice
 */
enum class VoiceProfile(
    val id: String,
    val displayName: String,
    val shortName: String,
    val pitch: Float,
    val rate: Float,
    val description: String
) {
    VOICE_1_IRIS(
        id = "iris",
        displayName = "Voice 1: Iris (Warm Female)",
        shortName = "Iris",
        pitch = 1.18f,
        rate = 1.0f,
        description = "Melodic, friendly Siri-inspired warm voice"
    ),
    VOICE_2_ORION(
        id = "orion",
        displayName = "Voice 2: Orion (Deep Male)",
        shortName = "Orion",
        pitch = 0.82f,
        rate = 0.95f,
        description = "Calm, resonant Siri-inspired deep tone"
    )
}
