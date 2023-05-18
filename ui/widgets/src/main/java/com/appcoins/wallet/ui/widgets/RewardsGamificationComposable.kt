package com.appcoins.wallet.ui.widgets

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_grey_blue
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_grey_blue_background
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_orange

@Composable
fun GamificationHeader(
  onClick: () -> Unit,
  indicatorColor: Color,
  valueSpendForNextLevel: String,
  currencySpend: String,
  currentProgress: Int,
  maxProgress: Int,
  bonusValue: String,
  planetDrawable: Drawable?
) {
  Card(
    modifier = Modifier
      .padding(
        start = 16.dp,
        end = 16.dp,
        top = 16.dp
      )
      .fillMaxWidth()
      .height(170.dp)
      .clickable { onClick() },
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
  ) {
    Column (
      modifier = Modifier
        .fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween
        )
    {

      Row (   // Top main content
        modifier = Modifier
          .fillMaxWidth()
          .height(122.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      )
      {
        Column(  // spend text, progressBar, progress
          modifier = Modifier
            .fillMaxHeight()
            .weight(2F)
            .padding(start = 16.dp, top = 16.dp),
          verticalArrangement = Arrangement.SpaceEvenly,
        ) {
          Text(
            text = stringResource(id = R.string.rewards_spend_to_next_level_body, valueSpendForNextLevel, currencySpend),
            style = MaterialTheme.typography.titleMedium,
            color = WalletColors.styleguide_white,
          )
          Column(  // progressBar, progress
            modifier = Modifier
              .padding(top = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
          ) {
            LinearProgressIndicator(
              progress = currentProgress.toFloat()/maxProgress.toFloat(),
              modifier = Modifier
                .background(Color.Transparent)
                .clip(CircleShape)
                .height(8.dp),
              color = indicatorColor,
              trackColor = styleguide_grey_blue,
            )
            Text(
              text = "$currentProgress / $maxProgress",
              style = MaterialTheme.typography.bodyMedium,
              color = WalletColors.styleguide_dark_grey,
              modifier = Modifier
                .align(alignment = Alignment.End)
            )
          }
        }
        Box(
          modifier = Modifier
            .weight(1F)
            .fillMaxHeight()
        ) {
          planetDrawable?.let {
            val bitmap = Bitmap.createBitmap(
              planetDrawable.intrinsicWidth,
              planetDrawable.intrinsicHeight,
              Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            planetDrawable.setBounds(0, 0, canvas.width, canvas.height)
            planetDrawable.draw(canvas)
            Image(
              bitmap = bitmap.asImageBitmap(),
              contentDescription = "Planet",
              modifier = Modifier
                .size(82.dp)
                .align(Alignment.Center)
            )
          }
        }

      }


      Row (   // Bottom main content
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .background(styleguide_grey_blue_background)
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
      )
      {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bonus_gift_animation))
        val progress by animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)
        LottieAnimation(
          modifier = Modifier
            .size(32.dp)
            .align(Alignment.CenterVertically),
          composition = composition,
          progress = { progress }
        )

        Text(
          text = stringResource(id = R.string.vip_program_max_bonus_short, bonusValue),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = WalletColors.styleguide_light_grey,
          lineHeight = 24.sp,
          modifier = Modifier
            .align(Alignment.CenterVertically)
            .padding(horizontal = 6.dp)
            .weight(1f, fill = false)
        )
        Image(
          painter = painterResource(R.drawable.ic_arrow_right),
          "Arrow",
          modifier = Modifier
            .height(36.dp)
            .width(36.dp)
            .align(Alignment.CenterVertically)
        )
      }


    }
  }
}

