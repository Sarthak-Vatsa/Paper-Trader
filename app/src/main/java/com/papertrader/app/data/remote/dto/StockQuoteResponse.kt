package com.papertrader.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// ----------------- GLOBAL QUOTE Endpoint DTOs -----------------

data class AlphaVantageGlobalQuoteResponse(
    @SerializedName("Global Quote")
    val globalQuote: GlobalQuoteDto?
)

data class GlobalQuoteDto(
    @SerializedName("01. symbol")
    val symbol: String?,
    @SerializedName("05. price")
    val price: String?,
    @SerializedName("09. change")
    val change: String?,
    @SerializedName("10. change percent")
    val changePercent: String?
)

// ----------------- TIME SERIES DAILY Endpoint DTOs -----------------

data class AlphaVantageTimeSeriesResponse(
    @SerializedName("Meta Data")
    val metaData: TimeSeriesMetaDataDto?,
    
    // Alpha Vantage returns historical data as dynamic keys (e.g., "2024-05-15": { "1. open": "...", ... })
    // Using Map<String, DailyCandleDto> to easily parse these dynamic JSON configurations via Gson.
    @SerializedName("Time Series (Daily)")
    val timeSeries: Map<String, DailyCandleDto>?
)

data class TimeSeriesMetaDataDto(
    @SerializedName("2. Symbol")
    val symbol: String?,
    @SerializedName("3. Last Refreshed")
    val lastRefreshed: String?
)

data class DailyCandleDto(
    @SerializedName("1. open")
    val open: String?,
    @SerializedName("2. high")
    val high: String?,
    @SerializedName("3. low")
    val low: String?,
    @SerializedName("4. close")
    val close: String?,
    @SerializedName("5. volume")
    val volume: String?
)
