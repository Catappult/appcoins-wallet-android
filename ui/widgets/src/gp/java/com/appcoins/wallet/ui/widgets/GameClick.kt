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

class GameClick(gamePackage: String?, context: Context) : HandleGameClick {
  init {
    try {
      val launchIntent: Intent? = gamePackage?.let {
        context.packageManager.getLaunchIntentForPackage(it)
      }
      if (launchIntent != null)
        ContextCompat.startActivity(context, launchIntent, null)
    } catch (e: Throwable) {

    }
  }
}