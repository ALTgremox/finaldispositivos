package com.example.controlfast.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val monto: Double,

    val categoria: String,

    val descripcion: String,

    val fecha: Long = System.currentTimeMillis()
) {
    // Función auxiliar para formatear la fecha
    fun getFechaFormateada(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(fecha))
    }

    // Función auxiliar para obtener el mes y año
    fun getMesAnio(): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(Date(fecha))
    }
}

// Enum para las categorías predefinidas
enum class CategoriaGasto(val displayName: String, val color: Int) {
    ALIMENTACION("Alimentación", 0xFFFF6B6B.toInt()),
    TRANSPORTE("Transporte", 0xFF4ECDC4.toInt()),
    ENTRETENIMIENTO("Entretenimiento", 0xFFFFBE0B.toInt()),
    SALUD("Salud", 0xFF95E1D3.toInt()),
    EDUCACION("Educación", 0xFF9B59B6.toInt()),
    SERVICIOS("Servicios", 0xFF3498DB.toInt()),
    COMPRAS("Compras", 0xFFE74C3C.toInt()),
    OTROS("Otros", 0xFF95A5A6.toInt());

    companion object {
        fun getAllCategorias(): List<String> {
            return values().map { it.displayName }
        }

        fun getColorByName(name: String): Int {
            return values().find { it.displayName == name }?.color ?: OTROS.color
        }
    }
}