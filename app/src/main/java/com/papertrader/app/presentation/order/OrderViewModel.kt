package com.papertrader.app.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.papertrader.app.domain.HoldingRepository
import com.papertrader.app.domain.StockRepository
import com.papertrader.app.domain.model.StockQuote
import com.papertrader.app.domain.usecase.BuyStockUseCase
import com.papertrader.app.domain.usecase.SellStockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderUiState(
    val ticker: String = "",
    val quote: StockQuote? = null,
    val isLoadingQuote: Boolean = false,
    val isExecutingTrade: Boolean = false,
    val quantityInput: String = "1",
    val ownedQuantity: Int = 0
)

sealed class OrderSideEffect {
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : OrderSideEffect()
    object NavigateBack : OrderSideEffect()
}

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val holdingRepository: HoldingRepository,
    private val buyStockUseCase: BuyStockUseCase,
    private val sellStockUseCase: SellStockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<OrderSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    fun initForTicker(ticker: String) {
        _uiState.update { it.copy(ticker = ticker, isLoadingQuote = true) }
        viewModelScope.launch {
            // Fetch live quote
            stockRepository.getLiveQuote(ticker).onSuccess { quote ->
                _uiState.update { it.copy(quote = quote, isLoadingQuote = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoadingQuote = false) }
                _sideEffect.emit(OrderSideEffect.ShowSnackbar("Wait to fetch price: ${e.message}", true))
            }

            // Fetch owned quantity
            val holding = holdingRepository.getHolding(ticker)
            _uiState.update { it.copy(ownedQuantity = holding?.quantity ?: 0) }
        }
    }

    fun onQuantityChanged(qty: String) {
        // Only allow numbers
        if (qty.isEmpty() || qty.all { it.isDigit() }) {
            _uiState.update { it.copy(quantityInput = qty) }
        }
    }

    fun executeTrade(isBuy: Boolean) {
        val qty = _uiState.value.quantityInput.toIntOrNull() ?: 0
        val quote = _uiState.value.quote

        if (qty <= 0) {
            viewModelScope.launch { _sideEffect.emit(OrderSideEffect.ShowSnackbar("Quantity must be at least 1", true)) }
            return
        }
        if (quote == null) {
            viewModelScope.launch { _sideEffect.emit(OrderSideEffect.ShowSnackbar("Live price not available yet", true)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExecutingTrade = true) }
            try {
                if (isBuy) {
                    buyStockUseCase(quote, qty)
                    _sideEffect.emit(OrderSideEffect.ShowSnackbar("Successfully bought $qty shares of ${quote.ticker}"))
                } else {
                    sellStockUseCase(quote, qty)
                    _sideEffect.emit(OrderSideEffect.ShowSnackbar("Successfully sold $qty shares of ${quote.ticker}"))
                }
                _sideEffect.emit(OrderSideEffect.NavigateBack)
            } catch (e: Exception) {
                _sideEffect.emit(OrderSideEffect.ShowSnackbar(e.message ?: "Trade failed", true))
            } finally {
                _uiState.update { it.copy(isExecutingTrade = false) }
            }
        }
    }
}
