package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.FetchTransactionsInteract;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.GetDefaultWalletBalance;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.OffChainTransactions;
import com.asfoundation.wallet.repository.OffChainTransactionsRepository;
import com.asfoundation.wallet.repository.TokenLocalSource;
import com.asfoundation.wallet.repository.TokenRepository;
import com.asfoundation.wallet.repository.TransactionLocalSource;
import com.asfoundation.wallet.repository.TransactionRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.router.AirdropRouter;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.router.ManageWalletsRouter;
import com.asfoundation.wallet.router.MyAddressRouter;
import com.asfoundation.wallet.router.MyTokensRouter;
import com.asfoundation.wallet.router.SendRouter;
import com.asfoundation.wallet.router.SettingsRouter;
import com.asfoundation.wallet.router.TransactionDetailRouter;
import com.asfoundation.wallet.service.TickerService;
import com.asfoundation.wallet.service.TokenExplorerClientType;
import com.asfoundation.wallet.transactions.TransactionsMapper;
import com.asfoundation.wallet.ui.AppcoinsApps;
import com.asfoundation.wallet.ui.MicroRaidenInteractor;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver;
import com.asfoundation.wallet.viewmodel.TransactionsViewModelFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Module class TransactionsModule {
  @Provides TransactionsViewModelFactory provideTransactionsViewModelFactory(
      FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      FetchTransactionsInteract fetchTransactionsInteract, ManageWalletsRouter manageWalletsRouter,
      SettingsRouter settingsRouter, SendRouter sendRouter,
      TransactionDetailRouter transactionDetailRouter, MyAddressRouter myAddressRouter,
      MyTokensRouter myTokensRouter, ExternalBrowserRouter externalBrowserRouter,
      DefaultTokenProvider defaultTokenProvider, GetDefaultWalletBalance getDefaultWalletBalance,
      TransactionsMapper transactionsMapper, AirdropRouter airdropRouter,
      MicroRaidenInteractor microRaidenInteractor, AppcoinsApps applications,
      OffChainTransactions offChainTransactions) {
    return new TransactionsViewModelFactory(findDefaultNetworkInteract, findDefaultWalletInteract,
        fetchTransactionsInteract, manageWalletsRouter, settingsRouter, sendRouter,
        transactionDetailRouter, myAddressRouter, myTokensRouter, externalBrowserRouter,
        defaultTokenProvider, getDefaultWalletBalance, transactionsMapper, airdropRouter,
        microRaidenInteractor, applications, offChainTransactions);
  }

  @Provides FetchTransactionsInteract provideFetchTransactionsInteract(
      TransactionRepositoryType transactionRepository) {
    return new FetchTransactionsInteract(transactionRepository);
  }

  @Provides ManageWalletsRouter provideManageWalletsRouter() {
    return new ManageWalletsRouter();
  }

  @Provides SettingsRouter provideSettingsRouter() {
    return new SettingsRouter();
  }

  @Provides SendRouter provideSendRouter() {
    return new SendRouter();
  }

  @Provides TransactionDetailRouter provideTransactionDetailRouter() {
    return new TransactionDetailRouter();
  }

  @Provides MyAddressRouter provideMyAddressRouter() {
    return new MyAddressRouter();
  }

  @Provides MyTokensRouter provideMyTokensRouter() {
    return new MyTokensRouter();
  }

  @Provides ExternalBrowserRouter provideExternalBrowserRouter() {
    return new ExternalBrowserRouter();
  }

  @Provides TokenRepository provideTokenRepository(OkHttpClient okHttpClient,
      EthereumNetworkRepositoryType ethereumNetworkRepository,
      WalletRepositoryType walletRepository, TokenExplorerClientType tokenExplorerClientType,
      TokenLocalSource tokenLocalSource, TransactionLocalSource inDiskCache,
      TickerService tickerService) {
    return new TokenRepository(okHttpClient, ethereumNetworkRepository, walletRepository,
        tokenExplorerClientType, tokenLocalSource, inDiskCache, tickerService);
  }

  @Provides TransactionsMapper provideTransactionsMapper(DefaultTokenProvider defaultTokenProvider,
      AppcoinsOperationsDataSaver operationsDataSaver) {
    return new TransactionsMapper(defaultTokenProvider, operationsDataSaver, Schedulers.io());
  }

  @Provides AirdropRouter provideAirdropRouter() {
    return new AirdropRouter();
  }

  @Provides OffChainTransactionsRepository providesOffChainTransactionsRepository(
      OkHttpClient client) {

    ObjectMapper objectMapper = new ObjectMapper();

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    objectMapper.setDateFormat(df);

    Retrofit retrofit =
        new Retrofit.Builder().addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .client(client)
            .baseUrl("http://52.209.250.255/")
            .build();

    return new OffChainTransactionsRepository(
        retrofit.create(OffChainTransactionsRepository.TransactionsApi.class));
  }

  @Provides OffChainTransactions providesOffChainTransactions(
      OffChainTransactionsRepository repository, TransactionsMapper mapper,
      FindDefaultWalletInteract walletFinder) {
    return new OffChainTransactions(repository, mapper, walletFinder, Schedulers.io());
  }
}
