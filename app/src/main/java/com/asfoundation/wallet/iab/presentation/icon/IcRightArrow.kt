package com.asfoundation.wallet.iab.presentation.icon

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
private fun PreviewGetRightArrow(modifier: Modifier = Modifier) {
  WalletTheme {
    Image(
      imageVector = getRightArrow(Color.Gray),
      contentDescription = null
    )
  }
}

@Composable
fun getRightArrow(
  arrowColor: Color,
) =
  Builder(
    name = "Right-arrow",
    defaultWidth = 8.0.dp,
    defaultHeight = 12.0.dp,
    viewportWidth = 8.0f,
    viewportHeight = 12.0f
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
      moveTo(1.7105f, 0.0f)
      curveTo(1.908f, 0.0795f, 2.0833f, 0.2076f, 2.2208f, 0.3729f)
      curveTo(3.8812f, 2.0749f, 5.5418f, 3.7743f, 7.2027f, 5.4713f)
      curveTo(7.305f, 5.5574f, 7.3795f, 5.6731f, 7.4163f, 5.8033f)
      curveTo(7.4532f, 5.9335f, 7.4506f, 6.072f, 7.409f, 6.2007f)
      curveTo(7.3704f, 6.3105f, 7.3084f, 6.4102f, 7.2274f, 6.4923f)
      curveTo(5.5078f, 8.2569f, 3.7859f, 10.0193f, 2.0619f, 11.7794f)
      curveTo(2.0022f, 11.8488f, 1.9287f, 11.9044f, 1.8465f, 11.9426f)
      curveTo(1.7642f, 11.9808f, 1.675f, 12.0008f, 1.5846f, 12.0012f)
      curveTo(1.4942f, 12.0017f, 1.4048f, 11.9825f, 1.3222f, 11.9451f)
      curveTo(1.2396f, 11.9076f, 1.1657f, 11.8527f, 1.1053f, 11.7839f)
      curveTo(0.9683f, 11.6481f, 0.8297f, 11.5124f, 0.7065f, 11.3647f)
      curveTo(0.6066f, 11.2463f, 0.5526f, 11.0945f, 0.5547f, 10.9381f)
      curveTo(0.5569f, 10.7817f, 0.6151f, 10.6315f, 0.7182f, 10.516f)
      curveTo(0.7503f, 10.4772f, 0.786f, 10.4414f, 0.821f, 10.4056f)
      lineTo(5.0242f, 6.1082f)
      curveTo(5.0725f, 6.0656f, 5.1164f, 6.0182f, 5.1554f, 5.9665f)
      curveTo(5.1127f, 5.9493f, 5.0725f, 5.9262f, 5.0358f, 5.8979f)
      curveTo(3.6199f, 4.452f, 2.2053f, 3.0051f, 0.7918f, 1.5573f)
      curveTo(0.6645f, 1.4508f, 0.5828f, 1.2976f, 0.5643f, 1.1304f)
      curveTo(0.5458f, 0.9632f, 0.5918f, 0.7952f, 0.6927f, 0.6623f)
      curveTo(0.8867f, 0.3767f, 1.1507f, 0.1483f, 1.4582f, 0.0f)
      lineTo(1.7105f, 0.0f)
      close()
    }
  }
    .build()
