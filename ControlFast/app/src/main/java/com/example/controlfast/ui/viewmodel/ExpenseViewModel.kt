package com.example.controlfast.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.controlfast.data.dao.CategoryTotal
import com.example.controlfast.data.database.AppDatabase
import com.example.controlfast.data.model.Expense
import com.example.controlfast.data.repository.ExpenseRepository
import com.example.controlfast.data.repository.ExpenseStatistics
import kotlinx.coroutines.launch

/**
 * ViewModel principal para la gestión de gastos.
 *
 * Responsabilidades:
 * - Servir de puente entre la capa de UI y el repositorio de datos.
 * - Exponer listas de gastos, totales y agrupaciones por categoría mediante LiveData.
 * - Gestionar operaciones CRUD (crear, actualizar, eliminar) sobre los gastos.
 * - Publicar mensajes de estado y estadísticas generales para la UI.
 */
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio que encapsula el acceso a la base de datos
    private val repository: ExpenseRepository

    // LiveData observables principales
    val allExpenses: LiveData<List<Expense>>
    val totalExpenses: LiveData<Double?>
    val expensesByCategory: LiveData<List<CategoryTotal>>

    // LiveData para mensajes de estado de las operaciones (éxito / error)
    private val _operationStatus = MutableLiveData<String>()
    val operationStatus: LiveData<String> = _operationStatus

    // LiveData para exponer estadísticas generales de gastos
    private val _statistics = MutableLiveData<ExpenseStatistics>()
    val statistics: LiveData<ExpenseStatistics> = _statistics

    init {
        // Inicializa el DAO y el repositorio usando la instancia de base de datos
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)

        // Asigna los LiveData proporcionados por el repositorio
        allExpenses = repository.allExpenses
        totalExpenses = repository.totalExpenses
        expensesByCategory = repository.expensesByCategory
    }

    /**
     * Inserta un nuevo gasto en la base de datos.
     * Publica un mensaje de estado indicando el resultado de la operación.
     */
    fun insertExpense(expense: Expense) = viewModelScope.launch {
        try {
            repository.insert(expense)
            _operationStatus.value = "Gasto registrado exitosamente"
            // Nota: estos mensajes podrían venir de strings.xml si se quisiera internacionalizar
        } catch (e: Exception) {
            _operationStatus.value = "Error al registrar gasto: ${e.message}"
        }
    }

    /**
     * Actualiza un gasto existente.
     * Publica un mensaje indicando si la operación fue exitosa o si ocurrió un error.
     */
    fun updateExpense(expense: Expense) = viewModelScope.launch {
        try {
            repository.update(expense)
            _operationStatus.value = "Gasto actualizado exitosamente"
        } catch (e: Exception) {
            _operationStatus.value = "Error al actualizar gasto: ${e.message}"
        }
    }

    /**
     * Elimina un gasto específico de la base de datos.
     */
    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        try {
            repository.delete(expense)
            _operationStatus.value = "Gasto eliminado exitosamente"
        } catch (e: Exception) {
            _operationStatus.value = "Error al eliminar gasto: ${e.message}"
        }
    }

    /**
     * Obtiene los gastos correspondientes al mes actual.
     * Devuelve un LiveData para que la UI los observe directamente.
     */
    fun getExpensesThisMonth(): LiveData<List<Expense>> {
        return repository.getExpensesThisMonth()
    }

    /**
     * Obtiene los gastos filtrados por una categoría específica.
     */
    fun getExpensesByCategory(categoria: String): LiveData<List<Expense>> {
        return repository.getExpensesByCategory(categoria)
    }

    /**
     * Obtiene el total de gastos para una categoría específica.
     */
    fun getTotalByCategory(categoria: String): LiveData<Double?> {
        return repository.getTotalByCategory(categoria)
    }

    /**
     * Calcula estadísticas generales a través del repositorio
     * y actualiza el LiveData correspondiente para que la UI las muestre.
     */
    fun calculateStatistics() = viewModelScope.launch {
        try {
            val stats = repository.getStatistics()
            _statistics.value = stats
        } catch (e: Exception) {
            _operationStatus.value = "Error al calcular estadísticas: ${e.message}"
        }
    }

    /**
     * Elimina todos los gastos de la base de datos.
     * Útil para acciones de reseteo total o limpieza.
     */
    fun deleteAllExpenses() = viewModelScope.launch {
        try {
            repository.deleteAllExpenses()
            _operationStatus.value = "Todos los gastos eliminados"
        } catch (e: Exception) {
            _operationStatus.value = "Error al eliminar gastos: ${e.message}"
        }
    }

    /**
     * Limpia el mensaje de estado actual para evitar que se muestre repetidamente.
     */
    fun clearOperationStatus() {
        _operationStatus.value = ""
    }
}
