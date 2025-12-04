package com.example.controlfast.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Archivo de utilidades generales de la aplicación.
 *
 * Contiene:
 * - Extensiones para Activity, Context y View (teclado, toasts, snackbars, visibilidad).
 * - Funciones de formateo (moneda, fechas, conversión segura a Double).
 * - Funciones auxiliares para obtener el inicio y fin del mes actual.
 * - Validación de montos numéricos.
 */

// ==========================
// Extensiones para Activity
// ==========================

/**
 * Oculta el teclado suave (soft keyboard) si está visible.
 *
 * Se usa típicamente después de guardar datos o al salir de un formulario.
 */
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = currentFocus ?: View(this)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

// ==========================
// Extensiones para Context
// ==========================

/**
 * Muestra un Toast con el mensaje indicado.
 *
 * @param message Texto a mostrar.
 * @param duration Duración del Toast (por defecto corto).
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// ==========================
// Extensiones para View
// ==========================

/**
 * Muestra un Snackbar anclado a esta vista con el mensaje indicado.
 *
 * @param message Texto a mostrar.
 * @param duration Duración del Snackbar (por defecto corta).
 */
fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

/**
 * Cambia la visibilidad de la vista a VISIBLE.
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * Cambia la visibilidad de la vista a GONE.
 * La vista no ocupa espacio en el layout.
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * Cambia la visibilidad de la vista a INVISIBLE.
 * La vista no se ve, pero sigue ocupando espacio en el layout.
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

// ==========================
// Extensiones para formateo
// ==========================

/**
 * Formatea un Double como moneda en soles peruanos (es-PE).
 *
 * Ejemplo: 25.5 -> S/ 25.50
 */
fun Double.toCurrency(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
    return formatter.format(this)
}

/**
 * Convierte un valor Long (en milisegundos desde epoch) a String de fecha.
 *
 * @param pattern Formato de salida (por defecto dd/MM/yyyy).
 */
fun Long.toDateString(pattern: String = "dd/MM/yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Convierte un String a Double, devolviendo 0.0 si el formato no es válido.
 *
 * Útil para evitar NumberFormatException cuando el texto puede estar vacío
 * o contener caracteres no numéricos.
 */
fun String.toDoubleOrZero(): Double {
    return this.toDoubleOrNull() ?: 0.0
}

// ========================================
// Funciones para inicio y fin del mes actual
// ========================================

/**
 * Obtiene el timestamp (en milisegundos) correspondiente
 * al inicio del mes actual (día 1, 00:00:00.000).
 */
fun getStartOfMonth(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/**
 * Obtiene el timestamp (en milisegundos) correspondiente
 * al fin del mes actual (último día, 23:59:59.999).
 */
fun getEndOfMonth(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}

// ==========================
// Validación de monto
// ==========================

/**
 * Verifica si el String representa un monto válido mayor que cero.
 *
 * Condiciones:
 * - No debe estar en blanco.
 * - Debe poder convertirse a Double.
 * - Debe ser estrictamente mayor que 0.
 */
fun String.isValidAmount(): Boolean {
    if (this.isBlank()) return false
    val amount = this.toDoubleOrNull() ?: return false
    return amount > 0
}
