package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class CineHDProvider : MainAPI() {
    override var mainUrl = "https://cinehd.app"
    override var name = "CineHD"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override var lang = "en"
    override val hasMainPage = true

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse? {
        val document = app.get(mainUrl).document
        // This targets the movie items on the home grid
        val items = document.select(".post-item, .card, .entry-title a").mapNotNull {
            val title = it.text()
            val href = it.attr("href") ?: return@mapNotNull null
            val poster = it.select("img").attr("src")
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = poster
            }
        }
        return newHomePageResponse("Latest Movies", items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=${query}"
        val document = app.get(url).document
        return document.select(".search-result, .card, .entry-title a").mapNotNull {
            val title = it.text()
            val href = it.attr("href") ?: return@mapNotNull null
            val poster = it.select("img").attr("src")
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = poster
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.select(".movie-title, h1.entry-title").text()
        val poster = document.select(".poster img, .wp-post-image").attr("src")
        val description = document.select(".description, .entry-content p").text()
        
        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster
            this.plot = description
        }
    }
}
