package ru.viable.bundlepivotalindicesdemo.presentation

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import ru.viable.bundlepivotalindicesdemo.R
import ru.viable.bundlepivotalindicesdemo.data.Preprocessing
import ru.viable.bundlepivotalindicesdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val types = resources.getStringArray(R.array.networks)
        val arrayAdapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, types)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.gvSelector.adapter = arrayAdapter
        binding.gvSelector.setSelection(1)

        binding.gvExample.setOnClickListener { _ ->

            val file = when (binding.gvSelector.selectedItemId) {
                0L -> "Gv_Example.gml"
                1L -> "Gv_Energy.gml"
                else -> "Gv_Example.gml"
            }

            binding.progress.visibility = View.VISIBLE
            Preprocessing().calculate(
                applicationContext,
                file,
                object : CalculationCallback {
                    override fun onReceiveResults(s: String) {
                        binding.resultText.text = s
                        binding.progress.visibility = View.INVISIBLE
                    }
                },
            )
        }
    }
}

interface CalculationCallback {
    fun onReceiveResults(s: String)
}
