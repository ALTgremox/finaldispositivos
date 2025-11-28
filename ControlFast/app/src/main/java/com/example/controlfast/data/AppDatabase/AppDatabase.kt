package com.example.controlfast.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.controlfast.data.dao.ExpenseDao
import com.example.controlfast.data.model.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Expense::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "controlfast_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Callback para poblar la base de datos con datos de ejemplo (opcional)
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Puedes descomentar esto si quieres datos de ejemplo
                // INSTANCE?.let { database ->
                //     CoroutineScope(Dispatchers.IO).launch {
                //         populateDatabase(database.expenseDao())
                //     }
                // }
            }
        }

        // Función para poblar con datos de ejemplo
        suspend fun populateDatabase(expenseDao: ExpenseDao) {
            // Datos de ejemplo
            val sampleExpenses = listOf(
                Expense(
                    monto = 25.50,
                    categoria = "Alimentación",
                    descripcion = "Almuerzo en restaurante",
                    fecha = System.currentTimeMillis()
                ),
                Expense(
                    monto = 15.00,
                    categoria = "Transporte",
                    descripcion = "Taxi",
                    fecha = System.currentTimeMillis() - 86400000 // 1 día antes
                ),
                Expense(
                    monto = 50.00,
                    categoria = "Entretenimiento",
                    descripcion = "Cine y cena",
                    fecha = System.currentTimeMillis() - 172800000 // 2 días antes
                )
            )

            sampleExpenses.forEach { expense ->
                expenseDao.insertExpense(expense)
            }
        }
    }
}