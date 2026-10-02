package com.glaze.shared

// Calendar dates use the listener's timezone; catalogue timestamps aren't launch times.
internal expect fun releaseDaysUntil(date: String, now: Long): Long?
