package com.example.mad_411_assignments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mad_411_assignments.model.Expense
import com.example.mad_411_assignments.model.viewmodel.TotalAmountViewModel
import com.example.mad_411_assignments.ui.FooterFragment
import java.util.Locale

// developer.android.com directed me here
// added context
class ExpenseAdapter(private val dataSet: MutableList<Expense>, var context:Context,
                     var totalAmountViewModel: TotalAmountViewModel, var footerFragment: FooterFragment,
                     var saveExpensesLambda: (context:Context, dataset:MutableList<Expense>) -> Unit,
                     var onDetailsClick: (expense: Expense) -> Unit,):RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // from what I understood, item in recycle view is a separate layout
        // here I "find" the elements there
        val expenseTextView = view.findViewById<TextView>(R.id.expenseTextView)
        val amountTextView = view.findViewById<TextView>(R.id.amountTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val deleteButton = view.findViewById<TextView>(R.id.deleteButton)
        val detailsButton = view.findViewById<TextView>(R.id.detailsButton)

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
        holder.expenseTextView.text = expense.name.toString()
        holder.dateTextView.text = expense.date.toString()
        // IntelliJ wanted String.format(), happy to oblige.
        holder.amountTextView.text = String.format(Locale.getDefault(), "$%.2f", expense.convertedCost)

        // event listener
        holder.deleteButton.setOnClickListener {
            // get the index of the expense we want to remove
            val index = dataSet.indexOf(expense)

            // just to be safe checking if expense exists at all
            if (index != -1) {
                // removing
                dataSet.removeAt(index)
                totalAmountViewModel.totalAmount -= expense.convertedCost
                // updating footer
                footerFragment.updateAmount()
                // need to notify to update the recycle view
                notifyItemRemoved(index)
            }

            saveExpensesLambda(context, dataSet)
        }

        // changing activity
        holder.detailsButton.setOnClickListener {
            onDetailsClick(expense)

        }
    }

    // need to insert new expense from other files
    fun addExpense(expense: Expense) {
        dataSet.add(expense)
        totalAmountViewModel.totalAmount += expense.convertedCost
        footerFragment.updateAmount()
        notifyItemInserted(dataSet.size - 1)
    }
}