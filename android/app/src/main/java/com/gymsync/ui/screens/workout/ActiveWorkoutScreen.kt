package com.gymsync.ui.screens.workout

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import com.gymsync.ui.components.CircularRestTimer
import com.gymsync.ui.components.PetCard
import com.gymsync.ui.components.WaterReminderCard
import com.gymsync.ui.components.WaterIntakeTracker
import com.gymsync.data.model.response.ExerciseDto
import com.gymsync.util.WaterReminderVoice
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    workoutId: String,
    onFinish: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(workoutId) {
        viewModel.loadWorkout(workoutId)
    }

    val context = LocalContext.current
    LaunchedEffect(uiState.showWaterReminder) {
        if (uiState.showWaterReminder) {
            WaterReminderVoice.init(context)
            uiState.pet?.let { pet ->
                WaterReminderVoice.speakWaterReminder(pet.name, pet.type)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            WaterReminderVoice.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.workoutName,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleChat() }) {
                        Icon(Icons.Filled.Chat, contentDescription = "Chat")
                    }
                    IconButton(onClick = {
                        viewModel.finishWorkout()
                        onFinish()
                    }) {
                        Icon(Icons.Filled.Stop, contentDescription = "Finish")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Workout stats bar
            WorkoutStatsBar(
                duration = uiState.duration,
                calories = uiState.caloriesBurned,
                totalVolume = uiState.totalVolume
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pet card (if user has a pet)
            val myPet = uiState.pet
            if (myPet != null) {
                PetCard(
                    pet = myPet,
                    isResting = uiState.petResting,
                    waterProgress = if (uiState.showWaterReminder) 0.5f else null
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Partner's pet activity
            val partnerPetName = uiState.petPartnerName
            val partnerPetTypeStr = uiState.petPartnerType
            if (partnerPetName != null && partnerPetTypeStr != null) {
                val partnerTypeInt = when (partnerPetTypeStr.lowercase()) {
                    "dog" -> 0
                    "cat" -> 1
                    else -> 0
                }
                PetCard(
                    pet = com.gymsync.data.model.response.PetDto(
                        id = "",
                        name = partnerPetName,
                        type = partnerTypeInt,
                        color = null
                    ),
                    isResting = uiState.petResting
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Water intake tracker
            WaterIntakeTracker(
                currentIntakeMl = uiState.waterIntakeMl
            )

            // Water reminder overlay
            Box(modifier = Modifier.fillMaxWidth()) {
                WaterReminderCard(
                    isVisible = uiState.showWaterReminder,
                    onDismiss = { viewModel.dismissWaterReminder() },
                    onLogWater = { viewModel.logWater(it) },
                    petName = uiState.pet?.name
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Exercises list
            val listState = rememberLazyListState()
            val scrollScope = rememberCoroutineScope()
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(uiState.exercises, key = { _, e -> e.id }) { index, exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onCompleteSet = { setNumber, reps, weight, rpe ->
                            viewModel.completeSet(exercise.id, setNumber, reps, weight, rpe)
                        },
                        onStartRest = { seconds ->
                            viewModel.startRestTimer(seconds)
                        },
                        onCompleteExercise = {
                            viewModel.completeExercise(exercise.id)
                            if (index + 1 < uiState.exercises.size) {
                                scrollScope.launch {
                                    listState.animateScrollToItem(index + 1)
                                }
                            }
                        }
                    )
                }
            }

            // Rest timer overlay
            AnimatedVisibility(visible = uiState.restTimerSeconds > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CircularRestTimer(
                            secondsRemaining = uiState.restTimerSeconds,
                            totalSeconds = 90
                        )
                        Column {
                            Text(
                                text = "Rest Period",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Take a breather",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        TextButton(onClick = { viewModel.dismissRestTimer() }) {
                            Text("Skip")
                        }
                    }
                }
            }

            // Partner Activity Feed
            if (uiState.partnerActivity.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PartnerActivityFeed(activities = uiState.partnerActivity)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add exercise button
            OutlinedButton(
                onClick = { viewModel.toggleAddExerciseDialog() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Exercise")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (uiState.showAddExerciseDialog) {
        AddExerciseDialog(
            exercises = uiState.allExercises,
            onDismiss = { viewModel.toggleAddExerciseDialog() },
            onAdd = { exerciseId, sets, reps ->
                viewModel.addExerciseToWorkout(exerciseId, sets, reps)
            }
        )
    }
}

@Composable
fun WorkoutStatsBar(duration: String, calories: Int, totalVolume: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WorkoutStatItem(
                    icon = Icons.Filled.Timer,
                    value = duration,
                    label = "Duration",
                    color = MaterialTheme.colorScheme.primary
                )
                WorkoutStatItem(
                    icon = Icons.Filled.LocalFireDepartment,
                    value = "$calories",
                    label = "Calories",
                    color = Color(0xFFFF6D00)
                )
                WorkoutStatItem(
                    icon = Icons.Filled.FitnessCenter,
                    value = "${totalVolume}kg",
                    label = "Volume",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun WorkoutStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ExerciseCard(
    exercise: ExerciseUiModel,
    onCompleteSet: (Int, Int?, Double?, Int?) -> Unit,
    onStartRest: (Int) -> Unit,
    onCompleteExercise: () -> Unit = {}
) {
    val exerciseName = exercise.name
    val completedFraction = if (exercise.totalSets > 0)
        exercise.completedSets.toFloat() / exercise.totalSets else 0f
    val isComplete = exercise.completedSets >= exercise.totalSets

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = exerciseName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${exercise.completedSets}/${exercise.totalSets} sets",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            exercise.sets.forEach { set ->
                SetRow(
                    set = set,
                    onComplete = { reps, weight, rpe ->
                        onCompleteSet(set.setNumber, reps, weight, rpe)
                    },
                    onStartRest = { onStartRest(90) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!isComplete) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onCompleteExercise,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Exercise & Next")
                }
            }
        }
    }
}

@Composable
fun SetRow(
    set: SetUiModel,
    onComplete: (Int?, Double?, Int?) -> Unit,
    onStartRest: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var reps by remember { mutableStateOf(set.reps?.toString() ?: "") }
    var weight by remember { mutableStateOf(set.weight?.toString() ?: "") }
    var rpe by remember { mutableStateOf(set.rpe?.toString() ?: "") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${set.setNumber}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it },
            label = { Text("Reps", fontSize = 11.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 14.sp)
        )

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight", fontSize = 11.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 14.sp)
        )

        if (set.isCompleted) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        } else {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onComplete(
                        reps.toIntOrNull(),
                        weight.toDoubleOrNull(),
                        rpe.toIntOrNull()
                    )
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Filled.RadioButtonUnchecked,
                    contentDescription = "Complete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (set.isCompleted) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onStartRest,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    Icons.Filled.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rest 90s", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RestTimerBanner(seconds: Int, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rest: ${seconds / 60}:${String.format("%02d", seconds % 60)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    }
}

@Composable
fun PartnerActivityFeed(activities: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Partner Activity",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            activities.takeLast(3).forEach { activity ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activity,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

data class ExerciseUiModel(
    val id: String,
    val name: String,
    val completedSets: Int,
    val totalSets: Int,
    val sets: List<SetUiModel>
)

data class SetUiModel(
    val setNumber: Int,
    val reps: Int?,
    val weight: Double?,
    val rpe: Int?,
    val isCompleted: Boolean
)

@Composable
fun AddExerciseDialog(
    exercises: List<ExerciseDto>,
    onDismiss: () -> Unit,
    onAdd: (exerciseId: String, sets: Int, reps: Int) -> Unit
) {
    var selectedExerciseId by remember { mutableStateOf<String?>(null) }
    var setsCount by remember { mutableStateOf("3") }
    var repsCount by remember { mutableStateOf("10") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredExercises = if (searchQuery.isBlank()) exercises
    else exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exercise") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(filteredExercises) { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedExerciseId = exercise.id },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedExerciseId == exercise.id,
                                onClick = { selectedExerciseId = exercise.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(exercise.name, fontWeight = FontWeight.Medium)
                                Text(
                                    exercise.muscleGroup,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = setsCount,
                        onValueChange = { setsCount = it },
                        label = { Text("Sets") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = repsCount,
                        onValueChange = { repsCount = it },
                        label = { Text("Reps") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedExerciseId?.let { id ->
                        onAdd(id, setsCount.toIntOrNull() ?: 3, repsCount.toIntOrNull() ?: 10)
                    }
                },
                enabled = selectedExerciseId != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
