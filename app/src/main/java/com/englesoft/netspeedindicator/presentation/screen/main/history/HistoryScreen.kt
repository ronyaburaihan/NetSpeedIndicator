package com.englesoft.netspeedindicator.presentation.screen.main.history

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.englesoft.netspeedindicator.R
import com.englesoft.netspeedindicator.core.util.FormatUtils
import com.englesoft.netspeedindicator.domain.model.UsageInfo
import com.englesoft.netspeedindicator.presentation.component.AppTopBar
import com.englesoft.netspeedindicator.presentation.theme.dimens
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    HistoryScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreenContent(
    uiState: HistoryUiState,
    onEvent: (HistoryUiEvent) -> Unit = {}
) {
    val dailyUsage = uiState.dailyUsage
    val isLoading = uiState.isLoading
    val selectedMonthIndex = uiState.selectedMonthIndex

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

        val rangeStr = try {
            val yearMonth = YearMonth.parse(targetMonthPrefix)
            val startOfMonth = yearMonth.atDay(1)
            val endOfMonth =
                if (selectedMonthIndex == 0) LocalDate.now() else yearMonth.atEndOfMonth()

            val formatter = DateTimeFormatter.ofPattern("MMM 1 - MMM dd", Locale.US)
            val startFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
            val endFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
            "${startOfMonth.format(startFormatter)} - ${endOfMonth.format(endFormatter)}"
        } catch (e: Exception) {
            ""
        }

        Pair(total, rangeStr)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.history),
                subTitle = stringResource(R.string.data_usage_logs),
                showTrailingIcon = false
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimens.horizontalPadding)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                // Month Selector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SegmentedButton(
                            text = stringResource(R.string.this_month),
                            isSelected = selectedMonthIndex == 0,
                            modifier = Modifier.weight(1f),
                            onClick = { onEvent(HistoryUiEvent.OnSelectMonth(0)) }
                        )
                        SegmentedButton(
                            text = stringResource(R.string.last_month),
                            isSelected = selectedMonthIndex == 1,
                            modifier = Modifier.weight(1f),
                            onClick = { onEvent(HistoryUiEvent.OnSelectMonth(1)) }
                        )
                        SegmentedButton(
                            text = stringResource(R.string.last_3_months),
                            isSelected = selectedMonthIndex == 3,
                            modifier = Modifier.weight(1f),
                            onClick = { onEvent(HistoryUiEvent.OnSelectMonth(3)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Table Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
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
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    bottom = 140.dp
                                )
                            ) {
                                items(dailyUsage) { usage ->
                                    UsageRow(usage = usage)
                                }
                            }
                        }
                    }
                }
            }

            // Sticky Footer overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.month_summary).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = dateRangeStr,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Total Usage
                        val (totalVal, totalUnit) = formatDataParts(thisMonthUsage.totalBytes)
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = totalVal,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 30.sp
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = totalUnit,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Text(
                                text = "TOTAL USAGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                                .padding(horizontal = 12.dp)
                        )

                        // Mobile / Wifi splits
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val (mobVal, mobUnit) = formatDataParts(thisMonthUsage.mobileTotalBytes)
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SignalCellularAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "MOBILE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "$mobVal $mobUnit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 20.dp, top = 2.dp)
                                )
                            }

                            val (wifiVal, wifiUnit) = formatDataParts(thisMonthUsage.wifiTotalBytes)
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WIFI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "$wifiVal $wifiUnit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 20.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SegmentedButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date
        Text(
            text = "Date".uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.weight(1f)
        )

        // Mobile
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.SignalCellularAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Mobile".uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        // WiFi
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Wifi,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "WiFi".uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        // Total
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.DonutLarge,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Total".uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun UsageRow(usage: UsageInfo) {
    val dateParts = formatDateParts(usage.date)
    val dayOfWeek = formatDayOfWeek(usage.date)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dayOfWeek,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${dateParts.first} ${dateParts.second}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // Mobile Column
        val (mobVal, mobUnit) = formatDataParts(usage.mobileTotalBytes)
        Text(
            text = "$mobVal $mobUnit",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )

        // WiFi Column
        val (wifiVal, wifiUnit) = formatDataParts(usage.wifiTotalBytes)
        Text(
            text = "$wifiVal $wifiUnit",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )

        // Total Column
        val (totVal, totUnit) = formatDataParts(usage.totalBytes)
        Text(
            text = "$totVal $totUnit",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

private fun formatDayOfWeek(dateString: String): String {
    if (dateString == "This Month" || dateString == "Today" || dateString == "Yesterday") return dateString
    return try {
        val date = LocalDate.parse(dateString)
        val today = LocalDate.now()
        when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> date.format(DateTimeFormatter.ofPattern("EEE", Locale.US))
        }
    } catch (e: Exception) {
        dateString
    }
}

private fun formatDateParts(dateString: String): Pair<String, String> {
    if (dateString == "This Month" || dateString == "Today" || dateString == "Yesterday") return Pair(
        "",
        dateString
    )
    return try {
        val date = LocalDate.parse(dateString)
        val month = date.format(DateTimeFormatter.ofPattern("MMM", Locale.US))
        val day = date.format(DateTimeFormatter.ofPattern("dd"))
        Pair(month, day)
    } catch (e: Exception) {
        Pair("", dateString)
    }
}

// Delegates to FormatUtils so byte formatting is consistent with the Home screen
// (previously this reimplemented its own thresholds, which could show different values
// for the same byte count depending on which screen you were looking at).
private fun formatDataParts(bytes: Long): Pair<String, String> =
    FormatUtils.formatBytesValue(bytes) to FormatUtils.formatBytesUnit(bytes)
