# 📈 Paper Trader

A feature-rich **paper trading** Android application that lets you practice stock trading with virtual currency — no real money involved. Built with **Kotlin**, **Jetpack Compose**, and **Clean Architecture** principles.

---

## ✨ Features

- **Virtual Wallet** — Start with ₹5,00,000 of virtual cash to trade with
- **Live Market Data** — Real-time stock quotes powered by [Alpha Vantage](https://www.alphavantage.co/)
- **Candlestick Charts** — Custom-built Compose candlestick chart for visualizing stock price history
- **Buy & Sell Orders** — Execute paper trades at live market prices with built-in validation
- **Portfolio Tracking** — Monitor your holdings, average buy price, and real-time P&L
- **Watchlist** — Search and track your favourite stocks with live price updates
- **Transaction History** — Full log of all your trades
- **Authentication** — Secure sign-up & login via Firebase Auth
- **Cloud Sync** — Portfolio and wallet data synced with Firebase Firestore

---

## 🏗️ Architecture

The app follows **MVVM + Clean Architecture**, organized into clearly separated layers:

```
app/src/main/java/com/papertrader/app/
├── di/                 # Dagger Hilt dependency injection modules
├── data/
│   ├── remote/         # Retrofit API service & DTOs
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Domain models (Wallet, Holding, Transaction, StockQuote)
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Business logic (BuyStock, SellStock, etc.)
└── presentation/
    ├── auth/           # Login & Register screens + ViewModel
    ├── watchlist/      # Watchlist screen, search, candlestick chart
    ├── order/          # Order execution screen + ViewModel
    ├── portfolio/      # Portfolio screen + ViewModel
    ├── account/        # Account / settings screen
    └── navigation/     # Jetpack Compose Navigation graph
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Clean Architecture |
| **DI** | Dagger Hilt |
| **Networking** | Retrofit 2 + OkHttp |
| **Auth & Database** | Firebase Auth + Cloud Firestore |
| **Async** | Kotlin Coroutines + StateFlow |
| **Navigation** | Jetpack Compose Navigation |
| **Build System** | Gradle (Kotlin DSL) with Version Catalog |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 17**
- **Android SDK** with `compileSdk 35` and `minSdk 26`

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Sarthak-Vatsa/Paper-Trader.git
   cd Paper-Trader
   ```

2. **Add your API key**

   Obtain a free API key from [Alpha Vantage](https://www.alphavantage.co/support/#api-key) and add it to your `local.properties`:
   ```properties
   ALPHA_VANTAGE_API_KEY=your_api_key_here
   ```

3. **Firebase setup**

   The project uses Firebase Auth and Firestore. Place your own `google-services.json` in the `app/` directory. You can generate one from the [Firebase Console](https://console.firebase.google.com/).

4. **Build & Run**

   Open the project in Android Studio, sync Gradle, and run on an emulator or physical device.

---

## 📱 Screens

| Screen | Description |
|---|---|
| **Login / Register** | Firebase-powered authentication |
| **Watchlist** | Browse stocks, search by ticker, view candlestick charts |
| **Order** | Place buy/sell orders at live market prices |
| **Portfolio** | View holdings, wallet balance, and real-time P&L |
| **Account** | User profile and sign-out |

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
