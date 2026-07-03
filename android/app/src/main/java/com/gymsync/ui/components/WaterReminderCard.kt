package com.gymsync.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun WaterReminderCard(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onLogWater: (Int) -> Unit,
    petName: String? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Water,
                    contentDescription = null,
                    tint = Color(0xFF1565C0),
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (petName != null) "$petName says: Time to hydrate!" else "Time to hydrate!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0D47A1)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Drink 250ml of water",
                    fontSize = 14.sp,
                    color = Color(0xFF1565C0)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onLogWater(250) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("250ml 💧")
                    }

                    Button(
                        onClick = { onLogWater(500) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0)
                        )
                    ) {
                        Text("500ml 💧💧")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Dismiss", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun WaterIntakeTracker(
    currentIntakeMl: Int,
    dailyGoalMl: Int = 2000,
    modifier: Modifier = Modifier
) {
    val progress = (currentIntakeMl.toFloat() / dailyGoalMl).coerceAtMost(1f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Water,
                contentDescription = null,
                tint = Color(0xFF42A5F5),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Water Intake",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = Color(0xFF42A5F5),
                    trackColor = Color(0xFFBBDEFB)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${currentIntakeMl}ml",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
        }
    }
}
