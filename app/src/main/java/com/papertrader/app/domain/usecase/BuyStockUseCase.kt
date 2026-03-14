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

class BuyStockUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val holdingRepository: HoldingRepository,
    private val transactionRepository: TransactionRepository
) {
    /**
     * Executes a BUY order.
     * @throws Exception if wallet has insufficient funds.
     */
    suspend operator fun invoke(quote: StockQuote, quantity: Int) {
        require(quantity > 0) { "Quantity must be greater than 0" }

        val totalCost = quote.price * quantity
        val currentWallet = walletRepository.getWalletFlow().first()

        if (currentWallet.balance < totalCost) {
            throw InsufficientFundsException("Insufficient funds. You need ₹$totalCost but have ₹${currentWallet.balance}")
        }

        // Deduct from wallet
        val newBalance = currentWallet.balance - totalCost
        walletRepository.updateBalance(newBalance)

        // Update holding (calculate new average buy price)
        val existingHolding = holdingRepository.getHolding(quote.ticker)
        val newHolding = if (existingHolding != null) {
            val totalQty = existingHolding.quantity + quantity
            val totalInvested = existingHolding.totalInvested + totalCost
            Holding(
                ticker = quote.ticker,
                quantity = totalQty,
                averageBuyPrice = totalInvested / totalQty
            )
        } else {
            Holding(
                ticker = quote.ticker,
                quantity = quantity,
                averageBuyPrice = quote.price
            )
        }
        holdingRepository.upsertHolding(newHolding)

        // Record transaction
        val transaction = Transaction(
            ticker = quote.ticker,
            type = TransactionType.BUY,
            quantity = quantity,
            price = quote.price,
            timestamp = System.currentTimeMillis()
        )
        transactionRepository.insertTransaction(transaction)
    }
}

class InsufficientFundsException(message: String) : Exception(message)
