package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.SubcomposeAsyncImage
import com.appcoins.wallet.ui.common.theme.WalletColors
import kotlinx.coroutines.delay
import java.time.Duration

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
fun PromotionsCardComposable(cardItem: CardPromotionItem) {
    var borderColor = Color.Transparent
    var topEndRoundedCornerCard = 16.dp
    Column {
        if (cardItem.hasVipPromotion) {
            //Set Changes in VIP Cards
            borderColor = WalletColors.styleguide_vip_yellow
            topEndRoundedCornerCard = 0.dp
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(topEnd = 8.dp, topStart = 8.dp))
                    .background(WalletColors.styleguide_vip_yellow)
            ) {
                Text(
                    //Need string to Carlos Translator
                    text = "Vip Offer",
                    fontSize = 12.sp,
                    color = WalletColors.styleguide_light_grey,
                    modifier = Modifier.padding(
                        top = 6.dp,
                        end = 14.dp,
                        start = 14.dp,
                        bottom = 6.dp
                    )
                )
            }
        }
        Surface(
            color = WalletColors.styleguide_blue_secondary,
            modifier = Modifier
                .border(
                    border = BorderStroke(2.dp, borderColor),
                    shape = RoundedCornerShape(
                        bottomEnd = 16.dp,
                        bottomStart = 16.dp,
                        topEnd = topEndRoundedCornerCard,
                        topStart = 16.dp
                    )
                )
                .clip(
                    shape = RoundedCornerShape(
                        bottomEnd = 16.dp,
                        bottomStart = 16.dp,
                        topEnd = topEndRoundedCornerCard,
                        topStart = 16.dp
                    )
                )
                .zIndex(4f)
        ) {
            Column(modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 8.dp)) {
                ImageWithTitleAndDescription(cardItem.imageUrl, cardItem.title, cardItem.subtitle)
                Spacer(modifier = Modifier.height(12.dp))
                if (!cardItem.hasFuturePromotion) {
                    Text(
                        // use string - promotion_ends_short_title when PR is merged by Carlos
                        text = "Promotion ends in",
                        color = WalletColors.styleguide_light_grey,
                        fontSize = 10.sp
                    )
                    Column(
                        modifier = Modifier
                            .height(49.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CountDownTimer(cardItem.promotionEndTime)
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "GET",
                                    fontWeight = FontWeight.Bold,
                                    color = WalletColors.styleguide_pink,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clickable(onClick = cardItem.action)
                                        .align(Alignment.End)
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .height(49.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            IconWithText("Available Soon")
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "GET",
                                    fontWeight = FontWeight.Bold,
                                    color = WalletColors.styleguide_pink,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clickable(onClick = cardItem.action)
                                        .align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountDownTimer(endDateTime: Long) {
    val remainingTime = remember { mutableStateOf(Duration.ZERO) }
    val endDateInMillis = endDateTime * 1000L

    LaunchedEffect(Unit) {
        while (true) {
            remainingTime.value = Duration.ofMillis(endDateInMillis - System.currentTimeMillis())
            if (remainingTime.value < Duration.ZERO) {
                remainingTime.value = Duration.ZERO
                //Break while in Home
                break
            }
            delay(1000)
        }
    }
    BoxWithTextAndDetail(
        text =  remainingTime.value.toDays().toString(),
        detail = "Days"
    )
    BoxWithTextAndDetail(
        text = (remainingTime.value.toHours() % 24).toString(),
        detail = "Hours"
    )
    BoxWithTextAndDetail(
        text = (remainingTime.value.toMinutes() % 60).toString(),
        detail = "Minutes"
    )
    BoxWithTextAndDetail(
        text = (remainingTime.value.seconds % 60).toString(),
        detail = "Seconds"
    )
}

@Composable
fun BoxWithTextAndDetail(text: String, detail: String) {
    Card(
        colors = CardDefaults.cardColors(WalletColors.styleguide_black),
        modifier = Modifier
            .padding(top = 6.dp, bottom = 6.dp, end = 3.dp)
            .width(41.dp)
            .height(39.dp)
            .clip(shape = RoundedCornerShape(3.dp))
            .zIndex(8f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = WalletColors.styleguide_light_grey,
            )
            Text(
                text = detail,
                fontSize = 7.sp,
                color = WalletColors.styleguide_light_grey,
            )
        }
    }
}

@Composable
fun IconWithText(text: String) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 19.dp, top = 12.dp)
        ) {
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


@Composable
fun ImageWithTitleAndDescription(imageUrl: String?, title: String?, description: String?) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SubcomposeAsyncImage(
                model = imageUrl,
                loading = {
                    CircularProgressIndicator()
                },
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(56.dp)
                    .clip(shape = RoundedCornerShape(8.dp))
            )
            Column(
                modifier = Modifier
                    .widthIn(min = 0.dp, max = 240.dp)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = title ?: "",
                    fontWeight = FontWeight.Bold,
                    color = WalletColors.styleguide_dark_grey,
                    maxLines = 1,
                    fontSize = 12.sp
                )
                Text(
                    text = description ?: "",
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WalletColors.styleguide_white,
                    modifier = Modifier.padding(top = 4.dp, end = 8.dp)
                )
            }
        }
    }
}

