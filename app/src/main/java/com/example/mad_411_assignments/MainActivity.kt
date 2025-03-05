package com.example.mad_411_assignments

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var expenseNameEditText: EditText
    private lateinit var expenseAmountEditText: EditText
    private lateinit var datePickerButton: Button
    private lateinit var addNewExpenseButton: Button
    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        expenseNameEditText = findViewById<EditText>(R.id.expenseNameEditText)
        expenseAmountEditText = findViewById<EditText>(R.id.expenseAmountEditText)
        datePickerButton = findViewById<Button>(R.id.datePickerButton)
        addNewExpenseButton = findViewById<Button>(R.id.addNewExpenseButton)

        // developer.android.com ♥
        // creating dataset
        val dataset = mutableListOf<Expense>()
        expenseAdapter = ExpenseAdapter(dataset)

        // setting up recycler
        val recyclerView: RecyclerView = findViewById(R.id.expenseRecyclerView)

        // using LinearLayout
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = expenseAdapter


        // adding onclick event listener
        addNewExpenseButton.setOnClickListener {
            addExpense()
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
}