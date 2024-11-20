package com.appcoins.wallet.ui.widgets.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.R
import com.appcoins.wallet.ui.widgets.component.WalletTextField.FILLED_FIELD_SIZE
import com.appcoins.wallet.ui.widgets.component.WalletTextField.TEXT_FIELD_LAST_INDEX
import com.appcoins.wallet.ui.widgets.validEmailChars

object WalletTextField {
  const val FILLED_FIELD_SIZE = 1
  const val TEXT_FIELD_LAST_INDEX = 3
}

@Composable
fun WalletTextFieldCustom(value: String, hintText: Int? = null, onValueChange: (String) -> Unit) {
  TextField(
    value = value,
    onValueChange = onValueChange,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
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
    placeholder = {
      Text(text = stringResource(hintText!!), color = WalletColors.styleguide_dark_grey)
    })
}

@Composable
fun WalletTextField(
  modifier: Modifier,
  value: String,
  placeHolder: String,
  backgroundColor: Color = WalletColors.styleguide_blue,
  trailingIcon: @Composable (() -> Unit)? = null,
  keyboardType: KeyboardType = KeyboardType.Text,
  roundedCornerShape: RoundedCornerShape,
  onValueChange: (String) -> Unit
) {
  TextField(
    value = value,
    onValueChange = { input ->
      if (keyboardType == KeyboardType.Email) {
        val filteredInput = input.replace(validEmailChars, "")
        onValueChange(filteredInput)
      } else {
        onValueChange(input)
      }
    },
    modifier = modifier,
    singleLine = true,
    shape = roundedCornerShape,
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
    placeholder = { Text(text = placeHolder, color = WalletColors.styleguide_dark_grey, fontSize = 14.sp) },
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WalletCodeTextField(wrongCode: Boolean, onValueChange: (String) -> Unit, code: String) {
  val values = remember { getCodeChars(code) }
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current

  fun updateValue(index: Int, newValue: String) {
    values[index] = newValue
    onValueChange(values.joinToString(""))
  }

  fun moveFocus(focusDirection: FocusDirection) = focusManager.moveFocus(focusDirection)

  Column {
    Row {
      for (i in 0..TEXT_FIELD_LAST_INDEX) {
        WalletCodeTextFieldItem(
          modifier =
          Modifier.onKeyEvent {
            if (it.key == Key.Backspace && i > 1) moveFocus(FocusDirection.Previous)
            false
          },
          value = values[i],
          onValueChange = { code ->
            if (code.length <= FILLED_FIELD_SIZE) updateValue(i, code)
            if (code.length == FILLED_FIELD_SIZE && i < TEXT_FIELD_LAST_INDEX)
              moveFocus(FocusDirection.Next)
            if (code.length == FILLED_FIELD_SIZE && i == TEXT_FIELD_LAST_INDEX)
              keyboardController?.hide()
          },
          wrongCode = wrongCode,
          imeAction = if (i == 3) ImeAction.Done else ImeAction.Next
        )
      }
    }
    if (wrongCode) {
      Text(
        text = stringResource(id = R.string.card_verification_code_wrong_error),
        color = WalletColors.styleguide_red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
      )
    }
  }
}

fun getCodeChars(code: String): MutableList<String> {
  val chars = mutableListOf<String>()
  for (index in 0..TEXT_FIELD_LAST_INDEX) chars.add(index, getCharOnPosition(code, index))
  return chars
}

fun getCharOnPosition(code: String, position: Int): String {
  return if (position < code.length) code[position].toString() else ""
}

@Composable
fun WalletCodeTextFieldItem(
  modifier: Modifier = Modifier,
  value: String,
  onValueChange: (String) -> Unit,
  wrongCode: Boolean,
  imeAction: ImeAction
) {
  TextField(
    modifier =
    modifier
      .width(64.dp)
      .padding(horizontal = 8.dp)
      .border(
        width = 1.dp,
        color =
        if (wrongCode) WalletColors.styleguide_red
        else WalletColors.styleguide_dark_grey,
        shape = RoundedCornerShape(8.dp)
      ),
    colors =
    TextFieldDefaults.colors(
      unfocusedContainerColor = WalletColors.styleguide_blue,
      focusedContainerColor = WalletColors.styleguide_blue,
      focusedIndicatorColor = WalletColors.styleguide_blue,
      unfocusedIndicatorColor = WalletColors.styleguide_blue,
      cursorColor = WalletColors.styleguide_light_grey
    ),
    shape = RoundedCornerShape(8.dp),
    textStyle =
    TextStyle(
      color = WalletColors.styleguide_light_grey,
      textAlign = TextAlign.Center,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold
    ),
    value = value,
    singleLine = true,
    keyboardOptions = KeyboardOptions(imeAction = imeAction, keyboardType = KeyboardType.Number),
    onValueChange = onValueChange,
  )
}

@Preview
@Composable
fun WalletTextFieldPreview() {
  WalletTextField(
    modifier = Modifier.fillMaxWidth(),
    "Password",
    "*******",
    roundedCornerShape = RoundedCornerShape(8.dp),
    onValueChange = {})
}

@Preview
@Composable
fun WalletCodeTextFieldPreview() {
  WalletCodeTextField(wrongCode = true, onValueChange = {}, code = "8888")
}