//Test Itens
data class CardPromotionItem(
    val title: String?,
    val subtitle: String?,
    val promotionStartTime: Long?,
    val promotionEndTime: Long,
    val imageUrl: String?,
    val urlRedirect: String?,
    val hasVipPromotion: Boolean,
    val hasFuturePromotion: Boolean,
    val action: () -> Unit
)

val cardItem = CardPromotionItem(
    title = "Days of empire",
    subtitle = "Receive an extra 15% Bonus in all your purchases.",
    promotionStartTime = System.currentTimeMillis(),
    promotionEndTime = System.currentTimeMillis(),
    imageUrl = "https://img.freepik.com/vetores-gratis/astronauta-bonito-relaxamento-frio-na-ilustracao-do-icone-do-vetor-dos-desenhos-animados-do-controlador-de-jogo-conceito-de-icone-de-ciencia-de-tecnologia-isolado-vetor-premium-estilo-flat-cartoon_138676-3717.jpg?w=2000",
    urlRedirect = "https://example.com",
    hasVipPromotion = false,
    hasFuturePromotion = false,
    action = { /* handle click action */ }
)

val vipCardItem = CardPromotionItem(
    title = "Days of empire",
    subtitle = "Receive an extra 15% Bonus in all your purchases.",
    promotionStartTime = System.currentTimeMillis(),
    promotionEndTime = System.currentTimeMillis(),
    imageUrl = "https://img.freepik.com/vetores-gratis/astronauta-bonito-relaxamento-frio-na-ilustracao-do-icone-do-vetor-dos-desenhos-animados-do-controlador-de-jogo-conceito-de-icone-de-ciencia-de-tecnologia-isolado-vetor-premium-estilo-flat-cartoon_138676-3717.jpg?w=2000",
    urlRedirect = "https://example.com",
    hasVipPromotion = true,
    hasFuturePromotion = false,
    action = { /* handle click action */ }
)

val futureCardItem = CardPromotionItem(
    title = "Days of empire",
    subtitle = "Receive an extra 15% Bonus in all your purchases.",
    promotionStartTime = System.currentTimeMillis(),
    promotionEndTime = System.currentTimeMillis(),
    imageUrl = "https://img.freepik.com/vetores-gratis/astronauta-bonito-relaxamento-frio-na-ilustracao-do-icone-do-vetor-dos-desenhos-animados-do-controlador-de-jogo-conceito-de-icone-de-ciencia-de-tecnologia-isolado-vetor-premium-estilo-flat-cartoon_138676-3717.jpg?w=2000",
    urlRedirect = "https://example.com",
    hasVipPromotion = false,
    hasFuturePromotion = true,
    action = { /* handle click action */ }
)