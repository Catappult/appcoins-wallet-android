package com.asfoundation.wallet.di;

import android.content.Context;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.analytics.RakamAnalyticsSetup;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.repository.NotTrackTransactionService;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.repository.SmsValidationRepository;
import com.asfoundation.wallet.repository.SmsValidationRepositoryType;
import com.asfoundation.wallet.repository.TokenRepository;
import com.asfoundation.wallet.repository.TokenRepositoryType;
import com.asfoundation.wallet.repository.TrackPendingTransactionService;
import com.asfoundation.wallet.repository.TrackTransactionService;
import com.asfoundation.wallet.repository.WalletRepository;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.repository.Web3jProvider;
import com.asfoundation.wallet.repository.Web3jService;
import com.asfoundation.wallet.service.AccountKeystoreService;
import com.asfoundation.wallet.service.KeyStoreFileManager;
import com.asfoundation.wallet.service.SmsValidationApi;
import com.asfoundation.wallet.service.WalletBalanceService;
import com.asfoundation.wallet.service.Web3jKeystoreAccountService;
import com.asfoundation.wallet.wallet_blocked.WalletStatusApi;
import com.asfoundation.wallet.wallet_blocked.WalletStatusRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.asfoundation.wallet.C.ETHEREUM_NETWORK_NAME;
import static com.asfoundation.wallet.C.ETH_SYMBOL;
import static com.asfoundation.wallet.C.ROPSTEN_NETWORK_NAME;

@Module public class RepositoriesModule {

  @Singleton @Provides AccountKeystoreService provideAccountKeyStoreService(Context context) {
    File file = new File(context.getFilesDir(), "keystore/keystore");
    return new Web3jKeystoreAccountService(
        new KeyStoreFileManager(file.getAbsolutePath(), new ObjectMapper()), Schedulers.io(),
        new ObjectMapper());
  }

  @Singleton @Provides WalletRepositoryType provideWalletRepository(
      PreferencesRepositoryType preferencesRepositoryType,
      AccountKeystoreService accountKeystoreService, WalletBalanceService walletBalanceService, RakamAnalyticsSetup analyticsSetup) {
    return new WalletRepository(preferencesRepositoryType, accountKeystoreService,
        walletBalanceService, Schedulers.io(), analyticsSetup);
  }

  @Singleton @Provides Web3jService providesWeb3jService(Web3jProvider web3jProvider) {
    return new Web3jService(web3jProvider);
  }

  @Singleton @Provides Web3jProvider providesWeb3jProvider(OkHttpClient client,
      NetworkInfo networkInfo) {
    return new Web3jProvider(client, networkInfo);
  }

  @Singleton @Provides NetworkInfo providesDefaultNetwork() {
    if (BuildConfig.DEBUG) {
      return new com.asfoundation.wallet.entity.NetworkInfo(ROPSTEN_NETWORK_NAME, ETH_SYMBOL,
          "https://ropsten.infura.io/v3/df5b41e6a3a44d9dbf9142fa3f58cabc",
          "https://ropsten.trustwalletapp.com/", "https://ropsten.etherscan.io/tx/", 3, false);
    } else {
      return new com.asfoundation.wallet.entity.NetworkInfo(ETHEREUM_NETWORK_NAME, ETH_SYMBOL,
          "https://mainnet.infura.io/v3/df5b41e6a3a44d9dbf9142fa3f58cabc",
          "https://api.trustwalletapp.com/", "https://etherscan.io/tx/", 1, true);
    }
  }

  @Singleton @Provides PendingTransactionService providesPendingTransactionService(
      Web3jService web3jService) {
    return new PendingTransactionService(web3jService, Schedulers.computation(), 5);
  }

  @Singleton @Provides @Named("wait_pending_transaction")
  TrackTransactionService providesWaitPendingTransactionTrackTransactionService(
      PendingTransactionService pendingTransactionService) {
    return new TrackPendingTransactionService(pendingTransactionService);
  }

  @Singleton @Provides @Named("no_wait_transaction")
  TrackTransactionService providesNoWaitTransactionTransactionTrackTransactionService() {
    return new NotTrackTransactionService();
  }

  @Singleton @Provides TokenRepositoryType provideTokenRepository(
      DefaultTokenProvider defaultTokenProvider, WalletRepositoryType walletRepositoryType) {
    return new TokenRepository(defaultTokenProvider, walletRepositoryType);
  }

  @Singleton @Provides SmsValidationRepositoryType provideSmsValidationRepository(
      SmsValidationApi smsValidationApi, Gson gson) {
    return new SmsValidationRepository(smsValidationApi, gson);
  }

  @Singleton @Provides WalletStatusRepository provideWalletStatusRepository(
      WalletStatusApi walletStatusApi) {
    return new WalletStatusRepository(walletStatusApi);
  }

  @Singleton @Provides WalletBalanceService provideWalletBalanceService(OkHttpClient client,
      Gson gson) {
    return new Retrofit.Builder().baseUrl(WalletBalanceService.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(WalletBalanceService.class);
  }
}
