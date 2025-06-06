package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.SubcomposeAsyncImage
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors
import kotlinx.coroutines.delay
import java.time.Duration

@Preview
@Composable
private fun CardItemExample() {
  PromotionsCardComposable(
    cardItem = cardItem,
    fragmentName = "RewardFragment",
    buttonsAnalytics = null
  )
}

@Preview
@Composable
private fun CardVipItemExample() {
  PromotionsCardComposable(
    cardItem = vipCardItem,
    fragmentName = "RewardFragment",
    buttonsAnalytics = null
  )
}

@Preview
@Composable
private fun CardFutureItemExample() {
  PromotionsCardComposable(
    cardItem = futureCardItem,
    fragmentName = "RewardFragment",
    buttonsAnalytics = null
  )
}

@Preview
@Composable
private fun CardVerticalItemExample() {
  PromotionsCardComposable(
    cardItem = verticalCardItem,
    fragmentName = "RewardFragment",
    buttonsAnalytics = null
  )
}

@Preview
@Composable
private fun LoadingPromotionCard() {
  SkeletonLoadingPromotionCards(hasVerticalList = false)
}

@Composable
fun PromotionsCardComposable(
  modifier: Modifier = Modifier,
  cardItem: CardPromotionItem,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  var borderColor = Color.Transparent
  var topEndRoundedCornerCard = 16.dp
  val spacerSize = if (cardItem.hasVerticalList) 8.dp else 16.dp
  Column(modifier = modifier) {
    if (cardItem.hasVipPromotion) {
      // Set Changes in VIP Cards
      borderColor = WalletColors.styleguide_vip_yellow
      topEndRoundedCornerCard = 0.dp
      Box(
        modifier =
        Modifier
          .align(Alignment.End)
          .clip(RoundedCornerShape(topEnd = 8.dp, topStart = 8.dp))
          .background(WalletColors.styleguide_vip_yellow)
      ) {
        Text(
          text = stringResource(id = R.string.vip_program_title_vip_offer),
          fontSize = 12.sp,
          color = WalletColors.styleguide_dark,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
        )
      }
    } else {
      Spacer(modifier = Modifier.height(spacerSize))
    }
    Surface(
      color = WalletColors.styleguide_dark_secondary,
      modifier =
      Modifier
        .border(
          border = BorderStroke(2.dp, borderColor),
          shape =
          RoundedCornerShape(
            bottomEnd = 16.dp,
            bottomStart = 16.dp,
            topEnd = topEndRoundedCornerCard,
            topStart = 16.dp
          )
        )
        .clip(
          shape =
          RoundedCornerShape(
            bottomEnd = 16.dp,
            bottomStart = 16.dp,
            topEnd = topEndRoundedCornerCard,
            topStart = 16.dp
          )
        )
        .zIndex(4f)
    ) {
      Column(modifier = Modifier.padding(8.dp)) {
        ImageWithTitleAndDescription(
          cardItem.imageUrl, cardItem.title, cardItem.subtitle, cardItem.hasVerticalList
        )
        if (!cardItem.hasFuturePromotion) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = stringResource(id = R.string.promotion_ends_short_title),
            color = WalletColors.styleguide_light_grey,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Column(modifier = Modifier.height(49.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              CountDownTimer(endDateTime = cardItem.promotionEndTime)
              Spacer(modifier = Modifier.weight(1f, fill = true))
              GetText(
                modifier = Modifier.wrapContentWidth(),
                action = cardItem.action,
                packageName = cardItem.packageName,
                isVip = cardItem.hasVipPromotion,
                fragmentName = fragmentName,
                buttonsAnalytics = buttonsAnalytics
              )
            }
          }
        } else {
          Spacer(modifier = Modifier.height(20.dp))
          Column(modifier = Modifier.height(40.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconWithText(stringResource(id = R.string.perks_available_soon_short))
              GetText(
                action = cardItem.action,
                packageName = cardItem.packageName,
                isVip = cardItem.hasVipPromotion,
                fragmentName = fragmentName,
                buttonsAnalytics = buttonsAnalytics
              )
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
        // Break while in Home
        break
      }
      delay(1000)
    }
  }
  Row(
    modifier = Modifier.padding(top = 6.dp),
    horizontalArrangement = Arrangement.Start,
  ) {
    CardWithTextAndDetail(
      text = remainingTime.value.toDays().toString(),
      detail = pluralStringResource(
        id = R.plurals.day,
        count = remainingTime.value.toDays().toInt()
      ),
      modifier = Modifier.padding(end = 2.dp)
    )
    CardWithTextAndDetail(
      text = (remainingTime.value.toHours() % 24).toString(),
      detail = pluralStringResource(
        id = R.plurals.hour,
        count = (remainingTime.value.toHours() % 24).toInt()
      ),
      modifier = Modifier.padding(horizontal = 2.dp)
    )
    CardWithTextAndDetail(
      text = (remainingTime.value.toMinutes() % 60).toString(),
      detail = pluralStringResource(
        id = R.plurals.minute,
        count = (remainingTime.value.toMinutes() % 60).toInt()
      ),
      modifier = Modifier.padding(horizontal = 2.dp)
    )
    CardWithTextAndDetail(
      text = (remainingTime.value.seconds % 60).toString(),
      detail = pluralStringResource(
        id = R.plurals.second,
        count = (remainingTime.value.seconds % 60).toInt()
      ),
      modifier = Modifier.padding(start = 2.dp)
    )
  }
}

@Composable
fun CardWithTextAndDetail(modifier: Modifier = Modifier, text: String, detail: String) {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_dark_variant),
    shape = RoundedCornerShape(4.dp),
    modifier = modifier.height(40.dp).width(40.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = text,
        fontSize = 14.sp,
        color = WalletColors.styleguide_light_grey,
        maxLines = 1,
      )
      Text(
        text = detail,
        fontSize = 6.sp,
        color = WalletColors.styleguide_light_grey,
      )
    }
  }
}

