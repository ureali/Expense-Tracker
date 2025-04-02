package com.example.mad_411_assignments.network

import retrofit2.http.GET

interface CurrencyApiService {
    @GET("random")
    suspend fun getQuotes(): List<Currency>
}