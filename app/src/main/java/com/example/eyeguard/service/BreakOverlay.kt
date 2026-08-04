package com.example.eyeguard.service

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BreakOverlayContent(
    remainingSeconds: Int,
    finished: Boolean,
    onContinue: () -> Unit
) {
    val blockingModifier = if (!finished) {
        Modifier.pointerInput(Unit) {
            detectTapGestures {
                // Consume touches during countdown.
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1114).copy(alpha = 0.97f))
            .then(blockingModifier),
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
                text = "\u0632\u0645\u0627\u0646 \u0627\u0633\u062A\u0631\u0627\u062D\u062A \u0686\u0634\u0645",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\u0628\u0647 \u0686\u0634\u0645\u062A\u0648\u0646 \u0637\u0648\u0644 \u0628\u062F\u0647\u06CC\u062F.",
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
                    text = "\u062A\u0646\u0648\u0647 \u0628\u0627\u0642\u06CC \u0645\u0627\u0646\u062F\u0647",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .height(56.dp)
                        .widthIn(min = 220.dp)
                ) {
                    Text("\u0627\u062F\u0627\u0645\u0647 \u06A9\u0627\u0631", fontSize = 16.sp)
                }
            }
        }
    }
}
