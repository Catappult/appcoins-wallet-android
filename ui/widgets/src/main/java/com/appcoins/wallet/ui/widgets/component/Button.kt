package com.appcoins.wallet.ui.widgets.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.R
import com.appcoins.wallet.ui.widgets.component.ButtonType.DEFAULT
import com.appcoins.wallet.ui.widgets.component.ButtonType.LARGE
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ButtonWithIcon(
  icon: Int,
  label: Int,
  onClick: () -> Unit,
  backgroundColor: Color = Color.Transparent,
  labelColor: Color,
  iconColor: Color = Color.Unspecified,
  iconSize: Dp = 12.dp,
  buttonType: ButtonType = DEFAULT,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  val isButtonEnabled = remember { mutableStateOf(true) }
  val scope = rememberCoroutineScope()
  val modifier = if (buttonType == LARGE) Modifier.fillMaxWidth() else Modifier
  val buttonString = stringResource(label)
  Button(
    onClick = {
      if (isButtonEnabled.value) {
        isButtonEnabled.value = false
        scope.launch {
          delay(2000)
          isButtonEnabled.value = true
        }
        buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, buttonString)
        onClick.invoke()
      }
    },
    shape = CircleShape,
    colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
    elevation = null,
    modifier = modifier.defaultMinSize(minHeight = 40.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        tint = iconColor,
        modifier = Modifier.size(iconSize)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = buttonString,
        style = MaterialTheme.typography.bodyMedium,
        color = labelColor,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun ButtonWithText(
  modifier: Modifier = Modifier,
  label: String,
  onClick: () -> Unit,
  backgroundColor: Color = Color.Transparent,
  labelColor: Color,
  outlineColor: Color? = null,
  buttonType: ButtonType = DEFAULT,
  textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
  enabled: Boolean = true,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?,
) {
  val isButtonEnabled = remember { mutableStateOf(true) }
  val scope = rememberCoroutineScope()
  val buttonModifier = if (buttonType == LARGE) modifier
    .fillMaxWidth()
    .height(48.dp) else modifier
  Button(
    onClick = {
      if (enabled) {
        if (isButtonEnabled.value) {
          isButtonEnabled.value = false
          scope.launch {
            delay(2000)
            isButtonEnabled.value = true
          }
          buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, label)
          onClick.invoke()
        }
      }
    },
    modifier = buttonModifier.defaultMinSize(minHeight = 40.dp),
    shape = CircleShape,
    colors = ButtonDefaults.buttonColors(containerColor = if (enabled) backgroundColor else WalletColors.styleguide_medium_grey),
    border = BorderStroke(width = 1.dp, color = outlineColor ?: Color.Transparent)
  ) {
    Text(text = label, style = textStyle, color = labelColor, fontWeight = FontWeight.Bold)
  }
}

@Composable
fun BottomSheetButton(
  icon: Int,
  label: Int,
  onClick: () -> Unit,
  labelColor: Color = WalletColors.styleguide_white,
  iconColor: Color = WalletColors.styleguide_primary,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  val buttonString = stringResource(id = label)
  Button(
    onClick = {
      buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, buttonString)
      onClick.invoke()
    },
    shape = RectangleShape,
    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
    modifier = Modifier
      .defaultMinSize(minHeight = 64.dp)
      .padding(horizontal = 8.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        tint = iconColor,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(24.dp))
      Text(
        text = stringResource(label),
        style = MaterialTheme.typography.bodyMedium,
        color = labelColor,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Preview
@Composable
fun PreviewButtonWithIcon() {
  ButtonWithIcon(
    icon = R.drawable.ic_home,
    label = R.string.action_add_wallet,
    onClick = {},
    backgroundColor = WalletColors.styleguide_primary,
    labelColor = WalletColors.styleguide_white,
    iconColor = WalletColors.styleguide_white,
    fragmentName = "HomeFragment",
    buttonsAnalytics = null
  )
}

@Preview
@Composable
fun PreviewLargeButtonWithIcon() {
  ButtonWithIcon(
    icon = R.drawable.ic_home,
    label = R.string.action_add_wallet,
    onClick = {},
    backgroundColor = WalletColors.styleguide_primary,
    labelColor = WalletColors.styleguide_white,
    iconColor = WalletColors.styleguide_white,
    buttonType = LARGE,
    iconSize = 16.dp,
    fragmentName = "HomeFragment",
    buttonsAnalytics = null
  )
}

@Preview
@Composable
fun PreviewButtonWithText() {
  ButtonWithText(
    backgroundColor = WalletColors.styleguide_primary,
    labelColor = WalletColors.styleguide_white,
    label = stringResource(R.string.action_add_wallet),
    onClick = {},
    fragmentName = "HomeFragment",
    buttonsAnalytics = null
  )
}

@Preview
@Composable
fun PreviewLargeButtonWithText() {
  ButtonWithText(
    backgroundColor = WalletColors.styleguide_primary,
    labelColor = WalletColors.styleguide_white,
    label = stringResource(R.string.action_add_wallet),
    onClick = {},
    buttonType = LARGE,
    fragmentName = "HomeFragment",
    buttonsAnalytics = null
  )
}

@Preview
@Composable
fun PreviewBottomSheetButton() {
  BottomSheetButton(
    icon = R.drawable.ic_home,
    label = R.string.action_add_wallet,
    onClick = {},
    labelColor = WalletColors.styleguide_white,
    fragmentName = "HomeFragment",
    buttonsAnalytics = null
  )
}

enum class ButtonType {
  LARGE, DEFAULT
}
