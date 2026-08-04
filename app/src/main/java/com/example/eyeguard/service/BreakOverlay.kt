package com.example.eyeguard.service

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eyeguard.R

private const val TAG = "EyeGuardOverlay"

sealed class BreakAction {
    data object Continue : BreakAction()
    data object RemindLater : BreakAction()
}

@Composable
fun BreakOverlayContent(
    remainingSeconds: Int,
    finished: Boolean,
    onAction: (BreakAction) -> Unit,
    showRemindLater: Boolean = true
) {
    LaunchedEffect(Unit) {
        Log.d(TAG, "BreakOverlay composition launched")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1114).copy(alpha = 0.97f))
            .onGloballyPositioned { coordinates ->
                Log.d(
                    TAG,
                    "BreakOverlay laid out: size=${coordinates.size.width}x${coordinates.size.height}"
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "\uD83D\uDC41",
                fontSize = 56.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.break_header),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.break_body),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!finished) {
                Text(
                    text = remainingSeconds.toString(),
                    fontSize = 72.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.break_seconds_remaining),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Button(
                    onClick = { onAction(BreakAction.Continue) },
                    modifier = Modifier
                        .height(56.dp)
                        .widthIn(min = 220.dp)
                ) {
                    Text(stringResource(R.string.break_continue), fontSize = 16.sp)
                }
            }

            if (showRemindLater) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { onAction(BreakAction.RemindLater) },
                    modifier = Modifier
                        .height(56.dp)
                        .widthIn(min = 220.dp)
                ) {
                    Text(
                        text = stringResource(R.string.remind_later),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
