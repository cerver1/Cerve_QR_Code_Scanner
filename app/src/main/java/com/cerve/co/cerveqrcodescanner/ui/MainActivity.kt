package com.cerve.co.cerveqrcodescanner.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.cerve.co.cerveqrcodescanner.services.MainViewModel
import com.cerve.co.cerveqrcodescanner.ui.theme.CerveQRCodeScannerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    @ExperimentalPermissionsApi
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CerveQRCodeScannerTheme {

                Surface(color = MaterialTheme.colors.background) {
                    ScanQrCodeScreen(
                        actionScannerLoadingResults = mainViewModel::setScannerStateAndBarcodeValue
                    )
                }
            }
        }
    }
}