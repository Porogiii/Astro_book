package com.example.astro_book.ui

import android.opengl.GLSurfaceView
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.astro_book.opengl.MoonRenderer
import com.example.astro_book.opengl.OpenGLView
import com.example.astro_book.ui.theme.*
import com.example.astro_book.viewmodel.NewsViewModel

@Composable
fun NewsScreen(viewModel: NewsViewModel = viewModel()) {
    val displayedNews by viewModel.displayedNews
    val newsLikes by viewModel.newsLikes
    var glView by remember { mutableStateOf<OpenGLView?>(null) }
    var showMoonDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                OpenGLView(context).also { glView = it }
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (displayedNews.size > 0) {
                        NewsQuarter(
                            news = displayedNews[0],
                            likes = newsLikes[displayedNews[0].id] ?: 0,
                            onLike = { viewModel.incrementLikes(displayedNews[0].id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (displayedNews.size > 1) {
                        NewsQuarter(
                            news = displayedNews[1],
                            likes = newsLikes[displayedNews[1].id] ?: 0,
                            onLike = { viewModel.incrementLikes(displayedNews[1].id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (displayedNews.size > 2) {
                        NewsQuarter(
                            news = displayedNews[2],
                            likes = newsLikes[displayedNews[2].id] ?: 0,
                            onLike = { viewModel.incrementLikes(displayedNews[2].id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (displayedNews.size > 3) {
                        NewsQuarter(
                            news = displayedNews[3],
                            likes = newsLikes[displayedNews[3].id] ?: 0,
                            onLike = { viewModel.incrementLikes(displayedNews[3].id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { glView?.renderer?.selectPrevPlanet() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Влево",
                        tint = LightBlue
                    )
                }

                Button(
                    onClick = {
                        val name = glView?.renderer?.getSelectedPlanetName()
                        if (name == "Moon") {
                            showMoonDialog = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Информация",
                        tint = AccentBlue
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Инфо", color = StarWhite)
                }

                Button(
                    onClick = { glView?.renderer?.selectNextPlanet() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Вправо",
                        tint = LightBlue
                    )
                }
            }
        }

        if (showMoonDialog) {
            MoonInfoDialog(onDismiss = { showMoonDialog = false })
        }
    }
}

@Composable
fun MoonInfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Луна",
                    color = StarWhite,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )

                AndroidView(
                    factory = { context ->
                        GLSurfaceView(context).apply {
                            setEGLContextClientVersion(2)
                            setRenderer(MoonRenderer(context))
                            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Text("Закрыть", color = StarWhite)
                }
            }
        }
    }
}

@Composable
fun NewsQuarter(
    news: NewsViewModel.NewsItem,
    likes: Int,
    onLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .border(2.dp, LightBlue.copy(alpha = 0.3f), MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = Navy.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = news.title,
                    color = StarWhite,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.15
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxWidth()
                    .clickable { onLike() }
                    .background(DarkNavy.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Лайк",
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = likes.toString(),
                        color = AccentBlue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
