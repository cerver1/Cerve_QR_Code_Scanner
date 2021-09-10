package com.cerve.co.cerveqrcodescanner.services

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cerve.co.cerveqrcodescanner.models.ScannerState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    var scanningState by mutableStateOf<ScannerState?>(null)
    var barcodeRawValue by mutableStateOf<String?>(null)

    fun setScannerState(newState: ScannerState) {
        Log.d("Utility", "setScannerState")
        if (scanningState != newState)
            scanningState = newState
    }

    private fun setBarcodeValue(newBarcodeValue: String?) {
        Log.d("Utility", "setBarcodeValue")
        if (barcodeRawValue != newBarcodeValue)
            barcodeRawValue = newBarcodeValue

    }

    fun setScannerStateAndBarcodeValue(newState: ScannerState, newBarcodeValue: String) {
        Log.d("Utility", "setScannerStateAndBarcodeValue: ")

        setScannerState(newState)
        setBarcodeValue(newBarcodeValue)
    }




}