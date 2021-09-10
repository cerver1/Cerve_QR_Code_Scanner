package com.cerve.co.cerveqrcodescanner.ui

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.cerve.co.cerveqrcodescanner.models.PermissionStatus
import com.cerve.co.cerveqrcodescanner.models.ScannerState
import com.cerve.co.cerveqrcodescanner.ui.components.DefaultCameraPreview
import com.cerve.co.cerveqrcodescanner.ui.components.DefaultTopAppBar
import com.cerve.co.cerveqrcodescanner.ui.theme.CerveQRCodeScannerTheme
import com.cerve.co.cerveqrcodescanner.ui.theme.boxCornerRadius
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

@ExperimentalAnimationApi
@ExperimentalPermissionsApi
@Composable
fun ScanQrCodeScreen(
    permissionStatus: PermissionStatus? = PermissionStatus.NEEDS_TO_BE_REQUESTED,
    currentScannerState: ScannerState? = null,
    actionRequestCameraAndLocationPermission: ((MultiplePermissionsState) -> Unit)? = null,
    scannedBarcodeValue: String? = null,
    actionScannerLoadingResults: ((ScannerState, String) -> Unit)? = null,
    actionSetScannerCompletionState: ((ScannerState) -> Unit)? = null,
) {

    val doNotShowRationale by rememberSaveable { mutableStateOf(false) }

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    Scaffold(
        topBar = {
            DefaultTopAppBar()
        },
        modifier = Modifier.defaultMinSize()
    ) { innerPadding ->

        when {
            cameraPermissionState.hasPermission -> {


                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {

                    DefaultCameraPreview(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        currentScannerState = currentScannerState,
                        actionScannerLoadingResults = actionScannerLoadingResults,
                        actionSetScannerCompletionState = actionSetScannerCompletionState
                    )

                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        visible = currentScannerState == ScannerState.COMPLETE,
                        enter = slideInVertically(
                            // Enters by sliding in from offset -fullHeight to 0.
                            initialOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
                        ),
                        exit = slideOutVertically(
                            // Exits by sliding out from offset 0 to -fullHeight.
                            targetOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
                        )
                    ) {

                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(MaterialTheme.colors.secondary)
                                .padding(16.dp),
                            text = scannedBarcodeValue?.let { scannedBarcodeValue }?:"",
                            textAlign = TextAlign.Center

                        )
                    }
                }



            }

            (cameraPermissionState.shouldShowRationale ||
                    !cameraPermissionState.permissionRequested) -> {

                if (doNotShowRationale) {
                    //TODO feature not available screen
                } else {

                    LaunchedEffect(Unit) {
                        cameraPermissionState.launchPermissionRequest()
                    }

                    //TODO rationale screen

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
