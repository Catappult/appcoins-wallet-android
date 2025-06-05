package com.appcoins.wallet.ui.widgets

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.appcoins.wallet.ui.common.theme.WalletColors

/**
 * VIP referral card that can expand/collapse itself.
 *
 * @param initialExpanded only used by previews/tests – default keeps behaviour unchanged.
 */
@Composable
fun VipReferralCardComposable(
  modifier: Modifier = Modifier,
  vipBonus: String,
  endDate: Long,
  referralCode: String? = null,
  earnedLabel: String = "25$ earned from 5 referrals",
  onCardClick: () -> Unit = {},
  onShare: (String) -> Unit = {},
  initialExpanded: Boolean = false
) {
  /* ────────── state & animation ────────── */
  var expanded by rememberSaveable { mutableStateOf(initialExpanded) }
  val arrowRotation by animateFloatAsState(
    targetValue = if (expanded) 180f else 0f, label = ""
  )

  /* ────────── colours ────────── */
  val darkCard = WalletColors.styleguide_dark_secondary
  val darkCardSub = WalletColors.styleguide_dark
  val lightCardColor = WalletColors.styleguide_dark.copy(alpha = 0.85f)   // lighter-grey inner card
  val yellow = WalletColors.styleguide_vip_yellow
  val greyText = WalletColors.styleguide_dark_grey

  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(BorderStroke(1.dp, yellow), RoundedCornerShape(12.dp))
      .animateContentSize()
      .clickable { onCardClick() },
    colors = CardDefaults.cardColors(containerColor = darkCard),
    shape = RoundedCornerShape(8.dp)
  ) {
    /* ───── ACTIVE BADGE ───── */
    Box(Modifier.fillMaxWidth()) {
      Surface(
        color = yellow,
        shape = RoundedCornerShape(bottomStart = 12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .zIndex(1f)
      ) {
        Text(
          "Active",
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
          style = TextStyle(
            fontSize = 10.sp,
            fontFamily = FontFamily(Font(R.font.roboto_regular)),
            fontWeight = FontWeight.W400,
            color = WalletColors.styleguide_dark_secondary
          )
        )
      }
    }

    /* ───── MAIN CONTENT ───── */
    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 18.dp)) {

      /***** HEADER *****/
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          painter = painterResource(R.drawable.ic_vip_logo),
          contentDescription = "VIP logo",
          tint = Color.Unspecified,
          modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
          Text(
            "VIP referral program",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White
            ),
            modifier = Modifier.padding(bottom = 6.dp)
          )
          Text(
            "$vipBonus% Bonus for you and your friends",
            style = MaterialTheme.typography.bodySmall.copy(color = greyText)
          )
        }
      }

      Spacer(Modifier.height(6.dp))

      /***** COUNT-DOWN *****/
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          "Referral ends in",
          style = TextStyle(
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.roboto_medium)),
            fontWeight = FontWeight.W600,
            color = WalletColors.styleguide_white
          )
        )
        CountDownTimer(endDateTime = endDate)
      }

      Spacer(Modifier.height(12.dp))

      /***** REFERRAL CODE (collapsed only) *****/
      referralCode?.let { code ->
        if (!expanded) {
          ReferralCodeRow(code, darkCardSub, yellow, onShare)
          Spacer(Modifier.height(16.dp))
        }
      }

      /* ───────── expanded content ───────── */
      if (expanded) {
        ExpandedSection(
          vipBonus,
          referralCode,
          earnedLabel,
          darkCardSub,
          lightCardColor,
          greyText,
          yellow,
          onShare
        )
        Spacer(Modifier.height(16.dp))
      }

      /* ────── SEE MORE / LESS ────── */
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { expanded = !expanded },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          if (expanded) "See Less" else "See More",
          style = TextStyle(
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.roboto_regular)),
            fontWeight = FontWeight.W400,
            color = yellow
          )
        )
        Icon(
          Icons.Filled.KeyboardArrowDown,
          contentDescription = null,
          tint = yellow,
          modifier = Modifier.rotate(arrowRotation)
        )
      }
    }
  }
}

/*──────────────────── helpers ────────────────────*/
@Composable
private fun ReferralCodeRow(
  code: String,
  containerColor: Color,
  buttonColor: Color,
  onShare: (String) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .background(containerColor)
      .padding(start = 16.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      code,
      modifier = Modifier.weight(1f),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      style = MaterialTheme.typography.bodyMedium.copy(
        color = Color.White,
        letterSpacing = 0.5.sp
      )
    )
    Button(
      onClick = { onShare(code) },
      shape = RoundedCornerShape(36.dp),
      contentPadding = PaddingValues(horizontal = 16.dp),
      colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
      Text(
        "Share",
        style = TextStyle(
          fontSize = 12.sp,
          fontFamily = FontFamily(Font(R.font.roboto_regular)),
          fontWeight = FontWeight.W400,
          color = WalletColors.styleguide_dark_secondary
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
  lightCardColor: Color,
  greyText: Color,
  yellow: Color,
  onShare: (String) -> Unit
) {
  /* Everything below sits inside a lighter-grey card */
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = WalletColors.styleguide_dark_variant)
  ) {
    Column(Modifier.padding(16.dp)) {

      Text(
        "Share this code with your friends",
        style = MaterialTheme.typography.bodyLarge.copy(
          fontWeight = FontWeight.SemiBold,
          color = Color.White
        )
      )
      Spacer(Modifier.height(12.dp))

      /* icon + game name */
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          painter = painterResource(R.drawable.ic_appcoins_notification_icon),
          contentDescription = null,
          tint = Color.Unspecified,
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column {
          Text(
            "Only for purchases on",
            style = MaterialTheme.typography.bodySmall.copy(color = greyText)
          )
          Text(
            "Royal Match",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          )
        }
      }

      Spacer(Modifier.height(16.dp))

      referralCode?.let {
        ReferralCodeRow(it, darkCardSub, yellow, onShare)
      }

      Spacer(Modifier.height(12.dp))

      Text(
        "Each in-app purchase gives you and your friends a $vipBonus% bonus, plus other offers for 7 days.",
        style = MaterialTheme.typography.bodySmall.copy(color = greyText)
      )

      Spacer(Modifier.height(12.dp))

      /* centered coins + label */
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_coins_stack),
          contentDescription = null,
          tint = Color.Unspecified,
          modifier = Modifier.size(30.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
          earnedLabel,
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

/*──────────────────── previews ────────────────────*/

/** Collapsed – default look */
@Preview(
  name = "VIP Referral – Collapsed",
  showBackground = true,
  backgroundColor = 0xFF121212
)
@Composable
private fun VipReferralCardPreviewCollapsed() {
  val threeDays = System.currentTimeMillis() + 1_000L * 60 * 60 * 24 * 3
  VipReferralCardComposable(
    vipBonus = "5",
    endDate = threeDays,
    referralCode = "1456152810291",
    onShare = {}
  )
}

/** Expanded – card starts open */
@Preview(
  name = "VIP Referral – Expanded",
  showBackground = true,
  backgroundColor = 0xFF121212
)
@Composable
private fun VipReferralCardPreviewExpanded() {
  val threeDays = System.currentTimeMillis() + 1_000L * 60 * 60 * 24 * 3
  VipReferralCardComposable(
    vipBonus = "5",
    endDate = threeDays,
    referralCode = "1456152810291",
    onShare = {},
    initialExpanded = true      // start expanded
  )
}
