package com.papertrader.app.presentation.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val GreenAccent = Color(0xFF00E676)
private val RedAccent = Color(0xFFFF5252)
private val CardBg = Color(0xFF1E1E1E)
private val ScreenBg = Color(0xFF121212)
private val SubText = Color(0xFF8B949E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onNavigateToOrder: (String) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isSearchActive by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ScreenBg
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ── HOME CONTENT (indices, tabs, movers) ─────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Search bar (tap to open overlay)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBg)
                            .clickable { isSearchActive = true }
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Search asset (e.g. IBM, AAPL)", color = Color.Gray, fontSize = 15.sp)
                        }
                    }
                }

                // ── INDICES ROW ─────────────────────────────────────────
                item {
                    Text(
                        text = "MARKET INDICES",
                        color = SubText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item { IndexCard("S&P 500", "5,150.92", "+1.2%") }
                        item { IndexCard("NASDAQ", "16,215.14", "+1.5%") }
                        item { IndexCard("DOW", "38,722.69", "-0.2%") }
                        item { IndexCard("NIFTY 50", "22,513.70", "+0.8%") }
                        item { IndexCard("SENSEX", "74,119.39", "+0.7%") }
                        item { IndexCard("NIFTY BANK", "48,201.05", "-0.3%") }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ── HOME TABS ───────────────────────────────────────────
                item {
                    val tabs = listOf("🔥 Top Movers", "🕐 Recently Viewed")
                    TabRow(
                        selectedTabIndex = uiState.activeHomeTab,
                        containerColor = ScreenBg,
                        contentColor = GreenAccent,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[uiState.activeHomeTab]),
                                color = GreenAccent
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = uiState.activeHomeTab == index,
                                onClick = { viewModel.onHomeTabSelected(index) },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 13.sp,
                                        fontWeight = if (uiState.activeHomeTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (uiState.activeHomeTab == index) GreenAccent else SubText
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ── TAB CONTENT ──────────────────────────────────────────
                if (uiState.activeHomeTab == 0) {
                    item { TopMoversSection() }
                } else {
                    item {
                        RecentlyViewedSection(
                            stocks = uiState.recentlyViewed,
                            onStockClick = { ticker ->
                                isSearchActive = true
                                viewModel.onSearchQueryChanged(ticker)
                            }
                        )
                    }
                }
            }

            // ── FULL-SCREEN SEARCH OVERLAY ─────────────────────────────────
            if (isSearchActive) {
                SearchOverlay(
                    uiState = uiState,
                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    onClose = {
                        isSearchActive = false
                        viewModel.onSearchQueryChanged("") // Clear search
                    },
                    onNavigateToOrder = { ticker ->
                        isSearchActive = false
                        onNavigateToOrder(ticker)
                    }
                )
            }
        }
    }
}

// ── FULL-SCREEN SEARCH OVERLAY ─────────────────────────────────────────────────
@Composable
fun SearchOverlay(
    uiState: WatchlistUiState,
    onQueryChanged: (String) -> Unit,
    onClose: () -> Unit,
    onNavigateToOrder: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ScreenBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Search header ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onQueryChanged,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = { Text("Search asset (e.g. IBM, AAPL)") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { onQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // ── Search results ─────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                when {
                    uiState.searchQuery.isBlank() -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔎", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Type a ticker to search",
                                    color = SubText,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "e.g. AAPL, MSFT, TSLA",
                                    color = SubText.copy(alpha = 0.5f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    uiState.isLoading -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = GreenAccent)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Fetching data…", color = SubText, fontSize = 13.sp)
                            }
                        }
                    }

                    uiState.error != null -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                uiState.error!!,
                                color = RedAccent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }

                    uiState.quote != null -> {
                        val quote = uiState.quote!!
                        // ── Quote card ──
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clickable { onNavigateToOrder(quote.ticker) },
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF2C2C2C)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                quote.ticker.take(2),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                quote.ticker,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Text("Equity", color = SubText, fontSize = 12.sp)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "${"%.2f".format(quote.price)}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text("Tap to Trade", color = GreenAccent, fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Candlestick chart
                            if (uiState.chartData.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(380.dp)
                                        .padding(horizontal = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = CardBg),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Daily Chart (60 days)", color = SubText, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        CandlestickChart(
                                            data = uiState.chartData,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onNavigateToOrder(quote.ticker) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .padding(horizontal = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "TRADE ${quote.ticker}",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── TOP MOVERS SECTION ─────────────────────────────────────────────────────────
@Composable
fun TopMoversSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Top Gainers", color = GreenAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        TOP_GAINERS.forEach { mover -> MoverRow(mover = mover, isGain = true) }

        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = RedAccent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Top Losers", color = RedAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        TOP_LOSERS.forEach { mover -> MoverRow(mover = mover, isGain = false) }
    }
}

@Composable
fun MoverRow(mover: MarketMover, isGain: Boolean) {
    val accentColor = if (isGain) GreenAccent else RedAccent
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(mover.ticker.take(2), color = accentColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(mover.ticker, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(mover.name, color = SubText, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(mover.price, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(mover.changePercent, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── RECENTLY VIEWED SECTION ────────────────────────────────────────────────────
@Composable
fun RecentlyViewedSection(
    stocks: List<com.papertrader.app.domain.model.StockQuote>,
    onStockClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (stocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No recently viewed stocks yet", color = SubText, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Text("Search for a ticker above to get started", color = SubText.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            stocks.forEachIndexed { index, quote ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onStockClick(quote.ticker) },
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2C2C2C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${index + 1}", color = SubText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(quote.ticker, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Tap to re-fetch", color = SubText, fontSize = 11.sp)
                            }
                        }
                        Text("${"%.2f".format(quote.price)}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

// ── INDEX CARD ─────────────────────────────────────────────────────────────────
@Composable
fun IndexCard(name: String, value: String, change: String) {
    val isPositive = change.startsWith("+")
    val color = if (isPositive) GreenAccent else RedAccent
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(name, color = SubText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(change, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
