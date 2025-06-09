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
import androidx.compose.foundation.layout.Spacer
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
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_variant
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_orange
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_primary
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
  isMaxVip: Boolean,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  Card(
    modifier = Modifier
      .padding(start = 16.dp, end = 16.dp, top = 16.dp)
      .fillMaxSize()
      .clickable {
        buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, "Gamification")
        onClick()
      },
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(styleguide_dark_secondary),
  ) {
    Column(
      modifier = Modifier.padding(start = 16.dp, top = 8.dp),
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
            modifier = Modifier
              .weight(2F)
              .align(Alignment.CenterVertically),
          ) {
            Text(
              text = stringResource(
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
            modifier = Modifier
              .weight(2F)
              .padding(end = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
          ) {
            Text(
              modifier = Modifier.padding(end = 8.dp, top = 16.dp, bottom = 8.dp),
              text = stringResource(
                id = R.string.rewards_spend_to_next_level_body,
                currencySpend,
                valueSpendForNextLevel
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
                  modifier = Modifier
                    .background(Color.Transparent)
                    .clip(CircleShape)
                    .height(8.dp)
                    .fillMaxWidth(),
                  color = if (isVip) styleguide_vip_yellow else indicatorColor,
                  trackColor = styleguide_dark_variant,
                )
              }
            }
          }
        }
        GamificationLevelIcon(
          planetDrawable, isVip, isMaxVip, modifier = Modifier.weight(1F)
        )
      }
    }
    GamificationBottomBar(bonusValue = bonusValue)
  }
}

@Composable
fun GamificationBottomBar(bonusValue: String) {
  Row(
    // Bottom main content
    modifier = Modifier
      .fillMaxWidth()
      .height(48.dp)
      .background(styleguide_dark_variant)
      .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row {
      val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bonus_gift_animation))
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
        modifier = Modifier
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

@Composable
fun GamificationLevelIcon(
  planetDrawable: Drawable?,
  isVip: Boolean,
  isMaxVip: Boolean,
  modifier: Modifier
) {
  Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.gamification_reached_vip))
    val progress by animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)

    if (isMaxVip || isVip) {
      Image(
        painter = painterResource(R.drawable.vip_background),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth()
      )
      LottieAnimation(
        modifier = Modifier
          .width(80.dp)
          .height(112.dp),
        composition = composition,
        progress = { progress })
    } else {
      planetDrawable?.let {
        val bitmap = Bitmap.createBitmap(
          planetDrawable.intrinsicWidth, planetDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
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
            .scale(1F)
        )
      }
    }
  }
}

@Composable
fun GamificationHeaderNoPurchases() {
  Card(
    modifier = Modifier
      .padding(start = 16.dp, end = 16.dp, top = 16.dp)
      .fillMaxWidth()
      .height(170.dp),
    shape = RoundedCornerShape(8.dp),
    border = BorderStroke(1.dp, styleguide_primary),
    colors = CardDefaults.cardColors(styleguide_dark_secondary),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceEvenly,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(
        painter = painterResource(id = R.drawable.ic_locked),
        contentDescription = "Locked"
      )
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
fun GamificationHeaderPartner(bonusPerkDescription: String) {
  Card(
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .padding(top = 16.dp)
      .fillMaxWidth(),
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(styleguide_dark_secondary),
  ) {
    Row( // Bottom main content
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bonus_gift_animation))
      val progress by animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)
      LottieAnimation(
        modifier = Modifier
          .size(48.dp)
          .align(Alignment.CenterVertically),
        composition = composition,
        progress = { progress })

      Text(
        text = bonusPerkDescription,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = WalletColors.styleguide_light_grey,
        lineHeight = 24.sp,
        modifier = Modifier.padding(start = 16.dp)
      )
    }
  }
}

@Composable
fun VipReferralCountDownTimer(
  dateTime: Long,
  modifier: Modifier = Modifier,
  referralAvailable: Boolean
) {
  Row(
    modifier = modifier
      .padding(bottom = 8.dp)
      .padding(horizontal = 16.dp)
      .fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      modifier = modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Start
    ) {
      if (!referralAvailable) Image(
        painter = painterResource(R.drawable.ic_timer),
        contentDescription = null,
        modifier = Modifier
          .size(24.dp)
          .padding(end = 8.dp)
      )
      Text(
        text = stringResource(
          if (referralAvailable) R.string.ending_in_title
          else R.string.perks_available_soon_short
        ),
        color = WalletColors.styleguide_light_grey,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
      )
    }
    CountDownTimer(endDateTime = dateTime)
  }
}

@Composable
fun SkeletonLoadingGamificationCard() {
  Card(
    colors = CardDefaults.cardColors(styleguide_dark_secondary),
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 16.dp, end = 16.dp, top = 16.dp)
      .clip(shape = RoundedCornerShape(8.dp))
  ) {
    Column(modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, start = 8.dp, end = 8.dp)
      ) {
        Column {
          Spacer(
            modifier = Modifier
              .width(width = 150.dp)
              .height(height = 22.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(brush = shimmerSkeleton()),
          )
          Spacer(
            modifier = Modifier
              .width(width = 230.dp)
              .height(height = 30.dp)
              .padding(top = 8.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(brush = shimmerSkeleton()),
          )
        }
        Spacer(
          modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(45.dp))
            .background(brush = shimmerSkeleton()),
        )
      }
    }
    Row(modifier = Modifier.fillMaxWidth()) {
      Spacer(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .background(brush = shimmerSkeleton()),
      )
    }
  }
}

fun isVipReferralAlreadyAvailable(startDateTime: Long) =
  startDateTime * 1000L <= System.currentTimeMillis()

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
    isMaxVip = false,
    fragmentName = "RewardsFragment",
    buttonsAnalytics = null
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
    isMaxVip = true,
    fragmentName = "RewardsFragment",
    buttonsAnalytics = null
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
  GamificationHeaderPartner(stringResource(id = R.string.vip_program_max_bonus_short, "5"))
}

@Preview
@Composable
fun PreviewRewardsSkeletonLoading() {
  SkeletonLoadingGamificationCard()
}
