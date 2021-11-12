package com.asfoundation.wallet.my_wallets.main.list.model

import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.VisibilityState
import com.asf.wallet.R
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.my_wallets.main.list.WalletsListEvent
import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import com.asfoundation.wallet.ui.balance.BalanceVerificationModel
import com.asfoundation.wallet.ui.balance.BalanceVerificationStatus
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.util.CurrencyFormatUtils

class ActiveWalletModelGroup(
    walletsAsync: Async<WalletsModel>,
    walletVerifiedAsync: Async<BalanceVerificationModel>,
    balanceAsync: Async<BalanceScreenModel>,
    backedUpOnceAsync: Async<Boolean>,
    currencyFormatUtils: CurrencyFormatUtils,
    walletClickListener: ((WalletsListEvent) -> Unit)?
) : EpoxyModelGroup(R.layout.item_active_wallet,
    buildModels(walletsAsync, walletVerifiedAsync, balanceAsync, backedUpOnceAsync,
        currencyFormatUtils, walletClickListener)) {

  companion object {
    fun buildModels(walletsAsync: Async<WalletsModel>,
                    walletVerifiedAsync: Async<BalanceVerificationModel>,
                    balanceAsync: Async<BalanceScreenModel>,
                    backedUpOnceAsync: Async<Boolean>,
                    currencyFormatUtils: CurrencyFormatUtils,
                    walletClickListener: ((WalletsListEvent) -> Unit)?): List<EpoxyModel<*>> {
      val models = mutableListOf<EpoxyModel<*>>()
      models.addWalletInfo(walletsAsync, walletClickListener)
      models.addBalance(balanceAsync, currencyFormatUtils, walletClickListener)
      models.addBackupCard(backedUpOnceAsync, walletClickListener)
      models.addVerify(walletVerifiedAsync, walletClickListener)
      return models
    }

    private fun MutableList<EpoxyModel<*>>.addWalletInfo(walletsAsync: Async<WalletsModel>,
                                                         walletClickListener: ((WalletsListEvent) -> Unit)?) {
      add(WalletInfoModel_()
          .id("active_wallet_info")
          .walletBalanceAsync(walletsAsync)
          .walletClickListener(walletClickListener)
      )
    }

    private fun MutableList<EpoxyModel<*>>.addBalance(balanceAsync: Async<BalanceScreenModel>,
                                                      currencyFormatUtils: CurrencyFormatUtils,
                                                      walletClickListener: ((WalletsListEvent) -> Unit)?) {
      add(WalletBalanceModel_()
          .id("active_wallet_balance")
          .balanceAsync(balanceAsync)
          .formatter(currencyFormatUtils)
          .onVisibilityStateChanged { _, _, visibilityState ->
            if (visibilityState == VisibilityState.VISIBLE) {
              walletClickListener?.invoke(WalletsListEvent.ChangedBalanceVisibility(true))
            } else if (visibilityState == VisibilityState.INVISIBLE) {
              walletClickListener?.invoke(WalletsListEvent.ChangedBalanceVisibility(false))
            }
          }
          .walletClickListener(walletClickListener)
      )
    }

    private fun MutableList<EpoxyModel<*>>.addVerify(
        walletVerifiedAsync: Async<BalanceVerificationModel>,
        walletClickListener: ((WalletsListEvent) -> Unit)?) {
      when (walletVerifiedAsync) {
        is Async.Success -> {
          val verifiedModel = walletVerifiedAsync()
          when (verifiedModel.status) {
            BalanceVerificationStatus.VERIFIED -> addVerified()
            BalanceVerificationStatus.UNVERIFIED -> addUnverified(false, walletClickListener)
            BalanceVerificationStatus.CODE_REQUESTED -> addUnverifiedInsertCode(false,
                walletClickListener)
            BalanceVerificationStatus.NO_NETWORK, BalanceVerificationStatus.ERROR -> {
              // Set cached value
              when (verifiedModel.cachedStatus) {
                BalanceVerificationStatus.VERIFIED -> addVerified()
                BalanceVerificationStatus.UNVERIFIED -> addUnverified(true, walletClickListener)
                BalanceVerificationStatus.CODE_REQUESTED -> addUnverifiedInsertCode(true,
                    walletClickListener)
                else -> addUnverified(true, walletClickListener)
              }
            }
            else -> {
              // Set cached value
              when (verifiedModel.cachedStatus) {
                BalanceVerificationStatus.VERIFIED -> addVerified()
                BalanceVerificationStatus.UNVERIFIED -> addUnverified(false, walletClickListener)
                BalanceVerificationStatus.CODE_REQUESTED -> addUnverifiedInsertCode(false,
                    walletClickListener)
                BalanceVerificationStatus.VERIFYING -> addVerifying()
                else -> addUnverified(true, walletClickListener)
              }
            }
          }
        }
        else -> addVerifyLoading()
      }
    }

    private fun MutableList<EpoxyModel<*>>.addVerified() {
      add(
          VerifiedModel_()
              .id("active_wallet_verified")
      )
    }

    private fun MutableList<EpoxyModel<*>>.addVerifyLoading() {
      add(
          VerifyLoadingModel_()
              .id("active_wallet_verify_loading")
      )
    }

    private fun MutableList<EpoxyModel<*>>.addUnverified(disableButton: Boolean,
                                                         walletClickListener: ((WalletsListEvent) -> Unit)?) {
      add(
          UnverifiedModel_()
              .disableButton(disableButton)
              .walletClickListener(walletClickListener)
              .id("active_wallet_unverified")
      )
    }

    private fun MutableList<EpoxyModel<*>>.addUnverifiedInsertCode(disableButton: Boolean,
                                                                   walletClickListener: ((WalletsListEvent) -> Unit)?) {
      add(UnverifiedInsertCodeModel_().disableButton(disableButton)
          .walletClickListener(walletClickListener)
          .id("active_wallet_insert_code"))
    }

    private fun MutableList<EpoxyModel<*>>.addVerifying() {
      add(VerifyingModel_().id("active_wallet_verifying"))
    }

    private fun MutableList<EpoxyModel<*>>.addBackupCard(backedUpOnceAsync: Async<Boolean>,
                                                         walletClickListener: ((WalletsListEvent) -> Unit)?) {
      val backedUp = backedUpOnceAsync() ?: false
      if (!backedUp) {
        add(BackupModel_().id("active_wallet_backup_card")
            .walletClickListener(walletClickListener))
      }
    }
  }
}