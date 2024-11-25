package com.asfoundation.wallet.iab.presentation

import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.getAppIconDrawable
import com.appcoins.wallet.ui.common.getAppName
import com.appcoins.wallet.ui.widgets.component.Animation
import com.asf.wallet.R
import com.appcoins.wallet.ui.common.iap.icon.getDownArrow
import com.asfoundation.wallet.iab.theme.IAPTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

@Composable
fun PurchaseInfo(
  modifier: Modifier = Modifier,
  purchaseInfo: PurchaseInfoData,
  isExpanded: Boolean,
  showLoadingPrice: Boolean = false,
) {
  val localContext = LocalContext.current
  val appIcon = localContext.getAppIconDrawable(purchaseInfo.packageName)
  val appName = localContext.getAppName(purchaseInfo.packageName)

  RealPurchaseInfo(
    modifier = modifier,
    appIcon = appIcon,
    appName = appName,
    purchaseInfo = purchaseInfo,
    isExpanded = isExpanded,
    showLoadingPrice = showLoadingPrice,
  )
}

@Composable
fun PurchaseInfoSkeleton(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    PurchaseSkeleton(
      modifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(12.dp))
    )
    Column(
      modifier = Modifier
        .padding(start = 12.dp)
        .fillMaxWidth()
        .weight(1f)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        PurchaseSkeleton(
          modifier = Modifier
            .padding(bottom = 4.dp)
            .height(16.dp)
            .fillMaxWidth(0.54f)
            .clip(RoundedCornerShape(20.dp))
        )
        Spacer(modifier = Modifier.weight(1f))
        PurchaseSkeleton(
          modifier = Modifier
            .height(height = 16.dp)
            .fillMaxWidth(0.30f)
            .clip(RoundedCornerShape(20.dp))
        )
      }
      PurchaseSkeleton(
        modifier = Modifier
          .height(height = 16.dp)
          .fillMaxWidth(0.4f)
          .clip(RoundedCornerShape(20.dp))
      )
    }
  }
}

@Composable
private fun RealPurchaseInfo(
  modifier: Modifier = Modifier,
  appIcon: Drawable?,
  appName: String,
  purchaseInfo: PurchaseInfoData,
  isExpanded: Boolean,
  showLoadingPrice: Boolean,
) {
  val colorState by animateColorAsState(
    targetValue = if (isExpanded) IAPTheme.colors.backArrow else IAPTheme.colors.arrowColor,
    label = "ColorState"
  )

  val rotationState by animateFloatAsState(
    targetValue = if (isExpanded) -180F else 0F,
    label = "RotationState"
  )

  Column(modifier = modifier) {
    Row {
      Image(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .align(Alignment.CenterVertically),
        painter = rememberDrawablePainter(drawable = appIcon),
        contentDescription = null
      )
      Column(
        modifier = Modifier
          .padding(start = 12.dp)
          .fillMaxWidth()
          .weight(1f)
          .align(Alignment.CenterVertically)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = appName,
            style = IAPTheme.typography.bodyLarge,
            color = IAPTheme.colors.onPrimary,
          )
          Spacer(modifier = Modifier.weight(1f))
          if (showLoadingPrice) {
            Animation(
              modifier = Modifier.size(height = 22.dp, width = 48.dp),
              animationRes = R.raw.dots_transition_animation
            )
          } else {
            AnimatedVisibility(!isExpanded || !purchaseInfo.hasFees) {
              Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = purchaseInfo.cost,
                style = IAPTheme.typography.bodyLarge,
                color = IAPTheme.colors.onPrimary,
              )
            }
          }
          if (!showLoadingPrice && purchaseInfo.hasFees) {
            Image(
              modifier = Modifier
                .padding(start = 8.dp)
                .size(12.dp)
                .rotate(rotationState),
              imageVector = getDownArrow(arrowColor = colorState),
              contentDescription = null,
            )
          }
        }
        Text(
          text = purchaseInfo.productName,
          style = IAPTheme.typography.bodyMedium,
          color = IAPTheme.colors.smallText,
        )
      }
    }
    AnimatedVisibility(isExpanded && purchaseInfo.hasFees) {
      Column {
        PurchaseDetails(
          modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
          name = "Subtotal", //TODO hardcoded text
          value = purchaseInfo.subtotal,
        )
        PurchaseDetails(
          modifier = Modifier.padding(vertical = 8.dp),
          name = "Fees", //TODO hardcoded text
          value = purchaseInfo.fees,
        )
        PurchaseDetails(
          modifier = Modifier.padding(vertical = 8.dp),
          name = "Gift Card Discount", //TODO hardcoded text
          value = purchaseInfo.giftCardDiscount,
        )
        PurchaseDetails(
          modifier = Modifier.padding(top = 8.dp),
          name = "Total", //TODO hardcoded text
          value = purchaseInfo.cost,
          style = IAPTheme.typography.bodyLarge,
          color = IAPTheme.colors.onPrimary,
        )
      }
    }
  }
}

@Composable
private fun PurchaseDetails(
  modifier: Modifier = Modifier,
  name: String,
  value: String?,
  style: TextStyle = IAPTheme.typography.bodyMedium,
  color: Color = IAPTheme.colors.smallText,
) {
  value?.let {
    Row(modifier = modifier) {
      Text(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        text = name,
        style = style,
        color = color,
      )
      Text(
        text = it,
        style = style,
        color = color,
      )
    }
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
  @PreviewParameter(IsPurchaseInfoExpandedProvider::class) isExpanded: Boolean
) {
  IAPTheme {
    Column {
      Spacer(Modifier.height(24.dp))
      RealPurchaseInfo(
        appIcon = null,
        appName = "App name",
        purchaseInfo = emptyPurchaseInfo.copy(hasFees = Random.nextBoolean()),
        isExpanded = isExpanded,
        showLoadingPrice = false,
      )
      Spacer(Modifier.height(16.dp))
      RealPurchaseInfo(
        appIcon = null,
        appName = "App name",
        purchaseInfo = emptyPurchaseInfo.copy(hasFees = Random.nextBoolean()),
        isExpanded = isExpanded,
        showLoadingPrice = true,
      )
    }
  }
}

@PreviewAll
@Composable
private fun PurchaseInfoSkeletonsPreview() {
  IAPTheme {
    PurchaseInfoSkeleton()
  }
}

private class IsPurchaseInfoExpandedProvider : PreviewParameterProvider<Boolean> {
  override val values = sequenceOf(
    false,
    true
  )
}

@Parcelize
data class PurchaseInfoData(
  val packageName: String,
  val productName: String,
  val cost: String,
  val hasFees: Boolean,
  val subtotal: String? = null,
  val fees: String? = null,
  val giftCardDiscount: String? = null,
) : Parcelable

val emptyPurchaseInfo = PurchaseInfoData(
  packageName = "com.appcoins.trivialdrivesample.test",
  productName = "Product name",
  cost = "€12.98",
  hasFees = true,
  subtotal = "€12.73",
  fees = "€0.24",
  giftCardDiscount = "€0.00",
)
