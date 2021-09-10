package com.cerve.co.cerveqrcodescanner.services

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
        if (scanningState != newState)
            scanningState = newState
    }

    fun setBarcodeValue(newBarcodeValue: String?) {
        if (barcodeRawValue != newBarcodeValue)
            barcodeRawValue = newBarcodeValue

    }

    fun setScannerStateAndBarcodeValue(newState: ScannerState, newBarcodeValue: String) {
        setScannerState(newState)
        setBarcodeValue(newBarcodeValue)
    }




}