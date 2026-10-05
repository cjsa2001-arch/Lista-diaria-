package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TaskItem
import com.example.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter {
    ALL,
    PENDING,
    COMPLETED
}

data class TaskUiState(
    val tasks: List<TaskItem> = emptyList(),
    val filteredTasks: List<TaskItem> = emptyList(),
    val currentFilter: TaskFilter = TaskFilter.ALL,
    val inputText: String = "",
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progress: Float = 0f,
    val recentDeletedTask: TaskItem? = null
)

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    private val _recentDeletedTask = MutableStateFlow<TaskItem?>(null)

    val uiState: StateFlow<TaskUiState> = combine(
        repository.allTasks,
        _inputText,
        _currentFilter,
        _recentDeletedTask
    ) { tasks, inputText, filter, deletedTask ->
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

        val filtered = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.PENDING -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
        }

        TaskUiState(
            tasks = tasks,
            filteredTasks = filtered,
            currentFilter = filter,
            inputText = inputText,
            completedCount = completed,
            totalCount = total,
            progress = progress,
            recentDeletedTask = deletedTask
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState()
    )

    fun onInputTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun onAddTask() {
        val text = _inputText.value.trim()
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                repository.addTask(text)
                _inputText.value = ""
            }
        }
    }

    fun onToggleTask(task: TaskItem) {
        viewModelScope.launch {
            repository.toggleTask(task)
        }
    }

    fun onDeleteTask(task: TaskItem) {
        viewModelScope.launch {
            _recentDeletedTask.value = task
            repository.deleteTask(task)
        }
    }

    fun onRestoreTask() {
        val task = _recentDeletedTask.value ?: return
        viewModelScope.launch {
            repository.addTask(task.text)
            _recentDeletedTask.value = null
        }
    }

    fun onDismissSnackbar() {
        _recentDeletedTask.value = null
    }

    fun onFilterSelected(filter: TaskFilter) {
        _currentFilter.value = filter
    }

    fun onClearCompleted() {
        viewModelScope.launch {
            repository.clearCompleted()
        }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                return TaskViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
