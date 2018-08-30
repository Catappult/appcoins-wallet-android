package com.asfoundation.wallet.di;

import com.asfoundation.wallet.advertise.WalletPoAService;
import com.asfoundation.wallet.ui.AddTokenActivity;
import com.asfoundation.wallet.ui.ConfirmationActivity;
import com.asfoundation.wallet.ui.Erc681Receiver;
import com.asfoundation.wallet.ui.GasSettingsActivity;
import com.asfoundation.wallet.ui.ImportWalletActivity;
import com.asfoundation.wallet.ui.MyAddressActivity;
import com.asfoundation.wallet.ui.SendActivity;
import com.asfoundation.wallet.ui.SettingsActivity;
import com.asfoundation.wallet.ui.SplashActivity;
import com.asfoundation.wallet.ui.TokenChangeCollectionActivity;
import com.asfoundation.wallet.ui.TokensActivity;
import com.asfoundation.wallet.ui.TransactionDetailActivity;
import com.asfoundation.wallet.ui.TransactionsActivity;
import com.asfoundation.wallet.ui.WalletsActivity;
import com.asfoundation.wallet.ui.airdrop.AirdropFragment;
import com.asfoundation.wallet.ui.iab.CreditCardAuthorizationFragment;
import com.asfoundation.wallet.ui.iab.ExpressCheckoutBuyFragment;
import com.asfoundation.wallet.ui.iab.IabActivity;
import com.asfoundation.wallet.ui.iab.OnChainBuyFragment;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module public abstract class BuildersModule {
  @ActivityScope @ContributesAndroidInjector(modules = SplashModule.class)
  abstract SplashActivity bindSplashModule();

  @ActivityScope @ContributesAndroidInjector(modules = AccountsManageModule.class)
  abstract WalletsActivity bindManageWalletsModule();

  @ActivityScope @ContributesAndroidInjector
  abstract ImportWalletActivity bindImportWalletModule();

  @ActivityScope @ContributesAndroidInjector(modules = TransactionsModule.class)
  abstract TransactionsActivity bindTransactionsModule();

  @ActivityScope @ContributesAndroidInjector(modules = TransactionDetailModule.class)
  abstract TransactionDetailActivity bindTransactionDetailModule();

  @ActivityScope @ContributesAndroidInjector(modules = SettingsModule.class)
  abstract SettingsActivity bindSettingsModule();

  @ActivityScope @ContributesAndroidInjector(modules = SendModule.class)
  abstract SendActivity bindSendModule();

  @ActivityScope @ContributesAndroidInjector(modules = ConfirmationModule.class)
  abstract ConfirmationActivity bindConfirmationModule();

  @ActivityScope @ContributesAndroidInjector(modules = ConfirmationModule.class)
  abstract IabActivity bindIabModule();

  @ContributesAndroidInjector abstract MyAddressActivity bindMyAddressModule();

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

  @ContributesAndroidInjector() abstract WalletPoAService bindWalletPoAService();

  @ContributesAndroidInjector() abstract AirdropFragment bindAirdropFragment();

  @ContributesAndroidInjector() abstract OnChainBuyFragment bindRegularBuyFragment();

  @ContributesAndroidInjector()
  abstract ExpressCheckoutBuyFragment bindExpressCheckoutBuyFragment();

  @ContributesAndroidInjector()
  abstract CreditCardAuthorizationFragment bindCreditCardAuthorizationFragment();

}
