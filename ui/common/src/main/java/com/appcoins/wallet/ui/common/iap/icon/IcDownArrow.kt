package com.appcoins.wallet.ui.common.iap.icon

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletTheme

@Composable
@Preview
private fun PreviewGetDownArrow(modifier: Modifier = Modifier) {
  WalletTheme {
    Image(
      imageVector = getDownArrow(Color.Gray),
      contentDescription = null
    )
  }
}

@Composable
fun getDownArrow(
  arrowColor: Color,
) =
  Builder(
    name = "Group",
    defaultWidth = 12.0.dp,
    defaultHeight = 7.0.dp,
    viewportWidth = 12.0f,
    viewportHeight = 7.0f
  ).apply {
    path(
      fill = SolidColor(arrowColor),
      stroke = null,
      strokeLineWidth = 0.0f,
      strokeLineCap = Butt,
      strokeLineJoin = Miter,
      strokeLineMiter = 4.0f,
      pathFillType = NonZero
    ) {
      moveTo(12.0f, 1.1553f)
      curveTo(11.9205f, 1.3528f, 11.7924f, 1.5281f, 11.6271f, 1.6657f)
      curveTo(9.9251f, 3.326f, 8.2257f, 4.9866f, 6.5287f, 6.6475f)
      curveTo(6.4426f, 6.7498f, 6.3269f, 6.8243f, 6.1967f, 6.8612f)
      curveTo(6.0665f, 6.898f, 5.928f, 6.8954f, 5.7993f, 6.8538f)
      curveTo(5.6895f, 6.8153f, 5.5898f, 6.7532f, 5.5077f, 6.6723f)
      curveTo(3.7431f, 4.9526f, 1.9807f, 3.2308f, 0.2206f, 1.5067f)
      curveTo(0.1512f, 1.447f, 0.0956f, 1.3736f, 0.0574f, 1.2913f)
      curveTo(0.0192f, 1.209f, -8.0E-4f, 1.1198f, -0.0012f, 1.0294f)
      curveTo(-0.0017f, 0.9391f, 0.0175f, 0.8496f, 0.0549f, 0.767f)
      curveTo(0.0924f, 0.6844f, 0.1473f, 0.6105f, 0.2161f, 0.5502f)
      curveTo(0.3519f, 0.4131f, 0.4876f, 0.2746f, 0.6353f, 0.1514f)
      curveTo(0.7537f, 0.0514f, 0.9055f, -0.0026f, 1.0619f, -4.0E-4f)
      curveTo(1.2183f, 0.0017f, 1.3685f, 0.0599f, 1.484f, 0.163f)
      curveTo(1.5228f, 0.1951f, 1.5586f, 0.2308f, 1.5944f, 0.2658f)
      lineTo(5.8918f, 4.469f)
      curveTo(5.9344f, 4.5173f, 5.9818f, 4.5612f, 6.0335f, 4.6002f)
      curveTo(6.0507f, 4.5575f, 6.0738f, 4.5173f, 6.1021f, 4.4806f)
      curveTo(7.548f, 3.0648f, 8.9949f, 1.6501f, 10.4427f, 0.2367f)
      curveTo(10.5492f, 0.1093f, 10.7024f, 0.0276f, 10.8696f, 0.0091f)
      curveTo(11.0368f, -0.0094f, 11.2048f, 0.0367f, 11.3377f, 0.1375f)
      curveTo(11.6233f, 0.3315f, 11.8517f, 0.5955f, 12.0f, 0.903f)
      lineTo(12.0f, 1.1553f)
      close()
    }
  }
    .build()
