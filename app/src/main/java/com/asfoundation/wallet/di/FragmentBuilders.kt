package com.asfoundation.wallet.di

import cm.aptoide.skills.SkillsFragment
import com.asfoundation.wallet.billing.address.BillingAddressFragment
import com.asfoundation.wallet.billing.address.BillingAddressModule
import com.asfoundation.wallet.billing.adyen.AdyenPaymentFragment
import com.asfoundation.wallet.change_currency.ChangeFiatCurrencyFragment
import com.asfoundation.wallet.change_currency.ChangeFiatCurrencyModule
import com.asfoundation.wallet.change_currency.bottom_sheet.ChooseCurrencyBottomSheetFragment
import com.asfoundation.wallet.change_currency.bottom_sheet.ChooseCurrencyBottomSheetModule
import com.asfoundation.wallet.eskills.withdraw.WithdrawFragment
import com.asfoundation.wallet.eskills.withdraw.WithdrawModule
import com.asfoundation.wallet.home.HomeFragment
import com.asfoundation.wallet.home.HomeModule
import com.asfoundation.wallet.my_wallets.change_wallet.ChangeActiveWalletDialogFragment
import com.asfoundation.wallet.my_wallets.change_wallet.ChangeActiveWalletDialogModule
import com.asfoundation.wallet.my_wallets.create_wallet.CreateWalletDialogFragment
import com.asfoundation.wallet.my_wallets.create_wallet.CreateWalletDialogModule
import com.asfoundation.wallet.my_wallets.main.MyWalletsFragment
import com.asfoundation.wallet.my_wallets.main.MyWalletsModule
import com.asfoundation.wallet.my_wallets.more.MoreDialogFragment
import com.asfoundation.wallet.my_wallets.more.MoreDialogModule
import com.asfoundation.wallet.my_wallets.token.TokenInfoDialogFragment
import com.asfoundation.wallet.my_wallets.token.TokenInfoDialogModule
import com.asfoundation.wallet.my_wallets.verify_picker.VerifyPickerDialogFragment
import com.asfoundation.wallet.my_wallets.verify_picker.VerifyPickerDialogModule
import com.asfoundation.wallet.nfts.ui.nftdetails.NFTDetailsFragment
import com.asfoundation.wallet.nfts.ui.nftdetails.NFTDetailsModule
import com.asfoundation.wallet.nfts.ui.nftlist.NFTFragment
import com.asfoundation.wallet.nfts.ui.nftlist.NFTModule
import com.asfoundation.wallet.onboarding.OnboardingFragment
import com.asfoundation.wallet.onboarding.OnboardingModule
import com.asfoundation.wallet.onboarding.bottom_sheet.TermsConditionsBottomSheetFragment
import com.asfoundation.wallet.onboarding.bottom_sheet.TermsConditionsBottomSheetModule
import com.asfoundation.wallet.permissions.manage.view.PermissionsListFragment
import com.asfoundation.wallet.permissions.request.view.CreateWalletFragment
import com.asfoundation.wallet.permissions.request.view.PermissionFragment
import com.asfoundation.wallet.promo_code.bottom_sheet.PromoCodeBottomSheetFragment
import com.asfoundation.wallet.promo_code.bottom_sheet.PromoCodeBottomSheetModule
import com.asfoundation.wallet.promotions.info.PromotionsInfoDialogFragment
import com.asfoundation.wallet.promotions.ui.PromotionsFragment
import com.asfoundation.wallet.promotions.ui.PromotionsModuleNew
import com.asfoundation.wallet.rating.entry.RatingEntryFragment
import com.asfoundation.wallet.rating.entry.RatingEntryModule
import com.asfoundation.wallet.rating.finish.RatingFinishFragment
import com.asfoundation.wallet.rating.finish.RatingFinishModule
import com.asfoundation.wallet.rating.negative.RatingNegativeFragment
import com.asfoundation.wallet.rating.negative.RatingNegativeModule
import com.asfoundation.wallet.rating.positive.RatingPositiveFragment
import com.asfoundation.wallet.rating.positive.RatingPositiveModule
import com.asfoundation.wallet.referrals.InviteFriendsFragment
import com.asfoundation.wallet.referrals.InviteFriendsVerificationFragment
import com.asfoundation.wallet.referrals.ReferralsFragment
import com.asfoundation.wallet.restore.intro.RestoreWalletFragment
import com.asfoundation.wallet.restore.intro.RestoreWalletModule
import com.asfoundation.wallet.restore.password.RestoreWalletPasswordFragment
import com.asfoundation.wallet.restore.password.RestoreWalletPasswordModule
import com.asfoundation.wallet.skills.SkillsModule
import com.asfoundation.wallet.subscriptions.cancel.SubscriptionCancelFragment
import com.asfoundation.wallet.subscriptions.cancel.SubscriptionCancelModule
import com.asfoundation.wallet.subscriptions.details.SubscriptionDetailsFragment
import com.asfoundation.wallet.subscriptions.details.SubscriptionDetailsModule
import com.asfoundation.wallet.subscriptions.list.SubscriptionListFragment
import com.asfoundation.wallet.subscriptions.list.SubscriptionListModule
import com.asfoundation.wallet.subscriptions.success.SubscriptionSuccessFragment
import com.asfoundation.wallet.subscriptions.success.SubscriptionSuccessModule
import com.asfoundation.wallet.topup.TopUpFragment
import com.asfoundation.wallet.topup.TopUpSuccessFragment
import com.asfoundation.wallet.topup.address.BillingAddressTopUpFragment
import com.asfoundation.wallet.topup.address.BillingAddressTopUpModule
import com.asfoundation.wallet.topup.adyen.AdyenTopUpFragment
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentFragment
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentModule
import com.asfoundation.wallet.transfers.EtherTransactionBottomSheetFragment
import com.asfoundation.wallet.transfers.EtherTransactionBottomSheetModule
import com.asfoundation.wallet.ui.AuthenticationErrorFragment
import com.asfoundation.wallet.ui.airdrop.AirdropFragment
import com.asfoundation.wallet.ui.backup.creation.BackupCreationFragment
import com.asfoundation.wallet.ui.backup.creation.BackupCreationModule
import com.asfoundation.wallet.ui.backup.entry.BackupWalletFragment
import com.asfoundation.wallet.ui.backup.entry.BackupWalletModule
import com.asfoundation.wallet.ui.backup.success.BackupSuccessFragment
import com.asfoundation.wallet.ui.backup.success.BackupSuccessModule
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
import com.asfoundation.wallet.ui.wallets.WalletRemoveConfirmationFragment
import com.asfoundation.wallet.verification.ui.credit_card.code.VerificationCodeFragment
import com.asfoundation.wallet.verification.ui.credit_card.code.VerificationCodeModule
import com.asfoundation.wallet.verification.ui.credit_card.error.VerificationErrorFragment
import com.asfoundation.wallet.verification.ui.credit_card.error.VerificationErrorModule
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationIntroFragment
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationIntroModule
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalFragment
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalModule
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

  @FragmentScope
  @ContributesAndroidInjector(modules = [EtherTransactionBottomSheetModule::class])
  abstract fun bindEtherTransactionBottomSheetFragment(): EtherTransactionBottomSheetFragment

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

  @FragmentScope
  @ContributesAndroidInjector(modules = [PromotionsModuleNew::class])
  abstract fun bindPromotionsFragment(): PromotionsFragment

  @FragmentScope
  @ContributesAndroidInjector
  abstract fun bindPromotionsInfoDialogFragment(): PromotionsInfoDialogFragment

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

  @FragmentScope
  @ContributesAndroidInjector(modules = [RatingEntryModule::class])
  abstract fun bindRatingEntryFragment(): RatingEntryFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [RatingNegativeModule::class])
  abstract fun bindRatingSuggestionsFragment(): RatingNegativeFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [RatingPositiveModule::class])
  abstract fun bindRatingThankYouFragment(): RatingPositiveFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [RatingFinishModule::class])
  abstract fun bindRatingFinishFragment(): RatingFinishFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [VerificationIntroModule::class])
  abstract fun bindVerificationIntroFragment(): VerificationIntroFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [VerificationCodeModule::class])
  abstract fun bindVerificationCodeFragment(): VerificationCodeFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [VerificationErrorModule::class])
  abstract fun bindVerificationErrorFragment(): VerificationErrorFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [VerificationPaypalModule::class])
  abstract fun bindVerificationPaypalFragment(): VerificationPaypalFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [SkillsModule::class])
  abstract fun bindSkillsFragment(): SkillsFragment


  @FragmentScope
  @ContributesAndroidInjector(modules = [WithdrawModule::class])
  abstract fun bindWithdrawFragment(): WithdrawFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [HomeModule::class])
  abstract fun bindHomeFragment(): HomeFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [MyWalletsModule::class])
  abstract fun bindNewMyWalletsFragment(): MyWalletsFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [ChangeActiveWalletDialogModule::class])
  abstract fun bindChangeActiveWalletDialogFragment(): ChangeActiveWalletDialogFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [CreateWalletDialogModule::class])
  abstract fun bindCreateWalletDialogFragment(): CreateWalletDialogFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [TokenInfoDialogModule::class])
  abstract fun bindTokenInfoDialogFragment(): TokenInfoDialogFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [MoreDialogModule::class])
  abstract fun bindMoreDialogFragment(): MoreDialogFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [ChangeFiatCurrencyModule::class])
  abstract fun bindChangeFiatCurrencyFragment(): ChangeFiatCurrencyFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [ChooseCurrencyBottomSheetModule::class])
  abstract fun bindChooseCurrencyBottomSheetFragment(): ChooseCurrencyBottomSheetFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [SubscriptionListModule::class])
  abstract fun bindSubscriptionListFragment(): SubscriptionListFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [SubscriptionDetailsModule::class])
  abstract fun bindSubscriptionDetailsFragment(): SubscriptionDetailsFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [SubscriptionCancelModule::class])
  abstract fun bindSubscriptionCancelFragment(): SubscriptionCancelFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [SubscriptionSuccessModule::class])
  abstract fun bindSubscriptionCancelSuccessFragment(): SubscriptionSuccessFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [NFTModule::class])
  abstract fun bindNFTFragment(): NFTFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [NFTDetailsModule::class])
  abstract fun bindNFTDetailsFragment(): NFTDetailsFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [VerifyPickerDialogModule::class])
  abstract fun bindVerifyPickerDialogFragment(): VerifyPickerDialogFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [PromoCodeBottomSheetModule::class])
  abstract fun bindPromoCodeBottomSheetFragment(): PromoCodeBottomSheetFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [OnboardingModule::class])
  abstract fun bindOnboardingFragment(): OnboardingFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [TermsConditionsBottomSheetModule::class])
  abstract fun bindTermsConditionsBottomSheetFragment(): TermsConditionsBottomSheetFragment

}