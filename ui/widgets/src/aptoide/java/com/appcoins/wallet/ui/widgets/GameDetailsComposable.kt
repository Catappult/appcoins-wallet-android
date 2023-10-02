package com.appcoins.wallet.ui.widgets

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.appcoins.wallet.ui.common.theme.WalletColors
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow

data class
GameDetailsData(
  val title: String,
  val gameIcon: String,
  val gameBackground: String?,
  val gamePackage: String,
  val description: String,
  val screenshots: List<Screenshot>,
  val rating: Double,
  val downloads: Long,
  val size: Long,
  val md5: String,
  val url: String,
  val version: Int
)

data class Screenshot(
  val imageUrl: String,
  val height: Int,
  val width: Int
)

val grantedPermission = mutableStateOf(false)

var showEskillsCard by mutableStateOf(true)
var showInstallButton by mutableStateOf(true)
var showResume by mutableStateOf(false)


@Composable
fun GameDetails(
  appDetailsData: GameDetailsData,
  progress: Int,
  close: () -> Unit,
  install: () -> Unit,
  isAppInstalled: () -> Boolean,
  cancel: () -> Unit,
  pause: () -> Unit,
  finishedInstall: Boolean,
  installing: Boolean,
  open: () -> Unit,
  function: () -> Unit,
) {
  function()
  Dialog(
    onDismissRequest = {
      close()
      showInstallButton = true
      showResume = false
    },
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      dismissOnBackPress = true
    )
  ) {
    Card(
      colors = CardDefaults.cardColors(WalletColors.styleguide_blue),
      elevation = CardDefaults.cardElevation(4.dp),
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
      TopAppView(appDetailsData = appDetailsData, close = close)
      Spacer(modifier = Modifier.height(20.dp))
      AnimatedVisibility(
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .fillMaxWidth(0.85f),
        visible = showInstallButton,
        enter = fadeIn()
      ) {
        if(finishedInstall || isAppInstalled.invoke()) {
          Button(
            onClick = open,
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .fillMaxWidth()
              .height(48.dp),
            colors = ButtonDefaults.buttonColors(WalletColors.styleguide_pink),
            shape = RoundedCornerShape(24.dp)

          ) {
            Text(
              text = "Open",
              fontSize = 14.sp,
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.Bold
            )
          }
        } else {
          Button(
            onClick = {
              install()
              if (grantedPermission.value) {
                showInstallButton = false
                showResume = false
              }
            },
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .fillMaxWidth()
              .height(48.dp),
            colors = ButtonDefaults.buttonColors(WalletColors.styleguide_pink),
            shape = RoundedCornerShape(24.dp)

          ) {
            Text(
              text = stringResource(id = R.string.install_button),
              fontSize = 14.sp,
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.Bold
            )
          }

        }

      }
      if(!showInstallButton) {
        Row(
          modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth(0.85f),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.Top
        ) {
          if (!installing && !finishedInstall) {
            Column(
              modifier = Modifier.fillMaxWidth(0.8f)
            ) {

              LinearProgressIndicator(progress = progress/100f,
                color = WalletColors.styleguide_pink,
                trackColor = WalletColors.styleguide_blue_secondary,
                modifier = Modifier
                  .clip(shape = RoundedCornerShape(24.dp))
                  .align(Alignment.CenterHorizontally)
                  .fillMaxWidth())

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = stringResource(id = R.string.downloading),
                  color = WalletColors.styleguide_dark_grey,
                  fontSize = 12.sp
                )
                Text(
                  text = "$progress%",
                  color = WalletColors.styleguide_dark_grey,
                  fontSize = 12.sp
                )
              }
            }
            IconButton(
              onClick = {
                        showInstallButton = true
                        cancel()
                        },
              modifier = Modifier.size(14.dp)
            ) {
              Icon(painter = painterResource(R.drawable.cancel),
                contentDescription = stringResource(id = R.string.cancel),
                tint = WalletColors.styleguide_dark_grey)
            }
            if (showResume) {
              IconButton(onClick = {
                showResume = false
                install()
               },
                modifier = Modifier.size(14.dp)
              ) {
                Icon(painter = painterResource(R.drawable.resume),
                  contentDescription = "Resume",
                  tint = WalletColors.styleguide_dark_grey)
              }
            } else {
              IconButton(onClick = {
                showResume = true
                pause()
               },
                modifier = Modifier.size(14.dp)
              ) {
                Icon(painter = painterResource(R.drawable.pause),
                  contentDescription = "Pause",
                  tint = WalletColors.styleguide_dark_grey)
              }
            }

          } else if (installing) {
            Column(
              modifier = Modifier.fillMaxWidth(0.8f)
            ) {

              LinearProgressIndicator(
                color = WalletColors.styleguide_pink,
                trackColor = WalletColors.styleguide_blue_secondary,
                modifier = Modifier
                  .clip(shape = RoundedCornerShape(24.dp))
                  .align(Alignment.CenterHorizontally)
                  .fillMaxWidth())

              Text(
                text = "Installing",
                color = WalletColors.styleguide_dark_grey,
                fontSize = 12.sp
              )
            }
          } else if(finishedInstall) {
            showInstallButton = true
          }
        }
      }
      AnimatedVisibility(
        visible = showEskillsCard,
        exit = slideOutVertically() + shrinkVertically() + fadeOut()
      ) {
        EskillsCard()
      }

      ScreenShotsBundle(appDetailsData = appDetailsData)
      Description(appDetailsData = appDetailsData)
    }

  }
}

