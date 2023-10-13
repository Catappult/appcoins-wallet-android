package com.appcoins.wallet.ui.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_grey_blue
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_grey_blue_background
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_orange
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_vip_yellow

@Composable
fun GamificationHeader(
  onClick: () -> Unit,
  indicatorColor: Color,
  valueSpendForNextLevel: String,
  currencySpend: String,
  currentProgress: Int,
  maxProgress: Int,
  bonusValue: String,
  planetDrawable: Drawable?,
  isVip: Boolean,
  isMaxVip: Boolean
) {
  Card(
    modifier =
    Modifier
      .padding(start = 16.dp, end = 16.dp, top = 16.dp)
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
  ) {
    Column(
      modifier = Modifier
        .padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row( // Top main content
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 80.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        if (isMaxVip) {
          Column(
            // vip text
            modifier =
            Modifier
              .weight(2F)
              .align(Alignment.CenterVertically),
          ) {
            Text(
              text =
              stringResource(
                id = R.string.vip_program_max_bonus_body,
                valueSpendForNextLevel,
                currencySpend
              ),
              style = MaterialTheme.typography.titleSmall,
              color = WalletColors.styleguide_light_grey,
            )
          }
        } else {
          Column(
            // spend text, progressBar, progress
            modifier =
            Modifier
              .weight(2F)
              .padding(end = 16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
          ) {
            Text(
              modifier = Modifier.padding(end = 8.dp, top = 8.dp, bottom = 8.dp),
              text =
              stringResource(
                id = R.string.rewards_spend_to_next_level_body,
                valueSpendForNextLevel,
                currencySpend
              ),
              style = MaterialTheme.typography.titleSmall,
              color = WalletColors.styleguide_light_grey,
            )
            Column(
              // progressBar, progress
              modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
              verticalArrangement = Arrangement.SpaceEvenly,
            ) {
              if (currentProgress >= 0 && maxProgress > 0) {
                LinearProgressIndicator(
                  progress = currentProgress.toFloat() / maxProgress.toFloat(),
                  modifier =
                  Modifier
                    .background(Color.Transparent)
                    .clip(CircleShape)
                    .height(8.dp)
                    .fillMaxWidth(),
                  color = if (isVip) styleguide_vip_yellow else indicatorColor,
                  trackColor = styleguide_grey_blue,
                )

                Text(
                  text = "$currentProgress / $maxProgress",
                  style = MaterialTheme.typography.bodySmall,
                  color = WalletColors.styleguide_dark_grey,
                  modifier = Modifier
                    .padding(end = 8.dp, top = 8.dp)
                    .align(alignment = Alignment.End),
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
        Box(
          modifier = Modifier
            .weight(1F)
        ) {
          planetDrawable?.let {
            val bitmap =
              Bitmap.createBitmap(
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
                .size(96.dp)
                .align(Alignment.Center)
                .scale(1.1F)
            )
          }
        }
      }
    }
    Row(
      // Bottom main content
      modifier =
      Modifier
        .fillMaxWidth()
        .height(48.dp)
        .background(styleguide_grey_blue_background)
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row() {
        val composition by
        rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bonus_gift_animation))
        val progress by animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)
        LottieAnimation(
          modifier = Modifier
            .size(32.dp)
            .align(Alignment.CenterVertically),
          composition = composition,
          progress = { progress })

        Text(
          text = stringResource(id = R.string.rewards_bonus_every_purchase_title, bonusValue),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = WalletColors.styleguide_light_grey,
          lineHeight = 24.sp,
          modifier =
          Modifier
            .align(Alignment.CenterVertically)
            .padding(start = 8.dp)
        )
      }

      Image(
        painter = painterResource(R.drawable.ic_arrow_right),
        contentDescription = null,
        modifier = Modifier
          .height(40.dp)
          .scale(1.3F)
          .align(Alignment.CenterVertically)
      )
    }
  }
}

@Composable
fun GamificationHeaderNoPurchases() {
  Card(
    modifier =
    Modifier
      .padding(start = 16.dp, end = 16.dp, top = 16.dp)
      .fillMaxWidth()
      .height(170.dp),
    shape = RoundedCornerShape(8.dp),
    border = BorderStroke(1.dp, WalletColors.styleguide_pink),
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceEvenly,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(painter = painterResource(id = R.drawable.ic_locked), contentDescription = "Locked")
      Text(
        text = stringResource(id = R.string.rewards_make_first_purchase_body),
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
fun GamificationHeaderPartner(bonusPercentage: String) {
  Card(
    modifier =
    Modifier
      .padding(start = 16.dp, end = 16.dp, top = 16.dp)
      .fillMaxWidth()
      .height(72.dp),
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .fillMaxSize()
        .align(Alignment.CenterHorizontally)
    ) {
      Row( // Bottom main content
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val composition by
        rememberLottieComposition(
          LottieCompositionSpec.RawRes(R.raw.bonus_gift_animation)
        )
        val progress by
        animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)
        LottieAnimation(
          modifier = Modifier
            .size(48.dp)
            .align(Alignment.CenterVertically),
          composition = composition,
          progress = { progress })

        Text(
          text =
          stringResource(id = R.string.vip_program_max_bonus_short, bonusPercentage),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = WalletColors.styleguide_light_grey,
          lineHeight = 24.sp,
          modifier =
          Modifier
            .align(Alignment.CenterVertically)
            .padding(horizontal = 6.dp)
            .weight(1f, fill = false)
        )
      }
    }
  }
}

@Composable
fun VipReferralCard(onClick: () -> Unit, vipBonus: String) {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
    modifier =
    Modifier
      .fillMaxWidth()
      .height(96.dp)
      .padding(start = 16.dp, end = 16.dp, top = 16.dp)
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
        painter = painterResource(R.drawable.ic_vip_symbol),
        contentDescription = stringResource(R.string.vip),
        modifier = Modifier
          .height(64.dp)
          .width(64.dp)
          .align(Alignment.CenterVertically)
      )
      Column(modifier = Modifier
        .fillMaxSize()
        .weight(1f)) {
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
        contentDescription = null,
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
fun PreviewRewardsGamification() {
  GamificationHeader(
    onClick = {},
    indicatorColor = styleguide_orange,
    valueSpendForNextLevel = "16",
    currencySpend = "AppCoins Credits",
    currentProgress = 8000,
    maxProgress = 15000,
    bonusValue = "16",
    planetDrawable = null,
    isVip = true,
    isMaxVip = false
  )
}

@Preview
@Composable
fun PreviewRewardsGamificationMaxVip() {
  GamificationHeader(
    onClick = {},
    indicatorColor = styleguide_orange,
    valueSpendForNextLevel = "16",
    currencySpend = "AppCoins Credits",
    currentProgress = 8000,
    maxProgress = 15000,
    bonusValue = "16",
    planetDrawable = null,
    isVip = true,
    isMaxVip = true
  )
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
  VipReferralCard({}, "5")
}
