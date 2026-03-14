package com.papertrader.app.domain.usecase

import com.papertrader.app.domain.HoldingRepository
import com.papertrader.app.domain.TransactionRepository
import com.papertrader.app.domain.WalletRepository
import com.papertrader.app.domain.model.Holding
import com.papertrader.app.domain.model.StockQuote
import com.papertrader.app.domain.model.Transaction
import com.papertrader.app.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SellStockUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val holdingRepository: HoldingRepository,
    private val transactionRepository: TransactionRepository
) {
    /**
     * Executes a SELL order.
     * @throws Exception if user tries to sell more shares than they own.
     */
    suspend operator fun invoke(quote: StockQuote, quantity: Int) {
        require(quantity > 0) { "Quantity must be greater than 0" }

        val existingHolding = holdingRepository.getHolding(quote.ticker)
            ?: throw InsufficientSharesException("You don't own any shares of ${quote.ticker}")

        if (existingHolding.quantity < quantity) {
            throw InsufficientSharesException("You only own ${existingHolding.quantity} shares of ${quote.ticker}")
        }

        val totalValue = quote.price * quantity
        val currentWallet = walletRepository.getWalletFlow().first()

        // Add to wallet
        val newBalance = currentWallet.balance + totalValue
        walletRepository.updateBalance(newBalance)

        // Update or delete holding
        val remainingQty = existingHolding.quantity - quantity
        if (remainingQty == 0) {
            holdingRepository.deleteHolding(quote.ticker)
        } else {
            val updatedHolding = Holding(
                ticker = existingHolding.ticker,
                quantity = remainingQty,
                averageBuyPrice = existingHolding.averageBuyPrice // Average price remains identical on selling
            )
            holdingRepository.upsertHolding(updatedHolding)
        }

        // Record transaction
        val transaction = Transaction(
            ticker = quote.ticker,
            type = TransactionType.SELL,
            quantity = quantity,
            price = quote.price,
            timestamp = System.currentTimeMillis()
        )
        transactionRepository.insertTransaction(transaction)
    }
}

class InsufficientSharesException(message: String) : Exception(message)
