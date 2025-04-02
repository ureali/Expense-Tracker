package com.example.mad_411_assignments.model

// Uncle Google led me here...
// I should create a data class holding info about expenses

// added converted cost and currency
data class Expense(val name: String, var amount: Double, val date: String,  var currency: Currency, var convertedCost: Double) {

}