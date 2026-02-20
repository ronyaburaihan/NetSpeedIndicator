package com.englesoft.netspeedindicator.presentation.screen.main.history

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.englesoft.netspeedindicator.R
import com.englesoft.netspeedindicator.domain.model.UsageInfo
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * History screen showing daily usage in a table format with a monthly summary card
 * Uses Material3 Theme colors for dynamic light/dark mode support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit
) {
    val dailyUsage by viewModel.dailyUsage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedMonthIndex by viewModel.selectedMonthIndex.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    // Calculate Target Month Prefix based on selection
    val targetMonthPrefix = remember(selectedMonthIndex) {
        YearMonth.now().minusMonths(selectedMonthIndex.toLong())
            .format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    val (thisMonthUsage, dateRangeStr) = remember(dailyUsage, targetMonthPrefix) {
        val monthUsages = dailyUsage.filter { it.date.startsWith(targetMonthPrefix) }

        val total = UsageInfo(
            date = "Total",
            wifiRxBytes = monthUsages.sumOf { it.wifiRxBytes },
            wifiTxBytes = monthUsages.sumOf { it.wifiTxBytes },
            mobileRxBytes = monthUsages.sumOf { it.mobileRxBytes },
            mobileTxBytes = monthUsages.sumOf { it.mobileTxBytes }
        )

        // Calculate date range string e.g. "Oct 01 - Oct 31"
        val rangeStr = try {
            val yearMonth = YearMonth.parse(targetMonthPrefix)
            val startOfMonth = yearMonth.atDay(1)
            val endOfMonth =
                if (selectedMonthIndex == 0) LocalDate.now() else yearMonth.atEndOfMonth()

            val formatter = DateTimeFormatter.ofPattern("MMM dd", Locale.US)
            "${startOfMonth.format(formatter)} - ${endOfMonth.format(formatter)}"
        } catch (e: Exception) {
            ""
        }

        Pair(total, rangeStr)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                showMenu = false
                                onSettingsClick()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Stop Service & Exit") },
                            onClick = {
                                showMenu = false
                                viewModel.exitApp()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.StopCircle,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            // Month Selector Chips
            MonthSelector(
                selectedIndex = selectedMonthIndex,
                onSelect = { viewModel.selectMonth(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Table
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                // Header
                TableHeader()

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(dailyUsage) { usage ->
                            UsageRow(usage = usage)
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Summary Card
            MonthSummaryCard(
                usage = thisMonthUsage,
                dateRange = dateRangeStr,
                title = if (selectedMonthIndex == 0) "THIS MONTH TOTAL" else "LAST MONTH TOTAL"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MonthSelector(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            // This Month (Index 0)
            MonthChip(
                label = "This Month",
                isSelected = selectedIndex == 0,
                onClick = { onSelect(0) },
                icon = Icons.Outlined.CalendarMonth
            )
        }
        item {
            // Last Month (Index 1)
            MonthChip(
                label = "Last Month",
                isSelected = selectedIndex == 1,
                onClick = { onSelect(1) }
            )
        }
        item {
            // Last 3 Months (Index 3 - Using 3 as identifier for 3 months)
            // Note: ViewModel logic needs update to handle index 3 if implemented fully.
            // For now UI only.
            MonthChip(
                label = "Last 3 Months",
                isSelected = selectedIndex == 3,
                onClick = { onSelect(3) }
            )
        }
    }
}

@Composable
fun MonthChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface, // Adjust colors as per image?
        // Image shows: Selected = Blue Outline + Blue Text + Blue Icon. Unselected = Grey BG + White Text.
        // Wait, Image shows: 
        // Selected "This Month": Blue Outline, Transparent/Dark BG, Blue Text.
        // Unselected "Last Month": Dark Grey BG, White Text.

        border = if (isSelected) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary
        ) else null,
        modifier = Modifier.height(36.dp)
    ) {
        // Correct coloring logic based on image
        val backgroundColor =
            if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant
        val contentColor =
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

        Row(
            modifier = Modifier
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            )
    ) {
        // DATE Header (Primary Background)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DATE",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        // Vertical Divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        // MOBILE Header
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SignalCellularAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "MOBILE",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        // WIFI Header
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "WIFI",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        // TOTAL Header
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TOTAL",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
}

@Composable
fun UsageRow(usage: UsageInfo) {
    val dateParts = formatDateParts(usage.date)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp) // Taller rows
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Date Column
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dateParts.first, // OCT
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = dateParts.second, // 24
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        // Mobile Column
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            val (value, unit) = formatDataParts(usage.mobileTotalBytes)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        // WiFi Column
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            val (value, unit) = formatDataParts(usage.wifiTotalBytes)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        // Total Column
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            val (value, unit) = formatDataParts(usage.totalBytes)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun MonthSummaryCard(usage: UsageInfo, dateRange: String, title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title + Date Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )

                // Date Chip
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = dateRange,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mobile
                SummaryStatItem(
                    label = "Mobile",
                    value = usage.mobileTotalBytes,
                    icon = Icons.Default.SignalCellularAlt,
                    color = MaterialTheme.colorScheme.primary
                )

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Wi-Fi
                SummaryStatItem(
                    label = "Wi-Fi",
                    value = usage.wifiTotalBytes,
                    icon = Icons.Default.Wifi,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Total
                SummaryStatItem(
                    label = "Total",
                    value = usage.totalBytes,
                    icon = Icons.Outlined.DonutLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SummaryStatItem(
    label: String,
    value: Long,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        val (valStr, unitStr) = formatDataParts(value)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = valStr,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = unitStr,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}

// Helper to format date into "MMM" and "dd" (e.g. "OCT" and "24")
fun formatDateParts(dateString: String): Pair<String, String> {
    if (dateString == "This Month" || dateString == "Today") return Pair("", dateString)
    return try {
        val date = LocalDate.parse(dateString)
        val month = date.format(DateTimeFormatter.ofPattern("MMM", Locale.US)).uppercase()
        val day = date.format(DateTimeFormatter.ofPattern("dd"))
        Pair(month, day)
    } catch (e: Exception) {
        Pair("", dateString)
    }
}

// Helper to format data into Value and Unit (e.g. "1.65" and "GB")
fun formatDataParts(bytes: Long): Pair<String, String> {
    if (bytes == 0L) return Pair("0", "MB")

    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    if (gb >= 1.0) {
        return Pair(String.format(Locale.US, "%.2f", gb), "GB")
    }

    val mb = bytes / (1024.0 * 1024.0)
    if (mb >= 10.0) {
        return Pair(String.format(Locale.US, "%.0f", mb), "MB")
    }
    return Pair(String.format(Locale.US, "%.1f", mb), "MB")
}

// Keeping original formatData for backward compatibility if needed
fun formatData(bytes: Long): String {
    val (v, u) = formatDataParts(bytes)
    return "$v $u"
}

