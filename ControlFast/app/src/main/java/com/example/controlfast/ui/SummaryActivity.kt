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

/**
 * Pantalla de resumen de gastos.
 *
 * Muestra:
 * - Total gastado en el periodo.
 * - Gráfico de pastel con distribución por categoría.
 * - Resumen de montos por categoría.
 * - Estadísticas generales de los gastos registrados.
 */
class SummaryActivity : AppCompatActivity() {

    // ViewBinding para acceder a las vistas del layout sin findViewById
    private lateinit var binding: ActivitySummaryBinding

    // ViewModel compartido para obtener la información de gastos
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura la barra de herramientas, el gráfico y empieza a observar los datos
        setupToolbar()
        setupPieChart()
        observeData()
    }

    /**
     * Configura la Toolbar como ActionBar de esta Activity
     * y habilita el botón de navegación hacia atrás.
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Título mostrado en la barra superior
        supportActionBar?.title = "Resumen de Gastos"
        // Nota: podría extraerse a strings.xml para mejor internacionalización
    }

    /**
     * Configura las opciones visuales del gráfico de pastel (MPAndroidChart).
     * Aquí solo se define el estilo; los datos se cargan en otro método.
     */
    private fun setupPieChart() {
        binding.pieChart.apply {
            // Mostrar los valores en porcentaje
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)

            // Configuración del agujero central y efecto de rotación
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 58f
            transparentCircleRadius = 61f

            // Texto que se muestra en el centro del gráfico
            setDrawCenterText(true)
            centerText = "Gastos por\nCategoría"
            setCenterTextSize(16f)

            // Permite rotar el gráfico y resaltar segmentos al tocar
            setRotationAngle(0f)
            isRotationEnabled = true
            isHighlightPerTapEnabled = true

            // Animación vertical al cargar los datos
            animateY(1400, Easing.EaseInOutQuad)

            // Configuración de la leyenda (ubicación y estilo)
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

            // Apariencia de las etiquetas de cada segmento
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }
    }

    /**
     * Observa los datos expuestos por el ViewModel:
     * - Total de gastos
     * - Gastos agrupados por categoría
     * - Lista completa de gastos para calcular estadísticas
     */
    private fun observeData() {
        // Observar total de gastos del periodo
        viewModel.totalExpenses.observe(this) { total ->
            // Se usa una extensión toCurrency() para formatear el monto
            binding.tvTotalGastado.text = "Total Gastado: ${(total ?: 0.0).toCurrency()}"
            // Nota: este texto también podría formatearse con getString(R.string.xxx, ...)
        }

        // Observar gastos agrupados por categoría para alimentar el gráfico y el resumen
        viewModel.expensesByCategory.observe(this) { categoryTotals ->
            if (categoryTotals.isNotEmpty()) {
                setupPieChartData(categoryTotals)
                displayCategorySummary(categoryTotals)
            }
            // Si está vacío, se dejará el mensaje por defecto definido en el layout/strings
        }

        // Observar todos los gastos para calcular estadísticas generales
        viewModel.allExpenses.observe(this) { expenses ->
            if (expenses.isNotEmpty()) {
                calculateAndDisplayStats(expenses)
            }
        }
    }

    /**
     * Carga los datos de gastos por categoría en el gráfico de pastel.
     *
     * @param categoryTotals lista con el total por cada categoría.
     */
    private fun setupPieChartData(
        categoryTotals: List<com.example.controlfast.data.dao.CategoryTotal>
    ) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        // Crear las entradas del gráfico y asignar un color a cada categoría
        categoryTotals.forEach { categoryTotal ->
            entries.add(
                PieEntry(
                    categoryTotal.total.toFloat(),
                    categoryTotal.categoria
                )
            )
            // Se obtiene el color asociado a la categoría desde el modelo
            colors.add(CategoriaGasto.getColorByName(categoryTotal.categoria))
        }

        // Configuración del conjunto de datos del gráfico
        val dataSet = PieDataSet(entries, "").apply {
            sliceSpace = 3f
            selectionShift = 5f
            setColors(colors)
            valueTextColor = Color.WHITE
            valueTextSize = 14f
        }

        // Se configura el formato de los valores (porcentaje) y estilo del texto
        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
            setValueTextSize(14f)
            setValueTextColor(Color.WHITE)
        }

        // Asignar datos al gráfico y refrescar la vista
        binding.pieChart.data = data
        binding.pieChart.invalidate()
    }

    /**
     * Construye un texto resumen con el total gastado por cada categoría
     * y lo muestra en la sección de "Gastos por Categoría".
     */
    private fun displayCategorySummary(
        categoryTotals: List<com.example.controlfast.data.dao.CategoryTotal>
    ) {
        val summaryText = StringBuilder()

        // Ordena las categorías de mayor a menor total para una lectura más clara
        categoryTotals
            .sortedByDescending { it.total }
            .forEach { categoryTotal ->
                summaryText.append(
                    "${categoryTotal.categoria}: ${categoryTotal.total.toCurrency()}\n"
                )
            }

        binding.tvCategorySummary.text = summaryText.toString().trim()
    }

    /**
     * Calcula estadísticas generales de los gastos:
     * - Cantidad de registros.
     * - Promedio por gasto.
     * - Categoría con mayor gasto acumulado.
     * - Monto del mayor gasto acumulado por categoría.
     */
    private fun calculateAndDisplayStats(
        expenses: List<com.example.controlfast.data.model.Expense>
    ) {
        val total = expenses.sumOf { it.monto }
        val average = if (expenses.isNotEmpty()) total / expenses.size else 0.0
        val count = expenses.size

        // Agrupa por categoría y suma el total de cada una,
        // luego toma la categoría con mayor monto acumulado
        val mostExpensiveCategory = expenses
            .groupBy { it.categoria }
            .mapValues { entry -> entry.value.sumOf { expense -> expense.monto } }
            .maxByOrNull { it.value }

        // Texto formateado con las estadísticas principales
        val statsText = """
            Cantidad de gastos: $count
            Promedio por gasto: ${average.toCurrency()}
            Categoría con más gastos: ${mostExpensiveCategory?.key ?: "N/A"}
            Mayor gasto: ${mostExpensiveCategory?.value?.toCurrency() ?: "S/0.00"}
        """.trimIndent()

        binding.tvStatistics.text = statsText
    }

    /**
     * Maneja el comportamiento del botón de retroceso en la Toolbar.
     */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
