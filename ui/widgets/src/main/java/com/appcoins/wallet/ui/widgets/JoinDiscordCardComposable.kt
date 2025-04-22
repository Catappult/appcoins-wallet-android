package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText



@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun JoinDiscordCardComposable(
  onJoinClick: () -> Unit,
  onCloseClick: () -> Unit,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 16.dp, end = 16.dp, top = 16.dp)
      .clip(RoundedCornerShape(16.dp))
      .heightIn(max = 176.dp)
      .clickable { onJoinClick() }
      .paint(
        painter = painterResource(R.drawable.background_join_discord),
        contentScale = ContentScale.FillBounds,
      )
      .padding(top = 8.dp, start = 16.dp, end = 8.dp)
  ) {
    Box(
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(
        painter = painterResource(id = R.drawable.ic_close_rounded),
        contentDescription = "Close",
        tint = Color.White,
        modifier = Modifier
          .size(18.dp)
          .align(Alignment.TopEnd)
          .clickable(onClick = onCloseClick)
      )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Image(
      painter = painterResource(id = R.drawable.ic_top_content_join_discord),
      contentDescription = "My Image",
      modifier = Modifier
        .width(156.dp)
        .height(50.dp)
        .align(Alignment.CenterHorizontally)
    )

    Spacer(modifier = Modifier.height(16.dp))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = stringResource(id = R.string.discord_card_title),
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = stringResource(id = R.string.discord_card_body),
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.W300,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      ButtonWithText(
        label = stringResource(id = com.appcoins.wallet.ui.common.R.string.join_button),
        onClick = { onJoinClick() },
        backgroundColor = WalletColors.styleguide_rebranding_blue,
        labelColor = WalletColors.styleguide_light_grey,
        buttonType = ButtonType.DEFAULT,
        enabled = true,
        modifier = Modifier
          .semantics { testTagsAsResourceId = true }
          .testTag("JoinDiscordButton"),
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics,
      )
      Spacer(modifier = Modifier.width(4.dp))
    }
  }
}

@Preview
@Composable
fun PreviewJoinDiscordCardComposable() {
  JoinDiscordCardComposable({}, {}, "HomeFragment", null)
}
