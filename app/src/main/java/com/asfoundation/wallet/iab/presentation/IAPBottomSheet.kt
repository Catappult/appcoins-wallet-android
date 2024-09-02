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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.ui.common.theme.WalletTheme
import com.asf.wallet.R
import com.asfoundation.wallet.iab.presentation.icon.getIcBack
import kotlin.random.Random

val backgroundColor = Color(0xFFF5F5FA)
val icBackTint = Color(0xFF242333)

@Composable
fun IAPBottomSheet(
  modifier: Modifier = Modifier,
  showWalletIcon: Boolean,
  fullscreen: Boolean,
  content: @Composable () -> Unit,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.60f))
  ) {
    when (LocalConfiguration.current.orientation) {
      Configuration.ORIENTATION_LANDSCAPE,
      -> {
        IAPBottomSheetLandscape(
          modifier = modifier,
          content = content,
          fullscreen = fullscreen,
          showWalletIcon = showWalletIcon,
        )
      }

      else -> {
        IAPBottomSheetPortrait(
          modifier = modifier,
          content = content,
          showWalletIcon = showWalletIcon,
          fullscreen = fullscreen,
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
  content: @Composable () -> Unit,
) {
  val minHeight = 304.dp

  if (fullscreen) {
    Column(
      modifier = modifier
        .fillMaxSize()
        .background(color = backgroundColor)
    ) {
      Image(
        modifier = Modifier
          .padding(16.dp)
          .size(24.dp),
        imageVector = getIcBack(icBackTint),
        contentDescription = null,
      )
      content()
    }
  } else {
    Box(modifier = modifier.fillMaxSize()) {
      Surface(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .height(intrinsicSize = IntrinsicSize.Min)
          .defaultMinSize(minHeight = minHeight)
          .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
      ) {
        Column(
          modifier = Modifier.background(color = backgroundColor)
        ) {
          if (showWalletIcon) {
            WalletLogo(modifier = Modifier.padding(top = 16.dp))
          }
          content()
        }
      }
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
      .padding(bottom = 16.dp)
      .width(70.dp)
      .height(20.dp),
    painter = painterResource(R.drawable.ic_app_logo_black),
    contentDescription = null,
  )
}

@Composable
private fun IAPBottomSheetLandscape(
  modifier: Modifier = Modifier,
  showWalletIcon: Boolean,
  fullscreen: Boolean,
  content: @Composable () -> Unit,
) {
  Box(modifier = modifier.fillMaxSize()) {
    Surface(
      modifier = Modifier
        .conditional(fullscreen, { fillMaxHeight() })
        .height(intrinsicSize = IntrinsicSize.Min)
        .padding(top = 18.dp)
        .align(Alignment.BottomCenter)
        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
    ) {
      Column(
        modifier = Modifier
          .conditional(fullscreen, { fillMaxHeight() })
          .width(456.dp)
          .background(color = backgroundColor)
      ) {
        if (showWalletIcon) {
          WalletLogo(modifier = Modifier.padding(top = 16.dp))
        }
        content()
      }
    }
  }
}

@PreviewAll
@Composable
private fun PreviewIAPBottomSheet() {
  val showingFullScreen = Random.nextBoolean()
  WalletTheme {
    IAPBottomSheet(
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
