package com.sharc.ramdhd

import android.app.Application
import com.sharc.ramdhd.data.database.AppDatabase

class RAMDHDApplication : Application() {
    // Lazy initialize database
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
}