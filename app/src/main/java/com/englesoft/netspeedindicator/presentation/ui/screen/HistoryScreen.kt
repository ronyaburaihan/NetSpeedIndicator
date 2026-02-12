package com.englesoft.netspeedindicator.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.englesoft.netspeedindicator.domain.model.UsageModel
import com.englesoft.netspeedindicator.presentation.viewmodel.HistoryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.pow

// Custom Colors based on the UI image
private val DarkBackground = Color(0xFF161920)
private val CardBackground = Color(0xFF1E222C)
private val HeaderBlue = Color(0xFF3366E6) // Bright blue for DATE header
private val TextBlue = Color(0xFF448AFF)   // Light blue for text highlights
private val TextWhite = Color(0xFFEEEEEE)
private val TextGray = Color(0xFFAAAAAA)
private val GridLineColor = Color(0xFF2A2E3A)

/**
 * History screen showing daily usage in a table format with a monthly summary card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit
) {
    val dailyUsage by viewModel.dailyUsage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Calculate This Month's Total
    val currentMonthPrefix = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    val thisMonthUsage = remember(dailyUsage) {
        val monthUsages = dailyUsage.filter { it.date.startsWith(currentMonthPrefix) }
        UsageModel(
            date = "This Month",
            wifiRxBytes = monthUsages.sumOf { it.wifiRxBytes },
            wifiTxBytes = monthUsages.sumOf { it.wifiTxBytes },
            mobileRxBytes = monthUsages.sumOf { it.mobileRxBytes },
            mobileTxBytes = monthUsages.sumOf { it.mobileTxBytes }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Internet Speed Indicator",
                        color = TextWhite,
                        fontSize = 20.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = HeaderBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground)
                .padding(16.dp)
        ) {
            // Table
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, GridLineColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(CardBackground, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            ) {
                // Header
                TableHeader()

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HeaderBlue)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(dailyUsage) { usage ->
                            UsageRow(usage = usage)
                            Divider(color = GridLineColor, thickness = 1.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Summary Card
            MonthSummaryCard(usage = thisMonthUsage)
        }
    }
}

@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(CardBackground)
    ) {
        // DATE Header (Blue Background)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(HeaderBlue, RoundedCornerShape(topStart = 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DATE",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        
        // Vertical Divider
        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(GridLineColor))

        // MOBILE Header
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SignalCellularAlt,
                    contentDescription = null,
                    tint = HeaderBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "MOBILE",
                    color = HeaderBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(GridLineColor))

        // WIFI Header
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = HeaderBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "WIFI",
                    color = HeaderBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(GridLineColor))

        // TOTAL Header
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TOTAL",
                color = HeaderBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
    Divider(color = GridLineColor, thickness = 1.dp)
}

@Composable
fun UsageRow(usage: UsageModel) {
    val dateParts = formatDateParts(usage.date)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp) // Taller rows as per design
            .background(CardBackground)
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
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = dateParts.second, // 24
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(GridLineColor))

        // Mobile Column
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatData(usage.mobileTotalBytes),
                color = TextGray,
                fontSize = 13.sp
            )
        }

        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(GridLineColor))

        // WiFi Column
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatData(usage.wifiTotalBytes),
                color = TextGray,
                fontSize = 13.sp
            )
        }

        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(GridLineColor))

        // Total Column
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatData(usage.totalBytes),
                color = TextBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun MonthSummaryCard(usage: UsageModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Total
            Column {
                Text(
                    text = "This Month's Total",
                    color = TextGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatData(usage.totalBytes),
                    color = TextWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right Side: Breakdown
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Mobile Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(HeaderBlue, androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mobile",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatData(usage.mobileTotalBytes),
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // WiFi Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(TextGray, androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WiFi",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatData(usage.wifiTotalBytes),
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
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

// Helper to format data (e.g. "1.65 GB" or "450 MB")
fun formatData(bytes: Long): String {
    if (bytes == 0L) return "0 MB"

    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    if (gb >= 1.0) {
        return String.format(Locale.US, "%.2f GB", gb)
    }

    val mb = bytes / (1024.0 * 1024.0)
    return String.format(Locale.US, "%.0f MB", mb)
}

