package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.appcoins.wallet.ui.common.theme.WalletColors


var gameClicked: String = ""

@Composable
fun GamesBundle(
  items: List<GameData>,
  dialog: (gamePackage: String) -> Unit,
  fetchFromApiCallback: () -> Unit
) {
  fetchFromApiCallback()
  if (items.isNotEmpty()) {
    Text(
      text = stringResource(id = R.string.home_appcoins_compatible_games_title),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = WalletColors.styleguide_dark_grey,
      modifier = Modifier.padding(top = 27.dp, end = 13.dp, start = 24.dp)
    )
    LazyRow(
      modifier = Modifier.padding(
        top = 16.dp
      ),
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      items(items) { item ->
        CardItem(
          gameCardData = item,
          dialogFragment = dialog
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardItem(
  gameCardData: GameData,
  dialogFragment: (gamePackage: String) -> Unit
) {
  val context = LocalContext.current
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
    elevation = CardDefaults.cardElevation(4.dp),
    shape = RoundedCornerShape(8.dp),
    //onClick = { dialogFragment(gameCardData.gamePackage)
    //Log.i("Card Item Package", "Package do Card -> "+ gameCardData.gamePackage)},
    onClick = {
      gameClicked = gameCardData.gamePackage
      dialogFragment.invoke(gameCardData.gamePackage)
    },
    modifier = Modifier
      .width(332.dp)
      .height(150.dp)
  ) {

    Box(
      modifier = Modifier.fillMaxSize()
    ) {
      AsyncImage(
        model = gameCardData.gameBackground,
        contentDescription = null,
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
          colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
          elevation = CardDefaults.cardElevation(4.dp),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .size(52.dp),
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
          ) {
            AsyncImage(
              model = gameCardData.gameIcon,
              contentDescription = null,
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
            .weight(1f)
            .padding(bottom = 6.dp, start = 20.dp, end = 20.dp)
        )
        Spacer(Modifier.weight(0.1f))
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
  }
}

data class GameData(
  val title: String,
  val gameIcon: String,
  val gameBackground: String,
  val gamePackage: String
)

@Preview
@Composable
fun PreviewGamesBundle() {
  GamesBundle(
    items = listOf(
      GameData(
        title = "Mobile Legends",
        gameIcon = "https://cdn6.aptoide.com/imgs/b/3/e/b3e336be6c4874605cbc597d811d1822_icon.png?w=128",
        gameBackground = "https://cdn6.aptoide.com/imgs/e/e/0/ee0469bf46c9a4423baf41fe8dd59b43_screen.jpg",
        gamePackage = "com.mobile.legends"
      ),
      GameData(
        title = "Lords Mobile",
        gameIcon = "https://cdn6.aptoide.com/imgs/0/7/e/07eb83a511499243706f0c791b0b8969_icon.png?w=128",
        gameBackground = "https://cdn6.aptoide.com/imgs/4/d/a/4dafe1624f6f5d626e8761dbe903e9a0_screen.jpg",
        gamePackage = "com.igg.android.lordsmobile"
      )
    ),
    {},
    {}
  )
}
