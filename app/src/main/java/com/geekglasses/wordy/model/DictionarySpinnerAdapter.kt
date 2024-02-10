package com.geekglasses.wordy.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.annotation.Nullable
import com.geekglasses.wordy.R

class DictionarySpinnerAdapter(context: Context, private val items: List<String>) :
    ArrayAdapter<String>(context, R.layout.item_dictionary, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_dictionary, parent, false)
        val textView = view.findViewById<TextView>(R.id.textView)

        textView.visibility = View.VISIBLE
        textView.text = items[position]


        return view
    }
}