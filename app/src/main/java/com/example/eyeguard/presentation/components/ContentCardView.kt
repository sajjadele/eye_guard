package com.example.eyeguard.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eyeguard.R
import com.example.eyeguard.domain.model.ContentCard
import com.example.eyeguard.domain.model.ContentCategory
import com.example.eyeguard.presentation.theme.BluePrimary
import com.example.eyeguard.presentation.theme.GlassSurface
import com.example.eyeguard.presentation.theme.GreenSecondary
import com.example.eyeguard.presentation.theme.StatusActive
import com.example.eyeguard.presentation.theme.TextPrimary
import com.example.eyeguard.presentation.theme.TextSecondary
import com.example.eyeguard.presentation.theme.TextTertiary

@Composable
fun ContentCardView(
    card: ContentCard,
    modifier: Modifier = Modifier,
    isSaved: Boolean = false,
    onSave: () -> Unit = {},
    surfaceColor: Color = GlassSurface
) {
    EyeCareCard(
        card = card,
        modifier = modifier,
        surfaceColor = surfaceColor
    )
}

@Composable
private fun EyeCareCard(
    card: ContentCard,
    modifier: Modifier = Modifier,
    surfaceColor: Color = GlassSurface
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        surfaceColor = surfaceColor
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "👁", fontSize = 18.sp)
                Text(
                    text = card.title.ifEmpty { stringResource(R.string.eye_care_tip) },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = card.content,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Normal
            )

            card.example?.let { example ->
                Text(
                    text = example,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
