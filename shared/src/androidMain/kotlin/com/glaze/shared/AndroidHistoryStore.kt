package com.glaze.shared

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.glaze.shared.db.HistoryDatabase

fun openHistoryStore(context: Context): HistoryStore = HistoryStore(
    AndroidSqliteDriver(HistoryDatabase.Schema, context.applicationContext, "listening-history.db")
)
