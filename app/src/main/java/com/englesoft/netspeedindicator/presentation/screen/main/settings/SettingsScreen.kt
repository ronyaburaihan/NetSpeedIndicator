package com.englesoft.netspeedindicator.presentation.screen.main.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.englesoft.netspeedindicator.R
import com.englesoft.netspeedindicator.presentation.component.AppTopBar

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsState()
    val isBatteryOptimizationDisabled by viewModel.isBatteryOptimizationDisabled.collectAsState()
    val isAutoStartAvailable by viewModel.isAutoStartAvailable.collectAsState()

    val appTheme by viewModel.appTheme.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val lockScreenNotification by viewModel.lockScreenNotification.collectAsState()
    val showUploadSpeed by viewModel.showUploadSpeed.collectAsState() // used for Notification Bar toggle here

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.checkPermissions()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        //GradientMeshBackground()

        Scaffold(
            topBar = {
                AppTopBar(
                    title = stringResource(R.string.settings),
                    subTitle = stringResource(R.string.preferences_and_customization),
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Appearance Section
                SettingsSection(
                    title = stringResource(R.string.appearance)
                ) {
                    GlassPanelItem(
                        icon = Icons.Default.Palette,
                        iconTint = Color(0xFF60A5FA), // blue-400
                        iconBgColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
                        title = stringResource(R.string.app_theme),
                        subtitle = stringResource(R.string.dark_mode),
                        onClick = { viewModel.setAppTheme((appTheme + 1) % 3) },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (appTheme == 2) stringResource(R.string.dark) else "System",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    GlassPanelItem(
                        icon = Icons.Default.FormatPaint,
                        iconTint = Color(0xFFC084FC), // purple-400
                        iconBgColor = Color(0xFFA855F7).copy(alpha = 0.2f),
                        title = stringResource(R.string.dynamic_color),
                        subtitle = stringResource(R.string.match_system_wallpaper),
                        trailingContent = {
                            CustomSwitch(
                                checked = dynamicColor,
                                onCheckedChange = { viewModel.setDynamicColor(it) })
                        }
                    )
                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    GlassPanelItem(
                        icon = Icons.Default.Language,
                        iconTint = Color(0xFFFB7185), // rose-400
                        iconBgColor = Color(0xFFF43F5E).copy(alpha = 0.2f),
                        title = stringResource(R.string.app_language),
                        subtitle = stringResource(R.string.english_us),
                        onClick = { /* Handle language change */ },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.en),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }

                // Display Section
                SettingsSection(title = stringResource(R.string.display)) {
                    GlassPanelItem(
                        icon = Icons.Default.Speed,
                        iconTint = Color(0xFF34D399), // emerald-400
                        iconBgColor = Color(0xFF10B981).copy(alpha = 0.2f),
                        title = stringResource(R.string.unit_display),
                        subtitle = stringResource(R.string.mb_s_vs_mb_s),
                        onClick = { /* Toggle unit */ },
                        trailingContent = {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    GlassPanelItem(
                        icon = Icons.Default.LockClock,
                        iconTint = Color(0xFFFBBF24), // amber-400
                        iconBgColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                        title = stringResource(R.string.lock_screen_widget),
                        subtitle = stringResource(R.string.show_speed_on_lockscreen),
                        trailingContent = {
                            CustomSwitch(
                                checked = lockScreenNotification,
                                onCheckedChange = { viewModel.setLockScreenNotification(it) })
                        }
                    )
                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    GlassPanelItem(
                        icon = Icons.Default.NotificationsActive,
                        iconTint = Color(0xFFF472B6), // pink-400
                        iconBgColor = Color(0xFFEC4899).copy(alpha = 0.2f),
                        title = stringResource(R.string.notification_bar),
                        subtitle = stringResource(R.string.persistent_speed_monitor),
                        trailingContent = {
                            CustomSwitch(
                                checked = showUploadSpeed,
                                onCheckedChange = { viewModel.setShowUploadSpeed(it) })
                        }
                    )
                }

                // System Section
                SettingsSection(title = stringResource(R.string.system)) {
                    GlassPanelItem(
                        icon = androidx.compose.material.icons.Icons.Default.Info, // Used as fallback for security as Security icon name may clash
                        iconTint = Color(0xFF22D3EE), // cyan-400
                        iconBgColor = Color(0xFF06B6D4).copy(alpha = 0.2f),
                        title = stringResource(R.string.usage_access),
                        subtitle = stringResource(R.string.required_for_data_tracking),
                        onClick = { viewModel.requestUsagePermission() },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (hasUsagePermission) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFF22C55E).copy(alpha = 0.2f)) // green-500/20
                                            .border(
                                                1.dp,
                                                Color(0xFF22C55E).copy(alpha = 0.3f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.granted),
                                            fontSize = 12.sp,
                                            color = Color(0xFF4ADE80)
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    GlassPanelItem(
                        icon = Icons.Default.BatteryChargingFull,
                        iconTint = Color(0xFFFB923C), // orange-400
                        iconBgColor = Color(0xFFF97316).copy(alpha = 0.2f),
                        title = stringResource(R.string.battery_optimization),
                        subtitle = stringResource(R.string.disable_for_accurate_monitoring),
                        onClick = { viewModel.requestDisableBatteryOptimization() },
                        trailingContent = {
                            CustomSwitch(
                                checked = isBatteryOptimizationDisabled,
                                onCheckedChange = { /* Battery flow*/ })
                        }
                    )
                    if (isAutoStartAvailable) {
                        Divider(
                            color = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        GlassPanelItem(
                            icon = Icons.Default.RocketLaunch,
                            iconTint = Color(0xFF2DD4BF), // teal-400
                            iconBgColor = Color(0xFF14B8A6).copy(alpha = 0.2f),
                            title = stringResource(R.string.auto_start),
                            subtitle = stringResource(R.string.launch_on_device_boot),
                            onClick = { viewModel.requestAutoStartPermission() },
                            trailingContent = {
                                CustomSwitch(checked = false, onCheckedChange = {})
                            }
                        )
                    }
                }

                // About Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                ) {
                    GlassPanelItem(
                        icon = Icons.Default.Info,
                        iconTint = Color(0xFFCBD5E1), // slate-300
                        iconBgColor = Color(0xFF64748B).copy(alpha = 0.2f),
                        title = stringResource(R.string.about),
                        subtitle = stringResource(R.string.version_text),
                        trailingContent = {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }

                // Footer
                Text(
                    text = "Designed with \u2764\uFE0F for SpeedMonitor\nBuild 2023.10.25",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 40.dp)
                )

                Spacer(modifier = Modifier.height(60.dp)) // bottom nav space
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFA5B4FC), // indigo-300
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun GlassPanelItem(
    icon: ImageVector,
    iconTint: Color,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailingContent()
    }
}

@Composable
fun CustomSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFF6366F1), // indigo-500
            uncheckedThumbColor = Color(0xFF94A3B8), // slate-400
            uncheckedTrackColor = Color(0xFF334155), // slate-700
            uncheckedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun GradientMeshBackground() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) { // slate-900 base
        val w = size.width
        val h = size.height

        // Top Left Blue
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.15f), Color.Transparent),
                center = Offset(0f, 0f),
                radius = w * 0.8f
            ),
            center = Offset(0f, 0f),
            radius = w * 0.8f
        )

        // Top Right Indigo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF6366F1).copy(alpha = 0.15f), Color.Transparent),
                center = Offset(w, 0f),
                radius = w * 0.8f
            ),
            center = Offset(w, 0f),
            radius = w * 0.8f
        )

        // Bottom Right Purple
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFA855F7).copy(alpha = 0.1f), Color.Transparent),
                center = Offset(w, h),
                radius = w * 0.8f
            ),
            center = Offset(w, h),
            radius = w * 0.8f
        )

        // Bottom Left Blue
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.1f), Color.Transparent),
                center = Offset(0f, h),
                radius = w * 0.8f
            ),
            center = Offset(0f, h),
            radius = w * 0.8f
        )
    }
}