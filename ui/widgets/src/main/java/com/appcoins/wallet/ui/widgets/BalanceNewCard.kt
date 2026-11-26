package com.appcoins.wallet.ui.widgets

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_variant
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_shimmer

private const val EMAIL_TEXT_WEIGHT = 400
private const val EMAIL_TEXT_FONT_SIZE = 14

private const val BACKUP_TWEEN = 300

private const val BALANCE_CARD_RADIUS = 28
private const val BALANCE_BUTTON_HEIGHT = 52

@Composable
fun BalanceNewCard(
  balance: String,
  email: String?,
  onClickPromoCode: () -> Unit,
  onClickDetailsBalance: () -> Unit,
  onClickTopUp: () -> Unit,
  onClickMore: () -> Unit,
  onClickBackup: () -> Unit,
  showBackup: Boolean = false,
  isLoading: Boolean = true,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?,
  level: Int = -1,
  bonus: String? = null,
) {
  val tier = GamificationTier.fromLevel(level)
  val glowColor = tier?.color ?: WalletColors.styleguide_primary

  Crossfade(targetState = isLoading, label = "balanceCardLoading") { loading ->
    if (loading) {
      SkeletonLoadingNewBalanceCardExpanded()
    } else {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp)
          .background(WalletColors.styleguide_dark),
      ) {
        Spacer(modifier = Modifier.height(16.dp))
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BALANCE_CARD_RADIUS.dp))
            .background(styleguide_dark_secondary)
            // Soft brand glow from the top-right corner.
            .drawBehind {
              drawRect(
                Brush.radialGradient(
                  colors = listOf(
                    glowColor.copy(alpha = 0.22f),
                    Color.Transparent
                  ),
                  center = Offset(size.width, 0f),
                  radius = size.width * 0.9f
                )
              )
            }
            .border(1.dp, styleguide_dark_variant, RoundedCornerShape(BALANCE_CARD_RADIUS.dp))
            .padding(20.dp)
        ) {
          Row(
            modifier = Modifier
              .clip(CircleShape)
              .clickable { onClickDetailsBalance() }
              .border(1.dp, styleguide_dark_variant, CircleShape)
              .heightIn(min = 36.dp)
              .padding(start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = stringResource(R.string.p2p_send_currency_appc_c),
              color = WalletColors.styleguide_medium_grey,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
            )
            Image(
              painter = painterResource(id = R.drawable.ic_arrow_default_head_down),
              contentDescription = stringResource(R.string.p2p_send_currency_appc_c),
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.height(18.dp))

          // Roll the new value in from below when the balance changes.
          AnimatedContent(
            targetState = balance,
            transitionSpec = {
              (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            },
            label = "balanceValue"
          ) { value ->
            Text(
              text = value,
              color = WalletColors.styleguide_white,
              fontSize = 48.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = (-1).sp,
              style = TextStyle(fontFeatureSettings = "tnum"),
            )
          }

          tier?.let {
            Spacer(modifier = Modifier.height(10.dp))
            TierChip(tier = it, isPlus = level % 2 == 1, bonus = bonus)
          }

          email?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = it,
              color = WalletColors.styleguide_dark_grey,
              fontSize = EMAIL_TEXT_FONT_SIZE.sp,
              fontWeight = FontWeight(EMAIL_TEXT_WEIGHT)
            )
          }
          Spacer(modifier = Modifier.height(20.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            val topUpText = stringResource(R.string.home_top_up_button)
            BalancePillButton(
              text = topUpText,
              icon = rememberVectorPainter(Icons.Filled.Add),
              containerColor = WalletColors.styleguide_primary,
              onClick = {
                buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, topUpText)
                onClickTopUp()
              },
              modifier = Modifier.weight(1f),
            )
            val promoText = stringResource(R.string.home_promo_code_button)
            BalancePillButton(
              text = promoText,
              containerColor = styleguide_dark_variant,
              onClick = {
                buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, promoText)
                onClickPromoCode()
              },
              modifier = Modifier.weight(1f),
            )
            val moreText = stringResource(R.string.action_more_details)
            Box(
              modifier = Modifier
                .size(BALANCE_BUTTON_HEIGHT.dp)
                .clip(CircleShape)
                .background(styleguide_dark_variant)
                .clickable {
                  buttonsAnalytics?.sendDefaultButtonClickAnalytics(fragmentName, moreText)
                  onClickMore()
                },
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.ic_more_icon),
                contentDescription = moreText,
                modifier = Modifier.size(24.dp)
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedVisibility(
          visible = showBackup,
          enter = fadeIn(animationSpec = tween(BACKUP_TWEEN)) + expandVertically(
            animationSpec = tween(
              BACKUP_TWEEN
            )
          ),
          exit = fadeOut(animationSpec = tween(BACKUP_TWEEN)) + shrinkVertically(
            animationSpec = tween(
              BACKUP_TWEEN
            )
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(styleguide_dark_secondary, shape = RoundedCornerShape(16.dp)),
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              BackupAlertCard(
                modifier = Modifier.background(styleguide_dark_secondary),
                onClickButton = onClickBackup,
                hasBackup = false,
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

/** Two levels per tier: even levels are the base tier, odd levels its "+" step. */
private enum class GamificationTier(@StringRes val label: Int, val color: Color) {
  BRONZE(R.string.gamification_tier_bronze, Color(0xFFD08A55)),
  SILVER(R.string.gamification_tier_silver, Color(0xFFC9D1DC)),
  GOLD(R.string.gamification_tier_gold, Color(0xFFE9C46A)),
  PLATINUM(R.string.gamification_tier_platinum, Color(0xFF8FD3E8)),
  VIP(R.string.gamification_tier_vip, WalletColors.styleguide_vip_yellow);

  companion object {
    fun fromLevel(level: Int): GamificationTier? = entries.getOrNull(level / 2).takeIf { level >= 0 }
  }
}

@Composable
private fun TierChip(tier: GamificationTier, isPlus: Boolean, bonus: String?) {
  Row(
    modifier = Modifier
      .clip(CircleShape)
      .background(tier.color.copy(alpha = 0.16f))
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = Icons.Filled.Star,
      contentDescription = null,
      tint = tier.color,
      modifier = Modifier.size(16.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = stringResource(tier.label) + if (isPlus) " +" else "",
      color = tier.color,
      fontSize = 14.sp,
      fontWeight = FontWeight.ExtraBold,
    )
    bonus?.let {
      Text(
        text = "  ·  " + stringResource(R.string.gamification_level_bonus, it),
        color = WalletColors.styleguide_medium_grey,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
private fun BalancePillButton(
  text: String,
  containerColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: Painter? = null,
) {
  Row(
    modifier = modifier
      .height(BALANCE_BUTTON_HEIGHT.dp)
      .clip(CircleShape)
      .background(containerColor)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    icon?.let {
      Icon(
        painter = it,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
    }
    // Long translations wrap to a second line instead of being cut off.
    Text(
      text = text,
      color = Color.White,
      fontSize = 15.sp,
      lineHeight = 17.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
fun SkeletonLoadingNewBalanceCardExpanded() {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_dark),
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(shape = RoundedCornerShape(8.dp))
  ) {
    Row(
      modifier = Modifier
        .padding(top = 8.dp, end = 8.dp, start = 8.dp, bottom = 8.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.Center
    ) {
      Column {
        Spacer(
          modifier = Modifier
            .width(width = 100.dp)
            .height(height = 20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(brush = shimmerSkeleton(shimmerColor = styleguide_shimmer)),
        )
      }
    }
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(end = 8.dp, start = 8.dp, bottom = 8.dp),

      horizontalArrangement = Arrangement.Center
    ) {
      Column {
        Spacer(
          modifier = Modifier
            .width(width = 250.dp)
            .height(height = 30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(brush = shimmerSkeleton(shimmerColor = styleguide_shimmer)),
        )
      }
    }
    Row(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        modifier = Modifier
          .size(115.dp)
          .padding(all = 8.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(brush = shimmerSkeleton(shimmerColor = styleguide_shimmer))
      ) {}
      Column(
        modifier = Modifier
          .size(115.dp)
          .padding(all = 8.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(brush = shimmerSkeleton(shimmerColor = styleguide_shimmer))
      ) {}
      Column(
        modifier = Modifier
          .size(115.dp)
          .padding(all = 8.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(
            brush = shimmerSkeleton(
              shimmerColor = styleguide_shimmer
            )
          )
      ) {}
    }
  }
}

@Preview
@Composable
fun PreviewBalanceNewCard() {
  BalanceNewCard(
    balance = "€32.12",
    onClickPromoCode = {},
    onClickTopUp = {},
    onClickDetailsBalance = {},
    onClickMore = {},
    onClickBackup = {},
    showBackup = true,
    isLoading = false,
    fragmentName = "HomeFragment",
    buttonsAnalytics = null,
    email = "email@test.com"
  )
}
