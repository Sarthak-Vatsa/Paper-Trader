package com.papertrader.app.data.repository

import com.papertrader.app.data.remote.api.StockApiService
import com.papertrader.app.data.remote.dto.AlphaVantageTimeSeriesResponse
import com.papertrader.app.domain.StockRepository
import com.papertrader.app.domain.model.StockQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val api: StockApiService
) : StockRepository {

    override suspend fun getLiveQuote(ticker: String): Result<StockQuote> {
        return withContext(Dispatchers.IO) {
            try {
                // Alpha Vantage Global Quote
                val response = api.getGlobalQuote(symbol = ticker)
                if (response.isSuccessful) {
                    val body = response.body()
                    val quote = body?.globalQuote
                    
                    if (quote?.price != null && quote.symbol != null) {
                        Result.success(
                            StockQuote(
                                ticker = quote.symbol,
                                name = quote.symbol, // Alpha Vantage Global Quote doesn't return full company name
                                price = quote.price.toDoubleOrNull() ?: 0.0
                            )
                        )
                    } else {
                        Result.failure(Exception("Ticker not found or API Rate Limited (25/day limit)"))
                    }
                } else {
                    Result.failure(Exception("Network error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getTimeSeries(ticker: String): Result<AlphaVantageTimeSeriesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getTimeSeriesDaily(symbol = ticker)
                if (response.isSuccessful && response.body()?.timeSeries != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Could not fetch Time Series or API Rate Limited"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
