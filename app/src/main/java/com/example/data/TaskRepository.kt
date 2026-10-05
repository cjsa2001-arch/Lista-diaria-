package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()

    suspend fun addTask(text: String): Long {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return -1L
        val newTask = TaskItem(text = trimmed, isCompleted = false)
        return taskDao.insertTask(newTask)
    }

    suspend fun toggleTask(task: TaskItem) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        taskDao.updateTask(updated)
    }

    suspend fun deleteTask(task: TaskItem) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Long) {
        taskDao.deleteTaskById(id)
    }

    suspend fun clearCompleted() {
        taskDao.deleteCompletedTasks()
    }
}
