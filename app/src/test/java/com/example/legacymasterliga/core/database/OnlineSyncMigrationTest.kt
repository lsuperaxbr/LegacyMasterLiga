package com.example.legacymasterliga.core.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.legacymasterliga.core.di.DatabaseModule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OnlineSyncMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val name = "online-sync-migration-test.db"

    @Before fun cleanBefore() { context.deleteDatabase(name) }
    @After fun cleanAfter() { context.deleteDatabase(name) }

    @Test
    fun `migration 18 to 19 preserves old data and creates durable sync tables`() {
        val old = helper(18, object : SupportSQLiteOpenHelper.Callback(18) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE legacy_data (id INTEGER PRIMARY KEY NOT NULL, value TEXT NOT NULL)")
            }
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        })
        old.writableDatabase.execSQL("INSERT INTO legacy_data(id, value) VALUES (1, 'preservado')")
        old.close()

        val migrated = helper(19, object : SupportSQLiteOpenHelper.Callback(19) {
            override fun onCreate(db: SupportSQLiteDatabase) = Unit
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                DatabaseModule.migration18_19.migrate(db)
            }
        })
        val db = migrated.writableDatabase

        db.query("SELECT value FROM legacy_data WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("preservado", cursor.getString(0))
        }
        db.query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name IN ('online_sync_records','online_sync_queue')").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(2, cursor.getInt(0))
        }
        migrated.close()
    }

    private fun helper(version: Int, callback: SupportSQLiteOpenHelper.Callback): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(name)
                .callback(callback)
                .build(),
        )
}
