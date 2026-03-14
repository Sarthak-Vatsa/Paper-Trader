package com.papertrader.app.data.remote.api

import com.papertrader.app.data.remote.dto.AlphaVantageGlobalQuoteResponse
import com.papertrader.app.data.remote.dto.AlphaVantageTimeSeriesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApiService {
    
    /**
     * Fetches simple live quote from Alpha Vantage.
     * Maps to function=GLOBAL_QUOTE
     */
    @GET("query?function=GLOBAL_QUOTE")
    suspend fun getGlobalQuote(
        @Query("symbol") symbol: String
    ): Response<AlphaVantageGlobalQuoteResponse>

    /**
     * Fetches daily historical time series data for Candlestick charts.
     * Maps to function=TIME_SERIES_DAILY
     */
    @GET("query?function=TIME_SERIES_DAILY")
    suspend fun getTimeSeriesDaily(
        @Query("symbol") symbol: String,
        @Query("outputsize") outputSize: String = "compact" // "compact" returns last 100 days
    ): Response<AlphaVantageTimeSeriesResponse>
}
