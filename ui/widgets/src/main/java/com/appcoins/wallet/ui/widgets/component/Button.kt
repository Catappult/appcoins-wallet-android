package com.appcoins.wallet.ui.widgets.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.R
import com.appcoins.wallet.ui.widgets.component.ButtonType.DEFAULT
import com.appcoins.wallet.ui.widgets.component.ButtonType.LARGE

@Composable
fun ButtonWithIcon(
  icon: Int,
  label: Int,
  onClick: () -> Unit,
  backgroundColor: Color = Color.Transparent,
  labelColor: Color,
  iconColor: Color = Color.Unspecified,
  iconSize: Dp = 12.dp,
  buttonType: ButtonType = DEFAULT
) {
  val modifier = if (buttonType == LARGE) Modifier.fillMaxWidth() else Modifier
  Button(
    onClick = onClick,
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
        text = stringResource(label),
        style = MaterialTheme.typography.bodyMedium,
        color = labelColor,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun ButtonWithText(
  label: String,
  onClick: () -> Unit,
  backgroundColor: Color = Color.Transparent,
  labelColor: Color,
  outlineColor: Color? = null,
  buttonType: ButtonType = DEFAULT
) {
  val modifier = if (buttonType == LARGE) Modifier.fillMaxWidth() else Modifier
  Button(
    onClick = { onClick.invoke() },
    modifier = modifier
      .defaultMinSize(minHeight = 40.dp),
    shape = CircleShape,
    colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
    border = BorderStroke(width = 1.dp, color = outlineColor ?: Color.Transparent)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = labelColor,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun BottomSheetButton(
  icon: Int,
  label: Int,
  onClick: () -> Unit,
  labelColor: Color = WalletColors.styleguide_white,
  iconColor: Color = WalletColors.styleguide_pink
) {
  Button(
    onClick = onClick, shape = RectangleShape,
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
    backgroundColor = WalletColors.styleguide_pink,
    labelColor = WalletColors.styleguide_white,
    iconColor = WalletColors.styleguide_white
  )
}

@Preview
@Composable
fun PreviewLargeButtonWithIcon() {
  ButtonWithIcon(
    icon = R.drawable.ic_home,
    label = R.string.action_add_wallet,
    onClick = {},
    backgroundColor = WalletColors.styleguide_pink,
    labelColor = WalletColors.styleguide_white,
    iconColor = WalletColors.styleguide_white,
    buttonType = LARGE,
    iconSize = 16.dp
  )
}

@Preview
@Composable
fun PreviewButtonWithText() {
  ButtonWithText(
    backgroundColor = WalletColors.styleguide_pink,
    labelColor = WalletColors.styleguide_white,
    label = stringResource(R.string.action_add_wallet),
    onClick = {})
}

@Preview
@Composable
fun PreviewLargeButtonWithText() {
  ButtonWithText(
    backgroundColor = WalletColors.styleguide_pink,
    labelColor = WalletColors.styleguide_white,
    label = stringResource(R.string.action_add_wallet),
    onClick = {},
    buttonType = LARGE
  )
}

@Preview
@Composable
fun PreviewBottomSheetButton() {
  BottomSheetButton(
    icon = R.drawable.ic_home,
    label = R.string.action_add_wallet,
    onClick = {},
    labelColor = WalletColors.styleguide_white
  )
}

enum class ButtonType {
  LARGE, DEFAULT
}
