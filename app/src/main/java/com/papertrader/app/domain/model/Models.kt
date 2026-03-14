package com.papertrader.app.domain.model

/**
 * Domain model representing the user's virtual cash balance.
 */
data class UserWallet(
    val balance: Double
)

/**
 * Domain model for a stock the user currently holds.
 */
data class Holding(
    val ticker: String,
    val quantity: Int,
    val averageBuyPrice: Double
) {
    val totalInvested: Double
        get() = quantity * averageBuyPrice
}

/**
 * Domain model for a historical trade.
 */
data class Transaction(
    val id: Long = 0,
    val ticker: String,
    val type: TransactionType,
    val quantity: Int,
    val price: Double,
    val timestamp: Long
)

enum class TransactionType {
    BUY, SELL
}

/**
 * Domain model representing a live fetched stock price.
 */
data class StockQuote(
    val ticker: String,
    val name: String,
    val price: Double
)
