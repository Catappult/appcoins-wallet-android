package com.appcoins.wallet.ui.widgets

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun RewardsActions(
  onClickEskills: () -> Unit,
  onClickPromoCode: () -> Unit,
  onClickGiftCard: () -> Unit
) {
  val scrollState = rememberScrollState()
  Row(
    modifier = Modifier
      .horizontalScroll(scrollState)
      .padding(horizontal = 16.dp)
      .padding(top = 24.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    ActionCard(
      image = R.drawable.ic_promocode,
      title = R.string.rewards_promo_code_card_title,
      description = R.string.rewards_promo_code_card_body,
      onClick = onClickPromoCode,
    )
    ActionCard(
      image = R.drawable.ic_giftcard,
      title = R.string.transaction_type_gift_card,
      description = R.string.gift_card_title,
      onClick = onClickGiftCard,
    )
    ActionCard(
      image = R.drawable.ic_eskills,
      title = R.string.rewards_eskills_card_title,
      description = R.string.rewards_eskills_card_body,
      onClick = onClickEskills,
    )
  }
}

@Composable
fun ActionCard(
  @DrawableRes image: Int,
  @StringRes title: Int,
  @StringRes description: Int,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .size(width = 160.dp, height = 208.dp)
      .clickable { onClick() },
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        painter = painterResource(image),
        contentDescription = null,
        modifier = Modifier
          .height(54.dp)
          .width(54.dp),
      )
      Text(
        text = stringResource(id = title),
        style = MaterialTheme.typography.titleSmall,
        color = WalletColors.styleguide_white,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
      )
      Text(
        text = stringResource(id = description),
        style = MaterialTheme.typography.bodySmall,
        color = WalletColors.styleguide_dark_grey,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Preview
@Composable
private fun PreviewRewardsActions() {
  RewardsActions(
    { },
    { },
    { }
  )
}
