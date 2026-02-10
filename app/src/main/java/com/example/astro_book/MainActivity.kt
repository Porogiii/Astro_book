package com.example.astro_book

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.astro_book.ui.theme.Astro_bookTheme
import com.example.astro_book.ui.NewsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Astro_bookTheme {
                NewsScreen()
            }
        }
    }
}
