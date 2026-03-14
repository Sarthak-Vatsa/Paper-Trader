package com.papertrader.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. The @HiltAndroidApp annotation triggers Hilt's
 * code generation and initialises the application-level dependency graph.
 */
@HiltAndroidApp
class PaperTraderApp : Application()
