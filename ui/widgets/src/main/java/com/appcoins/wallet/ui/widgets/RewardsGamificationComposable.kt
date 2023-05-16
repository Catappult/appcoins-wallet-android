package com.appcoins.wallet.ui.widgets

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun GamificationHeader(
  onClick: () -> Unit,
  indicatorColor: Color,
  valueSpendForNextLevel: String,
  currencySpend: String,
  currentProgress: Int,
  maxProgress: Int,
  bonusValue: String,
  @DrawableRes planetDrawable: Int
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
          Image(
            painter = painterResource(id = planetDrawable), //R.drawable.gamification_jupiter_reached
            contentDescription = "Planet",
            modifier = Modifier
              .size(82.dp)
              .align(Alignment.Center)
          )
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
    planetDrawable = R.drawable.gamification_jupiter_reached
  )
}