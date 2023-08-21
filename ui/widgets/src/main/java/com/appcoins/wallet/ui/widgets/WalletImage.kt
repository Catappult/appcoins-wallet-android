package com.appcoins.wallet.ui.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.Transformation
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun WalletImage(
  modifier: Modifier = Modifier,
  data: Any?,
  contentDescription: String? = null,
  placeholderColor: Color = WalletColors.styleguide_pink,
  contentScale: ContentScale = ContentScale.Crop,
  transformations: Transformation? = null
) {
  AsyncImage(
    model = buildModel(data, transformations),
    placeholder = remember{ColorPainter(placeholderColor)},
    error = remember{ ColorPainter(placeholderColor) },
    contentDescription = contentDescription,
    contentScale = contentScale,
    modifier = modifier
  )
}

@Composable
fun buildModel(data: Any?, transformations: Transformation?): ImageRequest {
  val builder: ImageRequest.Builder = ImageRequest.Builder(LocalContext.current)
    .data(data)
    .crossfade(600)
  transformations?.let { builder.transformations(it) }
  return builder.build()
}