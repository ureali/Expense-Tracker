package com.example.mad_411_assignments.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mad_411_assignments.ExpenseAdapter
import com.example.mad_411_assignments.R
import com.example.mad_411_assignments.model.Currency
import com.example.mad_411_assignments.model.Expense
import com.example.mad_411_assignments.model.viewmodel.TotalAmountViewModel
import com.example.mad_411_assignments.network.RetrofitInstance
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val FILE_NAME = "expenses.txt"
// the requirement of converting currency makes absolute zero sense to me (convert from what to what,why, when?)
// I _guess_ it means all currencies should have an option to be converted to cad
private val DEFAULT_CURRENCY: Currency = Currency("cad", "Canadian Dollar")

class ExpenseListFragment : Fragment() {
    private lateinit var expenseNameEditText: EditText
    private lateinit var expenseAmountEditText: EditText
    private lateinit var conversionNeededCheckBox: CheckBox
    private lateinit var currencySpinner: Spinner
    private lateinit var convertedAmount: TextView
    private lateinit var datePickerButton: Button
    private lateinit var addNewExpenseButton: Button
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var currencies: List<Currency>
    private var isConverting = false
    private val viewModel: TotalAmountViewModel by viewModels()
    private val fragmentJob = SupervisorJob()
    private val fragmentScope = CoroutineScope(Dispatchers.Main + fragmentJob)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_expense_list, container, false)
        // fragments
        // For some reason androidx.fragment.app wasn't implemented by default in gradle
        // had to add it manually for it to work

        // android docs told me that it's "using the FragmentManager to create a FragmentTransaction"
        // I hope that satisfies "Add HeaderFragment and FooterFragment dynamically using
        // FragmentTransaction and FragmentManager."
        childFragmentManager.commit {
            replace(R.id.headerFrameLayout, HeaderFragment())
            addToBackStack(null)
        }

        val footerFragment = FooterFragment(viewModel)


        childFragmentManager.commit {
            replace(R.id.footerFrameLayout, footerFragment)
            addToBackStack(null)
        }

        expenseNameEditText = view.findViewById<EditText>(R.id.expenseNameEditText)
        expenseAmountEditText = view.findViewById<EditText>(R.id.expenseAmountEditText)
        datePickerButton = view.findViewById<Button>(R.id.datePickerButton)
        addNewExpenseButton = view.findViewById<Button>(R.id.addNewExpenseButton)
        conversionNeededCheckBox = view.findViewById<CheckBox>(R.id.conversionNeededCheckBox)
        currencySpinner = view.findViewById<Spinner>(R.id.currencySpinner)
        convertedAmount = view.findViewById<TextView>(R.id.convertedAmount)

        // fetching currencies
        lifecycleScope.launch {
            try {
                currencies = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getCurrencies()
                }.map { (code, name) -> Currency(code, name) }

                // converting the currencies to something more user-readable
                val spinnerCurrencies = currencies.map { currency ->
                    currency.code.uppercase()
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerCurrencies)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                currencySpinner.adapter = adapter

                // selecting cad
                currencySpinner.setSelection(spinnerCurrencies.indexOf(DEFAULT_CURRENCY.code.uppercase()))

                // uncomment to test if currencies work
//                for (currency in currencies) {
//                    Log.d("CURRENCIES", "${currency.code}: ${currency.name}")
//                }

            } catch (e: Exception) {
                Log.e("ERROR_CURRENCY", "${e.message}")
                Snackbar.make(requireView(), "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }


        // developer.android.com ♥
        // creating dataset
        val dataset = mutableListOf<Expense>()

        // loading expenses
        fragmentScope.launch {
            //Off load file io to background thread
            val loadedExpenses = withContext(Dispatchers.IO) {
                loadExpensesFromFile()
            }
            dataset.clear()
            dataset.addAll(loadedExpenses)
            expenseAdapter.notifyDataSetChanged()
            viewModel.totalAmount = dataset.sumOf { it.convertedCost }
        }


        expenseAdapter = ExpenseAdapter(
            dataset, this.requireContext(), viewModel, footerFragment,
            ::saveExpensesToFile, ::onDetailsClick
        )

        // setting up recycler
        val recyclerView: RecyclerView = view.findViewById(R.id.expenseRecyclerView)

        // using LinearLayout
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        recyclerView.adapter = expenseAdapter


        // adding onclick event listener
        addNewExpenseButton.setOnClickListener {
            addExpense(dataset)
        }

        datePickerButton.setOnClickListener {
            showDateDialog()
        }

        conversionNeededCheckBox.setOnClickListener{
            isConverting = !isConverting
        }

        expenseAmountEditText.setOnFocusChangeListener { view, hasFocus ->
            if (isConverting) {
                val amountString = expenseAmountEditText.text.toString().trim()

                lifecycleScope.launch {
                    try {
                        val amount = amountString.toDouble()
                        // some formatting goes a long way
                        convertedAmount.text = String.format(Locale.getDefault(), "%.2f CAD", convertCurrency(currencySpinner.selectedItem.toString().lowercase(), amount))
                    } catch (e: NumberFormatException) {
                        expenseAmountEditText.error = "Invalid amount!"
                    }
                }
            }
        }

        return view

    }

    // method to asynchronously update currency
    suspend fun convertCurrency(currencyCode: String, amount: Double):Double {
        var convertedAmount = -1.0
            // this assignment taught me why API conventions exist :(
            // it's physically painful to work with this one
            try {
                // getting the conversion rates relative to the currency we currently have
                val response:Map<String, Any> = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getConversionRates(DEFAULT_CURRENCY.code)
                }

                // i don't know what exactly intellij doesn't like, but it works
                // trying to get the key equal to the currency selected (>:-() and casting it to String, Double
                // the fun part there are currencies with no conversion rates, it should prevent them from crashing the program
                val ratesData = response[DEFAULT_CURRENCY.code] as? Map<String, Double>
                    ?: throw error("Something went wrong with the api :( ")


                // just to be sure assigning 1.0 if something is wrong
                val conversionRate: Double = ratesData[currencyCode] ?: 1.0
                convertedAmount = amount / conversionRate

                // uncomment to test if currencies work
//                for (currency in currencies) {
//                    Log.d("CURRENCIES", "${currency.code}: ${currency.name}")
//                }

            } catch (e: Exception) {
                Log.e("ERROR_CURRENCY", "${e.message}")
                Snackbar.make(requireView(), "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }

        return convertedAmount
    }

    // separate method for adding expense
    fun addExpense(dataset:MutableList<Expense>) {
        val name = expenseNameEditText.text.toString().trim()
        val date = datePickerButton.text.toString()
        var amountString = ""

        if (!isConverting) {
            amountString = expenseAmountEditText.text.toString().trim()
        } else {
            amountString = convertedAmount.text.toString().trim().split(" ")[0]
        }

        Log.d("DEBUG_AMOUNT", amountString)
        var selectedCurrency = DEFAULT_CURRENCY
        try {
            if (!isConverting) {
                selectedCurrency = currencies.find{ it.code == currencySpinner.selectedItem.toString().lowercase()}!!
            }
        } catch (e:Error) {
            Toast.makeText(this.requireContext(), "CURRENCIES NOT LOADED OR SELECTED", Toast.LENGTH_SHORT).show()
            return
        }



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
            Toast.makeText(this.requireContext(), "PLEASE PICK A DATE!", Toast.LENGTH_SHORT).show()
            return
        }

        // I don't really understand what converted amount means (???) and why should we implement it
        // but ok, I'll assume it's amount converted to CAD
        lifecycleScope.launch {
            val convertedAmount = convertCurrency(selectedCurrency.code, amount)

            val expense = Expense(name, amount, date, selectedCurrency, convertedAmount)
            expenseAdapter.addExpense(expense)

            expenseNameEditText.text.clear()
            expenseAmountEditText.text.clear()
            datePickerButton.text = "Pick Date"

            saveExpensesToFile(requireContext(), dataset)
        }


    }

    // docs were pretty much useless in this case, but I hope I got it right
    fun showDateDialog() {
        // the funny thing is that the calendar is from Java
        // I hope that's intended
        val calendar = Calendar.getInstance()

        // creating datePicker
        val datePicker = DatePickerDialog(
            this.requireContext(),
            { _, year, month, dayOfMonth ->
                // updating calendar
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // formatting for prettier output
                datePickerButton.text =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            },
            // updating date picker with values picked by users
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // showing date picker
        datePicker.show()
    }

    private fun saveExpensesToFile(context: Context, taskList: MutableList<Expense>) {
        try {
            fragmentScope.launch(Dispatchers.IO) {
                val json = Gson().toJson(taskList)
                val file = File(context.filesDir, FILE_NAME)
                file.writeText(json)
            }
            Log.d("FileStorage", "Expenses saved successfully")
        } catch (e: IOException) {
            Log.e("FileStorage", "Error saving expenses: ${e.message}")
        }
    }

    private fun loadExpensesFromFile(): MutableList<Expense> {
        val expenseList: MutableList<Expense> = mutableListOf()
        try {
            val file = File(requireContext().filesDir, FILE_NAME)
            if (!file.exists()) return mutableListOf()

            val json = file.readText()
            val type = object : TypeToken<MutableList<Expense>>() {}.type
            return Gson().fromJson(json, type)

        } catch (e: FileNotFoundException) {
            Log.e("FileStorage", "File not found: ${e.message}")
        } catch (e: IOException) {
            Log.e("FileStorage", "Error reading file: ${e.message}")
        }
        return expenseList
    }

    fun onDetailsClick(expense: Expense) {
        val bundle = Bundle().apply {
            putString("EXPENSE_NAME", expense.name)
            putString("EXPENSE_DATE", expense.date)
            putDouble("EXPENSE_AMOUNT", expense.amount)
            putString("EXPENSE_CODE", expense.currency.code)
            putDouble("EXPENSE_CONVERTED", expense.convertedCost)
        }
        findNavController().navigate(R.id.action_mainFragment_to_expenseDetailsFragment, bundle)
    }
}