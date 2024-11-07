package com.asfoundation.wallet.iab.presentation

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.core.network.microservices.model.emptyPaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.emptyWalletInfo
import com.asfoundation.wallet.iab.domain.model.emptyProductInfoData
import com.asfoundation.wallet.iab.domain.model.emptyPurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.payment_manager.payment_methods.APPCPaymentMethod
import com.asfoundation.wallet.iab.payment_manager.payment_methods.CreditCardPaymentMethod
import com.asfoundation.wallet.iab.payment_manager.payment_methods.PayPalV1PaymentMethod
import com.asfoundation.wallet.iab.presentation.icon.getDownArrow
import com.asfoundation.wallet.iab.theme.IAPTheme

@Composable
fun PaymentMethodRow(
  modifier: Modifier = Modifier,
  paymentMethodData: PaymentMethod,
  showArrow: Boolean = false
) {
  val context = LocalContext.current
  val disabledColor = IAPTheme.colors.disabledBColor
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    WalletAsyncImage(
      modifier = Modifier
        .size(width = 34.dp, height = 24.dp),
      placeholder = false,
      data = paymentMethodData.icon,
      contentDescription = null,
      alpha = 0.4f.takeIf { !paymentMethodData.isEnable }
        ?: DefaultAlpha,
      colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
        .takeIf { !paymentMethodData.isEnable },
      contentScale = ContentScale.Fit,
    )
    Column(
      modifier = Modifier
        .padding(start = 12.dp)
        .fillMaxWidth()
        .weight(1f),
    ) {
      Text(
        text = paymentMethodData.name,
        style = IAPTheme.typography.bodyLarge,
        color = disabledColor.takeIf { !paymentMethodData.isEnable }
          ?: IAPTheme.colors.paymentMethodTextColor
      )
      val description = paymentMethodData.getDescription(context)
      if (!description.isNullOrEmpty()) {
        Text(
          modifier = Modifier.padding(top = 4.dp),
          text = description,
          style = IAPTheme.typography.bodySmall,
          color = disabledColor.takeIf { !paymentMethodData.isEnable }
            ?: IAPTheme.colors.smallText
        )
      }
    }
    if (showArrow) {
      Image(
        modifier = Modifier
          .padding(start = 8.dp)
          .rotate(-90F),
        imageVector = getDownArrow(arrowColor = IAPTheme.colors.arrowColor),
        contentDescription = null
      )
    }
  }
}

@Composable
fun PaymentMethodSkeleton(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(width = 36.dp, height = 20.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(IAPTheme.colors.placeholderColor)
    )
    Box(
      modifier = Modifier
        .padding(start = 12.dp)
        .height(16.dp)
        .fillMaxWidth(0.5f)
        .clip(RoundedCornerShape(20.dp))
        .background(IAPTheme.colors.placeholderColor)
    )
  }
}

@PreviewAll
@Composable
private fun PaymentMethodPreview(
  @PreviewParameter(PaymentMethodState::class) state: Pair<PaymentMethod, Boolean>
) {
  IAPTheme {
    PaymentMethodRow(
      paymentMethodData = state.first,
      showArrow = state.second,
    )
  }
}

@PreviewAll
@Composable
fun PaymentMethodSkeletonPreview() {
  IAPTheme {
    PaymentMethodSkeleton()
  }
}

private class PaymentMethodState : PreviewParameterProvider<Pair<PaymentMethod, Boolean>> {
  override val values: Sequence<Pair<PaymentMethod, Boolean>>
    get() = sequenceOf(
      APPCPaymentMethod(
        paymentMethod = emptyPaymentMethodEntity,
        purchaseData = emptyPurchaseData,
        currencyFormatUtils = CurrencyFormatUtils(),
        walletInfo = emptyWalletInfo,
        productInfoData = emptyProductInfoData
      ) to true,
      CreditCardPaymentMethod(
        paymentMethod = emptyPaymentMethodEntity,
        purchaseData = emptyPurchaseData,
      ) to false,
      PayPalV1PaymentMethod(
        paymentMethod = emptyPaymentMethodEntity,
        purchaseData = emptyPurchaseData,
      ) to true
    )
}
