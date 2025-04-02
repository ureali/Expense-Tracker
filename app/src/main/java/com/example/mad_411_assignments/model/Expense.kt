package com.example.mad_411_assignments.model

// Uncle Google led me here...
// I should create a data class holding info about expenses
data class Expense(val name: String, var convertedCost: Double, val date: String,  var currency: Currency) {

}