@Composable
private fun TopAppView(
  appDetailsData: GameDetailsData,
  close: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth()
  ) {

    Box(
      modifier = Modifier.height(228.dp)
    ) {
      if (appDetailsData.gameBackground == null) {
        Image(
          painter = painterResource(id = R.drawable.default_background_carousel),
          contentDescription = "background",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      } else {
        AsyncImage(
          model = appDetailsData.gameBackground,
          contentDescription = null,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      }
      BoxShadow()
      //TopBarButtons(close)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween

      ) {
        IconButton(onClick = {
          close()
          showInstallButton = true
          showResume = false
        }) {
          Icon(
            painter = painterResource(R.drawable.arrow),
            contentDescription = "Back",
            tint = WalletColors.styleguide_light_grey
          )
        }

        ActionButton(
          imagePainter = painterResource(R.drawable.ic_settings_support),
          description = "Support",
          onClick = { }
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth(0.9f)
          .height(91.dp)
          .align(Alignment.BottomCenter)
          .padding(bottom = 20.dp),
//        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
      ) {
        AppInfoRow(appInfo = appDetailsData)
      }
    }
  }
}

@Composable
private fun BoxShadow() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          0.0F to WalletColors.styleguide_blue_secondary.copy(alpha = 0.95F),
          0.1F to WalletColors.styleguide_blue_secondary.copy(alpha = 0.90F),
          0.2F to WalletColors.styleguide_blue_secondary.copy(alpha = 0.60F),
          0.3F to WalletColors.styleguide_blue_secondary.copy(alpha = 0.0F),
          0.7F to WalletColors.styleguide_blue.copy(alpha = 0.95F),
          1F to WalletColors.styleguide_blue.copy(alpha = 1F)
        )
      )
  )
}

