package com.appcoins.wallet.ui.widgets

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource

@Composable
fun ActionButton(
  imagePainter: Painter,
  description: String,
  onClick: () -> Unit
) {
  IconButton(
    onClick = {
      onClick()
    }) {
    Icon(
      painter = imagePainter,
      contentDescription = description,
      tint = colorResource(R.color.styleguide_white),
      modifier = Modifier
        .scale(1.1F)
    )
  }
}
