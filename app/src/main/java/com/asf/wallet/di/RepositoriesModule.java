package com.asf.wallet.di;

import android.content.Context;
import com.asf.wallet.repository.EthereumNetworkRepository;
import com.asf.wallet.repository.EthereumNetworkRepositoryType;
import com.asf.wallet.repository.GasSettingsRepository;
import com.asf.wallet.repository.GasSettingsRepositoryType;
import com.asf.wallet.repository.PreferenceRepositoryType;
import com.asf.wallet.repository.SharedPreferenceRepository;
import com.asf.wallet.repository.TokenLocalSource;
import com.asf.wallet.repository.TokenRepository;
import com.asf.wallet.repository.TokenRepositoryType;
import com.asf.wallet.repository.TokensRealmSource;
import com.asf.wallet.repository.TransactionLocalSource;
import com.asf.wallet.repository.TransactionRepository;
import com.asf.wallet.repository.TransactionRepositoryType;
import com.asf.wallet.repository.TransactionsRealmCache;
import com.asf.wallet.repository.WalletRepository;
import com.asf.wallet.repository.WalletRepositoryType;
import com.asf.wallet.service.AccountKeystoreService;
import com.asf.wallet.service.EthplorerTokenService;
import com.asf.wallet.service.GethKeystoreAccountService;
import com.asf.wallet.service.RealmManager;
import com.asf.wallet.service.TickerService;
import com.asf.wallet.service.TokenExplorerClientType;
import com.asf.wallet.service.TransactionsNetworkClient;
import com.asf.wallet.service.TransactionsNetworkClientType;
import com.asf.wallet.service.TrustWalletTickerService;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import java.io.File;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;

@Module public class RepositoriesModule {
  @Singleton @Provides PreferenceRepositoryType providePreferenceRepository(Context context) {
    return new SharedPreferenceRepository(context);
  }

  @Singleton @Provides AccountKeystoreService provideAccountKeyStoreService(Context context) {
    File file = new File(context.getFilesDir(), "keystore/keystore");
    return new GethKeystoreAccountService(file);
  }

  @Singleton @Provides TickerService provideTickerService(OkHttpClient httpClient, Gson gson) {
    return new TrustWalletTickerService(httpClient, gson);
  }

  @Singleton @Provides EthereumNetworkRepositoryType provideEthereumNetworkRepository(
      PreferenceRepositoryType preferenceRepository, TickerService tickerService) {
    return new EthereumNetworkRepository(preferenceRepository, tickerService);
  }

  @Singleton @Provides WalletRepositoryType provideWalletRepository(OkHttpClient okHttpClient,
      PreferenceRepositoryType preferenceRepositoryType,
      AccountKeystoreService accountKeystoreService,
      EthereumNetworkRepositoryType networkRepository) {
    return new WalletRepository(okHttpClient, preferenceRepositoryType, accountKeystoreService,
        networkRepository);
  }

  @Singleton @Provides TransactionRepositoryType provideTransactionRepository(
      EthereumNetworkRepositoryType networkRepository,
      AccountKeystoreService accountKeystoreService,
      TransactionsNetworkClientType blockExplorerClient, TransactionLocalSource inDiskCache) {
    return new TransactionRepository(networkRepository, accountKeystoreService, inDiskCache,
        blockExplorerClient);
  }

  @Singleton @Provides TransactionLocalSource provideTransactionInDiskCache(
      RealmManager realmManager) {
    return new TransactionsRealmCache(realmManager);
  }

  @Singleton @Provides TransactionsNetworkClientType provideBlockExplorerClient(
      OkHttpClient httpClient, Gson gson, EthereumNetworkRepositoryType ethereumNetworkRepository) {
    return new TransactionsNetworkClient(httpClient, gson, ethereumNetworkRepository);
  }

  @Singleton @Provides TokenRepositoryType provideTokenRepository(OkHttpClient okHttpClient,
      EthereumNetworkRepositoryType ethereumNetworkRepository,
      WalletRepositoryType walletRepository, TokenExplorerClientType tokenExplorerClientType,
      TokenLocalSource tokenLocalSource, TransactionLocalSource inDiskCache,
      TickerService tickerService) {
    return new TokenRepository(okHttpClient, ethereumNetworkRepository, walletRepository,
        tokenExplorerClientType, tokenLocalSource, inDiskCache, tickerService);
  }

  @Singleton @Provides TokenExplorerClientType provideTokenService(OkHttpClient okHttpClient,
      Gson gson) {
    return new EthplorerTokenService(okHttpClient, gson);
  }

  @Singleton @Provides TokenLocalSource provideRealmTokenSource(RealmManager realmManager) {
    return new TokensRealmSource(realmManager);
  }

  @Singleton @Provides GasSettingsRepositoryType provideGasSettingsRepository(
      EthereumNetworkRepositoryType ethereumNetworkRepository) {
    return new GasSettingsRepository(ethereumNetworkRepository);
  }
}
