package com.appcoins.wallet.ui.widgets

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun VipReferralCardComposable(
  modifier: Modifier = Modifier,
  vipBonus: String,
  startDate: Long,
  endDate: Long,
  isActive: Boolean,
  referralCode: String? = null,
  numberReferrals: String,
  totalEarned: String,
  appName: String?,
  appIcon: String?,
  currencySymbol: String,
  maxReward: String,
  onCardClick: () -> Unit = {},
  onShare: (String) -> Unit = {},
  initialExpanded: Boolean = false,
) {

  var expanded by rememberSaveable { mutableStateOf(initialExpanded) }
  var futureCode by rememberSaveable {
    mutableStateOf(
      ((System.currentTimeMillis() / 1000L) < startDate) || !isActive
    )
  }
  val arrowRotation by animateFloatAsState(
    targetValue = if (expanded) 180f else 0f, label = ""
  )

  val totalEarnedFormated = stringResource(
    R.string.value_fiat,
    currencySymbol,
    totalEarned
  )
  val earnedLabel = stringResource(R.string.earned_from_referrals_vip, totalEarnedFormated, numberReferrals)

  val darkCard = WalletColors.styleguide_dark_secondary
  val darkCardSub = WalletColors.styleguide_dark
  val yellow = WalletColors.styleguide_vip_yellow
  val greyText = WalletColors.styleguide_dark_grey

  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(
        BorderStroke(1.dp, if (futureCode) Color.Transparent else yellow),
        RoundedCornerShape(12.dp)
      )
      .animateContentSize(),
    colors = CardDefaults.cardColors(containerColor = darkCard),
    shape = RoundedCornerShape(8.dp)
  ) {
    Box(Modifier.fillMaxWidth()) {
      Surface(
        color = if (futureCode) WalletColors.styleguide_inactive_grey else yellow,
        shape = RoundedCornerShape(bottomStart = 12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .zIndex(1f)
      ) {
        Text(
          text = if (futureCode) stringResource(R.string.available_soon_vip) else stringResource(R.string.active_vip),
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
          style = TextStyle(
            fontSize = 10.sp,
            fontFamily = FontFamily(Font(R.font.roboto_regular)),
            fontWeight = FontWeight.W400,
            color = if (futureCode) WalletColors.styleguide_disabled_text else WalletColors.styleguide_dark_secondary
          )
        )
      }
    }

    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 18.dp)) {

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          painter = painterResource(if (futureCode) R.drawable.ic_future_code else R.drawable.ic_vip_logo),
          contentDescription = "VIP logo",
          tint = Color.Unspecified,
          modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
          Text(
            stringResource(R.string.vip_referral_program),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White
            ),
            modifier = Modifier.padding(bottom = 6.dp)
          )
          Text(
            stringResource(R.string.bonus_for_you_vip, vipBonus),
            style = MaterialTheme.typography.bodySmall.copy(color = greyText)
          )
        }
      }

      Spacer(Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (futureCode) {
          Icon(
            painter = painterResource(R.drawable.ic_clock_available_soon),
            contentDescription = "Clock icon",
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
          )
          Spacer(Modifier.width(4.dp))
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            if (futureCode) stringResource(R.string.available_in_vip) else stringResource(R.string.referral_ends_in_vip),
            style = TextStyle(
              fontSize = 14.sp,
              fontFamily = FontFamily(Font(R.font.roboto_medium)),
              fontWeight = FontWeight.W600,
              color = WalletColors.styleguide_white
            )
          )
          CountDownTimer(endDateTime = endDate)
        }
      }

      Spacer(Modifier.height(12.dp))

      referralCode?.let { code ->
        if (!expanded) {
          ReferralCodeRow(code, darkCardSub, yellow, futureCode, onShare)
          Spacer(Modifier.height(16.dp))
        }
      }

      if (expanded) {
        ExpandedSection(
          vipBonus = vipBonus,
          referralCode = referralCode,
          earnedLabel = earnedLabel,
          darkCardSub = darkCardSub,
          greyText = greyText,
          yellow = yellow,
          futureCode = futureCode,
          appName = appName,
          appIcon = appIcon,
          maxReward = maxReward,
          currencySymbol = currencySymbol,
          onShare = onShare
        )
        Spacer(Modifier.height(16.dp))
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .clickable { expanded = !expanded },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          if (expanded) stringResource(R.string.see_less_vip) else stringResource(R.string.see_more_vip),
          style = TextStyle(
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.roboto_regular)),
            fontWeight = FontWeight.W400,
            color = if (futureCode) WalletColors.styleguide_primary else yellow
          )
        )
        Icon(
          Icons.Filled.KeyboardArrowDown,
          contentDescription = null,
          tint = if (futureCode) WalletColors.styleguide_primary else yellow,
          modifier = Modifier.rotate(arrowRotation)
        )
      }
    }
  }
}

@Composable
private fun ReferralCodeRow(
  code: String,
  containerColor: Color,
  buttonColor: Color,
  futureCode: Boolean = false,
  onShare: (String) -> Unit
) {

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .background(containerColor)
      .padding(start = 16.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    SelectionContainer(
      modifier = Modifier.weight(1f)
    ) {
      Text(
        text = code,
        maxLines = 1,
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        style = MaterialTheme.typography.bodyMedium.copy(
          color = Color.White,
          letterSpacing = 0.5.sp
        )
      )
    }

    val context = LocalContext.current
    Button(
      onClick = {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
          putExtra(Intent.EXTRA_TEXT, code)
          type = "text/plain"
        }
        val chooser = Intent.createChooser(
          sendIntent,
          context.getString(R.string.share_vip)
        )
        context.startActivity(chooser)
      },
      shape = RoundedCornerShape(36.dp),
      contentPadding = PaddingValues(horizontal = 16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = if (futureCode)
          WalletColors.styleguide_dark_variant
        else
          buttonColor
      )
    ) {
      Text(
        stringResource(R.string.share_vip),
        style = TextStyle(
          fontSize = 12.sp,
          fontFamily = FontFamily(Font(R.font.roboto_regular)),
          fontWeight = FontWeight.W400,
          color = if (futureCode) WalletColors.styleguide_white else WalletColors.styleguide_dark_secondary
        )
      )
    }
  }
}

