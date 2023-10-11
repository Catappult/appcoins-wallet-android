package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.appcoins.wallet.ui.common.theme.WalletColors


@Composable
fun GamesBundle(
  items: List<GameData>,
  isEskills: Boolean,
  fetchFromApiCallback: () -> Unit,
) {
  fetchFromApiCallback()
  if (items.isNotEmpty()) {
    if(isEskills){
      CarouselTitle()
    }
    else{
      Title()
    }
    LazyRow(
      modifier = Modifier.padding(
        top = 16.dp
      ),
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      items(items) { item ->
        CardItem(gameCardData = item)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardItem(
  gameCardData: GameData,
) {
  val context = LocalContext.current
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary) ,
    elevation = CardDefaults.cardElevation(4.dp),
    shape = RoundedCornerShape(8.dp),
    onClick = { GameClick(gameCardData.gamePackage, context) },
    modifier = Modifier
      .width(332.dp)
      .height(150.dp)
  ) {

    Box(
      modifier = Modifier.fillMaxSize()
    ) {
      AsyncImage(
        model = gameCardData.gameBackground!!,
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
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.weight(0.1f))
        GetTextOrPlay(gameCardData.gamePackage)
      }
    }
  }
}

@Composable
private fun Title(){
  Text(
    text = stringResource(id = R.string.home_appcoins_compatible_games_title),
    fontSize = 14.sp,
    fontWeight = FontWeight.Bold,
    color = WalletColors.styleguide_dark_grey,
    modifier = Modifier.padding(top = 27.dp, end = 13.dp, start = 24.dp)
  )
}

@Composable
private fun CarouselTitle() {
  Row(
    modifier = Modifier
      .padding(top = 27.dp, end = 13.dp, start = 24.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Card(
      colors = CardDefaults.cardColors(WalletColors.styleguide_blue),
      modifier = Modifier.size(48.dp)
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
      ) {
        Image(painter = painterResource(R.drawable.eskills_cup), contentDescription = "Cup")
        //REDO CONTENT DESCRIPTION STRINGS
      }

    }
    Column(
      modifier = Modifier
        .padding(start = 8.dp),
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = stringResource(id = R.string.eskills_carousel_title),
        color = WalletColors.styleguide_golden,
        fontSize = 14.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = stringResource(id = R.string.eskills_carousel_body),
        color = WalletColors.styleguide_light_grey,
        fontSize = 12.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

data class GameData(
  val title: String,
  val gameIcon: String,
  val gameBackground: String?,
  val gamePackage: String,
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
        gamePackage = "com.mobile.legends",
      ),
      GameData(
        title = "Lords Mobile",
        gameIcon = "https://cdn6.aptoide.com/imgs/0/7/e/07eb83a511499243706f0c791b0b8969_icon.png?w=128",
        gameBackground = "https://cdn6.aptoide.com/imgs/4/d/a/4dafe1624f6f5d626e8761dbe903e9a0_screen.jpg",
        gamePackage = "com.igg.android.lordsmobile",
      ),
    ),
    false,
  ){}
}

