# ControlFast 🧮💸  
Aplicación móvil para el control simple de gastos personales

---

## 📌 Descripción General

**ControlFast** es una aplicación Android desarrollada en **Kotlin** que permite registrar, organizar y visualizar gastos personales de forma sencilla.  
Está pensada como una app educativa y funcional para practicar:

- Arquitectura **MVVM**
- Uso de **Room** (Base de datos local)
- **LiveData** / **ViewModel**
- Buenas prácticas en **UI** e internacionalización de cadenas (`strings.xml`)
- Gráficas estadísticas con **MPAndroidChart**

---

## 🎯 Objetivos de la aplicación

- Facilitar el **registro diario de gastos** (monto, categoría, descripción y fecha).
- Mostrar de forma clara el **total gastado** y los **gastos por categoría**.
- Ofrecer un **resumen gráfico** (gráfico de pastel) y estadísticas básicas para analizar hábitos de consumo.

---

## ✅ Funcionalidades principales

1. **Registro y edición de gastos**
   - Pantalla para crear y editar gastos (`AddExpenseActivity`).
   - Campos: **monto**, **categoría**, **descripción**, **fecha**.
   - Validación de monto y descripción antes de guardar.
   - Modo **nuevo** y **editar** (el botón cambia a *“Actualizar”*).

2. **Listado de gastos**
   - Lista de gastos en un **RecyclerView** con `ExpenseAdapter`.
   - Cada ítem muestra: descripción, categoría, fecha y monto.
   - **Click corto** → editar gasto.
   - **Click largo** → diálogo de confirmación para eliminar.

3. **Resumen y estadísticas**
   - Pantalla de resumen (`SummaryActivity`) con:
     - Total gastado.
     - **Gráfico de pastel** con gastos por categoría.
     - Resumen de totales por categoría.
     - Estadísticas generales: cantidad de gastos, promedio por gasto, categoría con mayor gasto.

4. **Eliminación de gastos**
   - Eliminar un gasto individual (diálogo de confirmación).
   - Eliminar **todos los gastos** desde el menú de la pantalla principal.

5. **Persistencia de datos**
   - Todos los gastos se almacenan en una base de datos local usando **Room**:
     - Entidad: `Expense`
     - DAO: `ExpenseDao`
     - Base de datos: `AppDatabase`

---

## 🏗 Arquitectura

La app sigue una arquitectura basada en **MVVM + Repository**:

- **UI (View)**
  - `MainActivity`
  - `AddExpenseActivity`
  - `SummaryActivity`
  - Layouts: `activity_main.xml`, `activity_add_expense.xml`, `activity_summary.xml`, `item_expense.xml`

- **ViewModel**
  - `ExpenseViewModel`
    - Expone `LiveData<List<Expense>>`, `LiveData<Double?>`, `LiveData<List<CategoryTotal>>`
    - Gestiona operaciones de inserción, actualización, eliminación y estadísticas.

- **Repository**
  - `ExpenseRepository`
    - Encapsula el acceso a `ExpenseDao`.
    - Ofrece métodos de más alto nivel: `insert`, `update`, `delete`, `getExpensesThisMonth`, `getStatistics`, etc.

- **Data (Room)**
  - `Expense` (Entidad principal)
  - `CategoriaGasto` (enum de categorías con colores)
  - `CategoryTotal` (proyección para totales por categoría)
  - `ExpenseDao`
  - `AppDatabase`

- **Utils**
  - Extensiones para:
    - `hideKeyboard()`
    - `showToast()`, `showSnackbar()`
    - `visible()`, `gone()`, `invisible()`
    - `toCurrency()` (formato moneda **es-PE**)
    - `toDateString()`, `toDoubleOrZero()`
    - `getStartOfMonth()`, `getEndOfMonth()`
    - `isValidAmount()`

---

## 🧱 Estructura del proyecto (resumen)

