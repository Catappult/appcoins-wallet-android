package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_shimmer

@Composable
fun BalanceNewCard(
  balance: String,
  onClickPromoCode: () -> Unit,
  onClickDetailsBalance: () -> Unit,
  onClickTopUp: () -> Unit,
  onClickMore: () -> Unit,
  onClickBackup: () -> Unit,
  showBackup: Boolean = false,
  isLoading: Boolean = true,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {

  if (isLoading) {
    SkeletonLoadingNewBalanceCardExpanded()
  } else {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp)
        .background(WalletColors.styleguide_dark),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(24.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClickDetailsBalance() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Text(
          text = stringResource(R.string.p2p_send_currency_appc_c),
          color = WalletColors.styleguide_dark_grey,
          modifier = Modifier.clickable(onClick = onClickDetailsBalance),
          fontSize = 16.sp,
          fontWeight = FontWeight(400),
        )
        Image(
          painter = painterResource(id = R.drawable.ic_arrow_default_head_down),
          contentDescription = stringResource(R.string.p2p_send_currency_appc_c),
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = balance,
        color = WalletColors.styleguide_white,
        fontSize = 36.sp,
        fontWeight = FontWeight(500)
      )
      Spacer(modifier = Modifier.height(24.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        BalanceButton(
          iconRes = painterResource(id = R.drawable.ic_coupon_icon),
          text = stringResource(R.string.home_promo_code_button),
          onClickFunction = onClickPromoCode,
          modifier = Modifier.weight(1f),
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics
        )
        BalanceButton(
          iconRes = painterResource(id = R.drawable.ic_plus_icon),
          text = stringResource(R.string.home_top_up_button),
          onClickFunction = onClickTopUp,
          modifier = Modifier.weight(1f),
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics
        )
        BalanceButton(
          iconRes = painterResource(id = R.drawable.ic_more_icon),
          text = stringResource(R.string.action_more_details),
          onClickFunction = onClickMore,
          modifier = Modifier.weight(1f),
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
      if (showBackup) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(styleguide_dark_secondary, shape = RoundedCornerShape(16.dp)),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            BackupAlertCard(
              modifier = Modifier.background(styleguide_dark_secondary),
              onClickButton = onClickBackup,
              hasBackup = false,
              fragmentName = fragmentName,
              buttonsAnalytics = buttonsAnalytics
            )
          }
        }
      }
    }
  }
}

@Composable
fun BalanceButton(
  iconRes: Painter,
  text: String,
  onClickFunction: () -> Unit,
  modifier: Modifier = Modifier,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  Column(
    modifier = modifier
      .height(106.dp)
      .background(styleguide_dark_secondary, shape = RoundedCornerShape(16.dp))
      .clickable {
        buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, text)
        onClickFunction()
      },
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = iconRes,
        contentDescription = text,
        modifier = Modifier.size(28.dp)
      )
    }

    Text(
      text = text,
      color = Color.White,
      fontSize = 12.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(bottom = 16.dp)
    )
  }
}
@Composable
fun SkeletonLoadingNewBalanceCardExpanded() {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_dark),
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(shape = RoundedCornerShape(8.dp))
  ) {
    Row(
      modifier = Modifier
        .padding(top = 8.dp, end = 8.dp, start = 8.dp, bottom = 8.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.Center
    ) {
      Column {
        Spacer(
          modifier = Modifier
            .width(width = 100.dp)
            .height(height = 20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(brush = shimmerSkeleton(shimmerColor = styleguide_shimmer)),
        )
      }
    }
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(end = 8.dp, start = 8.dp, bottom = 8.dp),

      horizontalArrangement = Arrangement.Center
    ) {
      Column {
        Spacer(
          modifier = Modifier
            .width(width = 250.dp)
            .height(height = 30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(brush = shimmerSkeleton(shimmerColor = styleguide_shimmer)),
        )
      }
    }
    Row(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        modifier = Modifier
          .size(115.dp)
          .padding(all = 8.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(brush = shimmerSkeleton(shimmerColor = styleguide_shimmer))
      ) {}
      Column(
        modifier = Modifier
          .size(115.dp)
          .padding(all = 8.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(brush = shimmerSkeleton(shimmerColor = styleguide_shimmer))
      ) {}
      Column(
        modifier = Modifier
          .size(115.dp)
          .padding(all = 8.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(
            brush = shimmerSkeleton(
              shimmerColor = styleguide_shimmer
            )
          )
      ) {}
    }
  }
}

@Preview
@Composable
fun PreviewBalanceNewCard() {
  BalanceNewCard(
    balance = "€32.12",
    onClickPromoCode = {},
    onClickTopUp = {},
    onClickDetailsBalance = {},
    onClickMore = {},
    onClickBackup = {},
    showBackup = true,
    isLoading = false,
    fragmentName = "HomeFragment",
    buttonsAnalytics = null,
  )
}
