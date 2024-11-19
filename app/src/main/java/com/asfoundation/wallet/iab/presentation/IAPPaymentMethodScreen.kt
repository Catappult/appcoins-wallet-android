package com.asfoundation.wallet.iab.presentation

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.appcoins.wallet.ui.common.iap.icon.getIcCheck
import com.appcoins.wallet.ui.common.iap.icon.getRightArrow
import com.asfoundation.wallet.iab.payment_manager.payment_methods.emptyAPPCPaymentMethod
import com.asfoundation.wallet.iab.payment_manager.payment_methods.emptyCreditCardPaymentMethod
import com.asfoundation.wallet.iab.payment_manager.payment_methods.emptyPayPalV1PaymentMethod
import com.asfoundation.wallet.iab.theme.IAPTheme
import kotlin.random.Random

@Composable
fun PaymentMethodRow(
  modifier: Modifier = Modifier,
  paymentMethodData: PaymentMethod,
  showDescription: Boolean = true,
  isSelected: Boolean = false,
  showArrow: Boolean = false,
) {
  PaymentMethodRow(
    modifier = modifier,
    paymentMethodData = PaymentMethodRowData(
      id = paymentMethodData.id,
      name = paymentMethodData.name,
      icon = paymentMethodData.icon,
      isEnable = paymentMethodData.isEnable,
      description = paymentMethodData.getDescription(),
      showDescription = showDescription,
      isSelected = isSelected,
      showArrow = showArrow,
    )
  )
}

@Composable
fun PaymentMethodRow(
  modifier: Modifier = Modifier,
  paymentMethodData: PaymentMethodRowData,
) {
  val disabledColor = IAPTheme.colors.disabledTextColor
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
      if (paymentMethodData.showDescription) {
        if (!paymentMethodData.description.isNullOrEmpty()) {
          Text(
            modifier = Modifier.padding(top = 4.dp),
            text = paymentMethodData.description,
            style = IAPTheme.typography.bodySmall,
            color = disabledColor.takeIf { !paymentMethodData.isEnable }
              ?: IAPTheme.colors.smallText
          )
        }
      }
    }
    val image = getRightArrow(arrowColor = IAPTheme.colors.arrowColor).takeIf { paymentMethodData.showArrow }
      ?: getIcCheck().takeIf { paymentMethodData.isSelected }

    image?.let {
      Image(
        modifier = Modifier.padding(start = 8.dp),
        imageVector = image,
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
    Column {
      PaymentMethodRow(
        paymentMethodData = state.first,
        showArrow = state.second,
        isSelected = true,
      )

      Spacer(Modifier.height(16.dp))

      PaymentMethodRow(
        paymentMethodData = state.first,
        isSelected = true,
      )
    }
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
      emptyAPPCPaymentMethod to true,
      emptyCreditCardPaymentMethod to false,
      emptyPayPalV1PaymentMethod to true
    )
}

data class PaymentMethodRowData(
  val id: String,
  val name: String,
  val icon: String,
  val isEnable: Boolean,
  val description: String?,
  val showDescription: Boolean = true,
  val isSelected: Boolean = false,
  val showArrow: Boolean = false,
)

val emptyPaymentMethodRowData = PaymentMethodRowData(
  id = emptyCreditCardPaymentMethod.id,
  name = emptyCreditCardPaymentMethod.name,
  icon = emptyCreditCardPaymentMethod.icon,
  isEnable = emptyCreditCardPaymentMethod.isEnable,
  description = emptyCreditCardPaymentMethod.getDescription(),
  showDescription = true,
  isSelected = Random.nextBoolean(),
  showArrow = Random.nextBoolean(),
)