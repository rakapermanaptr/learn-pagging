package com.example.learnpagging.model

import com.google.gson.annotations.SerializedName

data class TheMovieDb(
        @SerializedName("results") val results: List<Result>,
        @SerializedName("page") val page: Int,
        @SerializedName("total_results") val totalResults: Int,
        @SerializedName("dates") val dates: Dates,
        @SerializedName("total_pages") val totalPages: Int
)