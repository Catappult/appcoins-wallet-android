package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun GamesBundle(
  listState: LazyListState,
  items: List<GameData>,
  sendPromotionClickEvent: (String?, String) -> Unit,
  fetchFromApiCallback: () -> Unit
) {
  val context = LocalContext.current
  fetchFromApiCallback()
  Text(
    text = stringResource(id = R.string.home_appcoins_compatible_games_title),
    fontSize = 14.sp,
    fontWeight = FontWeight.Bold,
    color = WalletColors.styleguide_dark_grey,
    modifier = Modifier.padding(top = 27.dp, end = 13.dp, start = 24.dp)
  )
  LazyRow(
    state = listState,
    modifier = Modifier.padding(
      top = 16.dp
    ),
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    if (items.isEmpty()) {
      item {
        SkeletonLoadingGamesBundleCard()
      }
    } else {
      items(items) { item ->
        CardItem(gameCardData = item, sendPromotionClickEvent) {
          openGame(item.gamePackage, item.actionUrl, context, sendPromotionClickEvent)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardItem(
  gameCardData: GameData,
  sendPromotionClickEvent: (String?, String) -> Unit,
  onClick: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
    elevation = CardDefaults.cardElevation(4.dp),
    shape = RoundedCornerShape(8.dp),
    onClick = onClick,
    modifier = Modifier
      .width(280.dp)
      .height(144.dp)
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
                .size(56.dp),
              contentScale = ContentScale.Crop
            )
          }
        }
        Text(
          text = gameCardData.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = WalletColors.styleguide_light_grey,
          lineHeight = 16.sp,
          modifier = Modifier
            .align(Alignment.Bottom)
            .weight(1f)
            .padding(bottom = 6.dp, start = 16.dp, end = 8.dp),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(0.1f))
        GetTextOrPlay(gameCardData.gamePackage)
      }
    }
  }
}

@Composable
fun GetTextOrPlay(packageName: String?) {
  val hasGameInstall =
    isPackageInstalled(packageName, packageManager = LocalContext.current.packageManager)
  if (BuildConfig.FLAVOR == "gp" && hasGameInstall) {
    Text(
      text = stringResource(id = R.string.play_button),
      color = WalletColors.styleguide_pink,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier
        .padding(top = 24.dp, bottom = 6.dp, end = 12.dp)
    )
  } else if (BuildConfig.FLAVOR != "gp") {
    Text(
      text = stringResource(id = if (hasGameInstall) R.string.play_button else R.string.get_button),
      color = WalletColors.styleguide_pink,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier
        .padding(top = 24.dp, bottom = 6.dp, end = 12.dp)
    )
  }
}

data class GameData(
  val title: String,
  val gameIcon: String,
  val gameBackground: String,
  val gamePackage: String,
  val actionUrl: String?
)

@Composable
fun SkeletonLoadingGamesBundleCard() {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
    modifier =
    Modifier
      .fillMaxWidth()
      .clip(shape = RoundedCornerShape(8.dp))
      .width(332.dp)
      .height(150.dp)
  ) {
    Box(
      modifier = Modifier.fillMaxSize()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Spacer(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush = shimmerSkeleton()),
        )
      }
      Box(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .background(WalletColors.styleguide_blue_secondary)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Bottom
        ) {
          Spacer(
            modifier = Modifier
              .padding(top = 8.dp, bottom = 8.dp, start = 8.dp)
              .size(62.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(brush = shimmerSkeleton()),
          )
          Spacer(
            modifier = Modifier
              .width(width = 170.dp)
              .height(height = 30.dp)
              .padding(start = 8.dp, bottom = 8.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(brush = shimmerSkeleton()),
          )
        }
      }
    }
  }
}

@Preview
@Composable
fun PreviewGamesBundle() {
  GamesBundle(
    listState = LazyListState(),
    items = listOf(
      GameData(
        title = "Mobile Legends",
        gameIcon = "https://cdn6.aptoide.com/imgs/b/3/e/b3e336be6c4874605cbc597d811d1822_icon.png?w=128",
        gameBackground = "https://cdn6.aptoide.com/imgs/e/e/0/ee0469bf46c9a4423baf41fe8dd59b43_screen.jpg",
        gamePackage = "com.mobile.legends",
        actionUrl = "www.aptoide.com",
      ),
      GameData(
        title = "Lords Mobile",
        gameIcon = "https://cdn6.aptoide.com/imgs/0/7/e/07eb83a511499243706f0c791b0b8969_icon.png?w=128",
        gameBackground = "https://cdn6.aptoide.com/imgs/4/d/a/4dafe1624f6f5d626e8761dbe903e9a0_screen.jpg",
        gamePackage = "com.igg.android.lordsmobile",
        actionUrl = "www.aptoide.com",
      )
    ),
    { _, _ -> },
    {}
  )
}

@Preview
@Composable
fun PreviewSkeletonLoadingGamesBundle() {
  SkeletonLoadingGamesBundleCard()
}
