package com.englesoft.netspeedindicator.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.englesoft.netspeedindicator.presentation.viewmodel.SettingsViewModel

/**
 * Settings screen for app configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val showUploadSpeed by viewModel.showUploadSpeed.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // Refresh permissions when returning to the screen
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.checkPermissions()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Display Section
            SettingsSection(title = "Display") {
                // Appearance
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ThemeSegmentedControl(
                    selectedTheme = appTheme,
                    onThemeSelected = { viewModel.setAppTheme(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic Color
                SettingsSwitchRow(
                    title = "Dynamic Color",
                    subtitle = "Use wallpaper colors",
                    checked = dynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Lock Screen Notification
                SettingsSwitchRow(
                    title = "Lock Screen Notification",
                    subtitle = "Show speed on lock screen",
                    checked = lockScreenNotification,
                    onCheckedChange = { viewModel.setLockScreenNotification(it) }
                )

                // Show Upload Speed
                SettingsSwitchRow(
                    title = "Show Upload Speed",
                    subtitle = "Show upload speed in notification",
                    checked = showUploadSpeed,
                    onCheckedChange = { viewModel.setShowUploadSpeed(it) }
                )
            }
            
            // Permissions Section
            SettingsSection(title = "Permissions") {
                // Usage Access
                PermissionRow(
                    title = "Usage Access",
                    subtitle = "Required for data tracking",
                    isGranted = hasUsagePermission,
                    onGrantClick = { viewModel.requestUsagePermission() }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Battery Optimization
                PermissionRow(
                    title = "Battery Optimization",
                    subtitle = "Disable to prevent service killing",
                    isGranted = isBatteryOptimizationDisabled,
                    onGrantClick = { viewModel.requestDisableBatteryOptimization() },
                    isCritical = true
                )

                if (isAutoStartAvailable) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    PermissionRow(
                        title = "Auto Start",
                        subtitle = "Allow app to start in background",
                        isGranted = false, // Cannot detect reliably, so always show button
                        onGrantClick = { viewModel.requestAutoStartPermission() },
                        isCritical = true
                    )
                }
            }

            // About Section
            SettingsSection(title = "About") {
                AboutRow(title = "Version", value = "1.0.0")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                AboutRow(title = "Privacy Policy", onClick = { /* TODO */ })
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                AboutRow(title = "Licenses", onClick = { /* TODO */ })
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ThemeSegmentedControl(
    selectedTheme: Int,
    onThemeSelected: (Int) -> Unit
) {
    val options = listOf("System", "Light", "Dark")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = selectedTheme == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.background
                        else Color.Transparent
                    )
                    .clickable { onThemeSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun PermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit,
    isCritical: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Granted",
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Button(
                onClick = onGrantClick,
                colors = if (isCritical) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()
            ) {
                Text(if (isCritical) "Fix" else "Allow")
            }
        }
    }
}

@Composable
fun AboutRow(
    title: String,
    value: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Go",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}