package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionButton(
  imagePainter: Painter,
  description: String,
  onClick: () -> Unit,
  hasRedBadge: Boolean,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  if (hasRedBadge) {
    Box {
      IconButton(onClick = {
        buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, description)
        onClick() }) {
        Icon(
          painter = imagePainter,
          contentDescription = description,
          tint = WalletColors.styleguide_white,
          modifier = Modifier.scale(1.1F)
        )
      }
      Badge(
        modifier = Modifier
          .size(20.dp)
          .align(Alignment.TopEnd)
          .padding(end = 8.dp, top = 8.dp)
          .zIndex(9f)
      )
    }
  } else {
    IconButton(onClick = {
      buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, description)
      onClick() }) {
      Icon(
        painter = imagePainter,
        contentDescription = description,
        tint = WalletColors.styleguide_white,
        modifier = Modifier.scale(1.1F)
      )
    }
  }
}

@Composable
fun VectorIconButton(
  imageVector: ImageVector,
  contentDescription: Int,
  onClick: () -> Unit,
  paddingIcon: Dp = 8.dp,
  background: Color = WalletColors.styleguide_dark,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  val description = stringResource(id = contentDescription)
  IconButton(onClick = {
    buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, description)
    onClick()
  }) {
    Icon(
      imageVector = imageVector,
      contentDescription = description,
      tint = WalletColors.styleguide_white,
      modifier =
      Modifier
        .size(32.dp)
        .background(background, shape = RoundedCornerShape(8.dp))
        .padding(paddingIcon)
    )
  }
}

@Composable
fun VectorIconButton(
  painter: Painter,
  contentDescription: Int,
  onClick: () -> Unit,
  paddingIcon: Dp = 8.dp,
  iconSize: Dp = 32.dp,
  background: Color = WalletColors.styleguide_dark,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  val description = stringResource(id = contentDescription)
  IconButton(onClick = {
    buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, description)
    onClick()
  }) {
    Icon(
      painter = painter,
      contentDescription = description,
      tint = Color.White,
      modifier =
      Modifier
        .size(iconSize)
        .background(background, shape = RoundedCornerShape(8.dp))
        .padding(paddingIcon)
    )
  }
}


@Preview
@Composable
fun PreviewVectorIconButton() {
  VectorIconButton(
    imageVector = Icons.Default.MoreVert,
    contentDescription = R.string.action_more_details,
    onClick = { },
    fragmentName = "",
    buttonsAnalytics = null)
}

@Preview
@Composable
fun PreviewIconButton() {
  VectorIconButton(
    painter = painterResource(R.drawable.ic_copy_to_clip),
    contentDescription = R.string.action_more_details,
    onClick = { },
    fragmentName = "",
    buttonsAnalytics = null)
}

@Preview
@Composable
fun PreviewActionButton() {
  ActionButton(
    imagePainter = painterResource(R.drawable.ic_notifications),
    description = "Back",
    onClick = { },
    hasRedBadge = true,
    fragmentName = "",
    buttonsAnalytics = null
  )
}
