package com.asfoundation.wallet.iab.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.widgets.component.Animation
import com.asf.wallet.R
import com.asfoundation.wallet.iab.theme.IAPTheme

@Composable
fun IABLoading(modifier: Modifier = Modifier, animationSize: Dp = 112.dp, text: String? = null) {
  Column (
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Animation(
      modifier = Modifier.size(animationSize),
      animationRes = R.raw.loading_wallet
    )
    text?.let {
      Text(
        text = it,
        style = IAPTheme.typography.bodySmall,
        color = IAPTheme.colors.onPrimary,
      )
    }
  }
}

@PreviewAll
@Composable
private fun IABLoadingPreview(modifier: Modifier = Modifier) {
  IAPTheme {
    IABLoading(modifier = Modifier.fillMaxSize())
  }
}
