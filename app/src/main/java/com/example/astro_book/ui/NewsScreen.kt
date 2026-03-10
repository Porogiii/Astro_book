package com.example.astro_book.ui

import android.opengl.GLSurfaceView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.astro_book.opengl.MoonRenderer
import com.example.astro_book.opengl.NeptuneRenderer
import com.example.astro_book.opengl.OpenGLView
import com.example.astro_book.ui.theme.*
import com.example.astro_book.viewmodel.NewsViewModel

@Composable
fun NewsScreen(viewModel: NewsViewModel = viewModel()) {
    val displayedNews by viewModel.displayedNews
    val newsLikes by viewModel.newsLikes
    var glView by remember { mutableStateOf<OpenGLView?>(null) }
    var selectedPlanetName by remember { mutableStateOf<String?>(null) }

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
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (displayedNews.size > 0) NewsQuarter(
                        news = displayedNews[0],
                        likes = newsLikes[displayedNews[0].id] ?: 0,
                        onLike = { viewModel.incrementLikes(displayedNews[0].id) },
                        modifier = Modifier.weight(1f)
                    )
                    if (displayedNews.size > 1) NewsQuarter(
                        news = displayedNews[1],
                        likes = newsLikes[displayedNews[1].id] ?: 0,
                        onLike = { viewModel.incrementLikes(displayedNews[1].id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (displayedNews.size > 2) NewsQuarter(
                        news = displayedNews[2],
                        likes = newsLikes[displayedNews[2].id] ?: 0,
                        onLike = { viewModel.incrementLikes(displayedNews[2].id) },
                        modifier = Modifier.weight(1f)
                    )
                    if (displayedNews.size > 3) NewsQuarter(
                        news = displayedNews[3],
                        likes = newsLikes[displayedNews[3].id] ?: 0,
                        onLike = { viewModel.incrementLikes(displayedNews[3].id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { glView?.renderer?.selectPrevPlanet() },
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Влево", tint = LightBlue)
                }

                Button(
                    onClick = {
                        selectedPlanetName = glView?.renderer?.getSelectedPlanetName()
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Информация", tint = AccentBlue)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Инфо", color = StarWhite)
                }

                Button(
                    onClick = { glView?.renderer?.selectNextPlanet() },
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Вправо", tint = LightBlue)
                }
            }
        }

        selectedPlanetName?.let { name ->
            val info = planetInfoList.find { it.name == name }
            if (info != null) {
                if (name == "Moon") {
                    MoonInfoDialog(onDismiss = { selectedPlanetName = null })
                } else {
                    PlanetInfoDialog(
                        info = info,
                        onDismiss = { selectedPlanetName = null }
                    )
                }
            }
        }
    }
}

@Composable
fun PlanetInfoDialog(info: PlanetInfo, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = when (info.name) {
                        "Mercury" -> "Меркурий"
                        "Venus"   -> "Венера"
                        "Earth"   -> "Земля"
                        "Mars"    -> "Марс"
                        "Jupiter" -> "Юпитер"
                        "Saturn"  -> "Сатурн"
                        "Uranus"  -> "Уран"
                        "Neptune" -> "Нептун"
                        else      -> info.name
                    },
                    color = StarWhite,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )

                if (info.name == "Neptune") {
                    AndroidView(
                        factory = { context ->
                            GLSurfaceView(context).apply {
                                setEGLContextClientVersion(2)
                                setRenderer(NeptuneRenderer(context))
                                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = info.imageRes),
                        contentDescription = info.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = info.description,
                        color = StarWhite.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                    )
                }

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
fun MoonInfoDialog(onDismiss: () -> Unit) {
    val moonInfo = planetInfoList.find { it.name == "Moon" }!!

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
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
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(8.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = moonInfo.description,
                        color = StarWhite.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                    )
                }

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
