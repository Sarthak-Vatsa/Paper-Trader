package com.papertrader.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.papertrader.app.domain.HoldingRepository
import com.papertrader.app.domain.model.Holding
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseHoldingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : HoldingRepository {

    private fun holdingsCol(uid: String) = firestore
        .collection("users").document(uid)
        .collection("holdings")

    override fun getAllHoldingsFlow(): Flow<List<Holding>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = holdingsCol(uid).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val holdings = snapshot?.documents?.mapNotNull { doc ->
                    val ticker = doc.getString("ticker") ?: return@mapNotNull null
                    val quantity = doc.getLong("quantity")?.toInt() ?: return@mapNotNull null
                    val avgPrice = doc.getDouble("averageBuyPrice") ?: return@mapNotNull null
                    Holding(ticker = ticker, quantity = quantity, averageBuyPrice = avgPrice)
                } ?: emptyList()
                trySend(holdings)
            }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun getHolding(ticker: String): Holding? {
        val uid = auth.currentUser?.uid ?: return null
        val snapshot = holdingsCol(uid).document(ticker).get().await()
        if (!snapshot.exists()) return null
        val quantity = snapshot.getLong("quantity")?.toInt() ?: return null
        val avgPrice = snapshot.getDouble("averageBuyPrice") ?: return null
        return Holding(ticker = ticker, quantity = quantity, averageBuyPrice = avgPrice)
    }

    override suspend fun upsertHolding(holding: Holding) {
        val uid = auth.currentUser?.uid ?: return
        val data = mapOf(
            "ticker" to holding.ticker,
            "quantity" to holding.quantity,
            "averageBuyPrice" to holding.averageBuyPrice
        )
        holdingsCol(uid).document(holding.ticker).set(data, SetOptions.merge()).await()
    }

    override suspend fun deleteHolding(ticker: String) {
        val uid = auth.currentUser?.uid ?: return
        holdingsCol(uid).document(ticker).delete().await()
    }
}
