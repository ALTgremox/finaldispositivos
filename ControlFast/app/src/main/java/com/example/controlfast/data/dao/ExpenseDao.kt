package com.example.controlfast.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.controlfast.data.model.Expense

@Dao
interface ExpenseDao {

    // Insertar un gasto
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    // Actualizar un gasto
    @Update
    suspend fun updateExpense(expense: Expense)

    // Eliminar un gasto
    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Obtener todos los gastos ordenados por fecha descendente
    @Query("SELECT * FROM expenses ORDER BY fecha DESC")
    fun getAllExpenses(): LiveData<List<Expense>>

    // Obtener un gasto por ID
    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: Int): Expense?

    // Obtener gastos por categoría
    @Query("SELECT * FROM expenses WHERE categoria = :categoria ORDER BY fecha DESC")
    fun getExpensesByCategory(categoria: String): LiveData<List<Expense>>

    // Obtener gastos por rango de fechas
    @Query("SELECT * FROM expenses WHERE fecha BETWEEN :startDate AND :endDate ORDER BY fecha DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<Expense>>

    // Calcular el total de gastos
    @Query("SELECT SUM(monto) FROM expenses")
    fun getTotalExpenses(): LiveData<Double?>

    // Calcular el total por categoría
    @Query("SELECT SUM(monto) FROM expenses WHERE categoria = :categoria")
    fun getTotalByCategory(categoria: String): LiveData<Double?>

    // Obtener gastos del mes actual
    @Query("SELECT * FROM expenses WHERE fecha >= :startOfMonth ORDER BY fecha DESC")
    fun getExpensesThisMonth(startOfMonth: Long): LiveData<List<Expense>>

    // Eliminar todos los gastos
    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    // Obtener suma de gastos por categoría (para gráficas)
    @Query("SELECT categoria, SUM(monto) as total FROM expenses GROUP BY categoria")
    fun getExpensesByCategory(): LiveData<List<CategoryTotal>>
}

// Data class para los totales por categoría
data class CategoryTotal(
    val categoria: String,
    val total: Double
)