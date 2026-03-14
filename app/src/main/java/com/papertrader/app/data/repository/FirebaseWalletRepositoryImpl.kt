package com.papertrader.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.papertrader.app.domain.WalletRepository
import com.papertrader.app.domain.model.UserWallet
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseWalletRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : WalletRepository {

    private fun walletDoc() = firestore
        .collection("users")
        .document(auth.currentUser!!.uid)
        .collection("wallet")
        .document("data")

    override fun getWalletFlow(): Flow<UserWallet> {
        val uid = auth.currentUser?.uid ?: return flowOf(UserWallet(0.0))
        return callbackFlow {
            val docRef = firestore
                .collection("users").document(uid)
                .collection("wallet").document("data")

            // Initialise wallet on first login if it doesn't exist
            docRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    docRef.set(mapOf("balance" to 500000.0))
                }
            }

            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val balance = snapshot?.getDouble("balance") ?: 500000.0
                trySend(UserWallet(balance = balance))
            }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun updateBalance(newBalance: Double) {
        walletDoc().set(mapOf("balance" to newBalance), SetOptions.merge()).await()
    }
}
