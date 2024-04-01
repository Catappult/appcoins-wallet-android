package com.asfoundation.wallet.manage_cards

import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupWalletEntryFragment
import com.asfoundation.wallet.backup.BackupWalletEntryFragment.Companion.WALLET_NAME
import com.asfoundation.wallet.main.splash.SplashExtenderFragmentDirections
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ChangeActiveWalletBottomSheetFragment
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ManageWalletBalanceBottomSheetFragment
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ManageWalletBalanceBottomSheetFragment.Companion.WALLET_BALANCE_MODEL
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ManageWalletBottomSheetFragment
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ManageWalletNameBottomSheetFragment
import com.asfoundation.wallet.transfers.TransferFundsFragment
import com.asfoundation.wallet.ui.bottom_navigation.TransferDestinations
import javax.inject.Inject

class ManageCardsNavigator
@Inject
constructor(private val fragment: Fragment, private val navController: NavController) : Navigator {

  fun navigateToAddCard(navController: NavController) {
    navigate(navController, ManageCardsFragmentDirections.actionNavigateToManageAdyenPayment())
  }

  fun navigateBack() {
    navController.popBackStack()
  }
}
