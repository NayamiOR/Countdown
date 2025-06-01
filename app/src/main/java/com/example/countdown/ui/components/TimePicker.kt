package com.example.countdown.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.weight

@Composable
fun TimePicker(
    initialHours: Int = 0,
    initialMinutes: Int = 0,
    initialSeconds: Int = 0,
    onTimeChanged: (hours: Int, minutes: Int, seconds: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var hours by remember { mutableStateOf(initialHours) }
    var minutes by remember { mutableStateOf(initialMinutes) }
    var seconds by remember { mutableStateOf(initialSeconds) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimePickerColumn(
            label = "时",
            value = hours,
            onIncrease = {
                if (hours < 23) hours++ else hours = 0
                onTimeChanged(hours, minutes, seconds)
            },
            onDecrease = {
                if (hours > 0) hours-- else hours = 23
                onTimeChanged(hours, minutes, seconds)
            }
        )
        Spacer(modifier = Modifier.width(16.dp))
        TimePickerColumn(
            label = "分",
            value = minutes,
            onIncrease = {
                if (minutes < 59) minutes++ else minutes = 0
                onTimeChanged(hours, minutes, seconds)
            },
            onDecrease = {
                if (minutes > 0) minutes-- else minutes = 59
                onTimeChanged(hours, minutes, seconds)
            }
        )
        Spacer(modifier = Modifier.width(16.dp))
        TimePickerColumn(
            label = "秒",
            value = seconds,
            onIncrease = {
                if (seconds < 59) seconds++ else seconds = 0
                onTimeChanged(hours, minutes, seconds)
            },
            onDecrease = {
                if (seconds > 0) seconds-- else seconds = 59
                onTimeChanged(hours, minutes, seconds)
            }
        )
    }
}

@Composable
private fun TimePickerColumn(
    label: String,
    value: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        Button(onClick = onIncrease, modifier = Modifier.size(36.dp)) {
            Text("▲", fontSize = 16.sp)
        }
        Text(
            text = value.toString().padStart(2, '0'),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(48.dp)
        )
        Button(onClick = onDecrease, modifier = Modifier.size(36.dp)) {
            Text("▼", fontSize = 16.sp)
        }
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 