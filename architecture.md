# Architecture Framework: Paper Trading Android Application

## 1. Directives (Where files are stored & Core Stack)
* **Environment:** Android Studio project structure.
* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Strictly no XML).
* **Architecture Pattern:** MVVM (Model-View-ViewModel) with Clean Architecture principles.
* **Directory Structure:** Organize the codebase into standard layers:
    * `/di` (Dependency Injection modules using Dagger Hilt)
    * `/data` (Room Database entities/DAOs, Retrofit API interfaces, and Repository implementations)
    * `/domain` (Business logic, Models, and Use Cases)
    * `/presentation` (Compose UI screens and ViewModels)
* **Key Dependencies:** Kotlin Coroutines, Room Database, Retrofit 2 + OkHttp, and Jetpack Compose Navigation.

## 2. Orchestration (How the AI should think)
* **Step-by-Step Implementation:** Do not attempt to build the entire application in a single prompt. Propose a single feature or layer, await user approval, and then write the code.
* **Separation of Concerns:** Keep UI logic strictly inside ViewModels. The UI (Compose functions) should only observe `StateFlow` and emit user actions.
* **Financial Logic Strictness:** * The local Room database must initialize a `UserWallet` with a default virtual balance of ₹5,00,000.
    * A "BUY" order must be blocked if the `UserWallet` balance is less than `(Live Price * Quantity)`.
    * A "SELL" order must be blocked if the `Holding` quantity for that specific ticker is less than the requested sell quantity.
* **Error Handling:** Anticipate network failures (e.g., API rate limits from the stock data provider) and handle them gracefully using standard Compose Snackbars or Toasts, rather than crashing.

## 3. Execution (How to build the software)
Follow these exact phases in order:
* **Phase 1: Foundation.** Generate `build.gradle.kts` with all required dependencies (Compose, Room, Retrofit, Hilt) and scaffold the base folder structure (`di`, `data`, `domain`, `presentation`).
* **Phase 2: Local Storage Layer.** Implement the Room Database, DAOs, and Entity classes for the Virtual Wallet, Stock Holdings, and Transaction History.
* **Phase 3: Networking Layer.** Implement the Retrofit interface to fetch live Indian stock prices (assuming NSE/BSE tickers like `.NS` or `.BO`) from a free REST API.
* **Phase 4: State Management.** Build the ViewModels to handle the paper trading business logic, including executing Buy/Sell orders and calculating real-time Profit/Loss.
* **Phase 5: UI Layer.** Generate the Jetpack Compose screens:
    * **Watchlist Screen:** To view live prices.
    * **Order Screen:** To input quantity and execute trades at the current market price.
    * **Portfolio Screen:** To view current holdings, available wallet balance, and total P&L.