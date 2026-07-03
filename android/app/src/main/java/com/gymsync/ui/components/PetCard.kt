package com.gymsync.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gymsync.data.model.response.PetDto
import com.gymsync.ui.theme.CardShapeLarge
import com.gymsync.ui.theme.SpacingLg
import com.gymsync.ui.theme.SpacingMd
import com.gymsync.R

@Composable
fun PetCard(
    pet: PetDto,
    isResting: Boolean = false,
    waterProgress: Float? = null,
    showWaterMessage: String? = null,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null
) {
    val isWaterTime = waterProgress != null
    val petImageRes = getPetImageResource(pet.type, pet.color)

    val cardBg by animateColorAsState(
        targetValue = when {
            isWaterTime -> Color(0xFF1A3A5C)
            isResting -> Color(0xFF2A1A3A)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(400),
        label = "cardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isWaterTime -> Color(0xFF42A5F5).copy(alpha = 0.5f)
            isResting -> Color(0xFFCE93D8).copy(alpha = 0.4f)
            else -> Color.Transparent
        },
        animationSpec = tween(400),
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onTap != null) Modifier.clickable { onTap() } else Modifier
            )
            .then(
                if (borderColor != Color.Transparent) Modifier.border(1.5.dp, borderColor, CardShapeLarge)
                else Modifier
            ),
        shape = CardShapeLarge,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = petImageRes,
                    contentDescription = pet.name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(SpacingLg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                val displayType = if (pet.type == 0) "Dog" else "Cat"
                Text(
                    text = displayType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (waterProgress != null) {
                    Spacer(modifier = Modifier.height(SpacingMd))
                    LinearProgressIndicator(
                        progress = { waterProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF42A5F5),
                        trackColor = Color(0xFF42A5F5).copy(alpha = 0.15f)
                    )
                }

                if (showWaterMessage != null) {
                    Spacer(modifier = Modifier.height(SpacingMd))
                    Text(
                        text = showWaterMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF42A5F5)
                    )
                }

                if (isResting) {
                    Spacer(modifier = Modifier.height(SpacingMd))
                    Text(
                        text = if (pet.type == 0) "Pant pant... resting..." else "*purrs peacefully*",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCE93D8)
                    )
                }
            }
        }
    }
}

fun getPetImageResource(petType: Int, color: String?): Int {
    val isDog = petType == 0
    return when (color?.lowercase().orEmpty().replace(" ", "")) {
        "golden", "brown" -> if (isDog) R.drawable.pet_dog_1 else R.drawable.pet_cat_1
        "black", "white", "gray", "grey" -> if (isDog) R.drawable.pet_dog_2 else R.drawable.pet_cat_2
        "orange" -> if (isDog) R.drawable.pet_dog_2 else R.drawable.pet_cat_1
        "brownblack", "blackbrown", "tan", "blacktan", "sable" -> R.drawable.pet_dog_2
        "blackbrownwhite", "brownblackwhite", "calico" -> if (isDog) R.drawable.pet_dog_2 else R.drawable.pet_cat_2
        else -> if (isDog) R.drawable.pet_dog_1 else R.drawable.pet_cat_1
    }
}

@Composable
fun PetSelectionGrid(
    selectedType: String?,
    onTypeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingMd)
    ) {
        PetOptionCard(
            label = "Dog",
            isSelected = selectedType == "Dog",
            imageRes = R.drawable.pet_dog_1,
            onClick = { onTypeSelected("Dog") },
            modifier = Modifier.weight(1f)
        )
        PetOptionCard(
            label = "Cat",
            isSelected = selectedType == "Cat",
            imageRes = R.drawable.pet_cat_1,
            onClick = { onTypeSelected("Cat") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PetOptionCard(
    label: String,
    isSelected: Boolean,
    imageRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(200),
        label = "optionBg"
    )

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = CardShapeLarge,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingLg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageRes,
                    contentDescription = label,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(SpacingMd))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
