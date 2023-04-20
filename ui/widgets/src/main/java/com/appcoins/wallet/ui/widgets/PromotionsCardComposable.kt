package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.SubcomposeAsyncImage
import com.appcoins.wallet.ui.common.theme.WalletColors
import java.time.LocalDateTime

@Preview
@Composable
private fun CardItemExample() {
    PromotionsCardComposable(cardItem = cardItem)
}

@Preview
@Composable
private fun CardVipItemExample() {
    PromotionsCardComposable(cardItem = vipCardItem)
}

@Preview
@Composable
private fun CardFutureItemExample() {
    PromotionsCardComposable(cardItem = futureCardItem)
}



@Composable
fun PromotionsCardComposable(cardItem: CardItemTest) {
    val borderColor = if (cardItem.hasVipPromotion) {
        WalletColors.styleguide_vip_yellow
    } else {
        Color.Transparent
    }
    Card(
        colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
        modifier = Modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .zIndex(4f),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 8.dp)) {
            ImageWithTitleAndDescription(cardItem.imageUrl, cardItem.title, cardItem.subtitle)
            Spacer(modifier = Modifier.height(12.dp))
            if(!cardItem.hasFuturePromotion) {
            Text(
                // use string - promotion_ends_short_title when PR is merged by Carlos
                text = "Promotion ends in",
                color = WalletColors.styleguide_light_grey,
                fontSize = 10.sp
            )
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // compare - (cardItem.promotionTime.isAfter(LocalDateTime.now())
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
            } else {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        IconWithText("Available Soon")
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
                                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 21.dp)
                                    .clickable(onClick = cardItem.action)
                            )
                        }
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
            colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
            modifier = Modifier
                .padding(6.dp, 6.dp, 6.dp, 6.dp)
                .width(41.dp)
                .height(39.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .zIndex(8f)
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
                    modifier = Modifier.padding(start = 6.dp, bottom = 6.dp, end = 6.dp)
                )
            }
        }
    }
}

@Composable
fun IconWithText(text: String) {
    Box {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 19.dp, top = 8.dp)) {
                Image(
                    painter = painterResource(R.drawable.ic_clock),
                    colorFilter = ColorFilter.tint(WalletColors.styleguide_pink),
                    modifier = Modifier
                        .height(14.dp)
                        .width(14.dp)
                        .align(Alignment.CenterVertically),
                    contentDescription = null
                )
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    color = WalletColors.styleguide_dark_grey,
                    modifier = Modifier.padding(start = 8.dp),
                    maxLines = 1,
                    fontSize = 12.sp
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
                modifier = Modifier
                    .width(56.dp)
                    .clip(shape = RoundedCornerShape(8.dp))
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
    val hasVipPromotion: Boolean,
    val hasFuturePromotion: Boolean,
    val action: () -> Unit
)

val cardItem = CardItemTest(
    title = "Days of empire",
    subtitle = "Receive an extra 15% Bonus in all your purchases.",
    promotionTime = LocalDateTime.now(),
    imageUrl = "https://img.freepik.com/vetores-gratis/astronauta-bonito-relaxamento-frio-na-ilustracao-do-icone-do-vetor-dos-desenhos-animados-do-controlador-de-jogo-conceito-de-icone-de-ciencia-de-tecnologia-isolado-vetor-premium-estilo-flat-cartoon_138676-3717.jpg?w=2000",
    urlRedirect = "https://example.com",
    hasVipPromotion = false,
    hasFuturePromotion = false,
    action = { /* handle click action */ }
)

val vipCardItem = CardItemTest(
    title = "Days of empire",
    subtitle = "Receive an extra 15% Bonus in all your purchases.",
    promotionTime = LocalDateTime.now(),
    imageUrl = "https://img.freepik.com/vetores-gratis/astronauta-bonito-relaxamento-frio-na-ilustracao-do-icone-do-vetor-dos-desenhos-animados-do-controlador-de-jogo-conceito-de-icone-de-ciencia-de-tecnologia-isolado-vetor-premium-estilo-flat-cartoon_138676-3717.jpg?w=2000",
    urlRedirect = "https://example.com",
    hasVipPromotion = true,
    hasFuturePromotion = false,
    action = { /* handle click action */ }
)

val futureCardItem = CardItemTest(
    title = "Days of empire",
    subtitle = "Receive an extra 15% Bonus in all your purchases.",
    promotionTime = LocalDateTime.now(),
    imageUrl = "https://img.freepik.com/vetores-gratis/astronauta-bonito-relaxamento-frio-na-ilustracao-do-icone-do-vetor-dos-desenhos-animados-do-controlador-de-jogo-conceito-de-icone-de-ciencia-de-tecnologia-isolado-vetor-premium-estilo-flat-cartoon_138676-3717.jpg?w=2000",
    urlRedirect = "https://example.com",
    hasVipPromotion = false,
    hasFuturePromotion = true,
    action = { /* handle click action */ }
)