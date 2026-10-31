package com.joneslabs13.espressopedia.ui.settings

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.joneslabs13.espressopedia.R
import com.joneslabs13.espressopedia.data.AppPreferences

class SettingsFragment : Fragment() {

    private var selectedBrewTimeMillis: Long = 180000L
    private var selectedRingtoneUriString: String = ""

    private lateinit var textViewCurrentDefaultTime: TextView
    private lateinit var buttonChangeDefaultTime: Button
    private lateinit var textViewCurrentSound: TextView
    private lateinit var buttonSelectSound: Button
    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var editTextDefaultCoffeeRatio: TextInputEditText
    private lateinit var editTextDefaultWaterRatio: TextInputEditText
    private lateinit var buttonSaveSettings: MaterialButton

    private val ringtonePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            selectedRingtoneUriString = uri?.toString() ?: ""
            updateSoundDisplay()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        textViewCurrentDefaultTime = view.findViewById(R.id.textViewCurrentDefaultTime)
        buttonChangeDefaultTime = view.findViewById(R.id.buttonChangeDefaultTime)
        textViewCurrentSound = view.findViewById(R.id.textViewCurrentSound)
        buttonSelectSound = view.findViewById(R.id.buttonSelectSound)
        radioGroupTheme = view.findViewById(R.id.radioGroupTheme)
        editTextDefaultCoffeeRatio = view.findViewById(R.id.editTextDefaultCoffeeRatio)
        editTextDefaultWaterRatio = view.findViewById(R.id.editTextDefaultWaterRatio)
        buttonSaveSettings = view.findViewById(R.id.buttonSaveSettings)

        loadSettings()

        buttonChangeDefaultTime.setOnClickListener {
            showTimePickerDialog()
        }

        buttonSelectSound.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_NOTIFICATION)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Timer Sound")
                if (selectedRingtoneUriString.isNotEmpty()) {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(selectedRingtoneUriString))
                } else {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                }
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            }
            ringtonePickerLauncher.launch(intent)
        }

        buttonSaveSettings.setOnClickListener {
            saveSettings()
        }

        return view
    }

    private fun loadSettings() {
        selectedBrewTimeMillis = AppPreferences.getDefaultBrewTime(requireContext())
        updateTimeDisplay()

        selectedRingtoneUriString = AppPreferences.getDefaultRingtoneUri(requireContext())
        updateSoundDisplay()

        when (AppPreferences.getThemeMode(requireContext())) {
            AppCompatDelegate.MODE_NIGHT_NO -> radioGroupTheme.check(R.id.radioLight)
            AppCompatDelegate.MODE_NIGHT_YES -> radioGroupTheme.check(R.id.radioDark)
            else -> radioGroupTheme.check(R.id.radioSystem)
        }

        editTextDefaultCoffeeRatio.setText(AppPreferences.getDefaultCoffeeRatio(requireContext()).toString())
        editTextDefaultWaterRatio.setText(AppPreferences.getDefaultWaterRatio(requireContext()).toString())
    }

    private fun updateTimeDisplay() {
        val minutes = (selectedBrewTimeMillis / 1000) / 60
        val seconds = (selectedBrewTimeMillis / 1000) % 60
        textViewCurrentDefaultTime.text = getString(R.string.default_time_format, minutes, seconds)
    }

    private fun updateSoundDisplay() {
        val soundName = if (selectedRingtoneUriString.isNotEmpty()) {
            val ringtone = RingtoneManager.getRingtone(requireContext(), Uri.parse(selectedRingtoneUriString))
            ringtone?.getTitle(requireContext()) ?: "Custom Sound"
        } else {
            "Default (Alarm)"
        }
        textViewCurrentSound.text = "Sound: $soundName"
    }

    private fun showTimePickerDialog() {
        val currentMinutes = ((selectedBrewTimeMillis / 1000) / 60).toInt()
        val currentSeconds = ((selectedBrewTimeMillis / 1000) % 60).toInt()

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_set_time, null)
        val pickerMinutes = dialogView.findViewById<NumberPicker>(R.id.numberPickerMinutes).apply {
            minValue = 0
            maxValue = 59
            value = currentMinutes
            wrapSelectorWheel = true
        }
        val pickerSeconds = dialogView.findViewById<NumberPicker>(R.id.numberPickerSeconds).apply {
            minValue = 0
            maxValue = 59
            value = currentSeconds
            wrapSelectorWheel = true
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.set_default_brew_time)
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val minutes = pickerMinutes.value
                val seconds = pickerSeconds.value
                selectedBrewTimeMillis = ((minutes * 60) + seconds) * 1000L
                updateTimeDisplay()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveSettings() {
        val coffeeRatio = editTextDefaultCoffeeRatio.text.toString().toDoubleOrNull()
        val waterRatio = editTextDefaultWaterRatio.text.toString().toDoubleOrNull()

        if (coffeeRatio == null || coffeeRatio <= 0 || waterRatio == null || waterRatio <= 0) {
            Toast.makeText(requireContext(), "Please enter valid positive numbers for ratios", Toast.LENGTH_SHORT).show()
            return
        }

        val themeMode = when (radioGroupTheme.checkedRadioButtonId) {
            R.id.radioLight -> AppCompatDelegate.MODE_NIGHT_NO
            R.id.radioDark -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppPreferences.setDefaultBrewTime(requireContext(), selectedBrewTimeMillis)
        AppPreferences.setDefaultRingtoneUri(requireContext(), selectedRingtoneUriString)
        AppPreferences.setThemeMode(requireContext(), themeMode)
        AppCompatDelegate.setDefaultNightMode(themeMode)

        AppPreferences.setDefaultCoffeeRatio(requireContext(), coffeeRatio)
        AppPreferences.setDefaultWaterRatio(requireContext(), waterRatio)

        Toast.makeText(requireContext(), R.string.settings_saved_successfully, Toast.LENGTH_SHORT).show()
    }
}
