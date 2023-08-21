package com.appcoins.wallet.ui.common

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors

fun Modifier.shimmer(): Modifier = composed {
  var size by remember {
    mutableStateOf(IntSize.Zero)
  }
  val transition = rememberInfiniteTransition()
  val startOffsetX by transition.animateFloat(
    initialValue = -2 * size.width.toFloat(),
    targetValue = 2 * size.width.toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(1000)
    )
  )

  background(
    brush = Brush.linearGradient(
      colors = listOf(
        WalletColors.styleguide_light_grey,
        WalletColors.styleguide_dark_grey,
        WalletColors.styleguide_light_grey,
      ),
      start = Offset(startOffsetX, 0f),
      end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
    )
  )
    .onGloballyPositioned {
      size = it.size
    }
}

fun Modifier.rippleClick(onClick: () -> Unit) = composed {
  clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = rememberRipple(bounded = true, radius = 24.dp)
  ) {
    onClick()
  }
}
