package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun NoNetworkAlert() {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_network_animation))
  val progress by animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)

  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    LottieAnimation(
      composition = composition,
      progress = { progress },
      modifier = Modifier
        .size(160.dp)
        .padding(bottom = 8.dp)
    )
    Text(
      text = stringResource(id = R.string.notification_no_network_poa),
      color = WalletColors.styleguide_white,
      fontSize = 14.sp
    )
  }
}

@Preview
@Composable
fun PreviewAlertMessageWithIcon() {
  NoNetworkAlert()
}
