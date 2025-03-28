package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.component.BalanceValue
import com.appcoins.wallet.ui.widgets.component.ButtonWithIcon

@Composable
fun BalanceCardExpanded(
  balanceContent: @Composable () -> Unit,
  onClickTransfer: () -> Unit,
  onClickTopUp: () -> Unit,
  onClickBackup: () -> Unit,
  onClickMenuOptions: () -> Unit,
  showBackup: Boolean = true,
  newWallet: Boolean = true,
  isLoading: Boolean = true,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_dark_secondary),
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
      .clip(shape = RoundedCornerShape(8.dp))
  ) {
    if (isLoading) {
      SkeletonLoadingBalanceCardExpanded()
    } else if (newWallet) {
      BalanceCardNewUserExpanded(onClickTopUp = onClickTopUp,  fragmentName = fragmentName, buttonsAnalytics = buttonsAnalytics)
    } else {
      Column {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            balanceContent()
            Row {
              ButtonWithIcon(
                icon = R.drawable.ic_transfer,
                label = R.string.transfer_button,
                onClick = onClickTransfer,
                backgroundColor = WalletColors.styleguide_dark,
                labelColor = WalletColors.styleguide_white,
                iconColor = WalletColors.styleguide_primary,
                iconSize = 14.dp,
                fragmentName = fragmentName,
                buttonsAnalytics = buttonsAnalytics
              )
              Spacer(modifier = Modifier.padding(16.dp))
              ButtonWithIcon(
                icon = R.drawable.ic_plus_v3,
                label = R.string.top_up_button,
                onClick = onClickTopUp,
                backgroundColor = WalletColors.styleguide_primary,
                labelColor = WalletColors.styleguide_white,
                iconColor = WalletColors.styleguide_white,
                fragmentName = fragmentName,
                buttonsAnalytics = buttonsAnalytics
              )
              Spacer(modifier = Modifier.padding(16.dp))
              VectorIconButton(
                imageVector = Icons.Default.MoreVert,
                contentDescription = R.string.action_more_details,
                onClick = onClickMenuOptions,
                fragmentName = fragmentName,
                buttonsAnalytics = buttonsAnalytics
              )
            }
          }
        }
        if (showBackup) {
          Surface(
            modifier =
            Modifier
              .fillMaxWidth()
              .absolutePadding(top = 4.dp, bottom = 4.dp)
              .size(1.dp),
            color = WalletColors.styleguide_dark,
            content = {})
          Column(modifier = Modifier.padding(16.dp)) { BackupAlertCardExpanded(onClickBackup, fragmentName, buttonsAnalytics) }
        }
      }
    }
  }
}


@Composable
private fun BalanceCardNewUserExpanded(onClickTopUp: () -> Unit, fragmentName: String, buttonsAnalytics: ButtonsAnalytics?) {
  Row(
    modifier = Modifier
      .padding(32.dp)
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(horizontalAlignment = Alignment.Start) {
      Text(
        text = stringResource(id = R.string.intro_welcome_header),
        style =
        TextStyle(
          color = WalletColors.styleguide_white,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      )
      Text(
        text = stringResource(id = R.string.intro_welcome_body),
        style =
        TextStyle(
          color = WalletColors.styleguide_white,
          fontSize = 14.sp,
        )
      )
    }
    ButtonWithIcon(
      icon = R.drawable.ic_plus_v3,
      label = R.string.top_up_button,
      onClick = onClickTopUp,
      backgroundColor = WalletColors.styleguide_primary,
      labelColor = WalletColors.styleguide_white,
      iconColor = WalletColors.styleguide_white,
      fragmentName = fragmentName,
      buttonsAnalytics = buttonsAnalytics
    )
  }
}

@Composable
fun SkeletonLoadingBalanceCardExpanded() {
  Card(
    colors = CardDefaults.cardColors(WalletColors.styleguide_dark_secondary),
    modifier =
    Modifier
      .fillMaxWidth()
      .padding(bottom = 0.dp, start = 16.dp, end = 16.dp)
      .clip(shape = RoundedCornerShape(8.dp))
  ) {
    Row(
      modifier = Modifier
        .padding(32.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Spacer(
          modifier = Modifier
            .width(width = 250.dp)
            .height(height = 30.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(brush = shimmerSkeleton()),
        )
        Spacer(
          modifier = Modifier
            .padding(top = 4.dp)
            .width(width = 500.dp)
            .height(height = 22.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(brush = shimmerSkeleton()),
        )
      }
      Spacer(
        modifier = Modifier
          .width(width = 120.dp)
          .height(height = 40.dp)
          .clip(RoundedCornerShape(50.dp))
          .background(brush = shimmerSkeleton())
      )
    }
  }
}


@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewLandscapeBalanceCard() {
  BalanceCardExpanded(
    balanceContent = { BalanceValue("€ 30.12", "Eur", {}) },
    onClickTransfer = {},
    onClickBackup = {},
    onClickTopUp = {},
    onClickMenuOptions = {},
    showBackup = true,
    newWallet = false,
    isLoading = false,
    fragmentName = "HomeFragment",
    buttonsAnalytics = null
  )
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewLandscapeBalanceCardWithoutBackup() {
  BalanceCardExpanded(
    balanceContent = { BalanceValue("€ 30.12", "Eur", {}) },
    onClickTransfer = {},
    onClickBackup = {},
    onClickTopUp = {},
    onClickMenuOptions = {},
    showBackup = false,
    newWallet = false,
    isLoading = false,
    fragmentName = "HomeFragment",
    buttonsAnalytics = null
  )
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewLandscapeNewWalletBalanceCard() {
  BalanceCardExpanded(
    balanceContent = { BalanceValue("€ 30.12", "Eur", {}) },
    onClickTransfer = {},
    onClickBackup = {},
    onClickTopUp = {},
    onClickMenuOptions = {},
    showBackup = true,
    newWallet = true,
    isLoading = false,
    fragmentName = "HomeFragment",
    buttonsAnalytics = null
  )
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewSkeletonBalanceCardExpanded() {
  BalanceCardExpanded(
    balanceContent = { BalanceValue("€ 30.12", "Eur", {}) },
    onClickTransfer = {},
    onClickBackup = {},
    onClickTopUp = {},
    onClickMenuOptions = {},
    showBackup = true,
    newWallet = true,
    isLoading = true,
    fragmentName = "HomeFragment",
    buttonsAnalytics = null
  )
}

