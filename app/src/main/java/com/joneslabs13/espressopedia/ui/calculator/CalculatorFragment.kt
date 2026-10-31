package com.joneslabs13.espressopedia.ui.calculator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.joneslabs13.espressopedia.data.AppPreferences
import com.joneslabs13.espressopedia.databinding.FragmentCalculatorBinding
import java.text.DecimalFormat


class CalculatorFragment : Fragment() {

            private var _binding: FragmentCalculatorBinding? = null
            private val binding get() = _binding!!

            private lateinit var editTextWaterRatio: EditText
            private lateinit var editTextCoffeeRatio: EditText
            private lateinit var spinnerItemSelector: Spinner
            private lateinit var editTextAmount: EditText
            private lateinit var spinnerUnit: Spinner
            private lateinit var buttonCalculate: ExtendedFloatingActionButton
            private lateinit var buttonGuide: FloatingActionButton

            // New TextViews for all unit results
            private lateinit var textViewWaterGrams: TextView
            private lateinit var textViewWaterOz: TextView
            private lateinit var textViewWaterMl: TextView
            private lateinit var textViewWaterCups: TextView
            private lateinit var textViewCoffeeGrams: TextView
            private lateinit var textViewCoffeeOz: TextView
            private lateinit var textViewCoffeeTsp: TextView
            private lateinit var textViewCoffeeTbsp: TextView

            private var waterRatio = 1.0
            private var coffeeRatio = 1.0
            private var amount = 1.0
            private var isWaterSelected = true

            enum class Unit {
                GRAMS, OZ, ML, CUPS, TSP, TBSP
            }

            private var unit = Unit.GRAMS

            override fun onCreateView(
                inflater: LayoutInflater, container: ViewGroup?,
                savedInstanceState: Bundle?
            ): View {
                _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
                val view = binding.root

                editTextWaterRatio = binding.editTextWaterRatio
                editTextCoffeeRatio = binding.editTextCoffeeRatio
                editTextCoffeeRatio.setText(AppPreferences.getDefaultCoffeeRatio(requireContext()).toString())
                editTextWaterRatio.setText(AppPreferences.getDefaultWaterRatio(requireContext()).toString())
                spinnerItemSelector = binding.spinnerItemSelector
                editTextAmount = binding.editTextAmount
                spinnerUnit = binding.spinnerUnit
                buttonCalculate = binding.buttonCalculate
                buttonGuide = binding.buttonGuide

                // Initialize new TextViews
                textViewWaterGrams = binding.textViewWaterGrams
                textViewWaterOz = binding.textViewWaterOz
                textViewWaterMl = binding.textViewWaterMl
                textViewWaterCups = binding.textViewWaterCups
                textViewCoffeeGrams = binding.textViewCoffeeGrams
                textViewCoffeeOz = binding.textViewCoffeeOz
                textViewCoffeeTsp = binding.textViewCoffeeTsp
                textViewCoffeeTbsp = binding.textViewCoffeeTbsp
                //set spinner size
                val params = spinnerItemSelector.layoutParams
                params.width = 500
                params.height = 200
                spinnerItemSelector.layoutParams = params

                setupSpinners()
                setupEditTexts()
//                updateResults()
                setupCalculateButton()

                return view
            }

            private fun setupCalculateButton() {
                buttonCalculate.setOnClickListener {
                    calculateAndDisplayResults()
                }
                setupGuideButton()
            }

            private fun setupGuideButton() {
                buttonGuide.setOnClickListener {
                    showGuideDialog()
                }
            }

