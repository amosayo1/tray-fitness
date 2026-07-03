package com.gymsync.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class PetType { DOG, CAT }

data class PetVisuals(
    val bodyColor: Color,
    val bodyShade: Color,
    val earColor: Color,
    val earInner: Color,
    val noseColor: Color,
    val tongueColor: Color,
    val eyeColor: Color,
    val accentColor: Color
)

fun getPetColor(colorName: String?): Color = when (colorName?.lowercase()) {
    "golden" -> Color(0xFFD4A843)
    "brown" -> Color(0xFF6D4C2C)
    "black" -> Color(0xFF3A3A3A)
    "white" -> Color(0xFFE8E0D8)
    "orange" -> Color(0xFFE07B3A)
    "gray", "grey" -> Color(0xFF8E8E8E)
    else -> Color(0xFFD4A843)
}

fun getPetVisuals(bodyColor: Color): PetVisuals {
    val r = bodyColor.red
    val g = bodyColor.green
    val b = bodyColor.blue
    return PetVisuals(
        bodyColor = bodyColor,
        bodyShade = Color(
            (r * 0.7f).coerceIn(0f, 1f),
            (g * 0.7f).coerceIn(0f, 1f),
            (b * 0.7f).coerceIn(0f, 1f),
            1f
        ),
        earColor = Color(
            (r * 0.85f).coerceIn(0f, 1f),
            (g * 0.85f).coerceIn(0f, 1f),
            (b * 0.85f).coerceIn(0f, 1f),
            1f
        ),
        earInner = Color(
            (r * 0.5f + 0.3f).coerceIn(0f, 1f),
            (g * 0.3f + 0.2f).coerceIn(0f, 1f),
            (b * 0.2f + 0.2f).coerceIn(0f, 1f),
            1f
        ),
        noseColor = Color(0xFF2D2D2D),
        tongueColor = Color(0xFFE57373),
        eyeColor = Color(0xFF1A1A1A),
        accentColor = Color(
            (r * 0.3f + 0.5f).coerceIn(0f, 1f),
            (g * 0.3f + 0.4f).coerceIn(0f, 1f),
            (b * 0.3f + 0.3f).coerceIn(0f, 1f),
            1f
        )
    )
}

@Composable
fun AnimatedPet(
    petType: PetType,
    bodyColor: Color,
    isWaterTime: Boolean = false,
    isResting: Boolean = false,
    modifier: Modifier = Modifier
) {
    val visuals = getPetVisuals(bodyColor)
    val transition = rememberInfiniteTransition(label = "pet")

    val bounceOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (isResting) 2f else 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isResting) 1200 else 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val tailAngle by transition.animateFloat(
        initialValue = -20f,
        targetValue = if (isResting) 0f else 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isResting) 2000 else 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tailAngle"
    )

    val breathScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isResting) 1.06f else if (isWaterTime) 1.08f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isResting) 2800 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    Canvas(modifier = modifier.size(120.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f + 10f + bounceOffset
        val scale = breathScale

        drawShadow(cx, cy, scale)

        when (petType) {
            PetType.DOG -> drawDog(cx, cy, scale, tailAngle, visuals, isWaterTime, isResting)
            PetType.CAT -> drawCat(cx, cy, scale, tailAngle, visuals, isWaterTime, isResting)
        }
    }
}

private fun DrawScope.drawShadow(cx: Float, cy: Float, scale: Float) {
    val shadowWidth = 50f * scale
    val shadowHeight = 8f
    val shadowY = cy + 55f * scale

    drawOval(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = Offset(cx - shadowWidth / 2f, shadowY - shadowHeight / 2f),
        size = androidx.compose.ui.geometry.Size(shadowWidth, shadowHeight)
    )
}

