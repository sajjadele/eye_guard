package com.example.eyeguard.presentation.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val EyeGuardShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object AppShapes {
    val Card = RoundedCornerShape(20.dp)
    val LargeCard = RoundedCornerShape(28.dp)
    val Chip = RoundedCornerShape(12.dp)
    val Button = RoundedCornerShape(16.dp)
    val SmallChip = RoundedCornerShape(8.dp)
    val Circular = CircleShape
}
