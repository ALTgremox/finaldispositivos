package com.example.controlfast.data.repository

import androidx.lifecycle.LiveData
import com.example.controlfast.data.dao.CategoryTotal
import com.example.controlfast.data.dao.ExpenseDao
import com.example.controlfast.data.model.Expense
import java.util.*

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    // Obtener todos los gastos
    val allExpenses: LiveData<List<Expense>> = expenseDao.getAllExpenses()

    // Obtener total de gastos
    val totalExpenses: LiveData<Double?> = expenseDao.getTotalExpenses()

    // Obtener gastos por categoría para gráficas
    val expensesByCategory: LiveData<List<CategoryTotal>> = expenseDao.getExpensesByCategory()

    // Insertar gasto
    suspend fun insert(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    // Actualizar gasto
    suspend fun update(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    // Eliminar gasto
    suspend fun delete(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    // Obtener gasto por ID
    suspend fun getExpenseById(id: Int): Expense? {
        return expenseDao.getExpenseById(id)
    }

    // Obtener gastos por categoría
    fun getExpensesByCategory(categoria: String): LiveData<List<Expense>> {
        return expenseDao.getExpensesByCategory(categoria)
    }

    // Obtener total por categoría
    fun getTotalByCategory(categoria: String): LiveData<Double?> {
        return expenseDao.getTotalByCategory(categoria)
    }

    // Obtener gastos del mes actual
    fun getExpensesThisMonth(): LiveData<List<Expense>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfMonth = calendar.timeInMillis
        return expenseDao.getExpensesThisMonth(startOfMonth)
    }

    // Obtener gastos por rango de fechas
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<Expense>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }

    // Eliminar todos los gastos
    suspend fun deleteAllExpenses() {
        expenseDao.deleteAllExpenses()
    }

    // Obtener estadísticas generales
    suspend fun getStatistics(): ExpenseStatistics {
        val allExpenses = allExpenses.value ?: emptyList()

        val total = allExpenses.sumOf { it.monto }
        val promedio = if (allExpenses.isNotEmpty()) total / allExpenses.size else 0.0
        val categoriaConMasGastos = allExpenses
            .groupBy { it.categoria }
            .maxByOrNull { it.value.sumOf { expense -> expense.monto } }
            ?.key ?: "N/A"

        return ExpenseStatistics(
            totalGastado = total,
            promedioGasto = promedio,
            cantidadGastos = allExpenses.size,
            categoriaConMasGastos = categoriaConMasGastos
        )
    }
}

// Data class para estadísticas
data class ExpenseStatistics(
    val totalGastado: Double,
    val promedioGasto: Double,
    val cantidadGastos: Int,
    val categoriaConMasGastos: String
)