```text
com.example.controlfast
├── data
│   ├── model
│   │   ├── Expense.kt
│   │   └── CategoriaGasto.kt
│   ├── dao
│   │   └── ExpenseDao.kt
│   ├── database
│   │   └── AppDatabase.kt
│   └── repository
│       ├── ExpenseRepository.kt
│       └── ExpenseStatistics.kt
├── ui
│   ├── MainActivity.kt
│   ├── AddExpenseActivity.kt
│   ├── SummaryActivity.kt
│   ├── adapter
│   │   └── ExpenseAdapter.kt
│   └── viewmodel
│       └── ExpenseViewModel.kt
├── utils
│   └── Extensions.kt (funciones de utilidad)
└── res
    ├── layout
    │   ├── activity_main.xml
    │   ├── activity_add_expense.xml
    │   ├── activity_summary.xml
    │   └── item_expense.xml
    ├── menu
    │   └── menu_main.xml
    └── values
        └── strings.xml
```

---

## 🛠 Tecnologías y librerías utilizadas

- **Lenguaje**: Kotlin
- **Arquitectura**: MVVM + Repository
- **Base de datos local**: Room
- **Reactive UI**: LiveData + ViewModel
- **Corrutinas**: `viewModelScope.launch` para operaciones en background
- **Gráficos**: MPAndroidChart (`PieChart`)
- **UI**:
  - Material Components (`MaterialToolbar`, `MaterialCardView`, `TextInputLayout`, `FloatingActionButton`, etc.)
  - ViewBinding
- **Formateo de datos**:
  - `NumberFormat` para moneda (Locale "es", "PE")
  - `SimpleDateFormat` para fechas

---

## 💡 Buenas prácticas aplicadas

- **Cadenas de texto en `strings.xml`**
  - Textos de `text`, `hint`, `contentDescription` y diálogos centralizados en `res/values/strings.xml`.
  - Facilita mantenimiento e internacionalización.

- **Separación de capas**
  - UI → ViewModel → Repository → DAO
  - La UI no accede directamente a Room.

- **Uso de LiveData**
  - La UI observa cambios en la base de datos (lista de gastos, total, totales por categoría).

- **Extensiones reutilizables**
  - Para teclado, toasts, snackbars, visibilidad y formateo.

- **Código comentado**
  - Clases principales documentadas con comentarios en español para facilitar revisión y sustentación.

---

## 🚀 Cómo ejecutar el proyecto

1. **Clonar o importar el proyecto**

   ```bash
   git clone https://github.com/ALTgremox/finaldispositivos.git
   ```

   (O abrir la carpeta del proyecto desde Android Studio).

2. **Abrir en Android Studio**
   - Archivo → *Open* → seleccionar la carpeta del proyecto.
   - Esperar a que Gradle sincronice las dependencias.

3. **Configurar el emulador o dispositivo**
   - Usar un emulador o dispositivo físico compatible con la `minSdk` definida en el `build.gradle` del módulo app.

4. **Ejecutar**
   - Click en ▶ (*Run ‘app’*).
   - Seleccionar el dispositivo/emulador.

---

## 🖥 Flujo de uso

1. **Pantalla principal (`MainActivity`)**
   - Se muestra el **total de gastos** en la parte superior.
   - Debajo, la lista de gastos (o mensaje de estado vacío si no hay registros).
   - FAB “+” para agregar un nuevo gasto.
   - FAB de resumen para abrir la pantalla de estadísticas.

2. **Agregar / editar gasto (`AddExpenseActivity`)**
   - Ingresar monto, categoría, descripción y fecha.
   - Botón **Guardar** o **Actualizar** según el modo.
   - Botón **Cancelar** para volver sin guardar.

3. **Resumen (`SummaryActivity`)**
   - Total gastado en el periodo.
   - Gráfico de pastel con distribución por categoría.
   - Resumen en texto de gastos por categoría.
   - Estadísticas generales de los gastos.

---

## 🔮 Posibles mejoras futuras

- Filtro de gastos por rango de fechas.
- Exportar datos a CSV / PDF.
- Modo oscuro personalizado.
- Backup en la nube (Firebase / API propia).
- Autenticación de usuario para múltiples perfiles.
