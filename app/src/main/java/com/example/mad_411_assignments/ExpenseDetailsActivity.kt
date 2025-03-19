package com.example.mad_411_assignments

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class ExpenseDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val name = intent.getStringExtra("EXPENSE_NAME")
        val amount = intent.getDoubleExtra("EXPENSE_AMOUNT", 0.00)
        val date = intent.getStringExtra("EXPENSE_DATE")

        val nameTextView = findViewById<TextView>(R.id.expenseNameTextView)
        val amountTextView = findViewById<TextView>(R.id.amountTextView)
        val dateTextView = findViewById<TextView>(R.id.dateTextView)

        nameTextView.text = name
        // formatting so its always displayed correctly
        amountTextView.text =  String.format(Locale.getDefault(), "$%.2f", amount)
        dateTextView.text = date
    }
}