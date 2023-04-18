package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.appcoins.wallet.ui.common.theme.WalletColors
import java.time.LocalDateTime

@Composable
fun GamesBundle(
  items: List<GameCardData>,
  onCardClick: () -> Unit
) {
  LazyRow(
    modifier = Modifier.padding(
      top = 16.dp
    ),
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    items(items) { item ->
      CardItem(gameCardData = item, onCardClick = onCardClick)
    }
  }
}

data class GameCardData(
  val title: String,
  val gameIcon: String,
  val gameBackground: String,
  val gamePackage: String,
  val onClick: () -> Unit
)

@Composable
fun CardItem(gameCardData: GameCardData, onCardClick: () -> Unit) {
  Card(
    backgroundColor = WalletColors.styleguide_blue_secondary,
    elevation = 4.dp,
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier
      .width(300.dp)
      .height(150.dp)
  ) {

    Box(
      modifier = Modifier.fillMaxSize()
    ) {
      AsyncImage(
        model = gameCardData.gameBackground,
        contentDescription = "Game Icon",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )
      Box(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              0.3F to WalletColors.styleguide_blue_secondary.copy(alpha = 0.0F),
              0.75F to WalletColors.styleguide_blue_secondary.copy(alpha = 0.95F),
              1F to WalletColors.styleguide_blue_secondary.copy(alpha = 0.99F)
            )
          )
        )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .padding(12.dp),
//        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Card(
          backgroundColor = WalletColors.styleguide_blue_secondary,
          elevation = 4.dp,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .size(52.dp),
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
          ) {
            AsyncImage(
              model = gameCardData.gameIcon,
              contentDescription = "Game Icon",
              modifier = Modifier
                .size(52.dp),
              contentScale = ContentScale.Crop
            )
          }
        }
        Text(
          text = gameCardData.title,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = WalletColors.styleguide_light_grey,
          lineHeight = 24.sp,
          modifier = Modifier
            .align(Alignment.Bottom)
            .padding(bottom = 6.dp, start = 20.dp, end = 20.dp)
        )
        Spacer(Modifier.weight(1f))
        Text(
          text = stringResource(id = R.string.get_button),
          color = WalletColors.styleguide_pink,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .align(Alignment.Bottom)
            .padding(bottom = 6.dp, end = 12.dp)
        )

      }
    }

//    Column(
//      modifier = Modifier
//        .clickable { onCardClick }
//    ) {
//      Column(/*modifier = Modifier.padding(16.dp)*/) {
//
//
//
////        Text(text = gameCardData.title, style = MaterialTheme.typography.h5)
////        Text(
////          text = gameCardData.title,
////          style = MaterialTheme.typography.subtitle1,
////          modifier = Modifier.padding(top = 8.dp)
////        )
////        Text(
////          text = "Promotion starts on ${gameCardData.title}",
////          style = MaterialTheme.typography.caption,
////          modifier = Modifier.padding(top = 8.dp)
////        )
//      }
//    }
  }
}

@Preview
@Composable
fun previewGamesBundle() {
  GamesBundle(
    items = listOf(
      GameCardData(
        title = "Mobile Legends",
        gameIcon = "https://cdn6.aptoide.com/imgs/b/3/e/b3e336be6c4874605cbc597d811d1822_icon.png?w=128",
        gameBackground = "https://cdn6.aptoide.com/imgs/e/e/0/ee0469bf46c9a4423baf41fe8dd59b43_screen.jpg",
        gamePackage = "com.mobile.legends",
        onClick = { }
      ),
      GameCardData(
        title = "Lords Mobile",
        gameIcon = "https://cdn6.aptoide.com/imgs/0/7/e/07eb83a511499243706f0c791b0b8969_icon.png?w=128",
        gameBackground = "https://cdn6.aptoide.com/imgs/4/d/a/4dafe1624f6f5d626e8761dbe903e9a0_screen.jpg",
        gamePackage = "com.igg.android.lordsmobile",
        onClick = { }
      )
    )
  ) {

  }
}