@Composable
private fun ExpandedSection(
  vipBonus: String,
  referralCode: String?,
  earnedLabel: String,
  darkCardSub: Color,
  greyText: Color,
  yellow: Color,
  futureCode: Boolean,
  appName: String?,
  appIcon: String? = null,
  maxReward: String,
  currencySymbol: String,
  onShare: (String) -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = WalletColors.styleguide_dark_variant)
  ) {
    Column(Modifier.padding(16.dp)) {

      Text(
        stringResource(R.string.share_this_code_with_your_friends_vip),
        style = MaterialTheme.typography.bodyLarge.copy(
          fontWeight = FontWeight.SemiBold,
          color = Color.White
        )
      )

      Spacer(Modifier.height(12.dp))

      /* icon + game name */
      Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(appIcon)
            .crossfade(true)
            .placeholder(R.drawable.ic_appcoins_notification_icon)
            .error(R.drawable.ic_appcoins_notification_icon)
            .build(),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column {
          Text(
            stringResource(R.string.only_for_purchases_on_vip),
            style = MaterialTheme.typography.bodySmall.copy(color = greyText)
          )
          Text(
            appName ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          )
        }
      }

      Spacer(Modifier.height(16.dp))

      referralCode?.let {
        ReferralCodeRow(it, darkCardSub, yellow, futureCode, onShare)
      }

      Spacer(Modifier.height(12.dp))

      val maxRewardFormated = stringResource(
        R.string.value_fiat,
        currencySymbol,
        maxReward
      )
      Text(
        stringResource(R.string.each_in_app_purchase2_vip, vipBonus, maxRewardFormated),
        style = MaterialTheme.typography.bodySmall.copy(color = greyText)
      )

      if (futureCode) {
        Spacer(Modifier.height(12.dp))
        Text(
          stringResource(R.string.promo_code_referral_program_vip),
          style = MaterialTheme.typography.bodySmall.copy(color = greyText)
        )
      }

      Spacer(Modifier.height(12.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          painter = painterResource(
            if (futureCode)
              R.drawable.ic_lock_referral
            else
              R.drawable.ic_coins_stack
          ),
          contentDescription = null,
          tint = Color.Unspecified,
          modifier = Modifier.size(if (futureCode) 20.dp else 30.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
          if (futureCode) stringResource(R.string.referral_program_not_active_yet_vip) else earnedLabel,
          style = TextStyle(
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.roboto_regular)),
            fontWeight = FontWeight.W400,
            color = WalletColors.styleguide_white
          )
        )
      }
    }
  }
}

@Preview(
  name = "VIP Referral – Collapsed",
  showBackground = true,
  backgroundColor = 0xFF121212
)
@Composable
private fun VipReferralCardPreviewCollapsed() {
  val threeDays = System.currentTimeMillis() + 1_000L * 60L * 60L * 24L * 3L
  VipReferralCardComposable(
    vipBonus = "5",
    startDate = (System.currentTimeMillis() / 1000L) - 100L,
    endDate = (threeDays / 1000L),
    isActive = true,
    referralCode = "1456152810291",
    numberReferrals = "5",
    totalEarned = "25",
    appName = "Example App",
    appIcon = "https://upload.wikimedia.org/wikipedia/en/a/a9/Example.jpg",
    currencySymbol = "$",
    maxReward = "100",
    onShare = {}
  )
}

@Preview(
  name = "VIP Referral – Expanded",
  showBackground = true,
  backgroundColor = 0xFF121212
)
@Composable
private fun VipReferralCardPreviewExpanded() {
  val threeDays = System.currentTimeMillis() + 1_000L * 60L * 60L * 24L * 3L
  VipReferralCardComposable(
    vipBonus = "5",
    startDate = (System.currentTimeMillis() / 1000L) - 100L,
    endDate = (threeDays / 1000L),
    isActive = true,
    referralCode = "1456152810291",
    onShare = {},
    initialExpanded = true,
    numberReferrals = "5",
    totalEarned = "25",
    appName = "Example App",
    appIcon = "https://upload.wikimedia.org/wikipedia/en/a/a9/Example.jpg",
    currencySymbol = "$",
    maxReward = "100"
  )
}

@Preview(
  name = "VIP Referral – future",
  showBackground = true,
  backgroundColor = 0xFF121212
)
@Composable
private fun VipReferralCardPreviewFuture() {
  val threeDays = System.currentTimeMillis() + 1_000L * 60L * 60L * 24L * 3L
  VipReferralCardComposable(
    vipBonus = "5",
    startDate = (System.currentTimeMillis() / 1000L) + 100L,
    endDate = (threeDays / 1000L),
    isActive = false,
    referralCode = "1456152810291",
    onShare = {},
    initialExpanded = true,
    numberReferrals = "5",
    totalEarned = "25",
    appName = "Example App",
    appIcon = "https://upload.wikimedia.org/wikipedia/en/a/a9/Example.jpg",
    currencySymbol = "$",
    maxReward = "100"
  )
}
