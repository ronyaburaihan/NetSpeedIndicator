package com.englesoft.netspeedindicator.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

/**
 * History screen showing daily usage in a table format
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // Prevent full screen expansion
                .background(Color(0xFF555555)) // Dark gray header
                .padding(bottom = 2.dp), // Separator
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableCell(text = "Date", weight = 1.2f, isHeader = true)
            // 2dp separator
            Spacer(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.White))
            TableCell(text = "Mobile", weight = 1f, isHeader = true)
            Spacer(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.White))
            TableCell(text = "WiFi", weight = 1f, isHeader = true)
            Spacer(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.White))
            TableCell(text = "Total", weight = 1f, isHeader = true)
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f) // Take remaining space
            ) {
                // Usage List (Sorted by date descending is assumed from ViewModel)
                items(dailyUsage) { usage ->
                    UsageRow(usage = usage)
                    // Horizontal separator
                    Spacer(modifier = Modifier.height(2.dp).fillMaxWidth().background(Color.White))
                }
                
                // This Month Footer
                item {
                    UsageRow(usage = thisMonthUsage)
                }
            }
        }
    }
}

@Composable
fun UsageRow(usage: UsageModel) {
    val dateStr = formatDate(usage.date)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Ensure all cells have same height
    ) {
        // Date Column (Dark Background)
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .background(Color(0xFF555555)) // Dark gray
                .padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = dateStr,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Vertical Separator
        Spacer(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.White))
        
        // Data Columns (Light Background)
        DataCell(text = formatUsageBytes(usage.mobileTotalBytes), weight = 1f)
        Spacer(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.White))
        DataCell(text = formatUsageBytes(usage.wifiTotalBytes), weight = 1f)
        Spacer(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.White))
        DataCell(text = formatUsageBytes(usage.totalBytes), weight = 1f)
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        fontSize = 14.sp,
        modifier = Modifier
            .weight(weight)
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun RowScope.DataCell(
    text: String,
    weight: Float
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .background(Color(0xFFEEEEEE)) // Light gray (lighter)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 13.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

fun formatDate(dateString: String): String {
    if (dateString == "This Month") return "This Month"
    try {
        val date = LocalDate.parse(dateString)
        val today = LocalDate.now()
        
        if (date == today) return "Today"
        
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
        return date.format(formatter)
    } catch (e: Exception) {
        return dateString
    }
}

fun formatUsageBytes(bytes: Long): String {
    if (bytes == 0L) return "0 MB"
    
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    if (gb >= 1.0) {
        return String.format(Locale.US, "%.2f GB", gb)
    }
    
    val mb = bytes / (1024.0 * 1024.0)
    return String.format(Locale.US, "%.0f MB", mb)
}

