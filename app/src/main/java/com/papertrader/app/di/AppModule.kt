package com.papertrader.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.papertrader.app.data.remote.api.StockApiService
import com.papertrader.app.data.repository.FirebaseAuthRepositoryImpl
import com.papertrader.app.data.repository.FirebaseHoldingRepositoryImpl
import com.papertrader.app.data.repository.FirebaseTransactionRepositoryImpl
import com.papertrader.app.data.repository.FirebaseWalletRepositoryImpl
import com.papertrader.app.data.repository.StockRepositoryImpl
import com.papertrader.app.domain.AuthRepository
import com.papertrader.app.domain.HoldingRepository
import com.papertrader.app.domain.StockRepository
import com.papertrader.app.domain.TransactionRepository
import com.papertrader.app.domain.WalletRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Firebase ---

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // --- Auth ---

    @Provides
    @Singleton
    fun provideAuthRepository(impl: FirebaseAuthRepositoryImpl): AuthRepository = impl

    // --- Wallet ---

    @Provides
    @Singleton
    fun provideWalletRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): WalletRepository = FirebaseWalletRepositoryImpl(firestore, auth)

    // --- Holdings ---

    @Provides
    @Singleton
    fun provideHoldingRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): HoldingRepository = FirebaseHoldingRepositoryImpl(firestore, auth)

    // --- Transactions ---

    @Provides
    @Singleton
    fun provideTransactionRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): TransactionRepository = FirebaseTransactionRepositoryImpl(firestore, auth)

    // --- Stock / Remote ---

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val urlWithKey = originalRequest.url.newBuilder()
                    .addQueryParameter("apikey", "RKTXR0II9WJ2CD5L")
                    .build()
                val newRequest = originalRequest.newBuilder()
                    .url(urlWithKey)
                    .build()
                chain.proceed(newRequest)
            }
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.alphavantage.co/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideStockApiService(retrofit: Retrofit): StockApiService =
        retrofit.create(StockApiService::class.java)

    @Provides
    @Singleton
    fun provideStockRepository(api: StockApiService): StockRepository =
        StockRepositoryImpl(api)
}
