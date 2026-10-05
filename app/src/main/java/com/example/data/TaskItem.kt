package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val text: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
