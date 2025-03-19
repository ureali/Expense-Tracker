package com.example.mad_411_assignments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import java.util.Locale


class FooterFragment(private var viewModel: TotalAmountViewModel) : Fragment() {
    private val financialTipsLink = "https://www.victorianvoices.net/ARTICLES/CFM/CFM1878/CFM1878-PennyBanks.pdf"

    private lateinit var financialTipsButton:Button
    private lateinit var totalAmountTextView:TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_footer, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        financialTipsButton = view.findViewById<Button>(R.id.financialTipsButton)
        totalAmountTextView = view.findViewById<TextView>(R.id.totalAmountTextView)

        updateAmount()

        financialTipsButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(financialTipsLink)
            startActivity(intent)
        }

    }

    fun updateAmount() {
        totalAmountTextView.text = "${String.format(Locale.getDefault(), "$%.2f", viewModel.totalAmount)}"

    }

}