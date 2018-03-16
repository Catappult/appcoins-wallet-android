package com.asf.wallet.di;

import android.content.Context;
import com.asf.wallet.App;
import com.asf.wallet.interact.FetchGasSettingsInteract;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.interact.SendTransactionInteract;
import com.asf.wallet.repository.ApproveService;
import com.asf.wallet.repository.BuyService;
import com.asf.wallet.repository.GasSettingsRepositoryType;
import com.asf.wallet.repository.MemoryCache;
import com.asf.wallet.repository.PasswordStore;
import com.asf.wallet.repository.PendingTransactionService;
import com.asf.wallet.repository.TokenRepositoryType;
import com.asf.wallet.repository.TransactionRepositoryType;
import com.asf.wallet.repository.TransactionService;
import com.asf.wallet.repository.TrustPasswordStore;
import com.asf.wallet.repository.WalletRepositoryType;
import com.asf.wallet.router.GasSettingsRouter;
import com.asf.wallet.service.RealmManager;
import com.asf.wallet.util.LogInterceptor;
import com.asf.wallet.util.TransferParser;
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
