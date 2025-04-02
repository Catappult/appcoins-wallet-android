package com.appcoins.wallet.ui.widgets

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun shimmerSkeleton(showShimmer: Boolean = true, targetValue: Float = 1000f, shimmerColor: Color = WalletColors.styleguide_skeleton_loading): Brush {
  return if (showShimmer) {
    val shimmerColors = listOf(
      shimmerColor.copy(alpha = 0.8f),
      shimmerColor.copy(alpha = 0.2f),
      shimmerColor.copy(alpha = 0.8f),
    )

    val transition = rememberInfiniteTransition(label = "")
    val translateAnimation = transition.animateFloat(
      initialValue = 0f,
      targetValue = targetValue,
      animationSpec = infiniteRepeatable(
        animation = tween(800), repeatMode = RepeatMode.Reverse
      ), label = ""
    )
    Brush.linearGradient(
      colors = shimmerColors,
      start = Offset.Zero,
      end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )
  } else {
    Brush.linearGradient(
      colors = listOf(Color.Transparent, Color.Transparent),
      start = Offset.Zero,
      end = Offset.Zero
    )
  }
}