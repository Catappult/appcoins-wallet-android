package com.asfoundation.wallet.di;

import android.content.Context;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.App;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.BuildConfigDefaultTokenProvider;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.poa.BlockChainWriter;
import com.asfoundation.wallet.poa.DataMapper;
import com.asfoundation.wallet.poa.HashCalculator;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.poa.TransactionFactory;
import com.asfoundation.wallet.repository.ApproveService;
import com.asfoundation.wallet.repository.BuyService;
import com.asfoundation.wallet.repository.ErrorMapper;
import com.asfoundation.wallet.repository.EthereumNetworkRepository;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.GasSettingsRepository;
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
import com.asfoundation.wallet.repository.Web3jProvider;
import com.asfoundation.wallet.router.GasSettingsRouter;
import com.asfoundation.wallet.service.AccountKeystoreService;
import com.asfoundation.wallet.service.RealmManager;
import com.asfoundation.wallet.service.TickerService;
import com.asfoundation.wallet.service.TrustWalletTickerService;
import com.asfoundation.wallet.util.LogInterceptor;
import com.asfoundation.wallet.util.TransferParser;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
      PendingTransactionService pendingTransactionService, ErrorMapper errorMapper) {
    return new ApproveService(sendTransactionInteract, pendingTransactionService,
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()), errorMapper);
  }

  @Provides BuyService provideBuyService(SendTransactionInteract sendTransactionInteract,
      PendingTransactionService pendingTransactionService, ErrorMapper errorMapper) {
    return new BuyService(sendTransactionInteract, pendingTransactionService,
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()), errorMapper);
  }

  @Singleton @Provides ErrorMapper provideErrorMapper() {
    return new ErrorMapper();
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

  @Provides FindDefaultNetworkInteract provideFindDefaultNetworkInteract(
      EthereumNetworkRepositoryType ethereumNetworkRepositoryType) {
    return new FindDefaultNetworkInteract(ethereumNetworkRepositoryType);
  }

  @Provides DefaultTokenProvider provideDefaultTokenProvider(
      FindDefaultNetworkInteract defaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract) {
    return new BuildConfigDefaultTokenProvider(defaultNetworkInteract, findDefaultWalletInteract);
  }

  @Singleton @Provides MessageDigest provideMessageDigest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Singleton @Provides GasSettingsRepositoryType provideGasSettingsRepository(
      EthereumNetworkRepositoryType ethereumNetworkRepository) {
    return new GasSettingsRepository(ethereumNetworkRepository);
  }

  @Singleton @Provides DataMapper provideDataMapper() {
    return new DataMapper();
  }

  @Singleton @Provides TransactionFactory provideTransactionFactory(Web3jProvider web3jProvider,
      WalletRepositoryType walletRepository, GasSettingsRepositoryType gasSettings,
      AccountKeystoreService accountKeystoreService, PasswordStore passwordStore,
      DefaultTokenProvider defaultTokenProvider,
      EthereumNetworkRepositoryType ethereumNetworkRepository, DataMapper dataMapper) {

    return new TransactionFactory(web3jProvider, walletRepository, gasSettings,
        accountKeystoreService, passwordStore, defaultTokenProvider, ethereumNetworkRepository,
        dataMapper);
  }

  @Singleton @Provides BlockChainWriter provideBlockChainWriter(Web3jProvider web3jProvider,
      TransactionFactory transactionFactory) {
    return new BlockChainWriter(web3jProvider, transactionFactory);
  }

  @Singleton @Provides HashCalculator provideHashCalculator(Gson gson,
      MessageDigest messageDigest) {
    return new HashCalculator(gson, messageDigest);
  }

  @Singleton @Provides ProofOfAttentionService provideProofOfAttentionService(
      HashCalculator hashCalculator, BlockChainWriter blockChainWriter) {
    return new ProofOfAttentionService(new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()),
        BuildConfig.APPLICATION_ID, hashCalculator, new CompositeDisposable(), blockChainWriter);
  }
}
