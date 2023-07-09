package com.appcoins.wallet.ui.widgets.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun WalletTextField(
  value: String,
  placeHolder: String,
  backgroundColor: Color = WalletColors.styleguide_blue,
  trailingIcon: @Composable (() -> Unit)? = null,
  keyboardType: KeyboardType = KeyboardType.Text,
  onValueChange: (String) -> Unit
) {
  TextField(
    value = value,
    onValueChange = onValueChange,
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
    shape = RoundedCornerShape(8.dp),
    colors = TextFieldDefaults.colors(
      focusedContainerColor = backgroundColor,
      unfocusedContainerColor = backgroundColor,
      focusedIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent,
      focusedTextColor = WalletColors.styleguide_light_grey,
      unfocusedTextColor = WalletColors.styleguide_light_grey,
    ),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default, keyboardType = keyboardType),
    placeholder = {
      Text(
        text = placeHolder, color = WalletColors.styleguide_dark_grey
      )
    },
    trailingIcon = trailingIcon
  )
}