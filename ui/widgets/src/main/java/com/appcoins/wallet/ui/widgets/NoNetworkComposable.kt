package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.component.ButtonWithText

@Composable
fun NoNetworkScreen(tryAgain: Boolean = false, onTryAgain: () -> Unit = {}) {
  val composition by
  rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_network_animation))
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
        .size(120.dp)
        .padding(bottom = 16.dp)
    )
    Text(
      text = stringResource(id = R.string.oops_title),
      color = WalletColors.styleguide_white,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text = stringResource(id = R.string.connection_error_body),
      color = WalletColors.styleguide_white,
      fontSize = 14.sp,
      modifier = Modifier
        .padding(top = 8.dp, bottom = 16.dp)
        .padding(horizontal = 40.dp),
      textAlign = TextAlign.Center
    )
    if (tryAgain) {
      ButtonWithText(
        label = stringResource(R.string.try_again),
        onClick = onTryAgain,
        labelColor = WalletColors.styleguide_white,
        outlineColor = WalletColors.styleguide_white,
      )
    }
  }
}

@Composable
fun NoNetworkSnackBar() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(WalletColors.styleguide_pink),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    NoNetworkMessage()
  }
}

@Composable
fun NoNetworkCard() {
  Card(
    colors =
    CardDefaults.cardColors(
      containerColor = WalletColors.styleguide_pink.copy(alpha = 0.15f)
    ),
    shape = CircleShape
  ) {
    NoNetworkMessage(mainColor = WalletColors.styleguide_pink)
  }
}

@Composable
fun NoNetworkMessage(mainColor: Color = WalletColors.styleguide_blue) {
  Row(modifier = Modifier.padding(8.dp)) {
    Icon(
      painter = painterResource(id = R.drawable.ic_no_internet),
      contentDescription = null,
      tint = mainColor,
      modifier = Modifier.size(16.dp)
    )
    Text(
      text = stringResource(id = R.string.connection_error_title),
      modifier = Modifier.padding(start = 8.dp),
      color = mainColor,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Medium
    )
  }
}

@Preview
@Composable
fun PreviewNoNetworkScreen() {
  NoNetworkScreen(true, {})
}

@Preview
@Composable
fun PreviewNoNetworkSnackBar() {
  NoNetworkSnackBar()
}

@Preview
@Composable
fun PreviewNoNetworkCard() {
  NoNetworkCard()
}
