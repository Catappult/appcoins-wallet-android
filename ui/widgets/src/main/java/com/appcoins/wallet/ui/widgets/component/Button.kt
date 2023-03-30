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
import com.appcoins.wallet.ui.common.theme.GuidelineColors


@Composable
fun RoundedButtonWithIcon(
  id: Int = 0,
  icon: Int,
  label: Int,
  selected: Boolean,
  onClick: () -> Unit,
  selectedItem: MutableState<Int>
) {
  Button(
    onClick = {
      onClick.invoke()
      selectedItem.value = id
    },
    modifier = Modifier
      .height(48.dp),
    shape = CircleShape,
    colors = ButtonDefaults.buttonColors(
      backgroundColor = if (selected) GuidelineColors.pink else GuidelineColors.purple
    ),
    elevation = null
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = stringResource(label),
        style = MaterialTheme.typography.button,
        color = Color.White
      )
    }
  }
}
