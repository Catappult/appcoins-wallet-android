package com.asfoundation.wallet.di

import com.asfoundation.wallet.billing.address.BillingAddressFragment
import com.asfoundation.wallet.billing.address.BillingAddressModule
import com.asfoundation.wallet.billing.adyen.AdyenPaymentFragment
import com.asfoundation.wallet.permissions.manage.view.PermissionsListFragment
import com.asfoundation.wallet.permissions.request.view.CreateWalletFragment
import com.asfoundation.wallet.permissions.request.view.PermissionFragment
import com.asfoundation.wallet.promotions.PromotionsFragment
import com.asfoundation.wallet.referrals.InviteFriendsFragment
import com.asfoundation.wallet.referrals.InviteFriendsVerificationFragment
import com.asfoundation.wallet.referrals.ReferralsFragment
import com.asfoundation.wallet.restore.intro.RestoreWalletFragment
import com.asfoundation.wallet.restore.intro.RestoreWalletModule
import com.asfoundation.wallet.restore.password.RestoreWalletPasswordFragment
import com.asfoundation.wallet.restore.password.RestoreWalletPasswordModule
import com.asfoundation.wallet.topup.TopUpFragment
import com.asfoundation.wallet.topup.TopUpSuccessFragment
import com.asfoundation.wallet.topup.address.BillingAddressTopUpFragment
import com.asfoundation.wallet.topup.address.BillingAddressTopUpModule
import com.asfoundation.wallet.topup.adyen.AdyenTopUpFragment
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentFragment
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentModule
import com.asfoundation.wallet.ui.AuthenticationErrorFragment
import com.asfoundation.wallet.ui.airdrop.AirdropFragment
import com.asfoundation.wallet.ui.backup.creation.BackupCreationFragment
import com.asfoundation.wallet.ui.backup.creation.BackupCreationModule
import com.asfoundation.wallet.ui.backup.entry.BackupWalletFragment
import com.asfoundation.wallet.ui.backup.entry.BackupWalletModule
import com.asfoundation.wallet.ui.backup.success.BackupSuccessFragment
import com.asfoundation.wallet.ui.backup.success.BackupSuccessModule
import com.asfoundation.wallet.ui.balance.BalanceFragment
import com.asfoundation.wallet.ui.gamification.GamificationFragment
import com.asfoundation.wallet.ui.iab.*
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentFragment
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentModule
import com.asfoundation.wallet.ui.iab.payments.carrier.confirm.CarrierFeeFragment
import com.asfoundation.wallet.ui.iab.payments.carrier.confirm.CarrierFeeModule
import com.asfoundation.wallet.ui.iab.payments.carrier.status.CarrierPaymentFragment
import com.asfoundation.wallet.ui.iab.payments.carrier.status.CarrierPaymentModule
import com.asfoundation.wallet.ui.iab.payments.carrier.verify.CarrierVerifyFragment
import com.asfoundation.wallet.ui.iab.payments.carrier.verify.CarrierVerifyModule
import com.asfoundation.wallet.ui.iab.payments.common.error.IabErrorFragment
import com.asfoundation.wallet.ui.iab.payments.common.error.IabErrorModule
import com.asfoundation.wallet.ui.iab.share.SharePaymentLinkFragment
import com.asfoundation.wallet.ui.overlay.OverlayFragment
import com.asfoundation.wallet.ui.overlay.OverlayModule
import com.asfoundation.wallet.ui.settings.entry.SettingsFragment
import com.asfoundation.wallet.ui.settings.entry.SettingsModule
import com.asfoundation.wallet.ui.settings.wallets.SettingsWalletsFragment
import com.asfoundation.wallet.ui.settings.wallets.SettingsWalletsModule
import com.asfoundation.wallet.ui.settings.wallets.bottomsheet.SettingsWalletsBottomSheetFragment
import com.asfoundation.wallet.ui.settings.wallets.bottomsheet.SettingsWalletsBottomSheetModule
import com.asfoundation.wallet.ui.transact.AppcoinsCreditsTransferSuccessFragment
import com.asfoundation.wallet.ui.transact.TransferFragment
import com.asfoundation.wallet.ui.transact.TransferFragmentModule
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

  @ContributesAndroidInjector(modules = [TransferFragmentModule::class])
  abstract fun bindTransactFragment(): TransferFragment

  @ContributesAndroidInjector
  abstract fun bindAppcoinsCreditsTransactSuccessFragment(): AppcoinsCreditsTransferSuccessFragment

  @ContributesAndroidInjector
  abstract fun bindTopUpFragment(): TopUpFragment

  @ContributesAndroidInjector
  abstract fun bindTopUpSuccessFragment(): TopUpSuccessFragment

  @ContributesAndroidInjector
  abstract fun bindSharePaymentLinkFragment(): SharePaymentLinkFragment

  @ContributesAndroidInjector(modules = [LocalPaymentModule::class])
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
  @ContributesAndroidInjector(modules = [RestoreWalletModule::class])
  abstract fun bindRestoreWalletFragment(): RestoreWalletFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [RestoreWalletPasswordModule::class])
  abstract fun bindRestoreWalletPasswordFragment(): RestoreWalletPasswordFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [BackupWalletModule::class])
  abstract fun bindBackupWalletFragment(): BackupWalletFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [BackupCreationModule::class])
  abstract fun bindBackupCreationFragment(): BackupCreationFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [BackupSuccessModule::class])
  abstract fun bindBackupSuccessFragment(): BackupSuccessFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [SettingsModule::class])
  abstract fun bindSettingsFragment(): SettingsFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [SettingsWalletsModule::class])
  abstract fun bindSettingsWalletsFragment(): SettingsWalletsFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [SettingsWalletsBottomSheetModule::class])
  abstract fun bindSettingsBottomSheetFragment(): SettingsWalletsBottomSheetFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindGamificationFragment(): GamificationFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [LocalTopUpPaymentModule::class])
  abstract fun bindLocalTopUpPaymentFragment(): LocalTopUpPaymentFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [BillingAddressModule::class])
  abstract fun bindBillingAddressFragment(): BillingAddressFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [BillingAddressTopUpModule::class])
  abstract fun bindBillingAddressTopUpFragment(): BillingAddressTopUpFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindAuthenticationErrorFragment(): AuthenticationErrorFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [CarrierVerifyModule::class])
  abstract fun bindCarrierVerifyFragment(): CarrierVerifyFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [CarrierFeeModule::class])
  abstract fun bindCarrierFeeFragment(): CarrierFeeFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [CarrierPaymentModule::class])
  abstract fun bindCarrierPaymentStatusFragment(): CarrierPaymentFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [IabErrorModule::class])
  abstract fun bindIabErrorFragment(): IabErrorFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [OverlayModule::class])
  abstract fun bindOverlayFragment(): OverlayFragment
}