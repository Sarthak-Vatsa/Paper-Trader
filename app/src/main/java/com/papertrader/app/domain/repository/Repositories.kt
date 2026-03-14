package com.papertrader.app.domain

interface WalletRepository {
    fun getWalletFlow(): kotlinx.coroutines.flow.Flow<com.papertrader.app.domain.model.UserWallet>
    suspend fun updateBalance(newBalance: Double)
}

interface HoldingRepository {
    fun getAllHoldingsFlow(): kotlinx.coroutines.flow.Flow<List<com.papertrader.app.domain.model.Holding>>
    suspend fun getHolding(ticker: String): com.papertrader.app.domain.model.Holding?
    suspend fun upsertHolding(holding: com.papertrader.app.domain.model.Holding)
    suspend fun deleteHolding(ticker: String)
}

interface TransactionRepository {
    fun getAllTransactionsFlow(): kotlinx.coroutines.flow.Flow<List<com.papertrader.app.domain.model.Transaction>>
    suspend fun insertTransaction(transaction: com.papertrader.app.domain.model.Transaction)
}

interface StockRepository {
    /**
     * Result is used to capture success or network/parsing failures.
     */
    suspend fun getLiveQuote(ticker: String): Result<com.papertrader.app.domain.model.StockQuote>
    suspend fun getTimeSeries(ticker: String): Result<com.papertrader.app.data.remote.dto.AlphaVantageTimeSeriesResponse>
}
