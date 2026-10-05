package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.TaskRepository
import com.example.ui.TaskScreen
import com.example.ui.TaskViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao())
        TaskViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TaskScreen(viewModel = viewModel)
            }
        }
    }
}
