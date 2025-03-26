package com.example.mad_411_assignments

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.text.TextUtils.replace
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val FILE_NAME = "expenses.txt"

class MainActivity : AppCompatActivity() {
    private lateinit var expenseNameEditText: EditText
    private lateinit var expenseAmountEditText: EditText
    private lateinit var datePickerButton: Button
    private lateinit var addNewExpenseButton: Button
    private lateinit var expenseAdapter: ExpenseAdapter

    private val viewModel: TotalAmountViewModel by viewModels()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ActivityLifecycle", "onCreate called")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // fragments
        // For some reason androidx.fragment.app wasn't implemented by default in gradle
        // had to add it manually for it to work

        // android docs told me that it's "using the FragmentManager to create a FragmentTransaction"
        // I hope that satisfies "Add HeaderFragment and FooterFragment dynamically using
        // FragmentTransaction and FragmentManager."
        supportFragmentManager.commit {
            replace(R.id.headerFrameLayout, HeaderFragment())
            addToBackStack(null)
        }

        val footerFragment = FooterFragment(viewModel)

        supportFragmentManager.commit {
            replace(R.id.footerFrameLayout, footerFragment)
            addToBackStack(null)
        }

        expenseNameEditText = findViewById<EditText>(R.id.expenseNameEditText)
        expenseAmountEditText = findViewById<EditText>(R.id.expenseAmountEditText)
        datePickerButton = findViewById<Button>(R.id.datePickerButton)
        addNewExpenseButton = findViewById<Button>(R.id.addNewExpenseButton)

        // developer.android.com ♥
        // creating dataset
        val dataset = mutableListOf<Expense>()

        // loading expenses
        dataset.clear()
        dataset.addAll(loadExpensesFromFile(this))

        expenseAdapter = ExpenseAdapter(dataset, this, viewModel, footerFragment, ::saveExpensesToFile)

        // setting up recycler
        val recyclerView: RecyclerView = findViewById(R.id.expenseRecyclerView)

        // using LinearLayout
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = expenseAdapter


        // adding onclick event listener
        addNewExpenseButton.setOnClickListener {
            addExpense()
            saveExpensesToFile(this, dataset)
        }

        datePickerButton.setOnClickListener {
            showDateDialog()
        }

    }

    // separate method for adding expense
    fun addExpense() {
        val name = expenseNameEditText.text.toString().trim()
        val amountString = expenseAmountEditText.text.toString().trim()
        val date = datePickerButton.text.toString()


        if (expenseNameEditText.text.toString().isEmpty()) {
            // cool way to show error
            expenseNameEditText.error = "Please enter a name!"
            return
        }

        val amount = try {
            amountString.toDouble()
        } catch (e: NumberFormatException) {
            expenseAmountEditText.error = "Invalid amount!"
            return
        }

        // stupid, but works more than fine
        if (datePickerButton.text == "Pick Date") {
            // error doesnt work properly :(
            // have to toast
            Toast.makeText(this, "PLEASE PICK A DATE!", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(name, amount, date)
        expenseAdapter.addExpense(expense)

        expenseNameEditText.text.clear()
        expenseAmountEditText.text.clear()
        datePickerButton.text = "Pick Date"
    }

    // docs were pretty much useless in this case, but I hope I got it right
    fun showDateDialog() {
        // the funny thing is that the calendar is from Java
        // I hope that's intended
        val calendar = Calendar.getInstance()

        // creating datePicker
        val datePicker = DatePickerDialog(this,
            { _, year, month, dayOfMonth ->
                // updating calendar
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // formatting for prettier output
                datePickerButton.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            },
            // updating date picker with values picked by users
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        // showing date picker
        datePicker.show()
    }

    private fun saveExpensesToFile(context: Context, taskList: MutableList<Expense>) {
        try {
            val json = Gson().toJson(taskList)
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
            }
            Log.d("FileStorage", "Expenses saved successfully")
        } catch (e: IOException) {
            Log.e("FileStorage", "Error saving expenses: ${e.message}")
        }
    }

    private fun loadExpensesFromFile(context: Context): MutableList<Expense> {
        val taskList: MutableList<Expense> = mutableListOf()
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return taskList

            // using openFIleInput
            context.openFileInput(FILE_NAME).use { input ->
                // does the same job as file.readText()
                // it's extension function from Kotlin
                val json = input.bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<Expense>>() {}.type
                val loadedTasks: List<Expense> = Gson().fromJson(json, type)
                taskList.addAll(loadedTasks)
                Log.d("FileStorage", "Expenses loaded successfully")
        }

        } catch (e: FileNotFoundException) {
            Log.e("FileStorage", "File not found: ${e.message}")
        } catch (e: IOException) {
            Log.e("FileStorage", "Error reading file: ${e.message}")
        }
        return taskList
    }

    override fun onStart() {
        super.onStart()
        Log.d("ActivityLifecycle", "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d("ActivityLifecycle", "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d("ActivityLifecycle", "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d("ActivityLifecycle", "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ActivityLifecycle", "onDestroy called")
    }
}