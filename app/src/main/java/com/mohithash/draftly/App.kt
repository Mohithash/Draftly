package com.mohithash.draftly

import android.app.Application
import androidx.room.Room
import com.mohithash.draftly.ai.AiClient
import com.mohithash.draftly.ai.WriterAi
import com.mohithash.draftly.data.AppDb
import com.mohithash.draftly.data.JsonStore

class App : Application() {
    lateinit var db: AppDb
    lateinit var store: JsonStore
    val client = AiClient()
    val writer by lazy { WriterAi(client) }
    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDb::class.java, "draftly.db").build()
        store = JsonStore(this)
    }
}
