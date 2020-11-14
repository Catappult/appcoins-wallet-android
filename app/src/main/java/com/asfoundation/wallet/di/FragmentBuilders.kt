package com.asfoundation.wallet.di

import com.asfoundation.wallet.billing.address.BillingAddressFragment
import com.asfoundation.wallet.billing.adyen.AdyenPaymentFragment
import com.asfoundation.wallet.permissions.manage.view.PermissionsListFragment
import com.asfoundation.wallet.permissions.request.view.CreateWalletFragment
import com.asfoundation.wallet.permissions.request.view.PermissionFragment
import com.asfoundation.wallet.promotions.PromotionsFragment
import com.asfoundation.wallet.referrals.InviteFriendsFragment
import com.asfoundation.wallet.referrals.InviteFriendsVerificationFragment
import com.asfoundation.wallet.referrals.ReferralsFragment
import com.asfoundation.wallet.topup.LocalTopUpPaymentFragment
import com.asfoundation.wallet.topup.TopUpFragment
import com.asfoundation.wallet.topup.TopUpSuccessFragment
import com.asfoundation.wallet.topup.address.BillingAddressTopUpFragment
import com.asfoundation.wallet.topup.payment.AdyenTopUpFragment
import com.asfoundation.wallet.ui.AuthenticationErrorFragment
import com.asfoundation.wallet.ui.SettingsFragment
import com.asfoundation.wallet.ui.SettingsWalletsBottomSheetFragment
import com.asfoundation.wallet.ui.airdrop.AirdropFragment
import com.asfoundation.wallet.ui.backup.BackupCreationFragment
import com.asfoundation.wallet.ui.backup.BackupSuccessFragment
import com.asfoundation.wallet.ui.backup.BackupWalletFragment
import com.asfoundation.wallet.ui.balance.BalanceFragment
import com.asfoundation.wallet.ui.balance.RestoreWalletFragment
import com.asfoundation.wallet.ui.balance.RestoreWalletPasswordFragment
import com.asfoundation.wallet.ui.gamification.GamificationFragment
import com.asfoundation.wallet.ui.iab.*
import com.asfoundation.wallet.ui.iab.payments.carrier.confirm.CarrierConfirmFragment
import com.asfoundation.wallet.ui.iab.payments.carrier.confirm.CarrierConfirmModule
import com.asfoundation.wallet.ui.iab.payments.carrier.status.CarrierPaymentFragment
import com.asfoundation.wallet.ui.iab.payments.carrier.status.CarrierPaymentModule
import com.asfoundation.wallet.ui.iab.payments.carrier.verify.CarrierVerifyFragment
import com.asfoundation.wallet.ui.iab.payments.carrier.verify.CarrierVerifyModule
import com.asfoundation.wallet.ui.iab.payments.common.error.IabErrorFragment
import com.asfoundation.wallet.ui.iab.payments.common.error.IabErrorModule
import com.asfoundation.wallet.ui.iab.share.SharePaymentLinkFragment
import com.asfoundation.wallet.ui.transact.AppcoinsCreditsTransferSuccessFragment
import com.asfoundation.wallet.ui.transact.TransferFragment
import com.asfoundation.wallet.ui.wallets.RemoveWalletFragment
import com.asfoundation.wallet.ui.wallets.WalletDetailsFragment
import com.asfoundation.wallet.ui.wallets.WalletRemoveConfirmationFragment
import com.asfoundation.wallet.ui.wallets.WalletsFragment
import com.asfoundation.wallet.wallet_validation.dialog.CodeValidationDialogFragment
import com.asfoundation.wallet.wallet_validation.dialog.PhoneValidationDialogFragment
import com.asfoundation.wallet.wallet_validation.dialog.ValidationLoadingDialogFragment
import com.asfoundation.wallet.wallet_validation.dialog.ValidationSuccessDialogFragment
import com.asfoundation.wallet.wallet_validation.generic.CodeValidationFragment
import com.asfoundation.wallet.wallet_validation.generic.PhoneValidationFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilders {

  @ContributesAndroidInjector
  abstract fun bindAirdropFragment(): AirdropFragment

  @ContributesAndroidInjector
  abstract fun bindRegularBuyFragment(): OnChainBuyFragment

  @ContributesAndroidInjector
  abstract fun bindWebViewFragment(): BillingWebViewFragment

  @ContributesAndroidInjector
  abstract fun bindAppcoinsRewardsBuyFragment(): AppcoinsRewardsBuyFragment

  @ContributesAndroidInjector
  abstract fun bindPaymentMethodsFragment(): PaymentMethodsFragment

  @ContributesAndroidInjector
  abstract fun bindPermissionFragment(): PermissionFragment

  @ContributesAndroidInjector
  abstract fun bindCreateWalletFragment(): CreateWalletFragment

  @ContributesAndroidInjector
  abstract fun bindPermissionsListFragment(): PermissionsListFragment

  @ContributesAndroidInjector(modules = [ConfirmationModule::class])
  abstract fun bindTransactFragment(): TransferFragment

  @ContributesAndroidInjector
  abstract fun bindAppcoinsCreditsTransactSuccessFragment(): AppcoinsCreditsTransferSuccessFragment

  @ContributesAndroidInjector
  abstract fun bindTopUpFragment(): TopUpFragment

  @ContributesAndroidInjector
  abstract fun bindTopUpSuccessFragment(): TopUpSuccessFragment

  @ContributesAndroidInjector
  abstract fun bindSharePaymentLinkFragment(): SharePaymentLinkFragment

  @ContributesAndroidInjector
  abstract fun bindLocalPaymentFragment(): LocalPaymentFragment

  @ContributesAndroidInjector
  abstract fun bindMergedAppcoinsFragment(): MergedAppcoinsFragment


  @ContributesAndroidInjector
  abstract fun bindPoaPhoneValidationFragment(): PhoneValidationDialogFragment

  @ContributesAndroidInjector
  abstract fun bindPoaCodeValidationFragment(): CodeValidationDialogFragment

  @ContributesAndroidInjector
  abstract fun bindPoaValidationLoadingFragment(): ValidationLoadingDialogFragment

  @ContributesAndroidInjector
  abstract fun bindPoaValidationSuccessFragment(): ValidationSuccessDialogFragment

  @ContributesAndroidInjector
  abstract fun bindBalanceFragment(): BalanceFragment

  @ContributesAndroidInjector
  abstract fun bindPhoneValidationFragment(): PhoneValidationFragment

  @ContributesAndroidInjector
  abstract fun bindCodeValidationFragment(): CodeValidationFragment

  @ContributesAndroidInjector
  abstract fun bindPromotionsFragment(): PromotionsFragment

  @ContributesAndroidInjector
  abstract fun bindInviteFriendsVerificationFragment(): InviteFriendsVerificationFragment

  @ContributesAndroidInjector
  abstract fun bindInviteFriendsFragment(): InviteFriendsFragment

  @ContributesAndroidInjector
  abstract fun bindReferralsFragment(): ReferralsFragment

  @ContributesAndroidInjector
  abstract fun bindEarnAppcoinsFragment(): EarnAppcoinsFragment

  @ContributesAndroidInjector
  abstract fun bindIabUpdateRequiredFragment(): IabUpdateRequiredFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindWalletsFragment(): WalletsFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindWalletDetailFragment(): WalletDetailsFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindAdyenPaymentFragment(): AdyenPaymentFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindAdyenTopUpFragment(): AdyenTopUpFragment


  @ContributesAndroidInjector
  abstract fun bindRemoveWalletFragment(): RemoveWalletFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindWalletRemoveConfirmationFragment(): WalletRemoveConfirmationFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindRestoreWalletFragment(): RestoreWalletFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindRestoreWalletPasswordFragment(): RestoreWalletPasswordFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindBackupWalletFragment(): BackupWalletFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindBackupCreationFragment(): BackupCreationFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindBackupSuccessFragment(): BackupSuccessFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindSettingsFragment(): SettingsFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindSettingsBottomSheetFragment(): SettingsWalletsBottomSheetFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindGamificationFragment(): GamificationFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindLocalTopUpPaymentFragment(): LocalTopUpPaymentFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindBillingAddressFragment(): BillingAddressFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindBillingAddressTopUpFragment(): BillingAddressTopUpFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindAuthenticationErrorFragment(): AuthenticationErrorFragment

  @FragmentScope
  @ContributesAndroidInjector(
      modules = [CarrierVerifyModule::class])
  abstract fun bindCarrierVerifyFragment(): CarrierVerifyFragment

  @FragmentScope
  @ContributesAndroidInjector(
      modules = [CarrierConfirmModule::class])
  abstract fun bindCarrierConfirmFragment(): CarrierConfirmFragment

  @FragmentScope
  @ContributesAndroidInjector(
      modules = [CarrierPaymentModule::class])
  abstract fun bindCarrierPaymentStatusFragment(): CarrierPaymentFragment

  @FragmentScope
  @ContributesAndroidInjector(
      modules = [IabErrorModule::class])
  abstract fun bindIabErrorFragment(): IabErrorFragment
}