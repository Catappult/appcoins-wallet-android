package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun ActionButton(
  imagePainter: Painter,
  description: String,
  onClick: () -> Unit
) {
  IconButton(onClick = { onClick() }) {
    Icon(
      painter = imagePainter,
      contentDescription = description,
      tint = WalletColors.styleguide_white,
      modifier = Modifier.scale(1.1F)
    )
  }
}

@Composable
fun VectorIconButton(imageVector: ImageVector, contentDescription: Int, onClick: () -> Unit) {
  IconButton(onClick = onClick) {
    Icon(
      imageVector = imageVector,
      contentDescription = stringResource(id = contentDescription),
      tint = WalletColors.styleguide_white,
      modifier =
      Modifier
        .size(32.dp)
        .background(WalletColors.styleguide_blue, shape = RoundedCornerShape(8.dp)),
    )
  }

}

@Preview
@Composable
fun PreviewVectorIconButton() {
  VectorIconButton(
    imageVector = Icons.Default.MoreVert,
    contentDescription = R.string.action_more_details,
    onClick = { })
}

@Preview
@Composable
fun PreviewActionButton() {
  ActionButton(
    imagePainter = painterResource(R.drawable.ic_notifications),
    description = "Back",
    onClick = { })
}
