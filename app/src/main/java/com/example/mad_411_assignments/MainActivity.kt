package com.example.mad_411_assignments

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var expenseNameEditText: EditText
    private lateinit var expenseAmountEditText: EditText
    private lateinit var expenseDateEditText: EditText
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
        expenseDateEditText = findViewById<EditText>(R.id.expenseDateEditText)
        addNewExpenseButton = findViewById<Button>(R.id.addNewExpenseButton)

        // developer.android.com ♥
        // creating dataset
        val dataset = mutableListOf<Expense>()
        val expenseAdapter = ExpenseAdapter(dataset)

        // setting up recycler
        val recyclerView: RecyclerView = findViewById(R.id.expenseRecyclerView)

        // using LinearLayout
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = expenseAdapter

    }

    // separate method for adding expense
    fun addExpense() {
        val name = expenseNameEditText.text.toString().trim()
        val amountString = expenseAmountEditText.text.toString().trim()
        val date = expenseDateEditText.text.toString().trim()

        val amount = try {
            amountString.toDouble()
        } catch (e: NumberFormatException) {
            expenseAmountEditText.error = "Invalid amount!"
            return
        }

        val expense = Expense(name, amount, date)
        expenseAdapter.addExpense(expense)

        expenseNameEditText.text.clear()
        expenseAmountEditText.text.clear()
        expenseDateEditText.text.clear()
    }
}