private fun DrawScope.drawDog(
    cx: Float, cy: Float, scale: Float, tailAngle: Float,
    v: PetVisuals, isWaterTime: Boolean, isResting: Boolean
) {
    val s = scale
    val tailRad = Math.toRadians(tailAngle.toDouble())

    // Tail
    val tailStartX = cx - 30f * s
    val tailStartY = cy - 5f * s
    val tailEndX = tailStartX - (28f * s) * cos(tailRad).toFloat()
    val tailEndY = tailStartY - (28f * s) * sin(tailRad).toFloat() - 10f * s

    val tailPath = Path().apply {
        moveTo(tailStartX, tailStartY)
        cubicTo(
            tailStartX - 10f * s, tailStartY - 5f * s,
            tailEndX + 5f * s, tailEndY + 8f * s,
            tailEndX, tailEndY
        )
    }
    drawPath(
        path = tailPath,
        color = v.bodyColor,
        style = Stroke(width = 10f * s, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )

    // Body
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(v.bodyColor, v.bodyShade),
            center = Offset(cx - 5f * s, cy),
            radius = 50f * s
        ),
        topLeft = Offset(cx - 42f * s, cy - 28f * s),
        size = androidx.compose.ui.geometry.Size(60f * s, 50f * s)
    )

    // Back legs
    drawRoundRect(
        color = v.bodyShade,
        topLeft = Offset(cx - 35f * s, cy + 12f * s),
        size = androidx.compose.ui.geometry.Size(12f * s, 20f * s),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * s)
    )
    // Front legs
    drawRoundRect(
        color = v.bodyColor,
        topLeft = Offset(cx - 10f * s, cy + 12f * s),
        size = androidx.compose.ui.geometry.Size(12f * s, 20f * s),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * s)
    )

    // Paws
    drawCircle(color = v.bodyShade, radius = 7f * s, center = Offset(cx - 29f * s, cy + 32f * s))
    drawCircle(color = v.bodyColor, radius = 7f * s, center = Offset(cx - 4f * s, cy + 32f * s))

    // Head
    val headCx = cx + 24f * s
    val headCy = cy - 20f * s
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(v.bodyColor, v.bodyShade),
            center = Offset(headCx - 2f * s, headCy - 2f * s),
            radius = 28f * s
        ),
        radius = 28f * s,
        center = Offset(headCx, headCy)
    )

    // Snout
    drawOval(
        color = v.bodyColor,
        topLeft = Offset(headCx + 8f * s, headCy - 5f * s),
        size = androidx.compose.ui.geometry.Size(22f * s, 18f * s)
    )

    // Ears
    // Left ear (floppy)
    val earPath = Path().apply {
        moveTo(headCx - 22f * s, headCy - 20f * s)
        cubicTo(
            headCx - 28f * s, headCy - 38f * s,
            headCx - 14f * s, headCy - 40f * s,
            headCx - 16f * s, headCy - 22f * s
        )
        close()
    }
    drawPath(path = earPath, color = v.earColor)
    drawPath(
        path = earPath, color = v.earInner,
        style = Stroke(width = 2f * s)
    )

    // Right ear
    val earPath2 = Path().apply {
        moveTo(headCx - 8f * s, headCy - 22f * s)
        cubicTo(
            headCx - 4f * s, headCy - 42f * s,
            headCx + 10f * s, headCy - 38f * s,
            headCx + 4f * s, headCy - 18f * s
        )
        close()
    }
    drawPath(path = earPath2, color = v.earColor)
    drawPath(
        path = earPath2, color = v.earInner,
        style = Stroke(width = 2f * s)
    )

    // Eyes
    drawCircle(color = Color.White, radius = 8f * s, center = Offset(headCx - 2f * s, headCy - 8f * s))
    drawCircle(color = v.eyeColor, radius = 5f * s, center = Offset(headCx - 1f * s, headCy - 8f * s))
    drawCircle(color = Color.White, radius = 2f * s, center = Offset(headCx, headCy - 10f * s))

    // Nose
    drawOval(
        color = v.noseColor,
        topLeft = Offset(headCx + 18f * s, headCy - 4f * s),
        size = androidx.compose.ui.geometry.Size(8f * s, 6f * s)
    )

    // Mouth
    val mouthPath = Path().apply {
        moveTo(headCx + 22f * s, headCy + 2f * s)
        quadraticTo(headCx + 20f * s, headCy + 8f * s, headCx + 15f * s, headCy + 6f * s)
    }
    drawPath(path = mouthPath, color = v.noseColor, style = Stroke(width = 1.5f * s))

    // Tongue
    if (!isResting || isWaterTime) {
        val tonguePath = Path().apply {
            moveTo(headCx + 18f * s, headCy + 6f * s)
            cubicTo(
                headCx + 16f * s, headCy + 14f * s,
                headCx + 22f * s, headCy + 16f * s,
                headCx + 20f * s, headCy + 6f * s
            )
        }
        drawPath(path = tonguePath, color = v.tongueColor, style = Fill)
    }

    // Water drops
    if (isWaterTime) {
        val dropAlpha = ((sin(System.currentTimeMillis().toDouble() / 300) + 1) / 2).toFloat()
        drawCircle(
            color = Color(0xFF42A5F5).copy(alpha = dropAlpha * 0.5f),
            radius = 4f * s,
            center = Offset(headCx + 12f * s, headCy + 18f * s)
        )
        drawCircle(
            color = Color(0xFF42A5F5).copy(alpha = (1f - dropAlpha) * 0.5f),
            radius = 3f * s,
            center = Offset(headCx + 18f * s, headCy + 20f * s)
        )
    }

    // Zzz when resting
    if (isResting) {
        val zAlpha = ((sin(System.currentTimeMillis().toDouble() / 400) + 1) / 2).toFloat()
        drawTextZ(cx + 15f * s, cy - 50f * s, zAlpha, s)
        drawTextZ(cx + 25f * s, cy - 62f * s, (1f - zAlpha) * 0.7f, s * 1.2f)
        drawTextZ(cx + 8f * s, cy - 40f * s, zAlpha * 0.5f, s * 0.8f)
    }
}

