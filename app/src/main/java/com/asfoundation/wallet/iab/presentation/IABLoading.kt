package com.asfoundation.wallet.iab.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.widgets.component.Animation
import com.asf.wallet.R
import com.asfoundation.wallet.iab.theme.IAPTheme

@Composable
fun IABLoading(modifier: Modifier = Modifier) {
  RealIABLoading(modifier = modifier)
}

@Composable
private fun RealIABLoading(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Animation(
      modifier = Modifier.size(112.dp),
      animationRes = R.raw.loading_wallet
    )
  }
}

@PreviewAll
@Composable
private fun IABLoadingPreview(modifier: Modifier = Modifier) {
  IAPTheme {
    RealIABLoading()
  }
}
