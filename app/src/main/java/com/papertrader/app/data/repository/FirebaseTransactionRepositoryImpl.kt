package com.papertrader.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.papertrader.app.domain.TransactionRepository
import com.papertrader.app.domain.model.Transaction
import com.papertrader.app.domain.model.TransactionType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseTransactionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TransactionRepository {

    private fun txCol(uid: String) = firestore
        .collection("users").document(uid)
        .collection("transactions")

    override fun getAllTransactionsFlow(): Flow<List<Transaction>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = txCol(uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val transactions = snapshot?.documents?.mapNotNull { doc ->
                        val ticker = doc.getString("ticker") ?: return@mapNotNull null
                        val typeStr = doc.getString("type") ?: return@mapNotNull null
                        val quantity = doc.getLong("quantity")?.toInt() ?: return@mapNotNull null
                        val price = doc.getDouble("price") ?: return@mapNotNull null
                        val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
                        Transaction(
                            ticker = ticker,
                            type = if (typeStr == "BUY") TransactionType.BUY else TransactionType.SELL,
                            quantity = quantity,
                            price = price,
                            timestamp = timestamp
                        )
                    } ?: emptyList()
                    trySend(transactions)
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        val uid = auth.currentUser?.uid ?: return
        val data = mapOf(
            "ticker" to transaction.ticker,
            "type" to transaction.type.name,
            "quantity" to transaction.quantity,
            "price" to transaction.price,
            "timestamp" to transaction.timestamp
        )
        txCol(uid).add(data).await()
    }
}
