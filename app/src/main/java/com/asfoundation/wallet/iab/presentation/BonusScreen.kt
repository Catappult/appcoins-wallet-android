package com.asfoundation.wallet.iab.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieConstants
import com.appcoins.wallet.ui.widgets.component.Animation
import com.asf.wallet.R
import com.appcoins.wallet.ui.common.iap.icon.getDownArrow
import com.appcoins.wallet.ui.common.iap.icon.getIcGiftDisabled
import com.asfoundation.wallet.iab.theme.IAPTheme
import kotlin.random.Random

@Composable
fun BonusInfo(
  modifier: Modifier = Modifier,
  isExpanded: Boolean,
  bonusInfoData: BonusInfoData,
  bonusAvailable: Boolean,
  onPromoCodeAvailableClick: () -> Unit,
) {
  val colorState by animateColorAsState(
    targetValue = if (isExpanded) IAPTheme.colors.backArrow else IAPTheme.colors.arrowColor,
    label = "ColorState"
  )

  val rotationState by animateFloatAsState(
    targetValue = if (isExpanded) -180F else 0F,
    label = "RotationState"
  )

  Column(
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (bonusAvailable) {
        Animation(
          modifier = Modifier.size(20.dp),
          animationRes = R.raw.iab_gift,
          iterations = LottieConstants.IterateForever,
        )
      } else {
        Image(
          modifier = Modifier.size(20.dp),
          imageVector = getIcGiftDisabled(),
          contentDescription = null,
        )
      }
      Text(
        modifier = Modifier
          .padding(start = 4.dp)
          .fillMaxWidth()
          .weight(1f),
        text = bonusInfoData.bonusText,
        style = IAPTheme.typography.bodyMedium,
        color = if (bonusAvailable) IAPTheme.colors.onPrimary else IAPTheme.colors.disabledTextColor
      )
      if (bonusAvailable) {
        Image(
          modifier = Modifier
            .padding(start = 4.dp)
            .size(12.dp)
            .rotate(rotationState),
          imageVector = getDownArrow(colorState),
          contentDescription = null
        )
      }
    }
    AnimatedVisibility(isExpanded && bonusAvailable) {
      Column(
        modifier = Modifier.padding(top = 8.dp)
      ) {
        BonusDetails(
          modifier = Modifier.padding(vertical = 8.dp),
          name = "Promo code Perk",
          value = bonusInfoData.promoCodePerkValue,
        )
        BonusDetails(
          modifier = Modifier.padding(vertical = 8.dp),
          name = "Gamification",
          value = bonusInfoData.gamificationValue,
        )
        Text(
          modifier = Modifier
            .padding(top = 14.dp)
            .addClick(onClick = onPromoCodeAvailableClick, "onPromoCodeAvailableClick"),
          text = "Do you have a promo code?",
          style = IAPTheme.typography.bodyMedium,
          color = IAPTheme.colors.secondary,
        ) //TODO hardcoded text
      }
    }
  }
}

@Composable
fun BonusInfoSkeleton(modifier: Modifier = Modifier) {
  PurchaseSkeleton(
    modifier = modifier
      .height(16.dp)
      .fillMaxWidth(0.7f)
      .clip(RoundedCornerShape(20.dp))
  )
}

@Composable
private fun BonusDetails(
  modifier: Modifier = Modifier,
  name: String,
  value: String,
) {
  Row(modifier = modifier) {
    Text(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      text = name,
      style = IAPTheme.typography.bodyMedium,
      color = IAPTheme.colors.smallText,
    )
    Text(
      text = value,
      style = IAPTheme.typography.bodyMedium,
      color = IAPTheme.colors.smallText,
    )
  }
}

@Composable
private fun PurchaseSkeleton(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.background(IAPTheme.colors.placeholderColor)
  )
}

@PreviewAll
@Composable
private fun PurchaseInfoPreview(
  @PreviewParameter(IsBonusExpandedProvider::class) isExpanded: Boolean
) {
  IAPTheme {
    BonusInfo(
      isExpanded = isExpanded,
      bonusInfoData = emptyBonusInfoData,
      bonusAvailable = Random.nextBoolean(),
      onPromoCodeAvailableClick = {},
    )
  }
}

@PreviewAll
@Composable
private fun PurchaseInfoSkeletonPreview() {
  IAPTheme {
    BonusInfoSkeleton()
  }
}

private class IsBonusExpandedProvider : PreviewParameterProvider<Boolean> {
  override val values = sequenceOf(
    false,
    true
  )
}

data class BonusInfoData(
  val bonusText: String,
  val promoCodePerkValue: String,
  val gamificationValue: String,
)

val emptyBonusInfoData = BonusInfoData(
  bonusText = "Bonus: €0.60 in AppCoins Credits",
  promoCodePerkValue = "5% (€0.05)",
  gamificationValue = "8% (€0.08)",
)
