package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun GetTextOrPlay(packageName: String?) {
  if (isPackageGameInstalled(packageName, packageManager = LocalContext.current.packageManager)) {
    Text(
      text = stringResource(id = R.string.play_button),
      color = WalletColors.styleguide_pink,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier
        .padding(top = 24.dp, bottom = 6.dp, end = 12.dp)
    )
  }
}