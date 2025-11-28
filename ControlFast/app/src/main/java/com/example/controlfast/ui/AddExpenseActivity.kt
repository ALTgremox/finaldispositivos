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

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private val viewModel: ExpenseViewModel by viewModels()

    private var selectedDate: Long = System.currentTimeMillis()
    private var isEditMode = false
    private var expenseId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCategorySpinner()
        setupDatePicker()
        loadExpenseData()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Agregar Gasto"
    }

    private fun setupCategorySpinner() {
        val categories = CategoriaGasto.getAllCategorias()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        binding.spinnerCategoria.adapter = adapter
    }

    private fun setupDatePicker() {
        updateDateDisplay()

        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.tvSelectedDate.text = sdf.format(Date(selectedDate))
    }

    private fun loadExpenseData() {
        // Verificar si estamos editando un gasto existente
        intent?.let { intent ->
            expenseId = intent.getIntExtra("EXPENSE_ID", 0)
            if (expenseId != 0) {
                isEditMode = true
                supportActionBar?.title = "Editar Gasto"

                // Cargar datos del gasto
                val monto = intent.getDoubleExtra("EXPENSE_MONTO", 0.0)
                val categoria = intent.getStringExtra("EXPENSE_CATEGORIA") ?: ""
                val descripcion = intent.getStringExtra("EXPENSE_DESCRIPCION") ?: ""
                selectedDate = intent.getLongExtra("EXPENSE_FECHA", System.currentTimeMillis())

                // Mostrar datos en la UI
                binding.etMonto.setText(monto.toString())
                binding.etDescripcion.setText(descripcion)
                updateDateDisplay()

                // Seleccionar la categoría correcta
                val categories = CategoriaGasto.getAllCategorias()
                val position = categories.indexOf(categoria)
                if (position >= 0) {
                    binding.spinnerCategoria.setSelection(position)
                }

                binding.btnSave.text = "Actualizar"
            }
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            saveExpense()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveExpense() {
        // Validar campos
        val montoStr = binding.etMonto.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val categoria = binding.spinnerCategoria.selectedItem.toString()

        // Validaciones
        if (!montoStr.isValidAmount()) {
            binding.etMonto.error = "Ingrese un monto válido"
            return
        }

        if (descripcion.isEmpty()) {
            binding.etDescripcion.error = "Ingrese una descripción"
            return
        }

        val monto = montoStr.toDoubleOrZero()

        // Crear objeto Expense
        val expense = Expense(
            id = if (isEditMode) expenseId else 0,
            monto = monto,
            categoria = categoria,
            descripcion = descripcion,
            fecha = selectedDate
        )

        // Guardar o actualizar
        if (isEditMode) {
            viewModel.updateExpense(expense)
        } else {
            viewModel.insertExpense(expense)
        }

        hideKeyboard()
        showToast(if (isEditMode) "Gasto actualizado" else "Gasto guardado")
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}