package com.example.data.model

import java.util.UUID

enum class MessageSender {
    USER, SAYA
}

/**
 * Represents a single message bubble in SAYA's dark-mode iMessage transcript.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionBadge: String? = null,
    val isError: Boolean = false
)
