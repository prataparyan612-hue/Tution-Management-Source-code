package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        StudentProfile::class,
        TeacherProfile::class,
        Batch::class,
        Attendance::class,
        FeePayment::class,
        ClassSchedule::class,
        ReportCard::class,
        Announcement::class,
        Message::class,
        StudentDocument::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TuitionDatabase : RoomDatabase() {

    abstract fun tuitionDao(): TuitionDao

    companion object {
        @Volatile
        private var INSTANCE: TuitionDatabase? = null

        fun getDatabase(context: Context): TuitionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TuitionDatabase::class.java,
                    "tuition_manager_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
