package com.appcoins.wallet.ui.widgets

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun VipReferralCardComposable(
  modifier: Modifier = Modifier,
  vipBonus: String,
  endDate: Long,
  referralCode: String? = null,
  onClick: () -> Unit,
  onShare: (String) -> Unit,
) {

  val darkCard = WalletColors.styleguide_dark_secondary
  val darkCardSub = WalletColors.styleguide_dark
  val yellow = WalletColors.styleguide_vip_yellow
  val greyText = WalletColors.styleguide_dark_grey

  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(BorderStroke(1.dp, yellow), RoundedCornerShape(8.dp))
      .clickable { onClick() },
    colors = CardDefaults.cardColors(containerColor = darkCard),
    shape = RoundedCornerShape(8.dp)
  ) {

    Box(modifier = Modifier.fillMaxWidth()) {

      Surface(
        color = yellow,
        shape = RoundedCornerShape(bottomStart = 12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .zIndex(1f)
      ) {
        Text(
          text = "Active",
          modifier = Modifier.padding(
            horizontal = 12.dp,
            vertical = 3.dp
          ),
          style = TextStyle(
            fontSize = 10.sp,
            fontFamily = FontFamily(Font(R.font.roboto_regular)),
            fontWeight = FontWeight(400),
            color = WalletColors.styleguide_dark_secondary,
          ),
        )
      }

    }

    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 18.dp)) {

      /***** HEADER *****/
      Row(verticalAlignment = Alignment.CenterVertically) {

        Icon(
          painter = painterResource(R.drawable.ic_vip_wallet),
          contentDescription = "VIP logo",
          tint = Color.Unspecified,
          modifier = Modifier.size(56.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column {
          Text(
            text = "VIP referral program",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White
            ),
            modifier = Modifier.padding(bottom = 6.dp)
          )
          Text(
            text = "${vipBonus}% Bonus for you and your friends",
            style = MaterialTheme.typography.bodySmall.copy(
              color = greyText
            )
          )
        }
      }

      Spacer(Modifier.height(6.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {

        /***** LABEL *****/
        Text(
          text = "Referral ends in",
          style = TextStyle(
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.roboto_medium)),
            fontWeight = FontWeight.W600,
            color = WalletColors.styleguide_white,
          )
        )

        /***** COUNT-DOWN (re-use your existing composable) *****/
        CountDownTimer(endDateTime = endDate)
      }

      Spacer(Modifier.height(12.dp))

      /***** REFERRAL CODE *****/
      referralCode?.let { code ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(darkCardSub)
            .padding(start = 16.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = code,
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = yellow)
          ) {
            Text(
              "Share",
              color = Color.Black,
              style = TextStyle(
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(R.font.roboto_regular)),
                fontWeight = FontWeight(400),
                color = WalletColors.styleguide_dark_secondary,
              ),
            )
          }
        }

        Spacer(Modifier.height(16.dp))
      }

      /***** SEE-MORE *****/
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "See More",
          style = TextStyle(
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.roboto_regular)),
            fontWeight = FontWeight(400),
            color = yellow,
          ),
          modifier = Modifier
            .clickable { onClick }
        )
        Icon(
          imageVector = Icons.Filled.KeyboardArrowDown,
          contentDescription = null,
          tint = yellow
        )
      }
    }
  }
}

@Preview(
  name = "VIP Referral – Dark",
  showBackground = true,
  backgroundColor = 0xFF121212
)
@Composable
fun VipReferralCardPreview() {
  val threeDaysFromNow = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 3
  VipReferralCardComposable(
    onClick = {},
    onShare = {},
    vipBonus = "5",
    endDate = threeDaysFromNow,
    referralCode = "1456152810291"
  )
}
