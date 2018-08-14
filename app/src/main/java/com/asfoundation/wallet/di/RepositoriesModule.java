package com.asfoundation.wallet.di;

import android.content.Context;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.poa.BlockchainErrorMapper;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.NonceGetter;
import com.asfoundation.wallet.repository.NotTrackTransactionService;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.asfoundation.wallet.repository.PreferenceRepositoryType;
import com.asfoundation.wallet.repository.TokenLocalSource;
import com.asfoundation.wallet.repository.TokenRepository;
import com.asfoundation.wallet.repository.TokenRepositoryType;
import com.asfoundation.wallet.repository.TokensRealmSource;
import com.asfoundation.wallet.repository.TrackPendingTransactionService;
import com.asfoundation.wallet.repository.TrackTransactionService;
import com.asfoundation.wallet.repository.TransactionLocalSource;
import com.asfoundation.wallet.repository.TransactionRepository;
import com.asfoundation.wallet.repository.TransactionRepositoryType;
import com.asfoundation.wallet.repository.TransactionsRealmCache;
import com.asfoundation.wallet.repository.WalletRepository;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.repository.Web3jProvider;
import com.asfoundation.wallet.repository.Web3jService;
import com.asfoundation.wallet.service.AccountKeystoreService;
import com.asfoundation.wallet.service.EthplorerTokenService;
import com.asfoundation.wallet.service.GethKeystoreAccountService;
import com.asfoundation.wallet.service.RealmManager;
import com.asfoundation.wallet.service.TickerService;
import com.asfoundation.wallet.service.TokenExplorerClientType;
import com.asfoundation.wallet.service.TransactionsNetworkClient;
import com.asfoundation.wallet.service.TransactionsNetworkClientType;
import com.asfoundation.wallet.ui.iab.raiden.NonceObtainer;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;

@Module public class RepositoriesModule {

  @Singleton @Provides AccountKeystoreService provideAccountKeyStoreService(Context context) {
    File file = new File(context.getFilesDir(), "keystore/keystore");
    return new GethKeystoreAccountService(file);
  }

  @Singleton @Provides WalletRepositoryType provideWalletRepository(OkHttpClient okHttpClient,
      PreferenceRepositoryType preferenceRepositoryType,
      AccountKeystoreService accountKeystoreService,
      EthereumNetworkRepositoryType networkRepository, Web3jProvider web3jProvider) {
    return new WalletRepository(okHttpClient, preferenceRepositoryType, accountKeystoreService,
        networkRepository, web3jProvider);
  }

  @Singleton @Provides Web3jService providesWeb3jService(Web3jProvider web3jProvider) {
    return new Web3jService(web3jProvider);
  }

  @Singleton @Provides Web3jProvider providesWeb3jProvider(
      EthereumNetworkRepositoryType ethereumNetworkRepository, OkHttpClient client) {
    return new Web3jProvider(ethereumNetworkRepository, client);
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

  @Singleton @Provides TransactionRepositoryType provideTransactionRepository(
      EthereumNetworkRepositoryType networkRepository,
      AccountKeystoreService accountKeystoreService,
      TransactionsNetworkClientType blockExplorerClient, TransactionLocalSource inDiskCache,
      DefaultTokenProvider defaultTokenProvider, NonceGetter nonceGetter,
      NonceObtainer nonceObtainer) {
    return new TransactionRepository(networkRepository, accountKeystoreService, inDiskCache,
        blockExplorerClient, defaultTokenProvider, nonceGetter, new BlockchainErrorMapper(),
        nonceObtainer, Schedulers.io());
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
}
