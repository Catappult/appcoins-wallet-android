package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.appcoins.wallet.ui.common.theme.WalletColors
import java.time.LocalDateTime

@Preview
@Composable
fun CardItemExample() {
    PromotionsCardComposable(cardItem = cardItem)
}


@Composable
fun PromotionsCardComposable(cardItem: CardItemTest) {
    Card(
        backgroundColor = WalletColors.styleguide_blue_secondary,
        modifier = Modifier.clip(shape = RoundedCornerShape(8.dp)),
        elevation = 4.dp
    ) {
        Column ( modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 8.dp)) {
            ImageWithTitleAndDescription(cardItem.imageUrl, cardItem.title, cardItem.subtitle)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Promotion ends in",
                color = WalletColors.styleguide_light_grey,
                fontSize = 10.sp
            )
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    BoxWithTextAndDetail(cardItem.promotionTime.dayOfMonth.toString(), "DAYS")
                    BoxWithTextAndDetail(cardItem.promotionTime.hour.toString(), "HOURS")
                    BoxWithTextAndDetail(cardItem.promotionTime.minute.toString(), "MINUTES")
                    BoxWithTextAndDetail(cardItem.promotionTime.second.toString(), "SECONDS")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = "GET",
                            fontWeight = FontWeight.Bold,
                            color = WalletColors.styleguide_pink,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                                .clickable(onClick = cardItem.action)

                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BoxWithTextAndDetail(text: String, detail: String) {
    Box {
        Card(
            backgroundColor = WalletColors.styleguide_blue,
            modifier = Modifier
                .padding(6.dp, 6.dp, 6.dp, 6.dp)
                .width(50.dp)
                .clip(shape = RoundedCornerShape(8.dp)),
            elevation = 4.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = WalletColors.styleguide_light_grey,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = detail,
                    fontSize = 7.sp,
                    color = WalletColors.styleguide_light_grey,
                    modifier = Modifier.padding(start = 6.dp, bottom =  6.dp, end = 6.dp)
                )
            }
        }
    }
}


@Composable
fun ImageWithTitleAndDescription(imageUrl: String, title: String, description: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SubcomposeAsyncImage(
                model = imageUrl,
                loading = {
                    CircularProgressIndicator()
                },
                contentDescription = null,
                modifier = Modifier.width(56.dp).clip(shape = RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = WalletColors.styleguide_dark_grey,
                    maxLines = 1,
                    fontSize = 12.sp
                )
                Text(
                    text = description,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    color = WalletColors.styleguide_white,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, end = 16.dp)
                )
            }
        }
    }
}

//Test Itens
data class CardItemTest(
    val title: String,
    val subtitle: String,
    val promotionTime: LocalDateTime,
    val imageUrl: String,
    val urlRedirect: String,
    val action: () -> Unit
)

val cardItem = CardItemTest(
    title = "Days of empire",
    subtitle = "Receive an extra 15% Bonus in all your purchases.",
    promotionTime = LocalDateTime.now(),
    imageUrl = "https://img.freepik.com/vetores-gratis/astronauta-bonito-relaxamento-frio-na-ilustracao-do-icone-do-vetor-dos-desenhos-animados-do-controlador-de-jogo-conceito-de-icone-de-ciencia-de-tecnologia-isolado-vetor-premium-estilo-flat-cartoon_138676-3717.jpg?w=2000",
    urlRedirect = "https://example.com",
    action = { /* handle click action */ }
)