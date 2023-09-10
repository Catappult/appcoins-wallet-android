package com.appcoins.wallet.ui.widgets.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.R

@Composable
fun WalletTextFieldCustom(
    value: String,
    hintText: String = "",
    title: String = "",
    onValueChange: (String) -> Unit
) {
    Column {
        if (title.isNotEmpty())
            Text(
                text = title,
                Modifier.padding(start = 8.dp),
                style = WalletTypography.medium.sp14,
                color = WalletColors.styleguide_light_grey
            )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors =
            TextFieldDefaults.colors(
                focusedContainerColor = WalletColors.styleguide_blue_secondary,
                unfocusedContainerColor = WalletColors.styleguide_blue_secondary,
                focusedIndicatorColor = WalletColors.styleguide_blue,
                unfocusedIndicatorColor = WalletColors.styleguide_blue,
                focusedTextColor = WalletColors.styleguide_light_grey,
                unfocusedTextColor = WalletColors.styleguide_light_grey,
                cursorColor = WalletColors.styleguide_light_grey
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            placeholder = { Text(text = hintText, color = WalletColors.styleguide_dark_grey) }
        )
    }
}

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
      colors =
      TextFieldDefaults.colors(
          focusedContainerColor = backgroundColor,
          unfocusedContainerColor = backgroundColor,
          focusedIndicatorColor = Color.Transparent,
          unfocusedIndicatorColor = Color.Transparent,
          focusedTextColor = WalletColors.styleguide_light_grey,
          unfocusedTextColor = WalletColors.styleguide_light_grey,
      ),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default, keyboardType = keyboardType),
      placeholder = { Text(text = placeHolder, color = WalletColors.styleguide_dark_grey) },
      trailingIcon = trailingIcon
  )
}

@Composable
fun WalletTextFieldPassword(value: String, hintText: Int? = null, onValueChange: (String) -> Unit) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors =
        TextFieldDefaults.colors(
            focusedContainerColor = WalletColors.styleguide_blue,
            unfocusedContainerColor = WalletColors.styleguide_blue,
            focusedIndicatorColor = WalletColors.styleguide_blue,
            unfocusedIndicatorColor = WalletColors.styleguide_blue,
            focusedTextColor = WalletColors.styleguide_light_grey,
            unfocusedTextColor = WalletColors.styleguide_light_grey,
            cursorColor = WalletColors.styleguide_light_grey
        ),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter =
                    if (!passwordVisible) painterResource(R.drawable.ic_transaction_poa)
                    else painterResource(R.drawable.ic_password_off),
                    contentDescription = "show password",
                    modifier = Modifier.size(22.dp),
                    tint = WalletColors.styleguide_dark_grey
                )
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
        placeholder = {
            Text(text = stringResource(hintText!!), color = WalletColors.styleguide_dark_grey)
        },
        visualTransformation =
        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
    )
}