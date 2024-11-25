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
private fun PreviewGetIcBack(modifier: Modifier = Modifier) {
  WalletTheme {
    Image(
      imageVector = getIcBack(Color.Gray),
      contentDescription = null
    )
  }
}

@Composable
fun getIcBack(arrowColor: Color, backgroundColor: Color = Color.Transparent) =
  Builder(
    name = "Back Arrow",
    defaultWidth = 24.0.dp,
    defaultHeight = 24.0.dp,
    viewportWidth = 24.0f,
    viewportHeight = 24.0f
  ).apply {
    path(
      fill = SolidColor(backgroundColor),
      stroke = null,
      strokeLineWidth = 0.0f,
      strokeLineCap = Butt,
      strokeLineJoin = Miter,
      strokeLineMiter = 4.0f,
      pathFillType = NonZero
    ) {
      moveTo(0.0f, 0.0f)
      horizontalLineToRelative(24.0f)
      verticalLineToRelative(24.0f)
      horizontalLineToRelative(-24.0f)
      close()
    }
    path(
      fill = SolidColor(arrowColor),
      stroke = null,
      strokeLineWidth = 0.0f,
      strokeLineCap = Butt,
      strokeLineJoin = Miter,
      strokeLineMiter = 4.0f,
      pathFillType = NonZero
    ) {
      moveTo(15.6001f, 19.9995f)
      curveTo(15.2182f, 19.9241f, 14.9438f, 19.6918f, 14.6772f, 19.4331f)
      curveTo(12.265f, 17.0907f, 9.849f, 14.752f, 7.4325f, 12.4138f)
      curveTo(7.0521f, 12.0458f, 6.8925f, 11.6175f, 7.0764f, 11.117f)
      curveTo(7.1425f, 10.9366f, 7.2656f, 10.7589f, 7.4062f, 10.6223f)
      curveTo(9.8889f, 8.2073f, 12.3774f, 5.7984f, 14.8669f, 3.39f)
      curveTo(15.2444f, 3.0248f, 15.6969f, 2.9009f, 16.2014f, 3.0832f)
      curveTo(16.6753f, 3.2548f, 16.9438f, 3.6058f, 16.9939f, 4.0954f)
      curveTo(17.0343f, 4.4908f, 16.8704f, 4.8159f, 16.5843f, 5.092f)
      curveTo(15.9849f, 5.6693f, 15.3884f, 6.2494f, 14.7905f, 6.8285f)
      curveTo(13.2312f, 8.3383f, 11.6725f, 9.8485f, 10.1122f, 11.3578f)
      curveTo(10.0679f, 11.4007f, 10.0139f, 11.4332f, 9.9332f, 11.544f)
      curveTo(9.993f, 11.577f, 10.0636f, 11.5996f, 10.1108f, 11.6448f)
      curveTo(12.2611f, 13.7243f, 14.4091f, 15.8066f, 16.5595f, 17.8861f)
      curveTo(16.9331f, 18.2471f, 17.1f, 18.6622f, 16.938f, 19.1655f)
      curveTo(16.812f, 19.5561f, 16.5405f, 19.8148f, 16.1362f, 19.9397f)
      curveTo(16.072f, 19.9595f, 16.0078f, 19.9797f, 15.9431f, 20.0f)
      horizontalLineTo(15.6001f)
      verticalLineTo(19.9995f)
      close()
    }
  }
    .build()
