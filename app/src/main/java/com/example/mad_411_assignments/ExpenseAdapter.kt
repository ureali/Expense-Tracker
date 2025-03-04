package com.example.mad_411_assignments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment6.R
import java.util.Locale

// developer.android.com directed me here
class ExpenseAdapter(private val dataSet: MutableList<Expense>):RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {
    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // from what I understood, item in recycle view is a separate layout
        // here I "find" the elements there
        val expenseTextView = view.findViewById<TextView>(R.id.expenseTextView)
        val amountTextView = view.findViewById<TextView>(R.id.amountTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val deleteButton = view.findViewById<TextView>(R.id.deleteButton)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        // only Lord knows what's REALLY going on here, but if the compiler is happy, I'm happy too.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.expense, parent, false)

        return ExpenseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        // getting the current expense
        val expense = dataSet[position]

        // populating textViews
        holder.expenseTextView.text = expense.name.toString();
        holder.dateTextView.text = expense.date.toString();
        // IntelliJ wanted String.format(), happy to oblige.
        holder.amountTextView.text = String.format(Locale.getDefault(), "$%.2f", expense.amount)
        holder.expenseTextView.text = expense.name.toString();

        // event listener
        holder.deleteButton.setOnClickListener {
            // get the index of the expense we want to remove
            val index = dataSet.indexOf(expense)

            // just to be safe checking if expense exists at all
            if (index != -1) {
                // removing
                dataSet.removeAt(index)
                // need to notify to update the recycle view
                notifyItemRemoved(index)
            }
        }

    }

}