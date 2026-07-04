package com.gymsync.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymsync.data.model.response.ExerciseDto
import com.gymsync.data.model.response.PetDto
import com.gymsync.data.model.response.WorkoutDetailDto
import com.gymsync.data.repository.GymSyncRepository
import com.gymsync.service.PetRestingEvent
import com.gymsync.service.PetWaterEvent
import com.gymsync.service.SignalRService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val workoutId: String = "",
    val workoutName: String = "Workout",
    val duration: String = "00:00",
    val caloriesBurned: Int = 0,
    val totalVolume: Int = 0,
    val exercises: List<ExerciseUiModel> = emptyList(),
    val restTimerSeconds: Int = 0,
    val partnerActivity: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val showChat: Boolean = false,
    val error: String? = null,
    val pet: PetDto? = null,
    val petResting: Boolean = false,
    val petPartnerName: String? = null,
    val petPartnerType: String? = null,
    val showWaterReminder: Boolean = false,
    val waterIntakeMl: Int = 0,
    val partnerPetWaterEvent: PetWaterEvent? = null,
    val allExercises: List<ExerciseDto> = emptyList(),
    val showAddExerciseDialog: Boolean = false
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: GymSyncRepository,
    private val signalR: SignalRService
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var elapsedSeconds = 0

    init {
        observeSignalR()
    }

    private fun observeSignalR() {
        viewModelScope.launch {
            signalR.setCompleted.collect { event ->
                val activity = _uiState.value.partnerActivity.toMutableList()
                activity.add("Partner completed a set in ${event.exerciseName}")
                _uiState.value = _uiState.value.copy(partnerActivity = activity)
            }
        }

        viewModelScope.launch {
            signalR.restTimerUpdate.collect { event ->
                if (!event.isRunning) {
                    _uiState.value = _uiState.value.copy(restTimerSeconds = 0)
                }
            }
        }

        viewModelScope.launch {
            signalR.petBringsWater.collect { event ->
                _uiState.value = _uiState.value.copy(
                    showWaterReminder = true,
                    partnerPetWaterEvent = event
                )
            }
        }

        viewModelScope.launch {
            signalR.petResting.collect { event ->
                _uiState.value = _uiState.value.copy(
                    petResting = event.isResting,
                    petPartnerName = event.petName,
                    petPartnerType = event.petType
                )
            }
        }

        viewModelScope.launch {
            signalR.petWaterDrank.collect { event ->
                val activity = _uiState.value.partnerActivity.toMutableList()
                activity.add("${event.petName} (${event.partnerId}) drank water!")
                _uiState.value = _uiState.value.copy(partnerActivity = activity)
            }
        }
    }

    fun loadWorkout(workoutId: String) {
        _uiState.value = _uiState.value.copy(workoutId = workoutId, isLoading = true)
        signalR.joinWorkoutRoom(workoutId)
        startTimer()

        viewModelScope.launch {
            repository.getWorkout(workoutId).fold(
                onSuccess = { data ->
                    val exercises = data.exercises.map { we ->
                        ExerciseUiModel(
                            id = we.id,
                            name = we.exerciseName,
                            completedSets = we.sets.count { it.isCompleted },
                            totalSets = we.sets.size,
                            sets = we.sets.map { s ->
                                SetUiModel(
                                    setNumber = s.setNumber,
                                    reps = s.reps,
                                    weight = s.weight?.toDouble(),
                                    rpe = s.rpe,
                                    isCompleted = s.isCompleted
                                )
                            }
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        workoutName = data.name,
                        totalVolume = data.totalVolume,
                        caloriesBurned = data.caloriesBurned ?: 0,
                        exercises = exercises
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }

        viewModelScope.launch {
            repository.getMyPet().fold(
                onSuccess = { pet ->
                    _uiState.value = _uiState.value.copy(pet = pet)
                },
                onFailure = { }
            )
        }

        viewModelScope.launch {
            repository.getExercises().fold(
                onSuccess = { ex ->
                    _uiState.value = _uiState.value.copy(allExercises = ex)
                },
                onFailure = { }
            )
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                elapsedSeconds++
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                _uiState.value = _uiState.value.copy(
                    duration = String.format("%02d:%02d", minutes, seconds)
                )
            }
        }
    }

    fun completeSet(workoutExerciseId: String, setNumber: Int, reps: Int?, weight: Double?, rpe: Int?) {
        viewModelScope.launch {
            val workoutId = _uiState.value.workoutId
            repository.completeSet(workoutId, workoutExerciseId, setNumber, reps, weight, rpe).fold(
                onSuccess = { response ->
                    val exercises = _uiState.value.exercises.map { ex ->
                        if (ex.id == workoutExerciseId) {
                            val updatedSets = ex.sets.map { set ->
                                if (set.setNumber == setNumber) {
                                    set.copy(isCompleted = true, reps = reps, weight = weight, rpe = rpe)
                                } else set
                            }
                            ex.copy(
                                completedSets = updatedSets.count { it.isCompleted },
                                sets = updatedSets
                            )
                        } else ex
                    }
                    val exerciseName = _uiState.value.exercises.firstOrNull { it.id == workoutExerciseId }?.name ?: ""
                    _uiState.value = _uiState.value.copy(
                        exercises = exercises,
                        totalVolume = exercises.sumOf { ex ->
                            ex.sets.filter { it.isCompleted }.sumOf {
                                (it.reps ?: 0) * (it.weight ?: 0.0).toInt()
                            }.toInt()
                        }
                    )

                    signalR.notifySetCompleted(workoutId, exerciseName, setNumber, response.isPersonalRecord)

                    if (response.isPersonalRecord) {
                        _uiState.value = _uiState.value.copy(
                            partnerActivity = _uiState.value.partnerActivity + "Personal Record! 🎉"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            )
        }
    }

    fun completeExercise(workoutExerciseId: String) {
        val exercise = _uiState.value.exercises.firstOrNull { it.id == workoutExerciseId } ?: return
        viewModelScope.launch {
            exercise.sets.filter { !it.isCompleted }.forEach { set ->
                repository.completeSet(
                    _uiState.value.workoutId, workoutExerciseId,
                    set.setNumber, set.reps ?: 10, set.weight ?: 0.0, null
                )
            }
            val exercises = _uiState.value.exercises.map { ex ->
                if (ex.id == workoutExerciseId) {
                    val updatedSets = ex.sets.map { it.copy(isCompleted = true) }
                    ex.copy(completedSets = updatedSets.size, sets = updatedSets)
                } else ex
            }
            _uiState.value = _uiState.value.copy(
                exercises = exercises,
                totalVolume = exercises.sumOf { ex ->
                    ex.sets.filter { it.isCompleted }.sumOf {
                        (it.reps ?: 0) * (it.weight ?: 0.0).toInt()
                    }.toInt()
                }
            )
        }
    }

    fun startRestTimer(seconds: Int) {
        _uiState.value = _uiState.value.copy(restTimerSeconds = seconds)
        signalR.notifyRestTimerUpdate(_uiState.value.workoutId, seconds, true)

        viewModelScope.launch {
            while (_uiState.value.restTimerSeconds > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    restTimerSeconds = _uiState.value.restTimerSeconds - 1
                )
            }
            signalR.notifyRestTimerUpdate(_uiState.value.workoutId, 0, false)
        }
    }

    fun dismissRestTimer() {
        _uiState.value = _uiState.value.copy(restTimerSeconds = 0)
        signalR.notifyRestTimerUpdate(_uiState.value.workoutId, 0, false)
    }

    fun toggleChat() {
        _uiState.value = _uiState.value.copy(showChat = !_uiState.value.showChat)
    }

    fun toggleAddExerciseDialog() {
        _uiState.value = _uiState.value.copy(
            showAddExerciseDialog = !_uiState.value.showAddExerciseDialog
        )
    }

    fun addExerciseToWorkout(exerciseId: String, defaultSets: Int, defaultReps: Int) {
        viewModelScope.launch {
            val workoutId = _uiState.value.workoutId
            val order = (_uiState.value.exercises.size + 1)
            repository.addExerciseToWorkout(workoutId, exerciseId, order, defaultSets, defaultReps).fold(
                onSuccess = { we ->
                    val newExercise = ExerciseUiModel(
                        id = we.id,
                        name = we.exerciseName,
                        completedSets = 0,
                        totalSets = we.sets.size,
                        sets = we.sets.map { s ->
                            SetUiModel(
                                setNumber = s.setNumber,
                                reps = s.reps,
                                weight = s.weight?.toDouble(),
                                rpe = s.rpe,
                                isCompleted = s.isCompleted
                            )
                        }
                    )
                    _uiState.value = _uiState.value.copy(
                        exercises = _uiState.value.exercises + newExercise,
                        showAddExerciseDialog = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            )
        }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            repository.finishWorkout(
                _uiState.value.workoutId,
                _uiState.value.caloriesBurned
            )
            signalR.leaveWorkoutRoom(_uiState.value.workoutId)
        }
    }

    fun dismissWaterReminder() {
        _uiState.value = _uiState.value.copy(showWaterReminder = false)
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            val pet = _uiState.value.pet
            if (pet != null) {
                val petTypeStr = when (pet.type) { 0 -> "Dog" 1 -> "Cat" else -> "Dog" }
                signalR.notifyPetWaterDrank(
                    _uiState.value.workoutId, pet.name, petTypeStr
                )
            }
            repository.logWater(_uiState.value.workoutId.ifEmpty { null }, amountMl).fold(
                onSuccess = { intake ->
                    _uiState.value = _uiState.value.copy(
                        showWaterReminder = false,
                        waterIntakeMl = _uiState.value.waterIntakeMl + intake.amountMl
                    )
                },
                onFailure = { }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        signalR.leaveWorkoutRoom(_uiState.value.workoutId)
    }
}
