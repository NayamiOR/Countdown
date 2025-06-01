package com.example.countdown.ui.components

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.countdown.utils.TimeUtils

@Composable
fun TimeSettingDialog(
    initialMinutes: Int,
    initialSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int, seconds: Int) -> Unit
) {
    var inputMinutes by remember { mutableStateOf(initialMinutes.toString()) }
    var inputSeconds by remember { mutableStateOf(initialSeconds.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置倒计时时间") },
        text = {
            Column {
                Text(
                    "请输入倒计时时间：",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputMinutes,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                inputMinutes = it
                            }
                        },
                        label = { Text("分钟") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    Text(":", fontSize = 20.sp)

                    OutlinedTextField(
                        value = inputSeconds,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() } && it.length <= 2) {
                                val seconds = it.toIntOrNull() ?: 0
                                if (seconds < 60) {
                                    inputSeconds = it
                                }
                            }
                        },
                        label = { Text("秒") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val minutes = inputMinutes.toIntOrNull() ?: 0
                    val seconds = inputSeconds.toIntOrNull() ?: 0
                    val totalSeconds = TimeUtils.toTotalSeconds(minutes, seconds)
                    if (totalSeconds > 0) {
                        onConfirm(minutes, seconds)
                    }
                    onDismiss()
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 