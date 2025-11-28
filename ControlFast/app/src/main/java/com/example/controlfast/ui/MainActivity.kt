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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFAB()
        observeData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "ControlFast"
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onItemClick = { expense ->
                // Abrir detalles o editar
                editExpense(expense)
            },
            onItemLongClick = { expense ->
                // Mostrar diálogo para eliminar
                showDeleteDialog(expense)
            }
        )

        binding.recyclerViewExpenses.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupFAB() {
        binding.fabAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        binding.fabSummary.setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
        }
    }

    private fun observeData() {
        // Observar lista de gastos
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

        // Observar total de gastos
        viewModel.totalExpenses.observe(this) { total ->
            binding.tvTotalAmount.text = "Total: ${(total ?: 0.0).toCurrency()}"
        }

        // Observar estado de operaciones
        viewModel.operationStatus.observe(this) { status ->
            if (status.isNotEmpty()) {
                showToast(status)
                viewModel.clearOperationStatus()
            }
        }
    }

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

    private fun showDeleteDialog(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Gasto")
            .setMessage("¿Estás seguro de eliminar este gasto?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteExpense(expense)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all -> {
                showDeleteAllDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Todos los Gastos")
            .setMessage("¿Estás seguro? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteAllExpenses()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}