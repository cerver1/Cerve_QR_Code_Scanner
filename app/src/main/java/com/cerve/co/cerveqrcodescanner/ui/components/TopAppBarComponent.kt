package com.cerve.co.cerveqrcodescanner.ui.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cerve.co.cerveqrcodescanner.R
import com.cerve.co.cerveqrcodescanner.ui.theme.CerveQRCodeScannerTheme

@Composable
fun DefaultTopAppBar(

    //TODO may pass default parameters here

) {

    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {

            IconButton(onClick = { /*TODO toggle flash action of the camera*/ }) {
                Icon(imageVector = Icons.Default.FlashOff, contentDescription = null)
            }
        }
    )
}

@Preview
@Composable
fun DefaultTopAppBarPreview() {

    CerveQRCodeScannerTheme {
        DefaultTopAppBar()
    }
}