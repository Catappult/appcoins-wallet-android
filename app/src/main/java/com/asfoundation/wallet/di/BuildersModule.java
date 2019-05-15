package com.asfoundation.wallet.di;

import com.asfoundation.wallet.advertise.WalletPoAService;
import com.asfoundation.wallet.permissions.manage.view.PermissionsListFragment;
import com.asfoundation.wallet.permissions.request.view.CreateWalletFragment;
import com.asfoundation.wallet.permissions.request.view.PermissionFragment;
import com.asfoundation.wallet.permissions.request.view.PermissionsActivity;
import com.asfoundation.wallet.topup.TopUpActivity;
import com.asfoundation.wallet.topup.TopUpFragment;
import com.asfoundation.wallet.topup.TopUpSuccessFragment;
import com.asfoundation.wallet.topup.payment.PaymentAuthFragment;
import com.asfoundation.wallet.ui.AddTokenActivity;
import com.asfoundation.wallet.ui.ConfirmationActivity;
import com.asfoundation.wallet.ui.Erc681Receiver;
import com.asfoundation.wallet.ui.GasSettingsActivity;
import com.asfoundation.wallet.ui.ImportWalletActivity;
import com.asfoundation.wallet.ui.MyAddressActivity;
import com.asfoundation.wallet.ui.OneStepPaymentReceiver;
import com.asfoundation.wallet.ui.SendActivity;
import com.asfoundation.wallet.ui.SettingsActivity;
import com.asfoundation.wallet.ui.SplashActivity;
import com.asfoundation.wallet.ui.TokenChangeCollectionActivity;
import com.asfoundation.wallet.ui.TokensActivity;
import com.asfoundation.wallet.ui.TransactionDetailActivity;
import com.asfoundation.wallet.ui.TransactionsActivity;
import com.asfoundation.wallet.ui.WalletsActivity;
import com.asfoundation.wallet.ui.airdrop.AirdropFragment;
import com.asfoundation.wallet.ui.gamification.HowItWorksFragment;
import com.asfoundation.wallet.ui.gamification.MyLevelFragment;
import com.asfoundation.wallet.ui.iab.AdyenAuthorizationFragment;
import com.asfoundation.wallet.ui.iab.AppcoinsRewardsBuyFragment;
import com.asfoundation.wallet.ui.iab.BillingWebViewFragment;
import com.asfoundation.wallet.ui.iab.ExpressCheckoutBuyFragment;
import com.asfoundation.wallet.ui.iab.IabActivity;
import com.asfoundation.wallet.ui.iab.OnChainBuyFragment;
import com.asfoundation.wallet.ui.iab.PaymentMethodsFragment;
import com.asfoundation.wallet.ui.iab.WebViewActivity;
import com.asfoundation.wallet.ui.iab.share.SharePaymentLinkFragment;
import com.asfoundation.wallet.ui.onboarding.OnboardingActivity;
import com.asfoundation.wallet.ui.transact.AppcoinsCreditsTransferSuccessFragment;
import com.asfoundation.wallet.ui.transact.TransferFragment;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module public abstract class BuildersModule {
  @ActivityScope @ContributesAndroidInjector(modules = SplashModule.class)
  abstract SplashActivity bindSplashModule();

  @ActivityScope @ContributesAndroidInjector(modules = AccountsManageModule.class)
  abstract WalletsActivity bindManageWalletsModule();

  @ActivityScope @ContributesAndroidInjector abstract ImportWalletActivity bindImportWalletModule();

  @ActivityScope @ContributesAndroidInjector(modules = TransactionsModule.class)
  abstract TransactionsActivity bindTransactionsModule();

  @ActivityScope @ContributesAndroidInjector(modules = TransactionDetailModule.class)
  abstract TransactionDetailActivity bindTransactionDetailModule();

  @ActivityScope @ContributesAndroidInjector(modules = SettingsModule.class)
  abstract SettingsActivity bindSettingsModule();

  @ActivityScope @ContributesAndroidInjector(modules = SendModule.class)
  abstract SendActivity bindSendModule();

  @ActivityScope @ContributesAndroidInjector(modules = MyAddressModule.class)
  abstract MyAddressActivity bindMyAddressModule();

  @ActivityScope @ContributesAndroidInjector abstract PermissionsActivity bindPermissionsActivity();

  @ActivityScope @ContributesAndroidInjector(modules = ConfirmationModule.class)
  abstract ConfirmationActivity bindConfirmationModule();

  @ActivityScope @ContributesAndroidInjector(modules = ConfirmationModule.class)
  abstract IabActivity bindIabModule();

  @ActivityScope @ContributesAndroidInjector(modules = TokensModule.class)
  abstract TokensActivity bindTokensModule();

  @ActivityScope @ContributesAndroidInjector(modules = GasSettingsModule.class)
  abstract GasSettingsActivity bindGasSettingsModule();

  @ActivityScope @ContributesAndroidInjector(modules = AddTokenModule.class)
  abstract AddTokenActivity bindAddTokenActivity();

  @ActivityScope @ContributesAndroidInjector(modules = ChangeTokenModule.class)
  abstract TokenChangeCollectionActivity bindChangeTokenCollectionActivity();

  @ActivityScope @ContributesAndroidInjector(modules = ConfirmationModule.class)
  abstract Erc681Receiver bindErc681Receiver();

  @ActivityScope @ContributesAndroidInjector(modules = ConfirmationModule.class)
  abstract OneStepPaymentReceiver bindOneStepPaymentReceiver();

  @ActivityScope @ContributesAndroidInjector abstract TopUpActivity bindTopUpActivity();

  @ActivityScope @ContributesAndroidInjector abstract OnboardingActivity bindOnboardingModule();

  @ContributesAndroidInjector() abstract WalletPoAService bindWalletPoAService();

  @ContributesAndroidInjector() abstract AirdropFragment bindAirdropFragment();

  @ContributesAndroidInjector() abstract OnChainBuyFragment bindRegularBuyFragment();

  @ContributesAndroidInjector() abstract HowItWorksFragment bindHowItWorksFragment();

  @ContributesAndroidInjector() abstract MyLevelFragment bindMyLevelFragment();

  @ContributesAndroidInjector()
  abstract ExpressCheckoutBuyFragment bindExpressCheckoutBuyFragment();

  @ContributesAndroidInjector()
  abstract AdyenAuthorizationFragment bindCreditCardAuthorizationFragment();

  @ContributesAndroidInjector() abstract BillingWebViewFragment bindWebViewFragment();

  @ContributesAndroidInjector() abstract WebViewActivity bindWebViewActivity();

  @ContributesAndroidInjector()
  abstract AppcoinsRewardsBuyFragment bindAppcoinsRewardsBuyFragment();

  @ContributesAndroidInjector() abstract PaymentMethodsFragment bindPaymentMethodsFragment();

  @ContributesAndroidInjector() abstract PermissionFragment bindPermissionFragment();

  @ContributesAndroidInjector() abstract CreateWalletFragment bindCreateWalletFragment();

  @ContributesAndroidInjector() abstract PermissionsListFragment bindPermissionsListFragment();

  @ContributesAndroidInjector(modules = ConfirmationModule.class)
  abstract TransferFragment bindTransactFragment();

  @ContributesAndroidInjector()
  abstract AppcoinsCreditsTransferSuccessFragment bindAppcoinsCreditsTransactSuccessFragment();

  @ContributesAndroidInjector() abstract TopUpFragment bindTopUpFragment();

  @ContributesAndroidInjector() abstract PaymentAuthFragment bindPaymentAuthFragment();

  @ContributesAndroidInjector() abstract TopUpSuccessFragment bindTopUpSuccessFragment();

  @ContributesAndroidInjector() abstract SharePaymentLinkFragment bindSharePaymentLinkFragment();

}
