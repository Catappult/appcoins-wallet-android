package com.asfoundation.wallet.di

import com.asfoundation.wallet.permissions.request.view.PermissionsActivity
import com.asfoundation.wallet.rating.RatingActivity
import com.asfoundation.wallet.referrals.InviteFriendsActivity
import com.asfoundation.wallet.restore.RestoreWalletActivity
import com.asfoundation.wallet.restore.RestoreWalletActivityModule
import com.asfoundation.wallet.topup.TopUpActivity
import com.asfoundation.wallet.transfers.TransferConfirmationActivity
import com.asfoundation.wallet.ui.*
import com.asfoundation.wallet.ui.backup.BackupActivity
import com.asfoundation.wallet.ui.backup.BackupActivityModule
import com.asfoundation.wallet.ui.balance.QrCodeActivity
import com.asfoundation.wallet.ui.balance.TokenDetailsActivity
import com.asfoundation.wallet.ui.balance.TransactionDetailActivity
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.asfoundation.wallet.ui.onboarding.OnboardingActivity
import com.asfoundation.wallet.ui.onboarding.OnboardingModule
import com.asfoundation.wallet.ui.settings.SettingsActivity
import com.asfoundation.wallet.ui.splash.SplashActivity
import com.asfoundation.wallet.ui.splash.SplashModule
import com.asfoundation.wallet.verification.VerificationActivity
import com.asfoundation.wallet.verification.VerificationActivityModule
import com.asfoundation.wallet.wallet_blocked.WalletBlockedActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilders {

  @ActivityScope
  @ContributesAndroidInjector(modules = [SplashModule::class])
  internal abstract fun bindSplashActivity(): SplashActivity

  @ActivityScope
  @ContributesAndroidInjector
  internal abstract fun bindBaseActivityModule(): BaseActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [TransactionsModule::class])
  internal abstract fun bindTransactionsModule(): TransactionsActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [TransactionDetailModule::class])
  internal abstract fun bindTransactionDetailModule(): TransactionDetailActivity

  @ActivityScope
  @ContributesAndroidInjector
  internal abstract fun bindSettingsModule(): SettingsActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [SendModule::class])
  internal abstract fun bindSendModule(): SendActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [MyAddressModule::class])
  internal abstract fun bindMyAddressModule(): MyAddressActivity

  @ActivityScope
  @ContributesAndroidInjector
  internal abstract fun bindPermissionsActivity(): PermissionsActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [TransferConfirmationModule::class])
  internal abstract fun bindTransferConfirmationModule(): TransferConfirmationActivity

  @ActivityScope
  @ContributesAndroidInjector
  internal abstract fun bindIabModule(): IabActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [GasSettingsModule::class])
  internal abstract fun bindGasSettingsModule(): GasSettingsActivity

  @ActivityScope
  @ContributesAndroidInjector
  internal abstract fun bindTopUpActivity(): TopUpActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [OnboardingModule::class])
  internal abstract fun bindOnboardingActivity(): OnboardingActivity

  @ActivityScope
  @ContributesAndroidInjector
  internal abstract fun bindInviteFriendsActivity(): InviteFriendsActivity

  @ActivityScope
  @ContributesAndroidInjector
  internal abstract fun bindActiveWalletActivity(): QrCodeActivity

  @ContributesAndroidInjector
  internal abstract fun bindWebViewActivity(): WebViewActivity

  @ActivityScope
  @ContributesAndroidInjector
  internal abstract fun bindUpdateRequiredActivity(): UpdateRequiredActivity

  @ContributesAndroidInjector
  internal abstract fun bindTokenDetailsFragment(): TokenDetailsActivity

  @ActivityScope
  @ContributesAndroidInjector
  internal abstract fun bindWalletBlockedActivity(): WalletBlockedActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [RestoreWalletActivityModule::class])
  internal abstract fun bindRestoreWalletActivity(): RestoreWalletActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [BackupActivityModule::class])
  internal abstract fun bindWalletBackupActivity(): BackupActivity

  @ActivityScope
  @ContributesAndroidInjector
  abstract fun bindErc681Receiver(): Erc681Receiver

  @ActivityScope
  @ContributesAndroidInjector
  abstract fun bindOneStepPaymentReceiver(): OneStepPaymentReceiver

  @ActivityScope
  @ContributesAndroidInjector
  internal abstract fun bindAuthenticationPromptActivity(): AuthenticationPromptActivity

  @ActivityScope
  @ContributesAndroidInjector
  abstract fun bindRatingActivity(): RatingActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [VerificationActivityModule::class])
  internal abstract fun bindVerificationActivity(): VerificationActivity
}