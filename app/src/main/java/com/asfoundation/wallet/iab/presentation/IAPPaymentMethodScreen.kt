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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.asfoundation.wallet.iab.presentation.icon.getDownArrow
import com.asfoundation.wallet.iab.theme.IAPTheme
import kotlin.random.Random

@Composable
fun PaymentMethod(
  modifier: Modifier = Modifier,
  paymentMethodData: PaymentMethodData,
  paymentMethodEnabled: Boolean,
  showArrow: Boolean = false
) {
  val disabledColor = IAPTheme.colors.disabledBColor
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    WalletAsyncImage(
      modifier = Modifier
        .size(width = 36.dp, height = 20.dp),
      data = paymentMethodData.paymentMethodUrl,
      contentDescription = null,
      colorFilter = ColorFilter.tint(disabledColor)
        .takeIf { paymentMethodEnabled }
    )
    Column(
      modifier = Modifier
        .padding(start = 12.dp)
        .fillMaxWidth()
        .weight(1f),
    ) {
      Text(
        text = paymentMethodData.paymentMethodName,
        style = IAPTheme.typography.bodyLarge,
        color = disabledColor.takeIf { paymentMethodEnabled }
          ?: IAPTheme.colors.paymentMethodTextColor
      )
      paymentMethodData.paymentMethodDescription?.let {
        Text(
          modifier = Modifier.padding(top = 4.dp),
          text = it,
          style = IAPTheme.typography.bodySmall,
          color = disabledColor.takeIf { paymentMethodEnabled }
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
  @PreviewParameter(PaymentMethodState::class) state: Pair<PaymentMethodData, Boolean>
) {
  IAPTheme {
    PaymentMethod(
      paymentMethodData = state.first,
      showArrow = state.second,
      paymentMethodEnabled = Random.nextBoolean(),
    )
  }
}

@PreviewAll
@Composable
fun PaymentMethodSkeletonPreview(modifier: Modifier = Modifier) {
  IAPTheme {
    PaymentMethodSkeleton()
  }
}

private class PaymentMethodState : PreviewParameterProvider<Pair<PaymentMethodData, Boolean>> {
  override val values: Sequence<Pair<PaymentMethodData, Boolean>>
    get() = sequenceOf(
      emptyPaymentMethodData to true,
      emptyPaymentMethodData to false,
      emptyPaymentMethodData.copy(paymentMethodDescription = null) to true
    )
}

data class PaymentMethodData(
  val paymentMethodUrl: String,
  val paymentMethodName: String,
  val paymentMethodDescription: String? = null,
)

val emptyPaymentMethodData = PaymentMethodData(
  paymentMethodUrl = "",
  paymentMethodName = "Credit Card",
  paymentMethodDescription = "Payment method description",
)