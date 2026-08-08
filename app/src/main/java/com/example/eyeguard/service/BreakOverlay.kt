package com.example.eyeguard.service

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eyeguard.R
import com.example.eyeguard.domain.model.ContentCard
import com.example.eyeguard.presentation.components.CircularCountdown
import com.example.eyeguard.presentation.components.ContentCardView
import com.example.eyeguard.presentation.components.EyeIcon
import com.example.eyeguard.presentation.theme.BluePrimary
import com.example.eyeguard.presentation.theme.GradientDarkEnd
import com.example.eyeguard.presentation.theme.GradientDarkStart
import com.example.eyeguard.presentation.theme.GreenSecondary
import com.example.eyeguard.presentation.theme.TextPrimary
import com.example.eyeguard.presentation.theme.TextSecondary

private const val TAG = "EyeGuardOverlay"

enum class BreakPhase {
    STARTED,
    ACTIVE,
    FINISHED
}

sealed class BreakAction {
    data object Continue : BreakAction()
    data object RemindLater : BreakAction()
    data class SaveCard(val card: ContentCard) : BreakAction()
}

@Composable
fun BreakOverlayContent(
    remainingSeconds: Int,
    totalBreakDuration: Int,
    finished: Boolean,
    onAction: (BreakAction) -> Unit,
    showRemindLater: Boolean = true,
    contentCards: List<ContentCard> = emptyList(),
    savedCardIds: Set<Long> = emptySet()
) {
    LaunchedEffect(Unit) {
        Log.d(TAG, "BreakOverlay composition launched")
    }

    var breakPhase by remember { mutableStateOf(BreakPhase.STARTED) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        breakPhase = BreakPhase.ACTIVE
    }

    LaunchedEffect(finished) {
        if (finished) {
            breakPhase = BreakPhase.FINISHED
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientDarkStart, GradientDarkEnd)
                )
            )
            .onGloballyPositioned { coordinates ->
                Log.d(
                    TAG,
                    "BreakOverlay laid out: size=${coordinates.size.width}x${coordinates.size.height}"
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(pulseAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BluePrimary.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        radius = 600f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            EyeIcon(
                size = 48.dp,
                isActive = true,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (breakPhase) {
                BreakPhase.STARTED -> {
                    CircularCountdown(
                        remainingSeconds = remainingSeconds,
                        totalSeconds = totalBreakDuration,
                        size = 160.dp,
                        strokeWidth = 6.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.break_seconds_remaining),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (showRemindLater) {
                        OutlinedButton(
                            onClick = { onAction(BreakAction.RemindLater) },
                            modifier = Modifier
                                .widthIn(min = 240.dp)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.remind_later),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                BreakPhase.ACTIVE -> {
                    CircularCountdown(
                        remainingSeconds = remainingSeconds,
                        totalSeconds = totalBreakDuration,
                        size = 160.dp,
                        strokeWidth = 6.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.break_seconds_remaining),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedVisibility(
                        visible = contentCards.isNotEmpty(),
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { it / 2 }
                        ),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            contentCards.forEach { card ->
                                ContentCardView(
                                    card = card,
                                    isSaved = card.id in savedCardIds,
                                    onSave = { onAction(BreakAction.SaveCard(card)) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (showRemindLater) {
                        OutlinedButton(
                            onClick = { onAction(BreakAction.RemindLater) },
                            modifier = Modifier
                                .widthIn(min = 240.dp)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.remind_later),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                BreakPhase.FINISHED -> {
                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { onAction(BreakAction.Continue) },
                        modifier = Modifier
                            .widthIn(min = 240.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenSecondary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.break_continue),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
