package com.asf.wallet.di;

import com.asf.wallet.ui.AddTokenActivity;
import com.asf.wallet.ui.ConfirmationActivity;
import com.asf.wallet.ui.GasSettingsActivity;
import com.asf.wallet.ui.ImportWalletActivity;
import com.asf.wallet.ui.MyAddressActivity;
import com.asf.wallet.ui.SendActivity;
import com.asf.wallet.ui.SettingsActivity;
import com.asf.wallet.ui.SplashActivity;
import com.asf.wallet.ui.TokenChangeCollectionActivity;
import com.asf.wallet.ui.TokensActivity;
import com.asf.wallet.ui.TransactionDetailActivity;
import com.asf.wallet.ui.TransactionsActivity;
import com.asf.wallet.ui.WalletsActivity;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module public abstract class BuildersModule {
  @ActivityScope @ContributesAndroidInjector(modules = SplashModule.class)
  abstract SplashActivity bindSplashModule();

  @ActivityScope @ContributesAndroidInjector(modules = AccountsManageModule.class)
  abstract WalletsActivity bindManageWalletsModule();

  @ActivityScope @ContributesAndroidInjector(modules = ImportModule.class)
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

  @ContributesAndroidInjector abstract MyAddressActivity bindMyAddressModule();

  @ActivityScope @ContributesAndroidInjector(modules = TokensModule.class)
  abstract TokensActivity bindTokensModule();

  @ActivityScope @ContributesAndroidInjector(modules = GasSettingsModule.class)
  abstract GasSettingsActivity bindGasSettingsModule();

  @ActivityScope @ContributesAndroidInjector(modules = AddTokenModule.class)
  abstract AddTokenActivity bindAddTokenActivity();

  @ActivityScope @ContributesAndroidInjector(modules = ChangeTokenModule.class)
  abstract TokenChangeCollectionActivity bindChangeTokenCollectionActivity();
}
