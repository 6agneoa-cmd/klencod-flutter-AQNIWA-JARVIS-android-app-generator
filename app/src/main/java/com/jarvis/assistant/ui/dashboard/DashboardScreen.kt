package com.jarvis.assistant.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.ui.components.*
import com.jarvis.assistant.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(onTalk: () -> Unit, onAgents: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "orb")
    val pulse by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "pulse"
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF041019), BgDeep))
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp)
    ) {
        // ── Top bar ─────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("SYSTEM STATUS", color = TextDim, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                StatusChip("● OPTIMAL", Green)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date()),
                    color = TextDim, fontSize = 9.sp
                )
                Text(
                    SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date()),
                    color = Cyan, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Search, null, tint = TextDim, modifier = Modifier.size(18.dp))
                Icon(Icons.Default.Notifications, null, tint = TextDim, modifier = Modifier.size(18.dp))
                Icon(Icons.Default.Settings, null, tint = TextDim, modifier = Modifier.size(18.dp))
            }
        }

        // ── HERO ORB ──────────────────────────────
        Box(
            Modifier.fillMaxWidth().weight(1.15f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val c = center
                val base = size.minDimension * 0.22f
                // outer pulsing rings
                for (i in 1..3) {
                    val r = base * (1 + i * 0.35f + pulse * 0.08f)
                    drawCircle(Cyan.copy(alpha = 0.10f / i), r, c, style = Stroke(width = 1.5f))
                }
                // glowing core
                drawCircle(
                    Brush.radialGradient(
                        listOf(Cyan.copy(alpha = 0.9f), Cyan.copy(alpha = 0.15f), Color.Transparent),
                        c, base * 1.3f
                    ), base * 1.3f, c
                )
                drawCircle(Cyan.copy(alpha = 0.5f), base, c, style = Stroke(width = 2f))
                // orbiting particles
                for (i in 0..5) {
                    val a = pulse * 6.28f + i * 1.05f
                    drawCircle(
                        Color.White.copy(alpha = 0.85f), 4f,
                        Offset(c.x + kotlin.math.cos(a) * base * 1.35f,
                               c.y + kotlin.math.sin(a) * base * 0.55f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("J A R V I S", color = Color.White, fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center)
                Text("AI CORE  v1.0", color = Cyan, fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace, letterSpacing = 6.sp)
            }
        }

        // ── Bottom grid ───────────────────────────
        Row(Modifier.fillMaxWidth().weight(1f)) {
            // system monitor
            GlowPanel(Modifier.weight(1f).padding(end = 4.dp)) {
                PanelTitle("System Monitor")
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    RingStat("CPU", 15, Cyan)
                    RingStat("RAM", 54, Purple)
                    RingStat("DISK", 40, Green)
                }
            }
            // quick commands
            GlowPanel(Modifier.weight(1f).padding(start = 4.dp)) {
                PanelTitle("Quick Commands")
                Spacer(Modifier.height(8.dp))
                QuickBtn(Icons.Default.Add, "Start New Task") { onTalk() }
                QuickBtn(Icons.Default.DateRange, "Open Calendar") {}
                QuickBtn(Icons.Default.Face, "Start Voice Chat") {}
                QuickBtn(Icons.Default.PlayArrow, "Run Workflow") { onAgents() }
            }
        }
        Spacer(Modifier.height(6.dp))

        // ── TALK bar ──────────────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(PanelBg)
                .border(1.5.dp, Cyan, RoundedCornerShape(30.dp))
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.KeyboardVoice, null, tint = Cyan, modifier = Modifier.size(22.dp))
                TextButton(onClick = onTalk, modifier = Modifier.weight(1f)) {
                    Text("TALK TO JARVIS", color = Cyan, fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
                }
                Icon(Icons.Default.Send, null, tint = Cyan, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun QuickBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Cyan, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = TextMain, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}
