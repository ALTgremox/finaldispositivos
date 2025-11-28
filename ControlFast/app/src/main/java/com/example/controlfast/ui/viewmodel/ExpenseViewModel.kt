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

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    // LiveData observables
    val allExpenses: LiveData<List<Expense>>
    val totalExpenses: LiveData<Double?>
    val expensesByCategory: LiveData<List<CategoryTotal>>

    // LiveData para mensajes y estado
    private val _operationStatus = MutableLiveData<String>()
    val operationStatus: LiveData<String> = _operationStatus

    private val _statistics = MutableLiveData<ExpenseStatistics>()
    val statistics: LiveData<ExpenseStatistics> = _statistics

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)

        allExpenses = repository.allExpenses
        totalExpenses = repository.totalExpenses
        expensesByCategory = repository.expensesByCategory
    }

    // Insertar un nuevo gasto
    fun insertExpense(expense: Expense) = viewModelScope.launch {
        try {
            repository.insert(expense)
            _operationStatus.value = "Gasto registrado exitosamente"
        } catch (e: Exception) {
            _operationStatus.value = "Error al registrar gasto: ${e.message}"
        }
    }

    // Actualizar un gasto existente
    fun updateExpense(expense: Expense) = viewModelScope.launch {
        try {
            repository.update(expense)
            _operationStatus.value = "Gasto actualizado exitosamente"
        } catch (e: Exception) {
            _operationStatus.value = "Error al actualizar gasto: ${e.message}"
        }
    }

    // Eliminar un gasto
    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        try {
            repository.delete(expense)
            _operationStatus.value = "Gasto eliminado exitosamente"
        } catch (e: Exception) {
            _operationStatus.value = "Error al eliminar gasto: ${e.message}"
        }
    }

    // Obtener gastos del mes actual
    fun getExpensesThisMonth(): LiveData<List<Expense>> {
        return repository.getExpensesThisMonth()
    }

    // Obtener gastos por categoría específica
    fun getExpensesByCategory(categoria: String): LiveData<List<Expense>> {
        return repository.getExpensesByCategory(categoria)
    }

    // Obtener total por categoría
    fun getTotalByCategory(categoria: String): LiveData<Double?> {
        return repository.getTotalByCategory(categoria)
    }

    // Calcular estadísticas generales
    fun calculateStatistics() = viewModelScope.launch {
        try {
            val stats = repository.getStatistics()
            _statistics.value = stats
        } catch (e: Exception) {
            _operationStatus.value = "Error al calcular estadísticas: ${e.message}"
        }
    }

    // Eliminar todos los gastos (útil para reset)
    fun deleteAllExpenses() = viewModelScope.launch {
        try {
            repository.deleteAllExpenses()
            _operationStatus.value = "Todos los gastos eliminados"
        } catch (e: Exception) {
            _operationStatus.value = "Error al eliminar gastos: ${e.message}"
        }
    }

    // Limpiar el mensaje de estado
    fun clearOperationStatus() {
        _operationStatus.value = ""
    }
}