package com.example.countdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.countdown.data.CountdownRepository
import com.example.countdown.ui.screens.CountdownScreen
import com.example.countdown.ui.theme.CountdownTheme
import com.example.countdown.viewmodel.CountdownViewModel
import com.example.countdown.viewmodel.CountdownViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var repository: CountdownRepository
    private val viewModel: CountdownViewModel by viewModels {
        CountdownViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化Repository
        repository = CountdownRepository(applicationContext)
        
        setContent {
            CountdownTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CountdownScreen(viewModel = viewModel)
                }
            }
        }
    }
}

