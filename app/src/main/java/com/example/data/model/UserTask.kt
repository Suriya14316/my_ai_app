package com.example.data.model

import java.util.UUID

/**
 * Model representing a user task/reminder accessible via voice commands (like Siri).
 */
data class UserTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
