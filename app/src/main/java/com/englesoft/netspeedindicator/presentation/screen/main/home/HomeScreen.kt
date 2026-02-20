package com.englesoft.netspeedindicator.presentation.screen.main.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.englesoft.netspeedindicator.R
import com.englesoft.netspeedindicator.core.util.FormatUtils
import com.englesoft.netspeedindicator.presentation.component.AppTopBar
import com.englesoft.netspeedindicator.presentation.theme.OutfitFontFamily

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(uiState = uiState)
}

@Composable
private fun HomeScreenContent(uiState: HomeUiState) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.dashboard),
                subTitle = stringResource(R.string.real_time_monitor),
                showStopIcon = true,
                onStopClick = { /* Handle stop */ }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            GradientSpeedCard(uiState = uiState)
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.today_s_usage),
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.reset_12_00_am),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                TotalUsageCard(uiState = uiState)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MobileUsageCard(
                        modifier = Modifier.weight(1f),
                        usage = uiState.todayUsage.mobileTotalBytes
                    )
                    WifiUsageCard(
                        modifier = Modifier.weight(1f),
                        usage = uiState.todayUsage.wifiTotalBytes
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PeakSpeedCard(
                    modifier = Modifier.weight(1f),
                    peakSpeed = uiState.peakSpeed
                )
                SessionTimeCard(
                    modifier = Modifier.weight(1f),
                    sessionSeconds = uiState.sessionDurationSeconds
                )
            }
            Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom nav
        }
    }
}

@Composable
fun GradientSpeedCard(uiState: HomeUiState) {
    val totalSpeed = uiState.currentSpeed.totalBytesPerSecond
    val downloadSpeed = uiState.currentSpeed.downloadBytesPerSecond
    val uploadSpeed = uiState.currentSpeed.uploadBytesPerSecond

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2563EB), // blue-600
                        Color(0xFF4F46E5)  // indigo-600
                    )
                )
            )
            .padding(20.dp)
    ) {
        // Decorative blurred circles
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(192.dp)
                .padding(end = 40.dp, top = 40.dp) // manual offset equivalent
                .blur(64.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(160.dp)
                .padding(start = 40.dp, bottom = 40.dp)
                .blur(64.dp)
                .background(Color(0xFF6366F1).copy(alpha = 0.4f), CircleShape)
        )

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Session Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BlinkingDot()
                    Text(
                        text = stringResource(R.string.live_session),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
                
                AudioWaveBars()
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.total_speed).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDBEAFE).copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
                                append(FormatUtils.formatSpeedValue(totalSpeed))
                            }
                            append(" ")
                            withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.8f))) {
                                append(FormatUtils.formatSpeedUnit(totalSpeed))
                            }
                        },
                        fontFamily = OutfitFontFamily
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassPanelStat(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.download),
                    value = FormatUtils.formatSpeed(downloadSpeed),
                    icon = {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                )
                GlassPanelStat(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.upload),
                    value = FormatUtils.formatSpeed(uploadSpeed),
                    icon = {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}

@Composable
fun GlassPanelStat(modifier: Modifier = Modifier, label: String, value: String, icon: @Composable () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun BlinkingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkingDot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color(0xFF4ADE80)) // green-400
            .alpha(alpha)
    )
}

@Composable
fun AudioWaveBars() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(24.dp).alpha(0.6f)
    ) {
        listOf(0.3f, 0.5f, 0.7f, 0.4f, 0.6f).forEach { heightFraction ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(heightFraction)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun TotalUsageCard(uiState: HomeUiState) {
    val totalBytes = uiState.todayUsage.totalBytes
    val percentage = if (totalBytes > 0) 85f else 0f // mock static percentage from HTML design for now

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, Color(0xFF60A5FA).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3B82F6).copy(alpha = 0.1f)) // blue-500/10
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DataUsage,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA), // blue-400
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.total_usage).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = FormatUtils.formatBytesValue(totalBytes),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = OutfitFontFamily,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = FormatUtils.formatBytesUnit(totalBytes),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF60A5FA)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BlinkingDot()
                    Text(
                        text = stringResource(R.string.active_monitoring).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            // Conic gauge mock
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(112.dp)
            ) {
                // Outer track
                Canvas(modifier = Modifier.size(112.dp)) {
                    val strokeWidth = 10.dp.toPx()
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.2f),
                        style = Stroke(strokeWidth)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF3B82F6), // blue
                                Color(0xFF10B981), // emerald
                                Color(0xFF10B981)
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = percentage / 100f * 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.today).uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${percentage.toInt()}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MobileUsageCard(modifier: Modifier = Modifier, usage: Long) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF59E0B).copy(alpha = 0.05f)) // amber-500/5
            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(20.dp)
            .height(120.dp)
    ) {
        Icon(
            imageVector = Icons.Default.SignalCellularAlt,
            contentDescription = null,
            tint = Color(0xFFF59E0B).copy(alpha = 0.2f),
            modifier = Modifier.align(Alignment.TopEnd).size(48.dp)
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                Text(
                    text = stringResource(R.string.mobile),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                        append(FormatUtils.formatBytesValue(usage))
                    }
                    append(" ")
                    withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append(FormatUtils.formatBytesUnit(usage))
                    }
                },
                fontFamily = OutfitFontFamily
            )
        }
    }
}

@Composable
fun WifiUsageCard(modifier: Modifier = Modifier, usage: Long) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF6366F1).copy(alpha = 0.05f)) // indigo-500/5
            .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(20.dp)
            .height(120.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = null,
            tint = Color(0xFF6366F1).copy(alpha = 0.2f),
            modifier = Modifier.align(Alignment.TopEnd).size(48.dp)
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF6366F1)))
                Text(
                    text = stringResource(R.string.wifi),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                        append(FormatUtils.formatBytesValue(usage))
                    }
                    append(" ")
                    withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append(FormatUtils.formatBytesUnit(usage))
                    }
                },
                fontFamily = OutfitFontFamily
            )
        }
    }
}

@Composable
fun PeakSpeedCard(modifier: Modifier = Modifier, peakSpeed: Long) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.peak).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = FormatUtils.formatSpeed(peakSpeed),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = OutfitFontFamily,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun SessionTimeCard(modifier: Modifier = Modifier, sessionSeconds: Long) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.time).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = FormatUtils.formatDuration(sessionSeconds),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = OutfitFontFamily,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
