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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eyeguard.R
import com.example.eyeguard.domain.model.ContentCard
import com.example.eyeguard.domain.model.ContentCategory
import com.example.eyeguard.presentation.theme.BluePrimary
import com.example.eyeguard.presentation.theme.GreenSecondary
import com.example.eyeguard.presentation.theme.StatusActive
import com.example.eyeguard.presentation.theme.TextPrimary
import com.example.eyeguard.presentation.theme.TextSecondary
import com.example.eyeguard.presentation.theme.TextTertiary

@Composable
fun ContentCardView(
    card: ContentCard,
    isSaved: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (card.category) {
        ContentCategory.EYE_CARE -> EyeCareCard(card = card, modifier = modifier)
        ContentCategory.ENGLISH_VOCABULARY -> VocabularyCard(
            card = card,
            isSaved = isSaved,
            onSave = onSave,
            modifier = modifier
        )
    }
}

@Composable
private fun EyeCareCard(
    card: ContentCard,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "👁", fontSize = 16.sp)
                Text(
                    text = "نکته سلامت چشم",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            Text(
                text = card.content,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                lineHeight = 28.sp
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

@Composable
private fun VocabularyCard(
    card: ContentCard,
    isSaved: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val saveColor by animateColorAsState(
        targetValue = if (isSaved) StatusActive else TextSecondary,
        animationSpec = tween(300),
        label = "saveColor"
    )

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🔤", fontSize = 16.sp)
                Text(
                    text = "واژه انگلیسی",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            Text(
                text = card.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = BluePrimary,
                fontSize = 32.sp
            )

            Text(
                text = card.content,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                lineHeight = 28.sp
            )

            card.translation?.let { translation ->
                Text(
                    text = translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            }

            card.example?.let { example ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "Example:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = example,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onSave) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_save),
                        contentDescription = if (isSaved) "ذخیره شده" else "ذخیره",
                        tint = saveColor
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSaved) "ذخیره شده" else "ذخیره",
                    style = MaterialTheme.typography.labelMedium,
                    color = saveColor
                )
            }
        }
    }
}
