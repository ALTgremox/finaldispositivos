package com.example.controlfast.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.controlfast.R
import com.example.controlfast.data.model.Expense
import com.example.controlfast.databinding.ActivityMainBinding
import com.example.controlfast.ui.adapter.ExpenseAdapter
import com.example.controlfast.ui.viewmodel.ExpenseViewModel
import com.example.controlfast.utils.gone
import com.example.controlfast.utils.showToast
import com.example.controlfast.utils.toCurrency
import com.example.controlfast.utils.visible

/**
 * Pantalla principal de la aplicación.
 *
 * Muestra:
 * - Tarjeta con el total de gastos.
 * - Lista de gastos registrados en un RecyclerView.
 * - Mensaje de estado vacío cuando no hay registros.
 * - Accesos rápidos para agregar un gasto y ver el resumen.
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding para acceder a las vistas del layout
    private lateinit var binding: ActivityMainBinding

    // ViewModel que expone los datos de gastos y operaciones
    private val viewModel: ExpenseViewModel by viewModels()

    // Adaptador del RecyclerView para la lista de gastos
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización de componentes de la pantalla
        setupToolbar()
        setupRecyclerView()
        setupFAB()
        observeData()
    }

    /**
     * Configura la Toolbar como ActionBar y asigna el título principal de la app.
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        // Se podría usar getString(R.string.app_name) para extraerlo de strings.xml
        supportActionBar?.title = "ControlFast"
    }

    /**
     * Configura el RecyclerView y el adaptador que mostrará la lista de gastos.
     * También define las acciones de clic corto y largo en cada ítem.
     */
    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onItemClick = { expense ->
                // Al hacer clic, se abre la pantalla para editar el gasto
                editExpense(expense)
            },
            onItemLongClick = { expense ->
                // Al hacer clic prolongado, se muestra un diálogo para eliminar el gasto
                showDeleteDialog(expense)
            }
        )

        binding.recyclerViewExpenses.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true) // Optimiza el rendimiento cuando el tamaño no cambia
        }
    }

    /**
     * Configura los botones flotantes (FAB):
     * - Agregar un nuevo gasto.
     * - Abrir la pantalla de resumen de gastos.
     */
    private fun setupFAB() {
        // FAB para agregar un nuevo gasto
        binding.fabAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        // FAB para abrir la pantalla de resumen
        binding.fabSummary.setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
        }
    }

    /**
     * Observa los datos expuestos por el ViewModel:
     * - Lista completa de gastos.
     * - Total de gastos.
     * - Mensajes de estado de operaciones (guardar, actualizar, eliminar).
     */
    private fun observeData() {
        // Observar lista de gastos para mostrarla o mostrar el estado vacío
        viewModel.allExpenses.observe(this) { expenses ->
            if (expenses.isEmpty()) {
                binding.tvEmptyState.visible()
                binding.recyclerViewExpenses.gone()
            } else {
                binding.tvEmptyState.gone()
                binding.recyclerViewExpenses.visible()
                adapter.submitList(expenses)
            }
        }

        // Observar total de gastos y actualizar la tarjeta superior
        viewModel.totalExpenses.observe(this) { total ->
            // Se usa extensión toCurrency() para dar formato al monto
            // Este texto también podría formatearse usando un recurso de strings con placeholder.
            binding.tvTotalAmount.text = "Total: ${(total ?: 0.0).toCurrency()}"
        }

        // Observar mensajes de estado para mostrar feedback al usuario
        viewModel.operationStatus.observe(this) { status ->
            if (status.isNotEmpty()) {
                showToast(status)
                // Limpia el mensaje para no volver a mostrarlo
                viewModel.clearOperationStatus()
            }
        }
    }

    /**
     * Abre la pantalla de agregar gasto en modo edición,
     * enviando los datos del gasto seleccionado mediante extras.
     */
    private fun editExpense(expense: Expense) {
        val intent = Intent(this, AddExpenseActivity::class.java).apply {
            putExtra("EXPENSE_ID", expense.id)
            putExtra("EXPENSE_MONTO", expense.monto)
            putExtra("EXPENSE_CATEGORIA", expense.categoria)
            putExtra("EXPENSE_DESCRIPCION", expense.descripcion)
            putExtra("EXPENSE_FECHA", expense.fecha)
        }
        startActivity(intent)
    }

    /**
     * Muestra un diálogo de confirmación para eliminar un gasto puntual.
     *
     * @param expense gasto que se desea eliminar.
     */
    private fun showDeleteDialog(expense: Expense) {
        AlertDialog.Builder(this)
            // Estos textos están definidos también en strings.xml (dialog_delete_title, etc.)
            .setTitle("Eliminar Gasto")
            .setMessage("¿Estás seguro de eliminar este gasto?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteExpense(expense)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Infla el menú de opciones de la Toolbar.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Maneja las acciones seleccionadas en el menú de la Toolbar.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Acción para eliminar todos los gastos registrados
            R.id.action_delete_all -> {
                showDeleteAllDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Muestra un diálogo de confirmación para eliminar todos los gastos.
     * Esta acción no se puede deshacer.
     */
    private fun showDeleteAllDialog() {
        AlertDialog.Builder(this)
            // También tienes estos textos en strings.xml (dialog_delete_all_title, etc.)
            .setTitle("Eliminar Todos los Gastos")
            .setMessage("¿Estás seguro? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteAllExpenses()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
