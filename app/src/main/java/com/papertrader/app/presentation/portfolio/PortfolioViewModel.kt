package com.papertrader.app.presentation.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.papertrader.app.domain.HoldingRepository
import com.papertrader.app.domain.StockRepository
import com.papertrader.app.domain.WalletRepository
import com.papertrader.app.domain.model.Holding
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PortfolioUiState(
    val walletBalance: Double = 0.0,
    val totalInvested: Double = 0.0,
    val totalCurrentValue: Double = 0.0,
    val holdings: List<HoldingUiItem> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
) {
    val totalPnL: Double
        get() = totalCurrentValue - totalInvested
}

data class HoldingUiItem(
    val holding: Holding,
    val livePrice: Double? = null
) {
    val liveValue: Double?
        get() = livePrice?.let { it * holding.quantity }
        
    val livePnL: Double?
        get() = liveValue?.let { it - holding.totalInvested }
}

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    walletRepository: WalletRepository,
    holdingRepository: HoldingRepository,
    private val stockRepository: StockRepository
) : ViewModel() {

    // Cache of live prices fetched so we don't bombard Yahoo Finance unnecessarily
    // Key: Ticker, Value: Live Price
    private val livePricesCache = MutableStateFlow<Map<String, Double>>(emptyMap())

    val uiState: StateFlow<PortfolioUiState> = combine(
        walletRepository.getWalletFlow(),
        holdingRepository.getAllHoldingsFlow(),
        livePricesCache
    ) { wallet, holdings, prices ->
        
        // As new holdings show up, fetch their live prices asynchronously
        val unknownTickers = holdings.map { it.ticker }.filter { !prices.containsKey(it) }
        if (unknownTickers.isNotEmpty()) {
            fetchLivePrices(unknownTickers)
        }

        // Map to UI Items
        val uiItems = holdings.map { holding ->
            HoldingUiItem(
                holding = holding,
                livePrice = prices[holding.ticker]
            )
        }

        val totalInvested = uiItems.sumOf { it.holding.totalInvested }
        // For total current value, if a live price is missing, fallback to invested amount so P&L is 0 for that item rather than negative
        val totalCurrentValue = uiItems.sumOf { it.liveValue ?: it.holding.totalInvested }

        PortfolioUiState(
            walletBalance = wallet.balance,
            totalInvested = totalInvested,
            totalCurrentValue = totalCurrentValue,
            holdings = uiItems,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PortfolioUiState()
    )

    private fun fetchLivePrices(tickers: List<String>) {
        if (tickers.isEmpty()) return
        
        tickers.forEach { ticker ->
            viewModelScope.launch {
                stockRepository.getLiveQuote(ticker).onSuccess { quote ->
                    val updatedCache = livePricesCache.value.toMutableMap()
                    updatedCache[ticker] = quote.price
                    livePricesCache.value = updatedCache
                }
            }
        }
    }

    /** Clear cached prices to force a full refetch on next recomposition. */
    fun refresh() {
        livePricesCache.value = emptyMap()
    }
}
