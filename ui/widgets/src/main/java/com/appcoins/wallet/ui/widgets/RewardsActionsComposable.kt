package com.appcoins.wallet.ui.widgets

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun RewardsActions(
  onClickPromoCode: () -> Unit,
  onClickGiftCard: () -> Unit,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  val scrollState = rememberScrollState()
  Row(
    modifier = Modifier
      .horizontalScroll(scrollState)
      .padding(horizontal = 16.dp)
      .padding(top = 16.dp)
      .height(IntrinsicSize.Max),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    ActionCard(
      image = R.drawable.ic_promocode,
      title = R.string.rewards_promo_code_card_title,
      description = R.string.rewards_promo_code_card_body,
      onClick = onClickPromoCode,
      fragmentName = fragmentName,
      buttonsAnalytics = buttonsAnalytics
    )
    ActionCard(
      image = R.drawable.ic_giftcard,
      title = R.string.transaction_type_gift_card,
      description = R.string.gift_card_title,
      onClick = onClickGiftCard,
      fragmentName = fragmentName,
      buttonsAnalytics = buttonsAnalytics
    )
  }
}

@Composable
fun ActionCard(
  @DrawableRes image: Int,
  @StringRes title: Int,
  @StringRes description: Int,
  onClick: () -> Unit,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  val actionCardString = stringResource(id = title)
  Card(
    modifier = Modifier
      .width(width = 160.dp)
      .fillMaxHeight()
      .clickable {
        buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, actionCardString)
        onClick()
      },
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(WalletColors.styleguide_dark_secondary),
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
        text = actionCardString,
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


@Composable
fun SkeletonLoadingRewardsActionsCard() {
  val scrollState = rememberScrollState()
  Row(
    modifier = Modifier
      .horizontalScroll(scrollState)
      .padding(horizontal = 16.dp)
      .padding(top = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    SkeletonLoadingRewardActionCard()
    SkeletonLoadingRewardActionCard()
    SkeletonLoadingRewardActionCard()
  }
}

@Composable
private fun SkeletonLoadingRewardActionCard() {
  Card(
    modifier = Modifier
      .size(width = 160.dp, height = 208.dp),
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(WalletColors.styleguide_dark_secondary),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(
        modifier = Modifier
          .height(60.dp)
          .width(60.dp)
          .clip(RoundedCornerShape(30.dp))
          .background(brush = shimmerSkeleton()),
      )
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(
          modifier = Modifier
            .width(width = 110.dp)
            .height(height = 20.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(brush = shimmerSkeleton()),
        )
        Spacer(
          modifier = Modifier
            .padding(top = 16.dp)
            .width(width = 160.dp)
            .height(height = 17.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(brush = shimmerSkeleton()),
        )
        Spacer(
          modifier = Modifier
            .padding(top = 8.dp)
            .width(width = 90.dp)
            .height(height = 17.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(brush = shimmerSkeleton())
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewRewardsActions() {
  RewardsActions(
    onClickPromoCode = { },
    onClickGiftCard = { },
    fragmentName = "RewardFragment",
    buttonsAnalytics = null
  )
}


@Preview
@Composable
private fun PreviewSkeletonRewardsActions() {
  SkeletonLoadingRewardsActionsCard()
}
