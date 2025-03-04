package com.example.mad_411_assignments

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment6.R

// developer.android.com directed me here
class ExpenseAdapter(private val dataSet: MutableList<Expense>):RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {
    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            textView = view.findViewById(R.id.textView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

}