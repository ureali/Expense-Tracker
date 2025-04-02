package com.example.mad_411_assignments.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.mad_411_assignments.R
import android.icu.util.Currency as AndroidCurrency
import java.util.Locale

class ExpenseDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_expense_details, container, false)

        val nameTextView = view.findViewById<TextView>(R.id.expenseNameTextView)
        val amountTextView = view.findViewById<TextView>(R.id.amountTextView)
        val convertedTextView = view.findViewById<TextView>(R.id.convertedTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)


        val name = arguments?.getString("EXPENSE_NAME")
        val amount = arguments?.getDouble("EXPENSE_AMOUNT", 0.00)
        val convertedAmount = arguments?.getDouble("EXPENSE_CONVERTED", 0.00)

        // getting java currency symbol
        val currencyCode = arguments?.getString("EXPENSE_CODE")

        try {
            val javaCurrency = AndroidCurrency.getInstance(currencyCode?.uppercase())
            val symbol = javaCurrency.getSymbol()

            amountTextView.text = String.format(Locale.getDefault(), "%s%.2f", symbol, amount)

        } catch (e: IllegalArgumentException) {
            // the api contains crypto, need to handle it
            amountTextView.text = String.format(Locale.getDefault(), "%.2f %s", amount, currencyCode?.uppercase())
        }


        val date = arguments?.getString("EXPENSE_DATE")

        nameTextView.text = name
        // formatting so its always displayed correctly
        convertedTextView.text =  String.format(Locale.getDefault(), "$%.2f In CAD", convertedAmount)
        dateTextView.text = date
        return view
    }
}