@Composable
fun IconWithText(text: String) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 10.dp)) {
    Image(
      painter = painterResource(R.drawable.ic_clock),
      colorFilter = ColorFilter.tint(WalletColors.styleguide_primary),
      modifier = Modifier
        .height(14.dp)
        .width(14.dp),
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

@Composable
fun GetText(
  modifier: Modifier = Modifier,
  action: () -> Unit,
  packageName: String?,
  isVip: Boolean = false,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  val hasGameInstall =
    isPackageInstalled(packageName, packageManager = LocalContext.current.packageManager)
  val text = when {
    hasGameInstall -> stringResource(id = R.string.play_button)
    BuildConfig.FLAVOR != "gp" && packageName != null -> stringResource(R.string.get_button)
    else -> null
  }
  text?.let {
    TextButton(
      modifier = modifier,
      onClick = {
        buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, text)
        action()
      }) {
      Text(
        text = text,
        fontWeight = FontWeight.Bold,
        color = if (isVip) WalletColors.styleguide_vip_yellow else WalletColors.styleguide_primary,
        fontSize = 14.sp
      )
    }
  }
}

@Composable
fun ImageWithTitleAndDescription(
  imageUrl: String?,
  title: String?,
  description: String?,
  hasVerticalList: Boolean
) {
  val maxColumnWidth = if (hasVerticalList) 300.dp else 240.dp
  Column {
    Row(verticalAlignment = Alignment.CenterVertically) {
      SubcomposeAsyncImage(
        model = imageUrl,
        loading = { CircularProgressIndicator() },
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
          .width(56.dp)
          .height(56.dp)
          .clip(shape = RoundedCornerShape(8.dp))
      )
      Column(
        modifier = Modifier
          .widthIn(min = 0.dp, max = maxColumnWidth)
          .padding(start = 12.dp)
      ) {
        Text(
          text = title ?: "",
          color = WalletColors.styleguide_dark_grey,
          maxLines = 1,
          fontSize = 14.sp
        )
        Text(
          text = description ?: "",
          maxLines = 2,
          style = MaterialTheme.typography.bodyMedium,
          color = WalletColors.styleguide_white,
          modifier = Modifier.padding(top = 4.dp, end = 8.dp)
        )
      }
    }
  }
}

@Composable
fun SkeletonLoadingPromotionCards(hasVerticalList: Boolean) {
  SkeletonLoadingPromotionCardItem(hasVerticalList)
  SkeletonLoadingPromotionCardItem(hasVerticalList)
  SkeletonLoadingPromotionCardItem(hasVerticalList)
}

@Composable
private fun SkeletonLoadingPromotionCardItem(hasVerticalList: Boolean) {
  val maxColumnWidth = if (hasVerticalList) 320.dp else 300.dp
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_dark_secondary),
    modifier =
    Modifier
      .fillMaxWidth()
      .width(maxColumnWidth)
      .padding(top = 16.dp, start = if (hasVerticalList) 16.dp else 0.dp, end = 16.dp)
      .clip(shape = RoundedCornerShape(8.dp))
  ) {
    Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(
          modifier =
          Modifier
            .padding(top = 8.dp)
            .width(56.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(brush = shimmerSkeleton()),
        )
        Column(
          modifier = Modifier
            .width(240.dp)
            .padding(start = 12.dp)
        ) {
          Spacer(
            modifier =
            Modifier
              .width(width = 120.dp)
              .height(height = 22.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(brush = shimmerSkeleton()),
          )
          Spacer(
            modifier =
            Modifier
              .width(width = 200.dp)
              .height(height = 27.dp)
              .padding(top = 5.dp, end = 16.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(brush = shimmerSkeleton()),
          )
        }
      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp, end = 16.dp)
      ) {
        Spacer(
          modifier =
          Modifier
            .padding(top = 8.dp)
            .width(46.dp)
            .height(40.dp)
            .padding(end = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(brush = shimmerSkeleton()),
        )
        Spacer(
          modifier =
          Modifier
            .padding(top = 8.dp)
            .width(46.dp)
            .height(40.dp)
            .padding(end = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(brush = shimmerSkeleton()),
        )
        Spacer(
          modifier =
          Modifier
            .padding(top = 8.dp)
            .width(46.dp)
            .height(40.dp)
            .padding(end = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(brush = shimmerSkeleton()),
        )
        Spacer(
          modifier =
          Modifier
            .padding(top = 8.dp)
            .width(40.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(brush = shimmerSkeleton()),
        )
      }
    }
  }
}

// Test Itens
data class CardPromotionItem(
  val title: String?,
  val subtitle: String?,
  val promotionStartTime: Long?,
  val promotionEndTime: Long,
  val imageUrl: String?,
  val urlRedirect: String?,
  val packageName: String?,
  val hasVipPromotion: Boolean,
  val hasFuturePromotion: Boolean,
  val hasVerticalList: Boolean,
  val action: () -> Unit
)

val cardItem =
  CardPromotionItem(
    title = "Days of empire",
    subtitle = "Receive an extra 15% Bonus in all your purchases.",
    promotionStartTime = System.currentTimeMillis(),
    promotionEndTime = System.currentTimeMillis(),
    imageUrl =
    "https://img.freepik.com/vetores-gratis/astronauta-bonito-relaxamento-frio-na-ilustracao-do-icone-do-vetor-dos-desenhos-animados-do-controlador-de-jogo-conceito-de-icone-de-ciencia-de-tecnologia-isolado-vetor-premium-estilo-flat-cartoon_138676-3717.jpg?w=2000",
    urlRedirect = "https://example.com",
    packageName = null,
    hasVipPromotion = false,
    hasFuturePromotion = false,
    hasVerticalList = false,
    action = { /* handle click action */ })

val verticalCardItem =
  CardPromotionItem(
    title = "Days of empire",
    subtitle = "Receive an extra 15% Bonus in all your purchases.",
    promotionStartTime = System.currentTimeMillis(),
    promotionEndTime = System.currentTimeMillis(),
    imageUrl =
    "https://img.freepik.com/vetores-gratis/astronauta-bonito-relaxamento-frio-na-ilustracao-do-icone-do-vetor-dos-desenhos-animados-do-controlador-de-jogo-conceito-de-icone-de-ciencia-de-tecnologia-isolado-vetor-premium-estilo-flat-cartoon_138676-3717.jpg?w=2000",
    urlRedirect = "https://example.com",
    packageName = null,
    hasVipPromotion = false,
    hasFuturePromotion = false,
    hasVerticalList = true,
    action = { /* handle click action */ })

val vipCardItem =
  CardPromotionItem(
    title = "Days of empire",
    subtitle = "Receive an extra 15% Bonus in all your purchases.",
    promotionStartTime = System.currentTimeMillis(),
    promotionEndTime = System.currentTimeMillis(),
    imageUrl =
    "https://img.freepik.com/vetores-gratis/astronauta-bonito-relaxamento-frio-na-ilustracao-do-icone-do-vetor-dos-desenhos-animados-do-controlador-de-jogo-conceito-de-icone-de-ciencia-de-tecnologia-isolado-vetor-premium-estilo-flat-cartoon_138676-3717.jpg?w=2000",
    urlRedirect = "https://example.com",
    packageName = null,
    hasVipPromotion = true,
    hasFuturePromotion = false,
    hasVerticalList = false,
    action = { /* handle click action */ })

val futureCardItem =
  CardPromotionItem(
    title = "Days of empire",
    subtitle = "Receive an extra 15% Bonus in all your purchases.",
    promotionStartTime = System.currentTimeMillis(),
    promotionEndTime = System.currentTimeMillis(),
    imageUrl =
    "https://img.freepik.com/vetores-gratis/astronauta-bonito-relaxamento-frio-na-ilustracao-do-icone-do-vetor-dos-desenhos-animados-do-controlador-de-jogo-conceito-de-icone-de-ciencia-de-tecnologia-isolado-vetor-premium-estilo-flat-cartoon_138676-3717.jpg?w=2000",
    urlRedirect = "https://example.com",
    packageName = null,
    hasVipPromotion = false,
    hasFuturePromotion = true,
    hasVerticalList = false,
    action = { /* handle click action */ })