@Composable
fun GamificationHeaderNoPurchases() {
  Card(
    modifier = Modifier
      .padding(
        start = 16.dp,
        end = 16.dp,
        top = 16.dp
      )
      .fillMaxWidth()
      .height(170.dp),
    shape = RoundedCornerShape(8.dp),
    border = BorderStroke(1.dp, WalletColors.styleguide_pink),
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize(),
      verticalArrangement = Arrangement.SpaceEvenly,
      horizontalAlignment = Alignment.CenterHorizontally
    )
    {
      Image(
        painter = painterResource(id = R.drawable.ic_locked),
        contentDescription = "Locked"
      )
      Text(
        text = stringResource(id = R.string.promotions_empty_first_purchase_body),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = WalletColors.styleguide_light_grey,
        lineHeight = 24.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .weight(1f, fill = false)
      )
    }
  }
}

@Composable
fun GamificationHeaderPartner(
  bonusPercentage: String
) {
  Card(
    modifier = Modifier
      .padding(
        start = 16.dp,
        end = 16.dp,
        top = 16.dp
      )
      .fillMaxWidth()
      .height(72.dp),
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
  ) {
    Box (
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .fillMaxSize()
        .align(Alignment.CenterHorizontally)
        ) {
      Row(   // Bottom main content
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      )
      {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bonus_gift_animation))
        val progress by animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)
        LottieAnimation(
          modifier = Modifier
            .size(48.dp)
            .align(Alignment.CenterVertically),
          composition = composition,
          progress = { progress }
        )

        Text(
          text = stringResource(id = R.string.vip_program_max_bonus_short, bonusPercentage),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = WalletColors.styleguide_light_grey,
          lineHeight = 24.sp,
          modifier = Modifier
            .align(Alignment.CenterVertically)
            .padding(horizontal = 6.dp)
            .weight(1f, fill = false)
        )
      }
    }
  }
}

@Preview
@Composable
fun PreviewRewardsGamification() {
  GamificationHeader(
    onClick = { },
    indicatorColor = styleguide_orange,
    valueSpendForNextLevel = "16",
    currencySpend = "AppCoins Credits",
    currentProgress = 8000,
    maxProgress = 15000,
    bonusValue = "16",
    planetDrawable = null
  )
}

@Composable
fun VipReferralCard(
  onClick: () -> Unit,
  vipBonus: String
) {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
    modifier = Modifier
      .fillMaxWidth()
      .height(96.dp)
      .padding(
        start = 16.dp,
        end = 16.dp,
        top = 16.dp
      )
      .clip(shape = RoundedCornerShape(8.dp))
      .clickable { onClick() },
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.Start,
    ) {
      Image(
        painter = painterResource(R.drawable.ic_vip_symbol),  //TODO
        "VIP",
        modifier = Modifier
          .height(64.dp)
          .width(64.dp)
          .align(Alignment.CenterVertically)
      )
      Column (
        modifier = Modifier
          .fillMaxSize()
          .weight(1f)
          ) {
        Text(
          text = stringResource(R.string.vip_program_referral_button_title),
          style = MaterialTheme.typography.titleMedium,
          color = WalletColors.styleguide_white,
          modifier = Modifier
            .padding(horizontal = 20.dp)
            .weight(1f, fill = false)
        )
        Text(
          text = stringResource(R.string.vip_program_referral_button_body, vipBonus),
          style = MaterialTheme.typography.bodyMedium,
          color = WalletColors.styleguide_dark_grey,
          modifier = Modifier
            .padding(horizontal = 20.dp)
            .weight(1f, fill = false)
        )
      }
      Image(
        painter = painterResource(R.drawable.ic_arrow_right),
        "Arrow",
        modifier = Modifier
          .height(36.dp)
          .width(36.dp)
          .align(Alignment.CenterVertically)
      )
    }
  }
}

@Preview
@Composable
fun PreviewRewardsGamificationNoPurchases() {
  GamificationHeaderNoPurchases()
}

@Preview
@Composable
fun PreviewRewardsGamificationPartner() {
  GamificationHeaderPartner("5")
}

@Preview
@Composable
fun PreviewRewardsVip() {
  VipReferralCard(
    { },
    "5"
  )
}