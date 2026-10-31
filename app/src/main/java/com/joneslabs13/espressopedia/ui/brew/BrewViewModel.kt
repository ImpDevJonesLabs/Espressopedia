package com.joneslabs13.espressopedia.ui.brew

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BrewViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is brew Fragment"
    }
    val text: LiveData<String> = _text
}