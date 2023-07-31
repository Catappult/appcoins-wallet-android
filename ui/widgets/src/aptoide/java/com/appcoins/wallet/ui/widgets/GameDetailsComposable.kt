package com.appcoins.wallet.ui.widgets

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.appcoins.wallet.ui.common.theme.WalletColors
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
    val size: Long
)

data class Screenshot(
    val imageUrl : String,
    val height: Int,
    val width: Int
)


@Composable
fun GameDetails(
    appDetailsData: GameDetailsData,
    close: () -> Unit,
    function: () -> Unit
) {
    function()
    Dialog(onDismissRequest = close,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary) ,
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopAppView(appDetailsData = appDetailsData, close = close)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.85f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(WalletColors.styleguide_pink)

            ) {
                Text(
                    text = "Install",
                    fontSize = 20.sp
                )
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
        colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary) ,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Box(
            modifier = Modifier.height(228.dp)
        ) {
            AsyncImage(
                model = appDetailsData.gameBackground,
                contentDescription = null,
                modifier = Modifier.height(173.dp),
                contentScale = ContentScale.Crop
            )
            //TopBarButtons(close)
            Row(

            ) {
                ActionButton(
                    imagePainter = painterResource(R.drawable.ic_arrow_back),
                    description = "Back",
                    onClick = { close() }
                )
                Spacer(modifier = Modifier.weight(0.1f))
                ActionButton(
                    imagePainter = painterResource(R.drawable.ic_settings_support),
                    description = "Support",
                    onClick = { }
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.4F to WalletColors.styleguide_blue_secondary.copy(alpha = 0.0F),
                            0.75F to WalletColors.styleguide_blue_secondary.copy(alpha = 0.95F),
                            1F to WalletColors.styleguide_blue_secondary.copy(alpha = 0.99F)
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 23.dp, vertical = 12.dp),
//        horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            model = appDetailsData.gameIcon,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .height(71.dp)
                        .padding(start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = appDetailsData.title,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = WalletColors.styleguide_light_grey,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f, true)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box {
                           Icon(
                               painter = painterResource(R.drawable.downloads),
                               contentDescription = null,
                               tint = Color(0xFF8E93A1)
                           )
                            Spacer(modifier = Modifier.size(10.dp))
                            Text(
                                text = getDownloads(appDetailsData.downloads),
                                fontSize = 10.sp,
                                color = Color(0xFF8E93A1),
                                modifier = Modifier
                                    .height(11.dp)
                                    .padding(start = 5.dp)
                            )
                        }
                        Box {
                            Icon(
                                painter = painterResource(R.drawable.pie_chart),
                                contentDescription = null,
                                tint = Color(0xFF8E93A1)
                            )
                            Text(
                                text = getDownloads(appDetailsData.size),
                                fontSize = 10.sp,
                                color = Color(0xFF8E93A1),
                                modifier = Modifier
                                    .height(11.dp)
                                    .padding(start = 5.dp)
                            )
                        }
                        Box {
                            Icon(
                                painter = painterResource(R.drawable.rating_star),
                                contentDescription = null,
                                tint = Color(0xFF8E93A1)
                            )
                            Text(
                                text = appDetailsData.rating.toString(),
                                fontSize = 10.sp,
                                color = Color(0xFF8E93A1),
                                modifier = Modifier
                                    .height(11.dp)
                                    .padding(start = 5.dp)
                            )
                        }

                    }
                }
                /*
                Text(
                    text = appDetailsData.title,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = WalletColors.styleguide_light_grey,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                        .padding(bottom = 6.dp, start = 20.dp, end = 20.dp)
                )

                 */
                Spacer(Modifier.weight(0.1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarButtons(close: () -> Unit) {
    ActionButton(
        imagePainter = painterResource(R.drawable.ic_arrow_back),
        description = "Back",
        onClick = { close() }
    )

    ActionButton(
        imagePainter = painterResource(R.drawable.ic_settings_support),
        description = "Support",
        onClick = { }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EskillsCard() {
    Card(
        onClick = { /*TODO*/ }
    ) {

    }
}

@Composable
private fun ScreenShotsBundle(appDetailsData: GameDetailsData) {

    LazyRow(
        modifier = Modifier.padding(
            top = 16.dp
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
        colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
        modifier = Modifier.padding(20.dp)
    ) {
        Text(
            text = "Description",
            fontSize = 16.sp,
            color = WalletColors.styleguide_light_grey,
            fontWeight = FontWeight.Bold

        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = appDetailsData.description,
            fontSize = 10.sp,
            color = WalletColors.styleguide_dark_grey
        )

    }
}

@Preview
@Composable
private fun TopAppViewPreview() {
    TopAppView(appDetailsData =
    GameDetailsData(
        title = "Legendary Heroes MOBA Offline",
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
                imageUrl = 	"https://pool.img.aptoide.com/catappult/af40419c6caec7d1fc7afc6212f0dc5c_screen.jpg",
                height = 288,
                width = 512
            )
        ),
        size = 1995000,
        downloads = 60500,
        rating = 5.9
    )) {

    }
}

private fun getDownloads(downloads : Long) : String {
    if (downloads < 1000) return "" + downloads
    val exp = (ln(downloads.toDouble()) / ln(1000.0)).toInt()
    return String.format("%.1f %c", downloads / 1000.0.pow(exp.toDouble()), "KMGTPE"[exp - 1])
}