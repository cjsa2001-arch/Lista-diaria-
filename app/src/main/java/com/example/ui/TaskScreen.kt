package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.TaskItem
import com.example.ui.theme.AccentDelete
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentMintContainer
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.OnAccentMint
import com.example.ui.theme.TextDone
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Handle snackbar for undoing deletion
    LaunchedEffect(uiState.recentDeletedTask) {
        val deleted = uiState.recentDeletedTask
        if (deleted != null) {
            val result = snackbarHostState.showSnackbar(
                message = "Tarea eliminada",
                actionLabel = "Deshacer",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onRestoreTask()
            } else {
                viewModel.onDismissSnackbar()
            }
        }
    }

    // Auto-scroll when new task is added
    LaunchedEffect(uiState.tasks.size) {
        if (uiState.tasks.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Header with date & progress summary
            HeaderSection(
                uiState = uiState,
                onClearCompleted = { showClearDialog = true }
            )

            // Filter Tabs (Todas, Pendientes, Completadas)
            FilterChipsRow(
                currentFilter = uiState.currentFilter,
                onFilterSelected = viewModel::onFilterSelected,
                allCount = uiState.totalCount,
                pendingCount = uiState.totalCount - uiState.completedCount,
                completedCount = uiState.completedCount
            )

            // Tasks List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.filteredTasks.isEmpty()) {
                    EmptyState(
                        filter = uiState.currentFilter,
                        totalCount = uiState.totalCount
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("task_list")
                    ) {
                        items(
                            items = uiState.filteredTasks,
                            key = { it.id }
                        ) { task ->
                            TaskRowItem(
                                task = task,
                                onToggle = { viewModel.onToggleTask(task) },
                                onDelete = { viewModel.onDeleteTask(task) },
                                modifier = Modifier.animateItem()
                            )
                        }
                        // Bottom spacing so list items are not hidden behind input box
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // Input Bar docked at bottom
            TaskInputBar(
                text = uiState.inputText,
                onTextChanged = viewModel::onInputTextChanged,
                onAddTask = viewModel::onAddTask
            )
        }
    }

    // Dialog for clearing completed tasks
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = DarkSurfaceElevated,
            title = {
                Text(
                    text = stringResource(R.string.clear_completed_dialog_title),
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.clear_completed_dialog_message),
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onClearCompleted()
                        showClearDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = AccentDelete,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = TextMuted
                    )
                }
            }
        )
    }
}

@Composable
private fun HeaderSection(
    uiState: TaskUiState,
    onClearCompleted: () -> Unit
) {
    val dateFormat = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    }
    val currentDate = remember {
        val raw = dateFormat.format(Date())
        raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentDate.uppercase(),
                    color = AccentMint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    color = TextWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }

            if (uiState.completedCount > 0) {
                IconButton(
                    onClick = onClearCompleted,
                    modifier = Modifier
                        .testTag("clear_completed_button")
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkSurfaceBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = stringResource(R.string.clear_completed_action),
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress & Exact Counter Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("counter_card"),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Exact counter text requested: "Tareas de hoy: X completadas de Y totales"
                    Text(
                        text = stringResource(
                            R.string.today_tasks_counter,
                            uiState.completedCount,
                            uiState.totalCount
                        ),
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("task_counter_text")
                    )

                    // Percentage badge
                    val percent = if (uiState.totalCount > 0) {
                        ((uiState.completedCount.toFloat() / uiState.totalCount) * 100).toInt()
                    } else 0

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (percent == 100 && uiState.totalCount > 0) AccentMintContainer else DarkSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$percent%",
                            color = if (percent == 100 && uiState.totalCount > 0) AccentMint else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Smooth animated progress bar
                val animatedProgress by animateFloatAsState(
                    targetValue = uiState.progress,
                    animationSpec = tween(durationMillis = 400),
                    label = "progress_animation"
                )

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AccentMint,
                    trackColor = DarkSurfaceElevated,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    currentFilter: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit,
    allCount: Int,
    pendingCount: Int,
    completedCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterPill(
            label = stringResource(R.string.filter_all),
            count = allCount,
            isSelected = currentFilter == TaskFilter.ALL,
            onClick = { onFilterSelected(TaskFilter.ALL) },
            testTag = "filter_all"
        )
        FilterPill(
            label = stringResource(R.string.filter_pending),
            count = pendingCount,
            isSelected = currentFilter == TaskFilter.PENDING,
            onClick = { onFilterSelected(TaskFilter.PENDING) },
            testTag = "filter_pending"
        )
        FilterPill(
            label = stringResource(R.string.filter_completed),
            count = completedCount,
            isSelected = currentFilter == TaskFilter.COMPLETED,
            onClick = { onFilterSelected(TaskFilter.COMPLETED) },
            testTag = "filter_completed"
        )
    }
}

