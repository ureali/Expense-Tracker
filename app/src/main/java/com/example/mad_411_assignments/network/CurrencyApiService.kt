package com.example.mad_411_assignments.network

import retrofit2.http.GET

interface CurrencyApiService {
    @GET("currencies.json")
    suspend fun getCurrencies(): Map<String, String>
}