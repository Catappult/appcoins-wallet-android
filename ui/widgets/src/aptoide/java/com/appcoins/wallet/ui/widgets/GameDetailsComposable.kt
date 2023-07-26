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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.appcoins.wallet.ui.common.theme.WalletColors

data class GameDetailsData(
    val title: String,
    val gameIcon: String,
    val gameBackground: String?,
    val gamePackage: String,
    val description: String,
    val screenshots: List<Screenshot>
)

data class Screenshot(
    val imageUrl : String,
    val height: Int,
    val width: Int
)


@Composable
fun GameDetails(appDetailsData: GameDetailsData, function: () -> Unit) {
    function()
    Dialog(onDismissRequest = { /*TODO*/ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
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
            TopAppView(appDetailsData = appDetailsData)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.85f)
                    .height(60.dp),
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
private fun TopAppView(appDetailsData: GameDetailsData) {
    Card(
        colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary) ,
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Box(
            modifier = Modifier.height(250.dp)
        ) {
            AsyncImage(
                model = appDetailsData.gameBackground,
                contentDescription = null,
                modifier = Modifier.height(200.dp),
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
                    .padding(horizontal = 30.dp, vertical = 12.dp),
//        horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .size(80.dp)
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
                Text(
                    text = appDetailsData.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WalletColors.styleguide_light_grey,
                    lineHeight = 24.sp,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                        .padding(bottom = 6.dp, start = 20.dp, end = 20.dp)
                )
                Spacer(Modifier.weight(0.1f))
            }
        }
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
        shape = RoundedCornerShape(8.dp),
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
            color = WalletColors.styleguide_light_grey

        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = appDetailsData.description,
            fontSize = 10.sp,
            color = WalletColors.styleguide_dark_grey
        )

    }
}