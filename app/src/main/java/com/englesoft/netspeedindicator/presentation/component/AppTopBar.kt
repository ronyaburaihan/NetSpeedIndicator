package com.englesoft.netspeedindicator.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.englesoft.netspeedindicator.presentation.theme.dimens

@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    title: String = "Dashboard",
    subTitle: String = "Real-time Monitor",
    showStopIcon: Boolean = false,
    onStopClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimens.horizontalPadding,
                vertical = dimens.verticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium
                    .copy(color = MaterialTheme.colorScheme.onBackground)
            )
            Text(
                text = subTitle,
                style = MaterialTheme.typography.bodyMedium
                    .copy(color = MaterialTheme.colorScheme.onBackground)
            )
        }
        if (showStopIcon) {
            Icon(
                modifier = Modifier.clickable { onStopClick.invoke() },
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "Stop",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}