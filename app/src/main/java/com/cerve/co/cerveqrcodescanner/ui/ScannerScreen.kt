package com.cerve.co.cerveqrcodescanner.ui

import android.Manifest
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import com.cerve.co.cerveqrcodescanner.models.PermissionStatus
import com.cerve.co.cerveqrcodescanner.models.ScannerState
import com.cerve.co.cerveqrcodescanner.ui.components.DefaultCameraPreview
import com.cerve.co.cerveqrcodescanner.ui.components.DefaultTopAppBar
import com.cerve.co.cerveqrcodescanner.ui.theme.CerveQRCodeScannerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState

@ExperimentalAnimationApi
@ExperimentalPermissionsApi
@Composable
fun ScanQrCodeScreen(
    permissionStatus: PermissionStatus? = PermissionStatus.NEEDS_TO_BE_REQUESTED,
    actionRequestCameraAndLocationPermission: ((MultiplePermissionsState) -> Unit)? = null,
    actionScanBarcode: (() -> Unit)? = null,
    actionScanBarcodeSuccess: (() -> Unit)? = null,
    actionScannerLoadingResults: ((ScannerState, String?) -> Unit)? = null,
    actionNavigateToSelectFromList: (() -> Unit)? = null,
    actionNavigateHome: (() -> Unit)? = null,
) {

    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )


    Scaffold(
        topBar = {
            DefaultTopAppBar()
        },
        modifier = Modifier.defaultMinSize()
    ) { innerPadding ->

        var layoutSize by remember {
            mutableStateOf(IntSize.Zero)
        }
        val modifier = remember {
            mutableStateOf(
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        layoutSize = coordinates.size
                    }
            )
        }

        when {
            cameraPermissionState.hasPermission -> {

                DefaultCameraPreview(
                    modifier = modifier.value,
                    actionScannerLoadingResults = actionScannerLoadingResults

                )


            }
            (cameraPermissionState.shouldShowRationale ||
                    !cameraPermissionState.permissionRequested) -> {

                if (doNotShowRationale) {
                    //TODO "Feature not available"
                } else {

                    LaunchedEffect(Unit) {
                        cameraPermissionState.launchPermissionRequest()
                    }

                    //TODO
//                    PermissionRationaleBox(
//                        modifier = Modifier.padding(innerPadding),
//                        ActionRequestPermission = {
//                            cameraAndLocationPermissionState.launchMultiplePermissionRequest()
//                        },
////                    ActionIgnorePermission = { doNotShowRationale = true /** Navigate to select from list */ }
//                    )

                }

            }
        }

    }
}


@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun ScanQrCodeScreenPreview() {

    CerveQRCodeScannerTheme {
        ScanQrCodeScreen()
    }
}
