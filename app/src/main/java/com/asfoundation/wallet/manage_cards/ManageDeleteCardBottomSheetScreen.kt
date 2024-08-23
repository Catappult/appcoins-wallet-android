package com.asfoundation.wallet.manage_cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.asfoundation.wallet.manage_cards.models.StoredCard

@Composable
fun ManageDeleteCardBottomSheet(
  onCancelClick: () -> Unit,
  onConfirmClick: () -> Unit,
  storedCard: StoredCard,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  var isShowLoading by rememberSaveable { mutableStateOf(false) }
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .padding(28.dp)
  ) {
    Text(
      text = stringResource(id = R.string.manage_cards_remove_confirmation),
      style = WalletTypography.medium.sp18,
      color = WalletColors.styleguide_light_grey,
      textAlign = TextAlign.Center,
    )
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 32.dp),
      horizontalArrangement = Arrangement.Center
    ) {
      Image(
        modifier = Modifier
          .padding(start = 16.dp)
          .align(Alignment.CenterVertically)
          .size(32.dp),
        painter = painterResource(storedCard.cardIcon),
        contentDescription = "Card icon",
      )
      Text(
        text = stringResource(
          com.asf.wallet.R.string.manage_cards_card_ending_body,
          storedCard.cardLastNumbers
        ),
        modifier = Modifier
          .padding(start = 8.dp)
          .align(Alignment.CenterVertically),
        style = WalletTypography.medium.sp16,
        fontWeight = FontWeight.Medium,
        color = WalletColors.styleguide_light_grey,
      )
    }
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 40.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (isShowLoading) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          val composition by
          rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_wallet))
          val progress by
          animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)

          LottieAnimation(
            modifier = Modifier.size(80.dp),
            composition = composition,
            progress = { progress })
        }
      } else {
        Row(Modifier.fillMaxWidth(0.49f)) {
          ButtonWithText(
            label = stringResource(id = R.string.manage_cards_remove_no),
            onClick = { onCancelClick() },
            backgroundColor = Color.Transparent,
            labelColor = WalletColors.styleguide_white,
            outlineColor = WalletColors.styleguide_white,
            buttonType = ButtonType.LARGE,
            fragmentName = fragmentName,
            buttonsAnalytics = buttonsAnalytics
          )
        }
        ButtonWithText(
          label = stringResource(id = R.string.manage_cards_remove_yes),
          onClick = {
            isShowLoading = true
            onConfirmClick()
          },
          backgroundColor = WalletColors.styleguide_pink,
          labelColor = WalletColors.styleguide_white,
          buttonType = ButtonType.LARGE,
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics
        )
      }
    }
  }
}

@Preview
@Composable
fun PreviewBackupDialogCardAlertBottomSheet() {
  ManageDeleteCardBottomSheet(
    {},
    {},
    StoredCard("1234", com.asf.wallet.R.drawable.ic_card_brand_visa, null, false),
    "HomeFragment",
    ButtonsAnalytics(null)
  )
}
