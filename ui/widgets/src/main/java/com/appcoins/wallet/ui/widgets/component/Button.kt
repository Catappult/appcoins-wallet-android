package com.appcoins.wallet.ui.widgets.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun RoundedButtonWithIcon(
  icon: Int,
  label: Int,
  onClick: () -> Unit,
  destinationId: Int = 0,
  selected: Boolean = true,
  clickedItem: MutableState<Int>? = null
) {
  Button(
    onClick = {
      onClick.invoke()
      if (clickedItem != null) clickedItem.value = destinationId
    },
    modifier = Modifier
      .height(48.dp),
    shape = CircleShape,
    colors = ButtonDefaults.buttonColors(
      backgroundColor = if (selected) WalletColors.styleguide_pink else WalletColors.styleguide_blue_secondary
    ),
    elevation = null
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      val contentColor = if (selected) Color.White else WalletColors.styleguide_medium_grey
      Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = stringResource(label),
        style = MaterialTheme.typography.button,
        color = contentColor
      )
    }
  }
}
