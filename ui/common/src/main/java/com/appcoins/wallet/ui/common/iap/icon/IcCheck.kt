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
private fun PreviewGetIcCheck(modifier: Modifier = Modifier) {
  WalletTheme {
    Image(
      imageVector = getIcCheck(),
      contentDescription = null
    )
  }
}

@Composable
fun getIcCheck() =
  Builder(
    name = "Group 40429",
    defaultWidth = 17.0.dp,
    defaultHeight = 17.0.dp,
    viewportWidth = 17.0f,
    viewportHeight = 17.0f
  ).apply {
    path(
      fill = SolidColor(Color(0xFF3CBD8F)),
      stroke = null,
      strokeLineWidth = 0.0f,
      strokeLineCap = Butt,
      strokeLineJoin = Miter,
      strokeLineMiter = 4.0f,
      pathFillType = NonZero
    ) {
      moveTo(8.5f, 8.5f)
      moveToRelative(-8.5f, 0.0f)
      arcToRelative(8.5f, 8.5f, 0.0f, true, true, 17.0f, 0.0f)
      arcToRelative(8.5f, 8.5f, 0.0f, true, true, -17.0f, 0.0f)
    }
    path(
      fill = SolidColor(Color(0xFFffffff)),
      stroke = null,
      strokeLineWidth = 0.0f,
      strokeLineCap = Butt,
      strokeLineJoin = Miter,
      strokeLineMiter = 4.0f,
      pathFillType = NonZero
    ) {
      moveTo(12.8035f, 5.2343f)
      curveTo(12.9305f, 5.3945f, 13.0f, 5.5954f, 13.0f, 5.8027f)
      curveTo(13.0f, 6.0101f, 12.9305f, 6.211f, 12.8035f, 6.3712f)
      lineTo(8.2913f, 11.7647f)
      curveTo(8.234f, 11.8382f, 8.1617f, 11.8975f, 8.0795f, 11.9382f)
      curveTo(7.9973f, 11.9789f, 7.9073f, 12.0f, 7.8162f, 12.0f)
      curveTo(7.7251f, 12.0f, 7.6351f, 11.9789f, 7.5529f, 11.9382f)
      curveTo(7.4707f, 11.8975f, 7.3984f, 11.8382f, 7.3411f, 11.7647f)
      lineTo(5.1967f, 9.1972f)
      curveTo(5.0695f, 9.0372f, 5.0f, 8.8364f, 5.0f, 8.6291f)
      curveTo(5.0f, 8.4219f, 5.0695f, 8.2211f, 5.1967f, 8.061f)
      curveTo(5.2539f, 7.9875f, 5.3264f, 7.9281f, 5.4087f, 7.8873f)
      curveTo(5.491f, 7.8465f, 5.5811f, 7.8253f, 5.6724f, 7.8253f)
      curveTo(5.7636f, 7.8253f, 5.8537f, 7.8465f, 5.936f, 7.8873f)
      curveTo(6.0184f, 7.9281f, 6.0908f, 7.9875f, 6.1481f, 8.061f)
      lineTo(7.8192f, 10.0599f)
      lineTo(11.8527f, 5.2357f)
      curveTo(11.91f, 5.1621f, 11.9824f, 5.1027f, 12.0647f, 5.062f)
      curveTo(12.1471f, 5.0212f, 12.2372f, 5.0f, 12.3284f, 5.0f)
      curveTo(12.4197f, 5.0f, 12.5098f, 5.0212f, 12.5921f, 5.062f)
      curveTo(12.6744f, 5.1027f, 12.7469f, 5.1621f, 12.8041f, 5.2357f)
      lineTo(12.8035f, 5.2343f)
      close()
    }
  }
    .build()
