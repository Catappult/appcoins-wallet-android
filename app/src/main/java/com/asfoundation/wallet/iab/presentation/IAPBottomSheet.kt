package com.asfoundation.wallet.iab.presentation

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asf.wallet.R
import com.asfoundation.wallet.iab.presentation.icon.getIcBack
import com.asfoundation.wallet.iab.theme.IAPTheme

@Composable
fun IAPBottomSheet(
  modifier: Modifier = Modifier,
  showWalletIcon: Boolean,
  fullscreen: Boolean,
  onBackClick: (() -> Unit)? = null,
  content: @Composable () -> Unit,
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.BottomCenter
  ) {
    when (LocalConfiguration.current.orientation) {
      Configuration.ORIENTATION_LANDSCAPE -> {
        IAPBottomSheetLandscape(
          content = content,
          fullscreen = fullscreen,
          onBackClick = onBackClick,
        )
      }

      else -> {
        IAPBottomSheetPortrait(
          content = content,
          showWalletIcon = showWalletIcon,
          fullscreen = fullscreen,
          onBackClick = onBackClick,
        )
      }
    }
  }
}

@Composable
private fun IAPBottomSheetPortrait(
  modifier: Modifier = Modifier,
  showWalletIcon: Boolean,
  fullscreen: Boolean,
  onBackClick: (() -> Unit)?,
  content: @Composable () -> Unit,
) {
  val minHeight = 304.dp

  if (fullscreen) {
    Column(
      modifier = modifier
        .fillMaxSize()
        .background(color = IAPTheme.colors.primary)
    ) {
      Image(
        modifier = Modifier
          .size(48.dp)
          .conditional(
            condition = onBackClick != null,
            ifTrue = { addClick(onClick = onBackClick!!, "onBottomSheetBackClick") })
          .padding(12.dp),
        imageVector = getIcBack(IAPTheme.colors.backArrow),
        contentDescription = null,
      )
      content()
    }
  } else {
    Column(
      modifier = modifier
        .fillMaxWidth()
        .height(intrinsicSize = IntrinsicSize.Min)
        .defaultMinSize(minHeight = minHeight)
        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
        .background(color = IAPTheme.colors.primary)
    ) {
      if (showWalletIcon) {
        WalletLogo(modifier = Modifier.padding(top = 16.dp))
      }
      content()
    }
  }
}

@Composable
private fun IAPBottomSheetLandscape(
  modifier: Modifier = Modifier,
  fullscreen: Boolean,
  onBackClick: (() -> Unit)?,
  content: @Composable () -> Unit,
) {
  val width = 456.dp
  if (fullscreen) {
    Column(
      modifier = modifier
        .fillMaxHeight()
        .width(width)
        .background(color = IAPTheme.colors.primary)
    ) {
      Image(
        modifier = Modifier
          .size(48.dp)
          .conditional(
            condition = onBackClick != null,
            ifTrue = { addClick(onClick = onBackClick!!, "onBottomSheetBackClick") })
          .padding(12.dp),
        imageVector = getIcBack(IAPTheme.colors.backArrow),
        contentDescription = null,
      )
      content()
    }
  } else {
    Column(
      modifier = modifier
        .width(width)
        .height(intrinsicSize = IntrinsicSize.Min)
        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
        .background(color = IAPTheme.colors.primary)
    ) {
      content()
    }
  }
}

@Composable
private fun WalletLogo(
  modifier: Modifier = Modifier
) {
  Image(
    modifier = modifier
      .padding(horizontal = 24.dp)
      .width(70.dp)
      .height(20.dp),
    painter = painterResource(R.drawable.ic_app_logo_black),
    contentDescription = null,
  )
}

@PreviewAll
@Composable
private fun PreviewIAPBottomSheet(
  @PreviewParameter(PreviewBottomSheetProvider::class) showingFullScreen: Boolean
) {
  IAPTheme {
    IAPBottomSheet(
      modifier = Modifier.background(Color.Black.copy(0.60f)),
      showWalletIcon = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT,
      fullscreen = showingFullScreen,
      content = {
        Text(
          modifier = Modifier.padding(horizontal = 16.dp),
          text = "Showing full screen mode: $showingFullScreen",
          textAlign = TextAlign.Center,
          color = Color.Black,
          style = TextStyle(fontSize = 20.sp)
        )
      }
    )
  }
}

private class PreviewBottomSheetProvider : PreviewParameterProvider<Boolean> {
  override val values: Sequence<Boolean>
    get() = sequenceOf(
      false,
      true
    )
}
