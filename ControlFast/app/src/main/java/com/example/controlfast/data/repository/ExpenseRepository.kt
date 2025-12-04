package com.example.controlfast.data.repository

import androidx.lifecycle.LiveData
import com.example.controlfast.data.dao.CategoryTotal
import com.example.controlfast.data.dao.ExpenseDao
import com.example.controlfast.data.model.Expense
import java.util.*

/**
 * Repositorio de gastos.
 *
 * Encapsula el acceso al [ExpenseDao] y expone métodos de más alto nivel
 * para ser usados desde el ViewModel. Su función principal es separar
 * la lógica de datos de la capa de presentación.
 */
class ExpenseRepository(private val expenseDao: ExpenseDao) {

    // ==========================
    // LiveData expuestos
    // ==========================

    /** Lista reactiva con todos los gastos almacenados. */
    val allExpenses: LiveData<List<Expense>> = expenseDao.getAllExpenses()

    /** Total reactivo de todos los gastos registrados. */
    val totalExpenses: LiveData<Double?> = expenseDao.getTotalExpenses()

    /**
     * Totales de gastos agrupados por categoría.
     * Útil para gráficas y resúmenes.
     */
    val expensesByCategory: LiveData<List<CategoryTotal>> = expenseDao.getExpensesByCategory()

    // ==========================
    // Operaciones CRUD básicas
    // ==========================

    /**
     * Inserta un nuevo gasto en la base de datos.
     *
     * @return ID generado para el nuevo registro.
     */
    suspend fun insert(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    /**
     * Actualiza un gasto existente.
     */
    suspend fun update(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    /**
     * Elimina un gasto específico.
     */
    suspend fun delete(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    /**
     * Obtiene un gasto puntual por su ID.
     *
     * @return El gasto si existe, o null en caso contrario.
     */
    suspend fun getExpenseById(id: Int): Expense? {
        return expenseDao.getExpenseById(id)
    }

    // ==========================
    // Consultas específicas
    // ==========================

    /**
     * Obtiene los gastos filtrados por una categoría.
     */
    fun getExpensesByCategory(categoria: String): LiveData<List<Expense>> {
        return expenseDao.getExpensesByCategory(categoria)
    }

    /**
     * Obtiene el total gastado en una categoría específica.
     */
    fun getTotalByCategory(categoria: String): LiveData<Double?> {
        return expenseDao.getTotalByCategory(categoria)
    }

    /**
     * Obtiene los gastos correspondientes al mes actual.
     *
     * Calcula el primer día del mes a las 00:00:00.000 y
     * usa ese timestamp como inicio del rango.
     */
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

    /**
     * Obtiene los gastos dentro de un rango de fechas [startDate, endDate].
     *
     * @param startDate Timestamp inicial en milisegundos.
     * @param endDate Timestamp final en milisegundos.
     */
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<Expense>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }

    /**
     * Elimina todos los registros de gastos de la base de datos.
     */
    suspend fun deleteAllExpenses() {
        expenseDao.deleteAllExpenses()
    }

    // ==========================
    // Estadísticas generales
    // ==========================

    /**
     * Calcula estadísticas generales a partir de la lista actual de gastos.
     *
     * Nota importante:
     * - Usa el valor actual de [allExpenses.value], por lo que depende de que
     *   el LiveData haya sido observado y cargado previamente.
     */
    suspend fun getStatistics(): ExpenseStatistics {
        val allExpenses = allExpenses.value ?: emptyList()

        val total = allExpenses.sumOf { it.monto }
        val promedio = if (allExpenses.isNotEmpty()) total / allExpenses.size else 0.0
        val categoriaConMasGastos = allExpenses
            .groupBy { it.categoria }
            .maxByOrNull { entry -> entry.value.sumOf { expense -> expense.monto } }
            ?.key ?: "N/A"

        return ExpenseStatistics(
            totalGastado = total,
            promedioGasto = promedio,
            cantidadGastos = allExpenses.size,
            categoriaConMasGastos = categoriaConMasGastos
        )
    }
}

/**
 * Data class que agrupa las estadísticas generales de los gastos.
 *
 * @param totalGastado Suma total de todos los gastos.
 * @param promedioGasto Promedio por gasto registrado.
 * @param cantidadGastos Número total de registros.
 * @param categoriaConMasGastos Nombre de la categoría con mayor monto acumulado.
 */
data class ExpenseStatistics(
    val totalGastado: Double,
    val promedioGasto: Double,
    val cantidadGastos: Int,
    val categoriaConMasGastos: String
)
