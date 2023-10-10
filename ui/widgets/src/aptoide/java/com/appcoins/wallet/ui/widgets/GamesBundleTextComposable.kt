package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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
  val hasGameInstall =
    isPackageGameInstalled(packageName, packageManager = LocalContext.current.packageManager)
  Text(
    text = stringResource(id = if (hasGameInstall) R.string.play_button else R.string.get_button),
    color = WalletColors.styleguide_pink,
    fontSize = 16.sp,
    fontWeight = FontWeight.Bold,
    modifier = Modifier
      .padding(top = 24.dp, bottom = 6.dp, end = 12.dp)
  )
}