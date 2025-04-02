package com.example.mad_411_assignments.network

import com.example.mad_411_assignments.model.Conversion
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApiService {
    @GET("currencies.json")
    suspend fun getCurrencies(): Map<String, String>

    @GET("currencies/{currency}.json")
    // the api is completely unstructured, but it's too late to change it
    // i tried to use the Conversion class, but the api returns different json formats depending on currency
    // Any it is :-(
    suspend fun getConversionRates(@Path("currency") currencyCode:String): Map<String, Any>
//    suspend fun getConversionRates(@Path("currency") currencyCode:String): Conversion

}