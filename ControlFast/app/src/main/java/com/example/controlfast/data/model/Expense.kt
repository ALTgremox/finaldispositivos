package com.example.controlfast.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

/**
 * Entidad principal de gasto para Room.
 *
 * Representa un registro de gasto con:
 * - id autogenerado.
 * - monto gastado.
 * - categoría del gasto.
 * - descripción breve.
 * - fecha del gasto (en milisegundos desde epoch).
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val monto: Double,

    val categoria: String,

    val descripcion: String,

    // Fecha del gasto almacenada como timestamp (Long)
    val fecha: Long = System.currentTimeMillis()
) {
    /**
     * Devuelve la fecha del gasto formateada como dd/MM/yyyy.
     *
     * Ejemplo: 01/12/2025
     */
    fun getFechaFormateada(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(fecha))
    }

    /**
     * Devuelve el mes y año del gasto formateados.
     *
     * Ejemplo: diciembre 2025
     */
    fun getMesAnio(): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(Date(fecha))
    }
}

/**
 * Enumeración de categorías predefinidas para los gastos.
 *
 * Cada categoría tiene:
 * - displayName: nombre visible para el usuario.
 * - color: color asociado (ARGB en formato Int) para gráficos o indicadores.
 */
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
        /**
         * Devuelve la lista de nombres visibles de todas las categorías.
         *
         * Útil para poblar spinners, listas, etc.
         */
        fun getAllCategorias(): List<String> {
            return values().map { it.displayName }
        }

        /**
         * Devuelve el color asociado a una categoría a partir de su nombre visible.
         *
         * Si no encuentra coincidencias, devuelve el color de la categoría OTROS.
         */
        fun getColorByName(name: String): Int {
            return values().find { it.displayName == name }?.color ?: OTROS.color
        }
    }
}