@Composable
private fun AppInfoRow(appInfo: GameDetailsData) {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
    elevation = CardDefaults.cardElevation(4.dp),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .width(70.dp)
      .height(71.dp)
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
    ) {
      AsyncImage(
        model = appInfo.gameIcon,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )
    }
  }
  Column(
    modifier = Modifier
      .fillMaxHeight()
      .fillMaxWidth(0.9f)
      .padding(start = 20.dp, end = 20.dp),
    verticalArrangement = Arrangement.Bottom
  ) {

    Text(
      text = appInfo.title,
      fontFamily = FontFamily.SansSerif,
      fontSize = 17.sp,
      fontWeight = FontWeight.Bold,
      color = WalletColors.styleguide_light_grey,
      lineHeight = 20.sp,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )

    Row(
      modifier = Modifier
        .fillMaxWidth(0.9f)
        .fillMaxHeight(0.5f),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      AppStatsItem(
        icon = R.drawable.downloads,
        description = "downloads",
        text = getDownloads(appInfo.downloads)
      )
      AppStatsItem(
        icon = R.drawable.pie_chart,
        description = "size",
        text = getSize(appInfo.size)
      )
      AppStatsItem(
        icon = R.drawable.rating_star,
        description = "rating",
        text = String.format("%.1f", appInfo.rating)
      )

    }
  }
}

@Composable
private fun AppStatsItem(
  icon: Int,
  description: String,
  text: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = description,
      tint = Color(0xFF8E93A1),
      modifier = Modifier.padding(end = 4.dp)
    )
    Text(
      text = text,
      fontSize = 12.sp,
      color = Color(0xFF8E93A1)
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EskillsCard() {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
    elevation = CardDefaults.cardElevation(4.dp),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .height(234.dp)
      .fillMaxWidth()
      .padding(top = 20.dp, start = 20.dp, end = 20.dp),
    onClick = { showEskillsCard = false }
  ) {
    EskillsCardContent()
  }
}

@Composable
private fun EskillsCardContent() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 15.dp, vertical = 15.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier
        .height(65.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        modifier = Modifier.fillMaxHeight(0.8f),
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
      Card(
        colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
        modifier = Modifier.size(65.dp)
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
        ) {
          Image(painter = painterResource(R.drawable.eskills_cup), contentDescription = "Cup")
        }

      }

    }
    EskillsCardList()

    Text(
      text = stringResource(id = R.string.got_it_button),
      color = WalletColors.styleguide_golden,
      fontSize = 14.sp,
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.align(Alignment.End)
    )

  }
}

@Composable
private fun ScreenShotsBundle(appDetailsData: GameDetailsData) {

  LazyRow(
    modifier = Modifier.padding(
      top = 30.dp
    ),
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    items(appDetailsData.screenshots) { item ->
      ScreenshotItem(item)
    }
  }

}

@Composable
private fun ScreenshotItem(item: Screenshot) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
    modifier = Modifier.height(200.dp)
  ) {
    AsyncImage(
      model = item.imageUrl,
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,

      )
  }
}

@Composable
private fun Description(appDetailsData: GameDetailsData) {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue),
    modifier = Modifier.padding(20.dp)
  ) {
    Text(
      text = stringResource(id = R.string.carousel_game_description_title),
      fontSize = 16.sp,
      color = WalletColors.styleguide_light_grey,
      fontWeight = FontWeight.Medium,
      fontFamily = FontFamily.SansSerif

    )
    Spacer(Modifier.height(10.dp))
    Text(
      text = appDetailsData.description,
      fontSize = 12.sp,
      color = WalletColors.styleguide_dark_grey,
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Normal,
      textAlign = TextAlign.Justify
    )

  }
}

