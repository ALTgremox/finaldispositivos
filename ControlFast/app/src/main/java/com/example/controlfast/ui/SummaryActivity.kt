package com.example.controlfast.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.controlfast.data.model.CategoriaGasto
import com.example.controlfast.databinding.ActivitySummaryBinding
import com.example.controlfast.ui.viewmodel.ExpenseViewModel
import com.example.controlfast.utils.toCurrency
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class SummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySummaryBinding
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupPieChart()
        observeData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Resumen de Gastos"
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            // Configuración básica
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)

            // Rotación y animación
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 58f
            transparentCircleRadius = 61f

            // Texto en el centro
            setDrawCenterText(true)
            centerText = "Gastos por\nCategoría"
            setCenterTextSize(16f)

            // Rotación habilitada
            setRotationAngle(0f)
            isRotationEnabled = true
            isHighlightPerTapEnabled = true

            // Animación
            animateY(1400, Easing.EaseInOutQuad)

            // Leyenda
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                xEntrySpace = 7f
                yEntrySpace = 0f
                yOffset = 10f
                textSize = 12f
            }

            // Entrada de datos
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }
    }

    private fun observeData() {
        // Observar total de gastos
        viewModel.totalExpenses.observe(this) { total ->
            binding.tvTotalGastado.text = "Total Gastado: ${(total ?: 0.0).toCurrency()}"
        }

        // Observar gastos por categoría
        viewModel.expensesByCategory.observe(this) { categoryTotals ->
            if (categoryTotals.isNotEmpty()) {
                setupPieChartData(categoryTotals)
                displayCategorySummary(categoryTotals)
            }
        }

        // Observar todos los gastos para calcular estadísticas
        viewModel.allExpenses.observe(this) { expenses ->
            if (expenses.isNotEmpty()) {
                calculateAndDisplayStats(expenses)
            }
        }
    }

    private fun setupPieChartData(categoryTotals: List<com.example.controlfast.data.dao.CategoryTotal>) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        categoryTotals.forEach { categoryTotal ->
            entries.add(PieEntry(categoryTotal.total.toFloat(), categoryTotal.categoria))
            colors.add(CategoriaGasto.getColorByName(categoryTotal.categoria))
        }

        val dataSet = PieDataSet(entries, "").apply {
            sliceSpace = 3f
            selectionShift = 5f
            setColors(colors)
            valueTextColor = Color.WHITE
            valueTextSize = 14f
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
            setValueTextSize(14f)
            setValueTextColor(Color.WHITE)
        }

        binding.pieChart.data = data
        binding.pieChart.invalidate()
    }

    private fun displayCategorySummary(categoryTotals: List<com.example.controlfast.data.dao.CategoryTotal>) {
        val summaryText = StringBuilder()

        categoryTotals.sortedByDescending { it.total }.forEach { categoryTotal ->
            summaryText.append("${categoryTotal.categoria}: ${categoryTotal.total.toCurrency()}\n")
        }

        binding.tvCategorySummary.text = summaryText.toString().trim()
    }

    private fun calculateAndDisplayStats(expenses: List<com.example.controlfast.data.model.Expense>) {
        val total = expenses.sumOf { it.monto }
        val average = if (expenses.isNotEmpty()) total / expenses.size else 0.0
        val count = expenses.size

        val mostExpensiveCategory = expenses
            .groupBy { it.categoria }
            .mapValues { it.value.sumOf { expense -> expense.monto } }
            .maxByOrNull { it.value }

        val statsText = """
            Cantidad de gastos: $count
            Promedio por gasto: ${average.toCurrency()}
            Categoría con más gastos: ${mostExpensiveCategory?.key ?: "N/A"}
            Mayor gasto: ${mostExpensiveCategory?.value?.toCurrency() ?: "S/0.00"}
        """.trimIndent()

        binding.tvStatistics.text = statsText
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}