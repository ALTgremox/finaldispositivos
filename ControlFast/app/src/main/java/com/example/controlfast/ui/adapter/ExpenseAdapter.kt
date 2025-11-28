package com.example.controlfast.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.controlfast.data.model.CategoriaGasto
import com.example.controlfast.data.model.Expense
import com.example.controlfast.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.util.*

class ExpenseAdapter(
    private val onItemClick: (Expense) -> Unit,
    private val onItemLongClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(
        private val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.apply {
                // Formatear monto con símbolo de moneda
                val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
                tvMonto.text = formatter.format(expense.monto)

                // Mostrar categoría
                tvCategoria.text = expense.categoria

                // Mostrar descripción
                tvDescripcion.text = expense.descripcion

                // Mostrar fecha formateada
                tvFecha.text = expense.getFechaFormateada()

                // Cambiar color según categoría
                val categoriaColor = CategoriaGasto.getColorByName(expense.categoria)
                cardCategoria.setCardBackgroundColor(categoriaColor)

                // Click listeners
                root.setOnClickListener {
                    onItemClick(expense)
                }

                root.setOnLongClickListener {
                    onItemLongClick(expense)
                    true
                }
            }
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}