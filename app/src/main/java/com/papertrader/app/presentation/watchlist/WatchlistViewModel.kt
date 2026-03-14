package com.papertrader.app.presentation.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.papertrader.app.domain.StockRepository
import com.papertrader.app.domain.model.StockQuote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WatchlistUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val quote: StockQuote? = null,
    val chartData: List<CandleData> = emptyList(),
    val error: String? = null,
    val recentlyViewed: List<StockQuote> = emptyList(),
    val activeHomeTab: Int = 0 // 0 = Top Movers, 1 = Recently Viewed
)

/** Static curated market movers — refreshed each session. */
data class MarketMover(
    val ticker: String,
    val name: String,
    val price: String,
    val change: String,
    val changePercent: String
)

val TOP_GAINERS = listOf(
    MarketMover("NVDA", "NVIDIA Corp", "\$875.40", "+\$38.20", "+4.56%"),
    MarketMover("META", "Meta Platforms", "\$511.20", "+\$18.65", "+3.79%"),
    MarketMover("AMD", "Advanced Micro", "\$178.30", "+\$5.80", "+3.36%"),
    MarketMover("TSLA", "Tesla Inc", "\$203.50", "+\$5.90", "+2.98%"),
    MarketMover("AMZN", "Amazon.com", "\$192.80", "+\$4.20", "+2.23%"),
)

val TOP_LOSERS = listOf(
    MarketMover("INTC", "Intel Corp", "\$31.20", "-\$1.80", "-5.45%"),
    MarketMover("BABA", "Alibaba Group", "\$77.50", "-\$3.40", "-4.20%"),
    MarketMover("PFE", "Pfizer Inc", "\$27.10", "-\$0.90", "-3.21%"),
    MarketMover("DIS", "Disney Co.", "\$94.30", "-\$2.60", "-2.68%"),
    MarketMover("NFLX", "Netflix Inc", "\$602.10", "-\$13.50", "-2.19%"),
)

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, error = null) }

        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(quote = null, chartData = emptyList(), isLoading = false) }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Debounce user typing
            delay(1000)

            val formattedQuery = query.trim().uppercase()

            val seriesResult = stockRepository.getTimeSeries(formattedQuery)

            if (seriesResult.isSuccess) {
                val timeSeries = seriesResult.getOrNull()?.timeSeries
                val candles = timeSeries?.mapNotNull { (dateStr, dto) ->
                    try {
                        CandleData(
                            date = dateStr,
                            open = dto.open?.toFloat() ?: 0f,
                            high = dto.high?.toFloat() ?: 0f,
                            low = dto.low?.toFloat() ?: 0f,
                            close = dto.close?.toFloat() ?: 0f,
                            volume = dto.volume?.toLong() ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                }?.sortedBy { it.date }?.takeLast(60) ?: emptyList()

                val latestCandle = candles.lastOrNull()
                val generatedQuote = if (latestCandle != null) {
                    StockQuote(
                        ticker = formattedQuery,
                        name = formattedQuery,
                        price = latestCandle.close.toDouble()
                    )
                } else null

                // Track recently viewed (max 5, no duplicates)
                if (generatedQuote != null) {
                    _uiState.update { current ->
                        val updated = (listOf(generatedQuote) + current.recentlyViewed
                            .filter { it.ticker != generatedQuote.ticker })
                            .take(5)
                        current.copy(recentlyViewed = updated)
                    }
                }

                _uiState.update {
                    it.copy(
                        quote = generatedQuote,
                        chartData = candles,
                        isLoading = false,
                        error = if (candles.isEmpty()) "Ticker not found or Rate Limit Hit." else null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        quote = null,
                        chartData = emptyList(),
                        isLoading = false,
                        error = seriesResult.exceptionOrNull()?.message
                            ?: "Failed to fetch data or Rate Limit Hit."
                    )
                }
            }
        }
    }

    fun onHomeTabSelected(index: Int) {
        _uiState.update { it.copy(activeHomeTab = index) }
    }
}
