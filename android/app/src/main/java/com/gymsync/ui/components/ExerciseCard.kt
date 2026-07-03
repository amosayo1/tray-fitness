package com.gymsync.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymsync.ui.theme.CardShapeSmall
import com.gymsync.ui.theme.SpacingMd
import com.gymsync.ui.theme.SpacingSm

data class ExerciseDisplayData(
    val name: String,
    val muscleGroup: String,
    val isBodyweight: Boolean,
    val sets: Int,
    val reps: Int,
    val weight: Double?,
    val completedSets: Int
)

@Composable
fun ExerciseDisplayCard(
    exercise: ExerciseDisplayData,
    modifier: Modifier = Modifier
) {
    val muscleGroupColor = muscleGroupColor(exercise.muscleGroup)
    val completedFraction = if (exercise.sets > 0)
        exercise.completedSets.toFloat() / exercise.sets else 0f
    val isComplete = exercise.completedSets >= exercise.sets

    val cardBg by animateColorAsState(
        targetValue = if (isComplete)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "cardBg"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShapeSmall,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(muscleGroupColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (exercise.isBodyweight)
                            Icons.Filled.SelfImprovement
                        else Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = muscleGroupColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(SpacingMd))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MuscleGroupBadge(group = exercise.muscleGroup)
                        if (exercise.isBodyweight) {
                            BodyweightBadge()
                        }
                    }
                }

                if (isComplete) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Complete",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(SpacingMd))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SetInfoItem("Sets", "${exercise.completedSets}/${exercise.sets}")
                SetInfoItem("Reps", "${exercise.reps}")
                if (exercise.weight != null && exercise.weight > 0) {
                    SetInfoItem("Weight", "${exercise.weight.toInt()} kg")
                } else if (!exercise.isBodyweight) {
                    SetInfoItem("Weight", "-")
                } else {
                    SetInfoItem("Weight", "BW")
                }
            }

            if (!isComplete) {
                Spacer(modifier = Modifier.height(SpacingSm))
                val progressAlpha by animateFloatAsState(
                    targetValue = completedFraction,
                    animationSpec = tween(300),
                    label = "progressAlpha"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CardShapeSmall)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressAlpha)
                            .height(4.dp)
                            .clip(CardShapeSmall)
                            .background(muscleGroupColor)
                    )
                }
            }
        }
    }
}

@Composable
fun MuscleGroupBadge(group: String) {
    val color = muscleGroupColor(group)
    androidx.compose.material3.Surface(
        shape = com.gymsync.ui.theme.ChipShape,
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = group,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun BodyweightBadge() {
    androidx.compose.material3.Surface(
        shape = com.gymsync.ui.theme.ChipShape,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
    ) {
        Text(
            text = "BW",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun SetInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun muscleGroupColor(group: String): Color = when (group.lowercase()) {
    "chest" -> Color(0xFFFF7043)
    "back" -> Color(0xFF5C6BC0)
    "shoulders", "shoulder" -> Color(0xFFAB47BC)
    "legs", "leg" -> Color(0xFF66BB6A)
    "arms", "arm" -> Color(0xFF42A5F5)
    "core" -> Color(0xFFFFCA28)
    "cardio" -> Color(0xFFEF5350)
    "full body" -> Color(0xFF26C6DA)
    else -> Color(0xFF69F0AE)
}
