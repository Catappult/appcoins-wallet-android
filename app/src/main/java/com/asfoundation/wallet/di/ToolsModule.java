package com.asfoundation.wallet.di;

import android.content.Context;
import com.asfoundation.wallet.App;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.repository.ApproveService;
import com.asfoundation.wallet.repository.BuyService;
import com.asfoundation.wallet.repository.EthereumNetworkRepository;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.GasSettingsRepositoryType;
import com.asfoundation.wallet.repository.MemoryCache;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.asfoundation.wallet.repository.PreferenceRepositoryType;
import com.asfoundation.wallet.repository.SharedPreferenceRepository;
import com.asfoundation.wallet.repository.TokenRepositoryType;
import com.asfoundation.wallet.repository.TransactionRepositoryType;
import com.asfoundation.wallet.repository.TransactionService;
import com.asfoundation.wallet.repository.TrustPasswordStore;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.router.GasSettingsRouter;
import com.asfoundation.wallet.service.RealmManager;
import com.asfoundation.wallet.service.TickerService;
import com.asfoundation.wallet.service.TrustWalletTickerService;
import com.asfoundation.wallet.util.LogInterceptor;
import com.asfoundation.wallet.util.TransferParser;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import io.reactivex.subjects.BehaviorSubject;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;

@Module class ToolsModule {
  @Provides Context provideContext(App application) {
    return application.getApplicationContext();
  }

  @Singleton @Provides Gson provideGson() {
    return new Gson();
  }

  @Singleton @Provides OkHttpClient okHttpClient() {
    return new OkHttpClient.Builder().addInterceptor(new LogInterceptor())
        .connectTimeout(15, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.MINUTES)
        .writeTimeout(30, TimeUnit.MINUTES)
        .build();
  }

  @Singleton @Provides EthereumNetworkRepositoryType provideEthereumNetworkRepository(
      PreferenceRepositoryType preferenceRepository, TickerService tickerService) {
    return new EthereumNetworkRepository(preferenceRepository, tickerService);
  }

  @Singleton @Provides PreferenceRepositoryType providePreferenceRepository(Context context) {
    return new SharedPreferenceRepository(context);
  }

  @Singleton @Provides TickerService provideTickerService(OkHttpClient httpClient, Gson gson) {
    return new TrustWalletTickerService(httpClient, gson);
  }

  @Provides AddTokenInteract provideAddTokenInteract(TokenRepositoryType tokenRepository,
      WalletRepositoryType walletRepository) {
    return new AddTokenInteract(walletRepository, tokenRepository);
  }

  @Singleton @Provides PasswordStore passwordStore(Context context) {
    return new TrustPasswordStore(context);
  }

  @Singleton @Provides RealmManager provideRealmManager() {
    return new RealmManager();
  }

  @Provides ApproveService provideApproveService(SendTransactionInteract sendTransactionInteract,
      PendingTransactionService pendingTransactionService) {
    return new ApproveService(sendTransactionInteract, pendingTransactionService,
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()));
  }

  @Provides BuyService provideBuyService(SendTransactionInteract sendTransactionInteract,
      PendingTransactionService pendingTransactionService) {
    return new BuyService(sendTransactionInteract, pendingTransactionService,
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()));
  }

  @Provides GasSettingsRouter provideGasSettingsRouter() {
    return new GasSettingsRouter();
  }

  @Provides FetchGasSettingsInteract provideFetchGasSettingsInteract(
      GasSettingsRepositoryType gasSettingsRepository) {
    return new FetchGasSettingsInteract(gasSettingsRepository);
  }

  @Provides FindDefaultWalletInteract provideFindDefaultWalletInteract(
      WalletRepositoryType walletRepository) {
    return new FindDefaultWalletInteract(walletRepository);
  }

  @Provides SendTransactionInteract provideSendTransactionInteract(
      TransactionRepositoryType transactionRepository, PasswordStore passwordStore) {
    return new SendTransactionInteract(transactionRepository, passwordStore);
  }

  @Singleton @Provides TransactionService provideTransactionService(
      FetchGasSettingsInteract gasSettingsInteract, TransferParser parser,
      FindDefaultWalletInteract defaultWalletInteract, ApproveService approveService,
      BuyService buyService) {
    return new TransactionService(gasSettingsInteract, defaultWalletInteract, parser,
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()), approveService, buyService);
  }

  @Provides TransferParser provideTransferParser(
      FindDefaultWalletInteract provideFindDefaultWalletInteract,
      TokenRepositoryType tokenRepositoryType) {
    return new TransferParser(provideFindDefaultWalletInteract, tokenRepositoryType);
  }
}
