package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText

@Composable
fun GenericError(
  message: String,
  onSupportClick: () -> Unit,
  onTryAgain: () -> Unit,
  fragmentName: String,
  buttonAnalytics: ButtonsAnalytics?,
  isDarkTheme: Boolean = true,
  smallSpacing: Boolean = false,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Spacer(Modifier.weight(112f))
    Image(
      painter = painterResource(id = R.drawable.ic_error_pink),
      contentDescription = null,
      modifier = Modifier
        .size(72.dp)
        .padding(bottom = 16.dp)
    )
    Text(
      text = stringResource(id = R.string.error_general),
      color = if (isDarkTheme) WalletColors.styleguide_white else WalletColors.styleguide_blue,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text = message,
      color = if (isDarkTheme) WalletColors.styleguide_white else WalletColors.styleguide_blue,
      fontSize = 14.sp,
      modifier = Modifier
        .padding(top = 8.dp, bottom = 16.dp)
        .padding(horizontal = 16.dp),
      textAlign = TextAlign.Center,
      fontWeight = FontWeight.Medium
    )
    Text(
      text = stringResource(id = R.string.error_contac_us_body),
      color = if (isDarkTheme) WalletColors.styleguide_medium_grey else WalletColors.styleguide_dark_grey,
      fontSize = 14.sp,
      modifier = Modifier.padding(top = if (smallSpacing) 4.dp else 16.dp),
      textAlign = TextAlign.Center,
      fontWeight = FontWeight.Medium
    )
    SupportButton(onSupportClick = onSupportClick, isDarkTheme = isDarkTheme)
    Spacer(Modifier.weight(232f))
    ButtonWithText(
      modifier = Modifier
        .padding(top = 16.dp)
        .widthIn(max = 360.dp),
      label = stringResource(R.string.try_again),
      onClick = onTryAgain,
      labelColor =  WalletColors.styleguide_white,
      backgroundColor = WalletColors.styleguide_pink,
      buttonType = ButtonType.LARGE,
      fragmentName = fragmentName,
      buttonsAnalytics = buttonAnalytics
    )
  }
}

@Composable
fun SupportButton(onSupportClick: () -> Unit, isDarkTheme: Boolean) {
  Button(
    onClick = onSupportClick,
    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
  ) {
    Image(
      painterResource(id = if(isDarkTheme) R.drawable.ic_logo_appc_support_light else R.drawable.ic_logo_appc_support),
      contentDescription = null,
      modifier = Modifier.padding(end = 8.dp)
    )
    Image(painterResource(id = R.drawable.ic_open_in_24), contentDescription = null)
  }
}

@Preview
@Composable
fun PreviewGenericError() {
  GenericError(
    stringResource(id = R.string.manage_cards_error_details),
    {},
    {},
    "HomeFragment",
    null
  )
}

@Preview
@Composable
fun PreviewGenericErrorSmall() {
  GenericError(
    message = stringResource(id = R.string.manage_cards_error_details),
    onSupportClick = {},
    onTryAgain = {},
    fragmentName = "HomeFragment",
    buttonAnalytics = null,
    isDarkTheme = false,
    smallSpacing = true
  )
}
