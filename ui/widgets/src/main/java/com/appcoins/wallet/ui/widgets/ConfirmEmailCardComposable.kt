package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.component.Animation

@Composable
fun ConfirmEmailCard(
  email: MutableState<String>,
  onCloseClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .padding(bottom = 8.dp, top = 16.dp)
      .background(WalletColors.styleguide_dark_secondary, shape = RoundedCornerShape(16.dp))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(end = 8.dp, top = 8.dp),
      horizontalAlignment = Alignment.End
    ) {
      Icon(
        painter = painterResource(id = R.drawable.ic_close_rounded),
        contentDescription = "Close",
        tint = Color.Unspecified,
        modifier = Modifier
          .size(18.dp)
          .clickable(onClick = onCloseClick),
      )
    }
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,

      ) {
      Animation(modifier = Modifier.size(66.dp), animationRes = R.raw.verify_animation)
      Text(
        text = stringResource(id = R.string.mail_list_card_confirmation_title),
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
      )

      Text(
        text = stringResource(id = R.string.mail_list_card_confirmation_body, email.value),
        color = Color.White,
        fontSize = 12.sp
      )
    }
  }
}

@Preview
@Composable
fun PreviewConfimEmailComposable() {
  val email = remember { mutableStateOf("teste@aptoide.com") }
  ConfirmEmailCard(email, {})
}