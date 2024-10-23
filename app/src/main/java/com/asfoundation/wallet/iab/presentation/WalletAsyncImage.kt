package com.asfoundation.wallet.iab.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.Transformation
import com.asfoundation.wallet.iab.theme.IAPTheme

@Composable
fun WalletAsyncImage(
  modifier: Modifier = Modifier,
  data: Any?,
  placeholder: Boolean = true,
  contentDescription: String?,
  transformations: Transformation? = null,
  colorFilter: ColorFilter? = null,
  contentScale: ContentScale = ContentScale.Crop,
) {

  val placeholderColor = IAPTheme.colors.onPrimary

  AsyncImage(
    model = buildModel(data, transformations),
    placeholder = if (placeholder) remember { ColorPainter(placeholderColor) } else null,
    error = remember { ColorPainter(placeholderColor) },
    contentDescription = contentDescription,
    contentScale = contentScale,
    colorFilter = colorFilter,
    modifier = modifier,
  )
}

@Composable
fun buildModel(
  data: Any?,
  transformations: Transformation?,
): ImageRequest {
  val builder: ImageRequest.Builder = ImageRequest.Builder(LocalContext.current)
    .data(data)
    .crossfade(600)
  transformations?.let { builder.transformations(it) }
  return builder.build()
}
