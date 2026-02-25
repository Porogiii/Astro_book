package com.example.astro_book.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class NewsViewModel : ViewModel() {
    private val allNews = generateNews()
    private val _displayedNews = mutableStateOf(allNews.shuffled().take(4))
    val displayedNews: State<List<NewsItem>> = _displayedNews

    private val _newsLikes = mutableStateOf(allNews.associate { it.id to 0 })
    val newsLikes: State<Map<Int, Int>> = _newsLikes

    init {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                val currentNews = _displayedNews.value.toMutableList()
                val indexToReplace = Random.nextInt(4)
                var newNews = allNews.random()
                if (newNews !in currentNews)
                    currentNews[indexToReplace] = newNews
                else newNews = allNews.random()
                _displayedNews.value = currentNews
            }
        }
    }

    fun incrementLikes(newsId: Int) {
        _newsLikes.value = _newsLikes.value.toMutableMap().apply {
            this[newsId] = (this[newsId] ?: 0) + 1
        }
    }

    data class NewsItem(val id: Int, val title: String)
}

private fun generateNews() = listOf(
    NewsViewModel.NewsItem(1, "Открытие новой экзопланеты в системе Проксима Центавра"),
    NewsViewModel.NewsItem(2, "Телескоп Джеймс Уэбб обнаружил галактику возрастом 13.5 млрд лет"),
    NewsViewModel.NewsItem(3, "Астрономы зафиксировали слияние двух черных дыр"),
    NewsViewModel.NewsItem(4, "Новые данные о темной материи от коллайдера LHC"),
    NewsViewModel.NewsItem(5, "Марсоход Perseverance обнаружил органические молекулы"),
    NewsViewModel.NewsItem(6, "Суперлуние 9 февраля 2026: лучшие фото и факты"),
    NewsViewModel.NewsItem(7, "Открыт квазар с массой 17 миллиардов Солнц"),
    NewsViewModel.NewsItem(8, "Нейтронные звезды: рекордная плотность и магнитные поля"),
    NewsViewModel.NewsItem(9, "Возможная жизнь обнаружена в подледных океанах Европы"),
    NewsViewModel.NewsItem(10, "Гравитационные волны помогли найти источник гамма-всплесков")
)
