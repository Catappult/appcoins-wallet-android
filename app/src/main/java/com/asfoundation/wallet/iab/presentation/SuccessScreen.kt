package com.asfoundation.wallet.iab.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieConstants
import com.appcoins.wallet.ui.widgets.component.Animation
import com.asf.wallet.R
import com.asfoundation.wallet.iab.theme.IAPTheme

@Composable
fun SuccessScreen(
  modifier: Modifier = Modifier,
  bonus: String? = null
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    bonus?.let {
      Animation(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1f),
        animationRes = R.raw.iab_success_view_background,
        iterations = LottieConstants.IterateForever,
        contentScale = ContentScale.Crop,
      )
    }
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 20.dp)
        .conditional(bonus != null, { padding(top = 50.dp) }),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Spacer(
        modifier = Modifier
          .fillMaxHeight()
          .weight(1f)
      )
      Animation(
        modifier = Modifier.size(112.dp),
        animationRes = R.raw.iab_transaction_success,
        iterations = 1
      )
      Text(
        text = stringResource(id = R.string.transaction_status_success),
        style = IAPTheme.typography.titleMedium,
        color = IAPTheme.colors.onPrimary
      )
      Spacer(
        modifier = Modifier
          .fillMaxHeight()
          .weight(1f)
      )
      bonus?.let {
        BonusBox(
          modifier = Modifier
            .padding(top = 50.dp)
            .fillMaxWidth(),
          bonus = it
        )
      }
    }
  }
}

@Composable
private fun BonusBox(
  modifier: Modifier = Modifier,
  bonus: String
) {
  Row(
    modifier = modifier
      .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
      .background(IAPTheme.colors.primaryContainer)
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Animation(
      modifier = Modifier.size(46.dp),
      animationRes = R.raw.iab_gift,
      iterations = LottieConstants.IterateForever,
    )
    Column(
      modifier = Modifier.padding(start = 10.dp, end = 2.dp)
    ) {
      Text(
        text = stringResource(id = R.string.bonus_received_body, bonus),
        style = IAPTheme.typography.titleMedium,
        color = IAPTheme.colors.onPrimary
      )// TODO review copy. does not exist at the moment
      Text(
        text = stringResource(R.string.gamification_purchase_body),
        style = IAPTheme.typography.bodySmall,
        color = IAPTheme.colors.smallText
      )// TODO review copy. does not exist at the moment
    }
  }
}

@PreviewAll
@Composable
fun PreviewSuccessScreenWithBonus() {
  val bonus = "€0.05"
  IAPTheme {
    IAPBottomSheet(
      showWalletIcon = false,
      fullscreen = false,
    ) {
      SuccessScreen(bonus = bonus)
    }
  }
}

@PreviewAll
@Composable
fun PreviewSuccessScreenWithoutBonus() {
  IAPTheme {
    IAPBottomSheet(
      showWalletIcon = false,
      fullscreen = false,
    ) {
      SuccessScreen()
    }
  }
}
