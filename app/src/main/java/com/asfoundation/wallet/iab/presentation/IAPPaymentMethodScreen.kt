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
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.asfoundation.wallet.iab.payment_manager.domain.PaymentMethodInfo
import com.asfoundation.wallet.iab.presentation.icon.getDownArrow
import com.asfoundation.wallet.iab.theme.IAPTheme
import kotlin.random.Random

@Composable
fun PaymentMethodRow(
  modifier: Modifier = Modifier,
  paymentMethodData: PaymentMethodData,
  showArrow: Boolean = false
) {
  val disabledColor = IAPTheme.colors.disabledBColor
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    WalletAsyncImage(
      modifier = Modifier
        .size(width = 34.dp, height = 24.dp),
      placeholder = false,
      data = paymentMethodData.paymentMethodUrl,
      contentDescription = null,
      alpha = 0.4f.takeIf { !paymentMethodData.isEnable }
        ?: DefaultAlpha,
      contentScale = ContentScale.Fit,
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
        color = disabledColor.takeIf { !paymentMethodData.isEnable }
          ?: IAPTheme.colors.paymentMethodTextColor
      )
      if (!paymentMethodData.paymentMethodDescription.isNullOrEmpty() || paymentMethodData.balance != null) {
        Text(
          modifier = Modifier.padding(top = 4.dp),
          text = paymentMethodData.paymentMethodDescription.takeIf { !paymentMethodData.paymentMethodDescription.isNullOrEmpty() && paymentMethodData.balance == null }
            ?: "Balance: ${paymentMethodData.balance}",
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
  @PreviewParameter(PaymentMethodState::class) state: Pair<PaymentMethodData, Boolean>
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
  val id: String,
  val paymentMethodUrl: String,
  val paymentMethodName: String,
  val isEnable: Boolean,
  val paymentMethodDescription: String?,
  val balance: String?,
)

fun PaymentMethodInfo.toPaymentMethodData() = PaymentMethodData(
  id = paymentMethod.id,
  paymentMethodUrl = paymentMethod.iconUrl,
  paymentMethodName = paymentMethod.label,
  paymentMethodDescription = paymentMethod.message,
  isEnable = paymentMethod.isAvailable(),
  balance = balance
)

val emptyPaymentMethodData = PaymentMethodData(
  id = "1",
  paymentMethodUrl = "",
  paymentMethodName = "Credit Card",
  isEnable = Random.nextBoolean(),
  paymentMethodDescription = "Payment method description",
  balance = null,
)
