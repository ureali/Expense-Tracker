package com.example.mad_411_assignments.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.mad_411_assignments.R
import java.util.Locale

class ExpenseDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_expense_details, container, false)

        val name = arguments?.getString("EXPENSE_NAME")
        val amount = arguments?.getDouble("EXPENSE_AMOUNT", 0.00)
        val date = arguments?.getString("EXPENSE_DATE")

        val nameTextView = view.findViewById<TextView>(R.id.expenseNameTextView)
        val amountTextView = view.findViewById<TextView>(R.id.amountTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)

        nameTextView.text = name
        // formatting so its always displayed correctly
        amountTextView.text =  String.format(Locale.getDefault(), "$%.2f", amount)
        dateTextView.text = date
        return view
    }
}