@Composable
fun EskillsCardList() {
  val list = listOf(
    stringResource(id = R.string.eskills_game_1),
    stringResource(id = R.string.carousel_game_2),
    stringResource(id = R.string.carousel_game_3)
  )
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(list) {
      Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Canvas(modifier = Modifier.size(8.dp), onDraw = {
          drawCircle(color = WalletColors.styleguide_golden)
        })
        Text(
          modifier = Modifier.fillMaxWidth(0.8f),
          text = it,
          color = WalletColors.styleguide_light_grey,
          fontSize = 12.sp,
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

@Composable
fun AnimatedButton() {

}

@Preview
@Composable
private fun TopAppViewPreview() {
  TopAppView(
    appDetailsData =
    GameDetailsData(
      title = "Fruit Blast Master Test ",
      gameIcon = "https://pool.img.aptoide.com/catappult/57d4a771e6dbedff5e5f8db37687c3dc_icon.png",
      gameBackground = "https://pool.img.aptoide.com/catappult/ad72b51875828f10222af84ebc55b761_feature_graphic.png",
      gamePackage = "im.maya.legendaryheroes",
      description = "Just testing description",
      screenshots = listOf(
        Screenshot(
          imageUrl = "https://pool.img.aptoide.com/catappult/4c24292b56918cf363e7a1b3c3275045_screen.jpg",
          height = 288,
          width = 512
        ),
        Screenshot(
          imageUrl = "https://pool.img.aptoide.com/catappult/0e9a0a52b013a4eb0d636eb946221a4b_screen.jpg",
          height = 288,
          width = 512
        ),
        Screenshot(
          imageUrl = "https://pool.img.aptoide.com/catappult/44cd8d6c8e140e54bbf286b7b32b0fad_screen.jpg",
          height = 288,
          width = 512
        ),
        Screenshot(
          imageUrl = "https://pool.img.aptoide.com/catappult/af40419c6caec7d1fc7afc6212f0dc5c_screen.jpg",
          height = 288,
          width = 512
        )
      ),
      rating = 5.9,
      downloads = 60500,
      size = 199720828,
      md5 = "",
      url = "",
      version = 0
    )
  ) {

  }
}

@Preview
@Composable
private fun EskillsCardPreview() {
  EskillsCard()
}

private fun getDownloads(downloads: Long): String {
  if (downloads < 1000) return "" + downloads
  val exp = (ln(downloads.toDouble()) / ln(1000.0)).toInt()
  return String.format("%.1f %c", downloads / 1000.0.pow(exp.toDouble()), "KMGTPE"[exp - 1])
}

fun getSize(size: Long): String {
  if (size <= 0) return "0"
  val units = arrayOf("B", "kB", "MB", "GB", "TB")
  val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
  return DecimalFormat("#,##0.#").format(
    size / Math.pow(
      1024.0,
      digitGroups.toDouble()
    )
  ) + " " + units[digitGroups]
}

@Preview
@Composable
private fun Overview() {
  GameDetails(
    appDetailsData =
    GameDetailsData(
      title = "Fruit Blast Master Test ",
      gameIcon = "https://pool.img.aptoide.com/catappult/57d4a771e6dbedff5e5f8db37687c3dc_icon.png",
      gameBackground = "https://pool.img.aptoide.com/catappult/ad72b51875828f10222af84ebc55b761_feature_graphic.png",
      gamePackage = "im.maya.legendaryheroes",
      description = "Just testing description",
      screenshots = listOf(
        Screenshot(
          imageUrl = "https://pool.img.aptoide.com/catappult/4c24292b56918cf363e7a1b3c3275045_screen.jpg",
          height = 288,
          width = 512
        ),
        Screenshot(
          imageUrl = "https://pool.img.aptoide.com/catappult/0e9a0a52b013a4eb0d636eb946221a4b_screen.jpg",
          height = 288,
          width = 512
        ),
        Screenshot(
          imageUrl = "https://pool.img.aptoide.com/catappult/44cd8d6c8e140e54bbf286b7b32b0fad_screen.jpg",
          height = 288,
          width = 512
        ),
        Screenshot(
          imageUrl = "https://pool.img.aptoide.com/catappult/af40419c6caec7d1fc7afc6212f0dc5c_screen.jpg",
          height = 288,
          width = 512
        )
      ),
      rating = 5.9,
      downloads = 60500,
      size = 199720828,
      md5 = "123414",
      url = "asdasdasd",
      version = 12
    ),
    progress = 50, close = { /*TODO*/ },
    install = { false },
    cancel = { },
    pause = { },
    finishedInstall = false,
    installing = false,
    function = {},
    open = {},
    isAppInstalled = { false }
  )
}