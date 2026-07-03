package com.gymsync.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gymsync.data.model.response.HomeDataResponse
import com.gymsync.data.model.response.UserProfile
import com.gymsync.ui.components.PetCard
import com.gymsync.ui.components.PetChatDialog
import com.gymsync.ui.components.StatsDashboardCard
import com.gymsync.ui.theme.CardShapeLarge
import com.gymsync.ui.theme.SpacingLg
import com.gymsync.ui.theme.SpacingMd
import com.gymsync.ui.theme.SpacingSm
import com.gymsync.ui.theme.SpacingXl


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartWorkout: (String?) -> Unit,
    onChat: () -> Unit,
    onProgress: () -> Unit,
    onSettings: () -> Unit,
    onAdmin: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    var showPetChat by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TRAY",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    if (uiState.isAdmin) {
                        IconButton(onClick = onAdmin) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = "Admin")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val data = uiState.homeData
            val myPet = uiState.pet

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(SpacingLg)
            ) {
                item {
                    GreetingSection(data?.myProfile?.displayName ?: "")
                }

                item {
                    PartnerStatusCards(
                        myProfile = data?.myProfile,
                        partnerProfile = data?.partnerProfile,
                        onChat = onChat
                    )
                }

                if (myPet != null) {
                    item {
                        PetCard(
                            pet = myPet,
                            onTap = { showPetChat = true }
                        )
                    }
                }

                item {
                    StepsCard(steps = uiState.steps)
                }

                item {
                    StatsDashboardCard(
                        streak = data?.currentStreak ?: 0,
                        caloriesToday = data?.caloriesToday ?: 0,
                        durationToday = data?.workoutDurationToday ?: "0m",
                        hasWorkoutToday = data?.hasWorkoutToday ?: false
                    )
                }

                item {
                    QuickActions(
                        onStartWorkout = { onStartWorkout(null) },
                        onChat = onChat,
                        onProgress = onProgress
                    )
                }

                if (data?.currentChallenge != null) {
                    item {
                        ChallengeCard(challenge = data.currentChallenge)
                    }
                }

                if (data?.unreadMessages != null && data.unreadMessages > 0) {
                    item {
                        UnreadMessagesCard(
                            count = data.unreadMessages,
                            onClick = onChat
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(SpacingXl)) }
            }
        }
    }

    if (showPetChat && uiState.pet != null) {
        PetChatDialog(
            pet = uiState.pet!!,
            userName = uiState.userDisplayName.ifEmpty { null },
            context = null,
            onDismiss = { showPetChat = false },
            onSendMessage = { msg, type, name, user, ctx, success, err ->
                viewModel.sendPetChat(msg, type, name, user, ctx, success, err)
            }
        )
    }
}

@Composable
fun GreetingSection(displayName: String) {
    val greeting = when {
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) < 12 -> "Good morning"
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    Column {
        Text(
            text = greeting,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (displayName.isNotEmpty()) displayName else "Let's go!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun StepsCard(steps: com.gymsync.data.model.response.DailyStepLogDto?) {
    val currentSteps = steps?.steps ?: 0
    val target = steps?.target ?: 10000
    val percent = if (target > 0) (currentSteps.toFloat() / target).coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShapeLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingXl),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(percent * 100).toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(SpacingLg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$currentSteps steps",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "of $target daily goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(SpacingSm))
                LinearProgressIndicator(
                    progress = { percent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Filled.DirectionsWalk,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun PartnerStatusCards(
    myProfile: UserProfile?,
    partnerProfile: UserProfile?,
    onChat: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileCard(
                name = myProfile?.displayName ?: "You",
                status = myProfile?.status ?: "Offline",
                photoUrl = myProfile?.profilePhotoUrl,
                isPartner = false
            )

            Icon(
                imageVector = Icons.Filled.SwapHoriz,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            ProfileCard(
                name = partnerProfile?.displayName ?: "Partner",
                status = partnerProfile?.status ?: "Offline",
                photoUrl = partnerProfile?.profilePhotoUrl,
                isPartner = true,
                onClick = onChat
            )
        }
    }
}

@Composable
fun ProfileCard(
    name: String,
    status: String,
    photoUrl: String?,
    isPartner: Boolean,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Box {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = name,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(2).uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .offset(x = 52.dp, y = 52.dp)
                    .clip(CircleShape)
                    .background(statusColor(status))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = statusDisplayName(status),
            fontSize = 12.sp,
            color = statusColor(status)
        )
    }
}

@Composable
fun QuickActions(
    onStartWorkout: () -> Unit,
    onChat: () -> Unit,
    onProgress: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            icon = Icons.Filled.FitnessCenter,
            label = "Workout",
            modifier = Modifier.weight(1f),
            onClick = onStartWorkout
        )
        ActionButton(
            icon = Icons.Filled.Chat,
            label = "Partner",
            modifier = Modifier.weight(1f),
            onClick = onChat
        )
        ActionButton(
            icon = Icons.Filled.BarChart,
            label = "Progress",
            modifier = Modifier.weight(1f),
            onClick = onProgress
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChallengeCard(challenge: com.gymsync.data.model.response.ChallengeInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = challenge.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Goal: ${challenge.goal}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${challenge.myProgress.toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "My progress",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${challenge.partnerProgress.toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Partner",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun UnreadMessagesCard(count: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Chat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$count unread message${if (count != 1) "s" else ""}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

private fun statusColor(status: String): Color = when (status) {
    "Online" -> Color(0xFF4CAF50)
    "Offline" -> Color(0xFF757575)
    "Sleeping" -> Color(0xFF2196F3)
    "WorkingOut" -> Color(0xFFFF5722)
    "Resting" -> Color(0xFFFF9800)
    "Typing" -> Color(0xFF03A9F4)
    "InChat" -> Color(0xFF9C27B0)
    "FinishedWorkout" -> Color(0xFF4CAF50)
    else -> Color(0xFF757575)
}

private fun statusDisplayName(status: String): String = when (status) {
    "Online" -> "Online"
    "Offline" -> "Offline"
    "Sleeping" -> "Sleeping"
    "WorkingOut" -> "Working Out"
    "Resting" -> "Resting"
    "Typing" -> "Typing..."
    "InChat" -> "In Chat"
    "FinishedWorkout" -> "Finished!"
    else -> status
}
