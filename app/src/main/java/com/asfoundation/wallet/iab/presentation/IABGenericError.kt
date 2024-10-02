package com.asfoundation.wallet.iab.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asf.wallet.R
import com.asfoundation.wallet.iab.presentation.icon.getImgError
import com.asfoundation.wallet.iab.theme.IAPTheme

@Composable
fun GenericError(
  modifier: Modifier = Modifier,
  titleText: String = stringResource(id = R.string.error_general),
  messageText: String? = null,
  supportText: String = stringResource(id = R.string.error_contac_us_body),// TODO review copy. does not exist at the moment
  primaryButtonText: String? = null,
  onPrimaryButtonClick: (() -> Unit)? = null,
  secondaryButtonText: String,
  onSecondaryButtonClick: (() -> Unit),
  onSupportClick: () -> Unit,
) {
  val hasPrimaryButton = primaryButtonText != null && onPrimaryButtonClick != null

  // condition to minimize the probability of having a error screen with scroll
  val shouldShowSmallErrorIcon = hasPrimaryButton && isInLandscape() && messageText != null

  Column(
    modifier = modifier
      .fillMaxHeight()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 18.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(modifier = Modifier.weight(1f))
    Image(
      modifier = Modifier
        .padding(bottom = 12.dp)
        .conditional(shouldShowSmallErrorIcon, { size(40.dp) }),
      painter = rememberVectorPainter(image = getImgError(IAPTheme.colors.secondary)),
      contentDescription = null,
      colorFilter = ColorFilter.tint(IAPTheme.colors.secondary)
    )
    Text(
      modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
      text = titleText,
      style = IAPTheme.typography.titleMedium,
      color = IAPTheme.colors.onPrimary,
      textAlign = TextAlign.Center,
    )
    messageText?.let {
      Text(
        modifier = Modifier.padding(bottom = 10.dp),
        text = it,
        style = IAPTheme.typography.bodyMedium,
        color = IAPTheme.colors.smallText,
        textAlign = TextAlign.Center,
      )
    }
    Text(
      modifier = Modifier.padding(top = 10.dp),
      text = supportText,
      style = IAPTheme.typography.bodySmall,
      color = IAPTheme.colors.smallText,
      textAlign = TextAlign.Center,
    )
    Image(
      modifier = Modifier
        .padding(horizontal = 10.dp)
        .padding(top = 10.dp)
        .size(width = 132.dp, height = 20.dp)
        .addClick(onClick = onSupportClick, "onSupportClick"),
      painter = painterResource(id = R.drawable.ic_support_chat_dark),
      contentDescription = null,
    )

    Spacer(modifier = Modifier.weight(1f))

    if (onPrimaryButtonClick != null && primaryButtonText != null) {
      IABOpaqueButton(
        modifier = Modifier.padding(top = 18.dp),
        onClick = onPrimaryButtonClick,
        text = primaryButtonText,
        testTag = "errorPrimaryButton"
      )
    }

    IABTransparentButton(
      modifier = Modifier.padding(top = 18.dp),
      onClick = onSecondaryButtonClick,
      text = secondaryButtonText,
      testTag = "errorSecondaryButton"
    )
  }
}

@PreviewAll
@Composable
private fun PreviewGenericError() {
  IAPTheme {
    IAPBottomSheet(
      showWalletIcon = false,
      fullscreen = false
    ) {
      GenericError(
        messageText = "It seems there’re some restrictions to your card.\nPlease contact your bank or try with a different one.",
        primaryButtonText = "Verify Payment Method",
        onPrimaryButtonClick = {},
        secondaryButtonText = "Try Again",
        onSecondaryButtonClick = {},
        onSupportClick = {},
      )
    }
  }
}

@PreviewAll
@Composable
private fun PreviewMainScreenWithoutMessage() {
  IAPTheme {
    IAPBottomSheet(
      showWalletIcon = false,
      fullscreen = false
    ) {
      GenericError(
        primaryButtonText = "Verify Payment Method",
        onPrimaryButtonClick = {},
        secondaryButtonText = "Try again",
        onSecondaryButtonClick = {},
        onSupportClick = {},
      )
    }
  }
}

@PreviewAll
@Composable
private fun PreviewMainScreenWithoutPrimaryButton() {
  IAPTheme {
    IAPBottomSheet(
      showWalletIcon = false,
      fullscreen = false
    ) {
      GenericError(
        messageText = stringResource(id = R.string.unknown_error),
        secondaryButtonText = "Try again",
        onSecondaryButtonClick = {},
        onSupportClick = {},
      )
    }
  }
}

@PreviewAll
@Composable
private fun PreviewMainScreenWithoutMessageAndPrimaryButton() {
  IAPTheme {
    IAPBottomSheet(
      showWalletIcon = false,
      fullscreen = false
    ) {
      GenericError(
        secondaryButtonText = "Try again",
        onSecondaryButtonClick = {},
        onSupportClick = {},
      )
    }
  }
}