@Composable
private fun FilterPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) AccentMint else DarkSurface,
        label = "pill_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) OnAccentMint else TextMuted,
        label = "pill_text"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AccentMint else DarkSurfaceBorder,
        label = "pill_border"
    )

    Surface(
        modifier = Modifier
            .testTag(testTag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (count > 0) {
                Text(
                    text = "($count)",
                    color = if (isSelected) OnAccentMint.copy(alpha = 0.8f) else TextDone,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun TaskRowItem(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemBorderColor by animateColorAsState(
        targetValue = if (task.isCompleted) DarkSurfaceBorder.copy(alpha = 0.5f) else DarkSurfaceBorder,
        label = "card_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (task.isCompleted) TextDone else TextWhite,
        label = "task_text_color"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}")
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        color = if (task.isCompleted) DarkSurface.copy(alpha = 0.6f) else DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, itemBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox to check/cross out task
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier
                    .testTag("task_checkbox_${task.id}")
                    .padding(end = 4.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = AccentMint,
                    uncheckedColor = TextMuted,
                    checkmarkColor = OnAccentMint
                )
            )

            // Task Description Text
            Text(
                text = task.text,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .testTag("task_text_${task.id}")
            )

            // Trash Icon to delete task
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("delete_task_button_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete_task_button_desc),
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    filter: TaskFilter,
    totalCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(DarkSurfaceElevated)
                .border(1.dp, DarkSurfaceBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (filter) {
                    TaskFilter.COMPLETED -> Icons.Outlined.CheckCircle
                    else -> Icons.Outlined.FormatListBulleted
                },
                contentDescription = null,
                tint = if (filter == TaskFilter.COMPLETED && totalCount > 0) AccentMint else TextMuted,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val title = when (filter) {
            TaskFilter.ALL -> stringResource(R.string.empty_tasks_title)
            TaskFilter.PENDING -> "¡Todo al día!"
            TaskFilter.COMPLETED -> "Sin tareas completadas"
        }

        val subtitle = when (filter) {
            TaskFilter.ALL -> stringResource(R.string.empty_tasks_subtitle)
            TaskFilter.PENDING -> "No tienes tareas pendientes por realizar hoy."
            TaskFilter.COMPLETED -> "Marca una tarea con la casilla para verla aquí."
        }

        Text(
            text = title,
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun TaskInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onAddTask: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        color = DarkBackground,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = text,
                        onValueChange = onTextChanged,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.task_input_placeholder),
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            cursorColor = AccentMint,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onAddTask() }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("task_input_field")
                    )

                    // Add Button with '+' icon
                    val canAdd = text.isNotBlank()
                    val buttonBg by animateColorAsState(
                        targetValue = if (canAdd) AccentMint else DarkSurfaceBorder,
                        label = "button_bg"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (canAdd) OnAccentMint else TextMuted,
                        label = "button_tint"
                    )

                    IconButton(
                        onClick = onAddTask,
                        enabled = canAdd,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(buttonBg)
                            .testTag("add_task_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_task_button_desc),
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