            private fun showGuideDialog() {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Calculator Guide")
                    .setMessage(
                        "Welcome to the Coffee & Water Ratio Calculator!\n\n" +
                        "1. **Ratios**: Enter your desired Coffee and Water ratios. Common brew ratios include:\n" +
                        "   • **Espresso**: ~1:2 ratio (e.g., 1 part coffee to 2 parts water)\n" +
                        "   • **Pour Over / Drip**: ~1:15 to 1:17 ratio (e.g., 1:16 standard brew)\n\n" +
                        "2. **Item Selector**: Choose whether you are starting with a known amount of Water or Coffee.\n\n" +
                        "3. **Amount & Unit**: Enter the quantity you have and select your preferred unit (grams, oz, mL, cups, tsp, tbsp).\n\n" +
                        "4. **Calculate**: Tap the 'Calculate' button to view precise equivalent amounts for both coffee and water across all units!"
                    )
                    .setPositiveButton("Got It") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            private fun setupSpinners() {
                val itemOptions = arrayOf("Water", "Coffee")

                // Item Selector Spinner
                val itemAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemOptions)
                itemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerItemSelector.adapter = itemAdapter
                spinnerItemSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        isWaterSelected = (position == 0)
                        updateUnitSpinner()
//                        updateResults()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                // Unit Spinner
                updateUnitSpinner()
                spinnerUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        unit = when (parent?.getItemAtPosition(position)) {
                            "Grams" -> Unit.GRAMS
                            "oz" -> Unit.OZ
                            "mL" -> Unit.ML
                            "Cups" -> Unit.CUPS
                            "Tsp" -> Unit.TSP
                            "Tbsp" -> Unit.TBSP
                            else -> Unit.GRAMS
                        }
//                        updateResults()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            private fun updateUnitSpinner() {
                val unitOptions = if (isWaterSelected) {
                    arrayOf("Grams", "oz", "mL", "Cups")
                } else {
                    arrayOf("Grams", "oz", "Tsp", "Tbsp")
                }

                val unitAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unitOptions)
                unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerUnit.adapter = unitAdapter
            }

            private fun setupEditTexts() {
                editTextWaterRatio.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        waterRatio = s.toString().toDoubleOrNull() ?: 1.0
//                        updateResults()
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                editTextCoffeeRatio.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        coffeeRatio = s.toString().toDoubleOrNull() ?: 1.0
//                        updateResults()
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                editTextAmount.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        amount = s.toString().toDoubleOrNull() ?: 1.0
//                        updateResults()
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })
            }

            private fun calculateAndDisplayResults() {
                // Get user input values (with error handling)
                val waterRatioInput = editTextWaterRatio.text.toString().toDoubleOrNull() ?: 1.0
                val coffeeRatioInput = editTextCoffeeRatio.text.toString().toDoubleOrNull() ?: 1.0
                val amountInput = editTextAmount.text.toString().toDoubleOrNull() ?: 0.0

                // Check for valid input
                if (waterRatioInput <= 0 || coffeeRatioInput <= 0 || amountInput <= 0) {
                    Toast.makeText(requireContext(), "Invalid input. Please check your values.", Toast.LENGTH_SHORT).show()
                    return
                }
                // Convert the input to grams
                val amountInGrams = if (isWaterSelected) {
                    convertWaterToGrams(amountInput, unit)
                } else {
                    convertCoffeeToGrams(amountInput, unit)
                }

                // Calculate the ratio
                val waterAmountGrams: Double
                val coffeeAmountGrams: Double
                if (isWaterSelected) {
                    waterAmountGrams = amountInGrams
                    coffeeAmountGrams = (amountInGrams / waterRatioInput) * coffeeRatioInput
                } else {
                    coffeeAmountGrams = amountInGrams
                    waterAmountGrams = (amountInGrams / coffeeRatioInput) * waterRatioInput
                }

                updateAllUnits(waterAmountGrams, coffeeAmountGrams)
            }
            private fun updateAllUnits(waterAmountGrams: Double = 0.0, coffeeAmountGrams: Double = 0.0) {
                // Water conversions
                textViewWaterGrams.text = "Grams: ${DecimalFormat("0.##").format(convertGramsToWaterUnit(waterAmountGrams, Unit.GRAMS))}"
                textViewWaterOz.text = "oz: ${DecimalFormat("0.##").format(convertGramsToWaterUnit(waterAmountGrams, Unit.OZ))}"
                textViewWaterMl.text = "mL: ${DecimalFormat("0.##").format(convertGramsToWaterUnit(waterAmountGrams, Unit.ML))}"
                textViewWaterCups.text = "Cups: ${DecimalFormat("0.##").format(convertGramsToWaterUnit(waterAmountGrams, Unit.CUPS))}"

                // Coffee conversions
                textViewCoffeeGrams.text = "Grams: ${DecimalFormat("0.##").format(convertGramsToCoffeeUnit(coffeeAmountGrams, Unit.GRAMS))}"
                textViewCoffeeOz.text = "oz: ${DecimalFormat("0.##").format(convertGramsToCoffeeUnit(coffeeAmountGrams, Unit.OZ))}"
                textViewCoffeeTsp.text = "Tsp: ${DecimalFormat("0.##").format(convertGramsToCoffeeUnit(coffeeAmountGrams, Unit.TSP))}"
                textViewCoffeeTbsp.text = "Tbsp: ${DecimalFormat("0.##").format(convertGramsToCoffeeUnit(coffeeAmountGrams, Unit.TBSP))}"
            }
            // Conversion functions
            private fun convertWaterToGrams(amount: Double, unit: Unit): Double {
                return when (unit) {
                    Unit.GRAMS -> amount
                    Unit.OZ -> amount * 29.5735
                    Unit.ML -> amount
                    Unit.CUPS -> amount * 236.588
                    else -> amount
                }
            }

            private fun convertCoffeeToGrams(amount: Double, unit: Unit): Double {
                return when (unit) {
                    Unit.GRAMS -> amount
                    Unit.OZ -> amount * 28.3495
                    Unit.TSP -> amount * 2
                    Unit.TBSP -> amount * 6
                    else -> amount
                }
            }

            private fun convertGramsToWaterUnit(amountGrams: Double, targetUnit: Unit): Double {
                return when (targetUnit) {
                    Unit.GRAMS -> amountGrams
                    Unit.OZ -> amountGrams * 0.033814
                    Unit.ML -> amountGrams
                    Unit.CUPS -> amountGrams * 0.00422675
                    else -> amountGrams
                }
            }

            private fun convertGramsToCoffeeUnit(amountGrams: Double, targetUnit: Unit): Double {
                return when (targetUnit) {
                    Unit.GRAMS -> amountGrams
                    Unit.OZ -> amountGrams * 0.035274
                    Unit.TSP -> amountGrams / 2
                    Unit.TBSP -> amountGrams / 6
                    else -> amountGrams
                }
            }

            override fun onDestroyView() {
                super.onDestroyView()
                _binding = null
            }

            override fun onResume() {
                super.onResume()
                editTextCoffeeRatio.setText(AppPreferences.getDefaultCoffeeRatio(requireContext()).toString())
                editTextWaterRatio.setText(AppPreferences.getDefaultWaterRatio(requireContext()).toString())
            }
        }