package com.example.countdown.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

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
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WheelPicker(
            label = "时",
            selectedValue = hours,
            valueRange = 0..23,
            onValueChanged = { newValue ->
                hours = newValue
                onTimeChanged(hours, minutes, seconds)
            }
        )
        
        Text(
            text = ":",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        WheelPicker(
            label = "分",
            selectedValue = minutes,
            valueRange = 0..59,
            onValueChanged = { newValue ->
                minutes = newValue
                onTimeChanged(hours, minutes, seconds)
            }
        )
        
        Text(
            text = ":",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        WheelPicker(
            label = "秒",
            selectedValue = seconds,
            valueRange = 0..59,
            onValueChanged = { newValue ->
                seconds = newValue
                onTimeChanged(hours, minutes, seconds)
            }
        )
    }
}

@Composable
private fun WheelPicker(
    label: String,
    selectedValue: Int,
    valueRange: IntRange,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 56.dp
    val visibleItemsCount = 5
    val halfVisibleItems = visibleItemsCount / 2
    val scope = rememberCoroutineScope()
    
    // 创建一个很大的列表来模拟无限滚动
    val totalItems = valueRange.count() * 100
    val startIndex = totalItems / 2 - totalItems / 2 % valueRange.count() + selectedValue - valueRange.first
    
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = startIndex - halfVisibleItems
    )
    
    var isScrolling by remember { mutableStateOf(false) }
    var lastScrollTime by remember { mutableStateOf(0L) }
    
    // 计算当前选中的值
    val currentIndex by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset
            
            // 计算中心项的索引
            val centerItemIndex = if (firstVisibleOffset > itemHeight.value / 2) {
                firstVisibleIndex + halfVisibleItems + 1
            } else {
                firstVisibleIndex + halfVisibleItems
            }
            
            // 转换为实际值
            val actualValue = (centerItemIndex % valueRange.count()).let { index ->
                if (index < 0) index + valueRange.count() else index
            } + valueRange.first
            
            actualValue
        }
    }
    
    // 监听滚动状态，实现自动吸附
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrollInProgress ->
                if (scrollInProgress) {
                    isScrolling = true
                    lastScrollTime = System.currentTimeMillis()
                } else {
                    isScrolling = false
                    // 延迟一点时间确保滚动完全停止
                    delay(50)
                    if (!listState.isScrollInProgress && 
                        System.currentTimeMillis() - lastScrollTime > 100) {
                        // 执行自动吸附
                        val firstVisibleIndex = listState.firstVisibleItemIndex
                        val firstVisibleOffset = listState.firstVisibleItemScrollOffset
                        
                        val targetIndex = if (firstVisibleOffset > itemHeight.value / 2) {
                            firstVisibleIndex + 1
                        } else {
                            firstVisibleIndex
                        }
                        
                        listState.animateScrollToItem(targetIndex)
                    }
                }
            }
    }
    
    // 点击时滚动到中心位置的函数
    val scrollToCenter: (Int) -> Unit = { targetValue ->
        scope.launch {
            val currentCenterIndex = listState.firstVisibleItemIndex + halfVisibleItems
            val currentCenterValue = (currentCenterIndex % valueRange.count()).let { index ->
                if (index < 0) index + valueRange.count() else index
            } + valueRange.first
            
            val offset = targetValue - currentCenterValue
            val targetIndex = listState.firstVisibleItemIndex + offset
            
            listState.animateScrollToItem(targetIndex)
        }
    }
    
    // 监听选中值变化
    LaunchedEffect(currentIndex) {
        if (currentIndex != selectedValue && !isScrolling) {
            onValueChanged(currentIndex)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .height(itemHeight * visibleItemsCount)
                .width(80.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.height(itemHeight * visibleItemsCount)
            ) {
                items(totalItems) { index ->
                    val value = (index % valueRange.count()) + valueRange.first
                    val itemIndex = index - listState.firstVisibleItemIndex
                    
                    // 计算距离中心的偏移量
                    val distanceFromCenter = kotlin.math.abs(itemIndex - halfVisibleItems)
                    val isSelected = distanceFromCenter == 0 && value == currentIndex
                    
                    // 根据距离中心的位置计算透明度和缩放
                    val alpha by animateFloatAsState(
                        targetValue = when (distanceFromCenter) {
                            0 -> 1f
                            1 -> 0.7f
                            2 -> 0.4f
                            else -> 0.2f
                        },
                        label = "alpha"
                    )
                    
                    val scale by animateFloatAsState(
                        targetValue = when (distanceFromCenter) {
                            0 -> 1f
                            1 -> 0.8f
                            else -> 0.6f
                        },
                        label = "scale"
                    )
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth()
                            .alpha(alpha)
                            .clickable {
                                onValueChanged(value)
                                scrollToCenter(value)
                            }
                    ) {
                        Text(
                            text = value.toString().padStart(2, '0'),
                            fontSize = (24 * scale).sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // 添加选中区域的背景
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(itemHeight)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            
            // 添加顶部和底部的渐变遮罩
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .height(itemHeight * 2)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                Color.Transparent
                            )
                        )
                    )
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(itemHeight * 2)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
} 