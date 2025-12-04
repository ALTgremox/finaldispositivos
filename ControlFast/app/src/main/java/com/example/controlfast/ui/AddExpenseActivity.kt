package com.example.controlfast.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.controlfast.R
import com.example.controlfast.data.model.CategoriaGasto
import com.example.controlfast.data.model.Expense
import com.example.controlfast.databinding.ActivityAddExpenseBinding
import com.example.controlfast.ui.viewmodel.ExpenseViewModel
import com.example.controlfast.utils.hideKeyboard
import com.example.controlfast.utils.isValidAmount
import com.example.controlfast.utils.showToast
import com.example.controlfast.utils.toDoubleOrZero
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para registrar o editar un gasto.
 *
 * Funcionalidades principales:
 * - Crear nuevos gastos con monto, categoría, descripción y fecha.
 * - Editar un gasto existente cuando se reciben los datos por Intent.
 * - Validar campos antes de guardar la información.
 */
class AddExpenseActivity : AppCompatActivity() {

    // ViewBinding para acceder a las vistas del layout
    private lateinit var binding: ActivityAddExpenseBinding

    // ViewModel que gestiona la lógica de acceso a datos de gastos
    private val viewModel: ExpenseViewModel by viewModels()

    // Fecha seleccionada del gasto (en milisegundos desde epoch)
    private var selectedDate: Long = System.currentTimeMillis()

    // Indica si la pantalla está en modo edición (true) o creación (false)
    private var isEditMode = false

    // ID del gasto que se está editando (0 si es un nuevo registro)
    private var expenseId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización de elementos de la UI y carga de datos
        setupToolbar()
        setupCategorySpinner()
        setupDatePicker()
        loadExpenseData()
        setupButtons()
    }

    /**
     * Configura la Toolbar como ActionBar y habilita el botón de volver.
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Título inicial. En modo edición se reemplaza más adelante.
        // Nota: podría extraerse a strings.xml (add_expense_title)
        supportActionBar?.title = "Agregar Gasto"
    }

    /**
     * Configura el Spinner de categorías usando los valores definidos en CategoriaGasto.
     */
    private fun setupCategorySpinner() {
        val categories = CategoriaGasto.getAllCategorias()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        binding.spinnerCategoria.adapter = adapter
    }

    /**
     * Configura la lógica del selector de fecha:
     * - Muestra la fecha actual por defecto.
     * - Abre el DatePickerDialog al pulsar el botón.
     */
    private fun setupDatePicker() {
        // Mostrar la fecha inicial seleccionada
        updateDateDisplay()

        // Abrir el diálogo de fecha al pulsar el botón
        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }
    }

    /**
     * Muestra un DatePickerDialog para que el usuario seleccione una fecha.
     * Al confirmar, se actualiza la variable selectedDate y el texto en pantalla.
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Actualizar la fecha seleccionada con el valor elegido por el usuario
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /**
     * Actualiza el TextView de la fecha con el valor actual de selectedDate,
     * usando el formato dd/MM/yyyy.
     */
    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.tvSelectedDate.text = sdf.format(Date(selectedDate))
    }

    /**
     * Carga los datos del Intent para determinar si se está editando un gasto.
     * Si recibe un ID distinto de 0, se asume modo edición y se llenan los campos.
     */
    private fun loadExpenseData() {
        // Verificar si estamos editando un gasto existente
        intent?.let { intent ->
            expenseId = intent.getIntExtra("EXPENSE_ID", 0)
            if (expenseId != 0) {
                isEditMode = true
                // En modo edición, cambiar el título de la barra
                // Nota: podría extraerse a strings.xml (edit_expense_title)
                supportActionBar?.title = "Editar Gasto"

                // Cargar datos del gasto enviados por extras
                val monto = intent.getDoubleExtra("EXPENSE_MONTO", 0.0)
                val categoria = intent.getStringExtra("EXPENSE_CATEGORIA") ?: ""
                val descripcion = intent.getStringExtra("EXPENSE_DESCRIPCION") ?: ""
                selectedDate = intent.getLongExtra("EXPENSE_FECHA", System.currentTimeMillis())

                // Mostrar datos en la UI
                binding.etMonto.setText(monto.toString())
                binding.etDescripcion.setText(descripcion)
                updateDateDisplay()

                // Seleccionar la categoría correcta en el Spinner
                val categories = CategoriaGasto.getAllCategorias()
                val position = categories.indexOf(categoria)
                if (position >= 0) {
                    binding.spinnerCategoria.setSelection(position)
                }

                // Cambiar el texto del botón para indicar actualización
                // Nota: podría venir de strings.xml (btn_actualizar)
                binding.btnSave.text = "Actualizar"
            }
        }
    }

    /**
     * Configura las acciones de los botones:
     * - Guardar/actualizar el gasto.
     * - Cancelar y cerrar la pantalla.
     */
    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            saveExpense()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    /**
     * Valida los campos del formulario y, si son correctos,
     * crea o actualiza el objeto Expense a través del ViewModel.
     */
    private fun saveExpense() {
        // Obtener valores de los campos
        val montoStr = binding.etMonto.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val categoria = binding.spinnerCategoria.selectedItem.toString()

        // Validación del monto ingresado
        if (!montoStr.isValidAmount()) {
            // Estos textos de error también podrían centralizarse en strings.xml
            binding.etMonto.error = "Ingrese un monto válido"
            return
        }

        // Validación de la descripción
        if (descripcion.isEmpty()) {
            binding.etDescripcion.error = "Ingrese una descripción"
            return
        }

        // Conversión segura del texto del monto a Double
        val monto = montoStr.toDoubleOrZero()

        // Crear objeto Expense con los datos proporcionados
        val expense = Expense(
            id = if (isEditMode) expenseId else 0,
            monto = monto,
            categoria = categoria,
            descripcion = descripcion,
            fecha = selectedDate
        )

        // Guardar o actualizar según el modo actual
        if (isEditMode) {
            viewModel.updateExpense(expense)
        } else {
            viewModel.insertExpense(expense)
        }

        // Ocultar teclado, mostrar mensaje y cerrar la pantalla
        hideKeyboard()
        // También podría utilizarse un string con placeholder en strings.xml
        showToast(if (isEditMode) "Gasto actualizado" else "Gasto guardado")
        finish()
    }

    /**
     * Maneja el comportamiento del botón de retroceso en la Toolbar.
     */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
