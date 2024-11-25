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
      imageVector = getImgError(Color(0xFFFF6381)),
      contentDescription = null
    )
  }
}

@Composable
fun getImgError(color: Color) =
  Builder(
    name = "Image Error",
    defaultWidth = 60.0.dp,
    defaultHeight = 60.0.dp,
    viewportWidth = 60.0f,
    viewportHeight = 60.0f
  ).apply {
    path(
      fill = SolidColor(color),
      stroke = null,
      strokeLineWidth = 0.0f,
      strokeLineCap = Butt,
      strokeLineJoin = Miter,
      strokeLineMiter = 4.0f,
      pathFillType = NonZero
    ) {
      moveTo(28.239f, -0.001f)
      horizontalLineTo(31.754f)
      curveTo(31.9007f, 0.043f, 32.0504f, 0.0764f, 32.202f, 0.099f)
      curveTo(36.3477f, 0.3377f, 40.3871f, 1.5029f, 44.023f, 3.509f)
      curveTo(53.883f, 9.028f, 59.243f, 17.465f, 59.939f, 28.747f)
      curveTo(60.2945f, 34.0849f, 59.084f, 39.4104f, 56.456f, 44.07f)
      curveTo(50.95f, 53.907f, 42.51f, 59.238f, 31.256f, 59.946f)
      curveTo(26.4344f, 60.2578f, 21.6159f, 59.292f, 17.287f, 57.146f)
      curveTo(12.9108f, 55.1357f, 9.0918f, 52.087f, 6.1619f, 48.2649f)
      curveTo(3.2319f, 44.4429f, 1.2797f, 39.9632f, 0.475f, 35.215f)
      curveTo(0.252f, 34.074f, 0.157f, 32.907f, 0.002f, 31.752f)
      verticalLineTo(28.237f)
      curveTo(0.0461f, 28.071f, 0.0795f, 27.9023f, 0.102f, 27.732f)
      curveTo(0.3603f, 23.5902f, 1.5291f, 19.5564f, 3.525f, 15.918f)
      curveTo(5.6388f, 11.8822f, 8.6558f, 8.3887f, 12.3409f, 5.7098f)
      curveTo(16.026f, 3.0309f, 20.2798f, 1.2388f, 24.771f, 0.473f)
      curveTo(25.915f, 0.254f, 27.083f, 0.155f, 28.239f, -0.001f)
      close()
      moveTo(30.009f, 54.363f)
      curveTo(34.8296f, 54.3604f, 39.5411f, 52.9283f, 43.5477f, 50.2476f)
      curveTo(47.5542f, 47.5669f, 50.6757f, 43.7583f, 52.5174f, 39.3033f)
      curveTo(54.359f, 34.8484f, 54.8381f, 29.9473f, 53.894f, 25.22f)
      curveTo(52.9498f, 20.4928f, 50.6249f, 16.1517f, 47.2133f, 12.746f)
      curveTo(43.8016f, 9.3402f, 39.4566f, 7.0228f, 34.7277f, 6.0868f)
      curveTo(29.9989f, 5.1508f, 25.0986f, 5.6383f, 20.6468f, 7.4877f)
      curveTo(16.1951f, 9.337f, 12.3918f, 12.4651f, 9.718f, 16.4762f)
      curveTo(7.0443f, 20.4874f, 5.6202f, 25.2014f, 5.6259f, 30.022f)
      curveTo(5.6466f, 36.4787f, 8.2234f, 42.6644f, 12.7928f, 47.2261f)
      curveTo(17.3622f, 51.7878f, 23.5523f, 54.3543f, 30.009f, 54.364f)
    }
    path(
      fill = SolidColor(color),
      stroke = null,
      strokeLineWidth = 0.0f,
      strokeLineCap = Butt,
      strokeLineJoin = Miter,
      strokeLineMiter = 4.0f,
      pathFillType = NonZero
    ) {
      moveTo(26.2348f, 24.3109f)
      curveTo(26.2348f, 21.6755f, 26.2348f, 19.0402f, 26.2348f, 16.4049f)
      curveTo(26.174f, 15.685f, 26.3875f, 14.9688f, 26.8326f, 14.3998f)
      curveTo(27.2777f, 13.8308f, 27.9215f, 13.4511f, 28.6348f, 13.3369f)
      curveTo(29.6975f, 13.0456f, 30.826f, 13.107f, 31.8508f, 13.5119f)
      curveTo(32.4308f, 13.7033f, 32.9319f, 14.0801f, 33.2768f, 14.5842f)
      curveTo(33.6217f, 15.0883f, 33.7914f, 15.6918f, 33.7598f, 16.3019f)
      curveTo(33.7678f, 17.9809f, 33.7598f, 19.6589f, 33.7598f, 21.3379f)
      verticalLineTo(32.0559f)
      curveTo(33.7598f, 34.1559f, 32.9278f, 35.1639f, 30.8498f, 35.5429f)
      curveTo(29.9435f, 35.7148f, 29.0071f, 35.6253f, 28.1498f, 35.2849f)
      curveTo(27.565f, 35.1052f, 27.0579f, 34.7335f, 26.7106f, 34.2299f)
      curveTo(26.3633f, 33.7263f, 26.196f, 33.1203f, 26.2358f, 32.5099f)
      curveTo(26.2268f, 29.7769f, 26.2358f, 27.0439f, 26.2358f, 24.3099f)
    }
    path(
      fill = SolidColor(color),
      stroke = null,
      strokeLineWidth = 0.0f,
      strokeLineCap = Butt,
      strokeLineJoin = Miter,
      strokeLineMiter = 4.0f,
      pathFillType = NonZero
    ) {
      moveTo(30.015f, 39.3721f)
      curveTo(30.7555f, 39.3757f, 31.4783f, 39.599f, 32.0918f, 40.0137f)
      curveTo(32.7054f, 40.4284f, 33.182f, 41.0158f, 33.4614f, 41.7016f)
      curveTo(33.7408f, 42.3874f, 33.8103f, 43.1407f, 33.6613f, 43.8661f)
      curveTo(33.5122f, 44.5915f, 33.1512f, 45.2563f, 32.624f, 45.7763f)
      curveTo(32.0968f, 46.2964f, 31.4271f, 46.6482f, 30.6998f, 46.7874f)
      curveTo(29.9725f, 46.9265f, 29.2202f, 46.8467f, 28.5383f, 46.558f)
      curveTo(27.8563f, 46.2692f, 27.2754f, 45.7846f, 26.8692f, 45.1655f)
      curveTo(26.4629f, 44.5464f, 26.2495f, 43.8206f, 26.256f, 43.0801f)
      curveTo(26.273f, 42.0931f, 26.6761f, 41.1522f, 27.3788f, 40.459f)
      curveTo(28.0815f, 39.7658f, 29.0279f, 39.3756f, 30.015f, 39.3721f)
      close()
    }
  }
    .build()
