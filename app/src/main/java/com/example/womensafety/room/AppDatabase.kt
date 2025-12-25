//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import com.example.womensafety.room.ContactDao
//import com.example.womensafety.room.EmergencyContactEntity

//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import com.example.womensafety.room.ContactDao
//import com.example.womensafety.room.EmergencyContactEntity
//
//@Database(entities = [EmergencyContactEntity::class], version = 1)
//abstract class AppDatabase : RoomDatabase() {
//
//    abstract fun contactDao(): ContactDao
//
//    companion object {
//        @Volatile private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "women_safety_db"
//                ).build().also { INSTANCE = it }
//            }
//        }
//    }
//}


//@Database(
//    entities = [EmergencyContactEntity::class],
//    version = 1,
//    exportSchema = false
//)
//abstract class AppDatabase : RoomDatabase() {
//
//    abstract fun contactDao(): ContactDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "women_safety_db"
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}



package com.example.womensafety.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EmergencyContactEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "women_safety_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
