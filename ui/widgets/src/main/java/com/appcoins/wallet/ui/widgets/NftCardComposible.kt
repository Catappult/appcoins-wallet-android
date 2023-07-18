package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun NftCard(
  onClick: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
      .clip(shape = RoundedCornerShape(8.dp))
      .clickable { onClick() },
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier
          .weight(1f),
        horizontalArrangement = Arrangement.Start,
      ) {
        Image(
          painter = painterResource(R.drawable.ic_nft),
          "NFT",
          modifier = Modifier
            .height(64.dp)
            .width(64.dp)
            .align(Alignment.CenterVertically)
        )
        Text(
          text = stringResource(id = R.string.home_nft_title),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = WalletColors.styleguide_light_grey,
          lineHeight = 24.sp,
          modifier = Modifier
            .align(Alignment.CenterVertically)
            .padding(horizontal = 20.dp)
            .weight(1f, fill = false)
        )
      }
      Image(
        painter = painterResource(R.drawable.ic_arrow_right),
        "Arrow",
        modifier = Modifier
          .height(36.dp)
          .width(36.dp)
          .align(Alignment.CenterVertically)
      )
    }
  }
}

@Preview
@Composable
fun PreviewNftCard() {
  NftCard(
    onClick = {}
  )
}

@Preview(widthDp = 800, heightDp = 200, showBackground = true)
@Composable
fun PreviewNftCardLong() {
  NftCard(
    onClick = {}
  )
}
