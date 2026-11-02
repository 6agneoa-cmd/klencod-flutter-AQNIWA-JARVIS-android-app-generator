package com.jarvis.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.ui.theme.*

/** Glowing sci-fi panel used across the dashboard. */
@Composable
fun GlowPanel(
    modifier: Modifier = Modifier,
    glowColor: Color = Cyan,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(PanelBg)
            .border(1.dp, glowColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        content = content
    )
}

@Composable
fun PanelTitle(text: String, color: Color = Cyan) {
    Text(
        text = text.uppercase(),
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp
    )
}

@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(text.uppercase(), color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace)
    }
}

/** Circular progress ring (CPU/RAM/Disk style). */
@Composable
fun RingStat(label: String, percent: Int, color: Color, size: Dp = 64.dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.size(size),
                color = color,
                strokeWidth = 5.dp,
                trackColor = color.copy(alpha = 0.12f)
            )
            Text("$percent%", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextDim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}