private fun DrawScope.drawCat(
    cx: Float, cy: Float, scale: Float, tailAngle: Float,
    v: PetVisuals, isWaterTime: Boolean, isResting: Boolean
) {
    val s = scale
    val tailRad = Math.toRadians(tailAngle.toDouble())

    // Tail
    val tailStartX = cx - 28f * s
    val tailStartY = cy
    val tailEndX = tailStartX - (30f * s) * cos(tailRad).toFloat()
    val tailEndY = tailStartY - (30f * s) * sin(tailRad).toFloat() - 5f * s

    val tailPath = Path().apply {
        moveTo(tailStartX, tailStartY)
        cubicTo(
            tailStartX - 15f * s, tailStartY - 10f * s,
            tailEndX + 5f * s, tailEndY + 5f * s,
            tailEndX, tailEndY
        )
    }
    drawPath(
        path = tailPath,
        color = v.bodyColor,
        style = Stroke(width = 7f * s, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )

    // Body
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(v.bodyColor, v.bodyShade),
            center = Offset(cx - 3f * s, cy),
            radius = 45f * s
        ),
        topLeft = Offset(cx - 38f * s, cy - 22f * s),
        size = androidx.compose.ui.geometry.Size(50f * s, 44f * s)
    )

    // Legs
    drawRoundRect(
        color = v.bodyShade,
        topLeft = Offset(cx - 30f * s, cy + 10f * s),
        size = androidx.compose.ui.geometry.Size(8f * s, 16f * s),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * s)
    )
    drawRoundRect(
        color = v.bodyColor,
        topLeft = Offset(cx - 8f * s, cy + 10f * s),
        size = androidx.compose.ui.geometry.Size(8f * s, 16f * s),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * s)
    )

    // Paws
    drawCircle(color = v.bodyShade, radius = 5f * s, center = Offset(cx - 26f * s, cy + 26f * s))
    drawCircle(color = v.bodyColor, radius = 5f * s, center = Offset(cx - 4f * s, cy + 26f * s))

    // Head
    val headCx = cx + 22f * s
    val headCy = cy - 16f * s
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(v.bodyColor, v.bodyShade),
            center = Offset(headCx - 2f * s, headCy - 2f * s),
            radius = 24f * s
        ),
        radius = 24f * s,
        center = Offset(headCx, headCy)
    )

    // Ears (pointy)
    val leftEarPath = Path().apply {
        moveTo(headCx - 18f * s, headCy - 16f * s)
        lineTo(headCx - 12f * s, headCy - 38f * s)
        lineTo(headCx - 4f * s, headCy - 20f * s)
        close()
    }
    drawPath(path = leftEarPath, color = v.earColor)
    val leftEarInner = Path().apply {
        moveTo(headCx - 16f * s, headCy - 18f * s)
        lineTo(headCx - 12f * s, headCy - 32f * s)
        lineTo(headCx - 7f * s, headCy - 20f * s)
        close()
    }
    drawPath(path = leftEarInner, color = v.earInner)

    val rightEarPath = Path().apply {
        moveTo(headCx + 2f * s, headCy - 18f * s)
        lineTo(headCx + 10f * s, headCy - 40f * s)
        lineTo(headCx + 16f * s, headCy - 18f * s)
        close()
    }
    drawPath(path = rightEarPath, color = v.earColor)
    val rightEarInner = Path().apply {
        moveTo(headCx + 4f * s, headCy - 20f * s)
        lineTo(headCx + 10f * s, headCy - 34f * s)
        lineTo(headCx + 13f * s, headCy - 20f * s)
        close()
    }
    drawPath(path = rightEarInner, color = v.earInner)

    // Eyes (slit pupils)
    drawCircle(color = Color.White, radius = 7f * s, center = Offset(headCx - 2f * s, headCy - 6f * s))
    drawOval(
        color = v.eyeColor,
        topLeft = Offset(headCx - 3f * s, headCy - 10f * s),
        size = androidx.compose.ui.geometry.Size(3f * s, 8f * s)
    )
    drawCircle(color = Color.White, radius = 2f * s, center = Offset(headCx, headCy - 8f * s))

    // Nose
    val nosePath = Path().apply {
        moveTo(headCx + 12f * s, headCy - 4f * s)
        lineTo(headCx + 10f * s, headCy - 1f * s)
        lineTo(headCx + 14f * s, headCy - 1f * s)
        close()
    }
    drawPath(path = nosePath, color = v.noseColor)

    // Whiskers
    val whiskerColor = Color(0xFFB0B0B0).copy(alpha = 0.6f)
    drawLine(whiskerColor, Offset(headCx + 12f * s, headCy - 2f * s), Offset(headCx + 30f * s, headCy - 6f * s), strokeWidth = 1.5f * s)
    drawLine(whiskerColor, Offset(headCx + 12f * s, headCy), Offset(headCx + 30f * s, headCy), strokeWidth = 1.5f * s)
    drawLine(whiskerColor, Offset(headCx + 12f * s, headCy - 2f * s), Offset(headCx + 30f * s, headCy + 4f * s), strokeWidth = 1.5f * s)

    // Mouth
    val mouthPath = Path().apply {
        moveTo(headCx + 12f * s, headCy + 1f * s)
        quadraticTo(headCx + 8f * s, headCy + 6f * s, headCx + 6f * s, headCy + 2f * s)
    }
    drawPath(path = mouthPath, color = v.noseColor, style = Stroke(width = 1.5f * s))

    val mouthPath2 = Path().apply {
        moveTo(headCx + 12f * s, headCy + 1f * s)
        quadraticTo(headCx + 16f * s, headCy + 6f * s, headCx + 18f * s, headCy + 2f * s)
    }
    drawPath(path = mouthPath2, color = v.noseColor, style = Stroke(width = 1.5f * s))

    // Water
    if (isWaterTime) {
        val dropAlpha = ((sin(System.currentTimeMillis().toDouble() / 300) + 1) / 2).toFloat()
        drawCircle(
            color = Color(0xFF42A5F5).copy(alpha = dropAlpha * 0.5f),
            radius = 3f * s,
            center = Offset(headCx + 8f * s, headCy + 14f * s)
        )
    }

    // Zzz
    if (isResting) {
        val zAlpha = ((sin(System.currentTimeMillis().toDouble() / 400) + 1) / 2).toFloat()
        drawTextZ(cx + 12f * s, cy - 44f * s, zAlpha, s)
        drawTextZ(cx + 22f * s, cy - 54f * s, (1f - zAlpha) * 0.7f, s * 1.2f)
    }
}

private fun DrawScope.drawTextZ(x: Float, y: Float, alpha: Float, scale: Float) {
    val zColor = Color(0xFFCE93D8).copy(alpha = alpha.coerceIn(0f, 1f))
    val r = 4f * scale * (0.8f + alpha * 0.2f)
    drawCircle(color = zColor, radius = r, center = Offset(x, y))
    drawCircle(
        color = zColor.copy(alpha = 0.5f),
        radius = r * 0.5f,
        center = Offset(x + r * 0.8f, y - r * 0.6f)
    )
}
