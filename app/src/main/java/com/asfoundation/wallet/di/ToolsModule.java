package com.asfoundation.wallet.di;

import android.arch.persistence.room.Room;
import android.content.Context;
import com.appcoins.wallet.billing.BdsBilling;
import com.appcoins.wallet.billing.BillingFactory;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.BillingPaymentProofSubmission;
import com.appcoins.wallet.billing.BillingThrowableCodeMapper;
import com.appcoins.wallet.billing.ProxyService;
import com.appcoins.wallet.billing.WalletService;
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer;
import com.appcoins.wallet.billing.repository.BdsApiResponseMapper;
import com.appcoins.wallet.billing.repository.BdsRepository;
import com.appcoins.wallet.billing.repository.RemoteRepository;
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxyBuilder;
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxySdk;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.Airdrop;
import com.asfoundation.wallet.AirdropService;
import com.asfoundation.wallet.App;
import com.asfoundation.wallet.FabricLogger;
import com.asfoundation.wallet.Logger;
import com.asfoundation.wallet.apps.Applications;
import com.asfoundation.wallet.apps.AppsApi;
import com.asfoundation.wallet.billing.AdyenBilling;
import com.asfoundation.wallet.billing.BDSTransactionService;
import com.asfoundation.wallet.billing.TransactionService;
import com.asfoundation.wallet.billing.payment.Adyen;
import com.asfoundation.wallet.billing.purchase.CreditCardBillingFactory;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.BuildConfigDefaultTokenProvider;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FetchTokensInteract;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.GetDefaultWalletBalance;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.poa.BlockchainErrorMapper;
import com.asfoundation.wallet.poa.Calculator;
import com.asfoundation.wallet.poa.CountryCodeProvider;
import com.asfoundation.wallet.poa.DataMapper;
import com.asfoundation.wallet.poa.HashCalculator;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.poa.ProofWriter;
import com.asfoundation.wallet.poa.TaggedCompositeDisposable;
import com.asfoundation.wallet.poa.TransactionFactory;
import com.asfoundation.wallet.repository.ApproveService;
import com.asfoundation.wallet.repository.ApproveTransactionValidator;
import com.asfoundation.wallet.repository.BalanceService;
import com.asfoundation.wallet.repository.BdsPendingTransactionService;
import com.asfoundation.wallet.repository.BdsTransactionService;
import com.asfoundation.wallet.repository.BlockChainWriter;
import com.asfoundation.wallet.repository.BuyService;
import com.asfoundation.wallet.repository.BuyTransactionValidator;
import com.asfoundation.wallet.repository.ErrorMapper;
import com.asfoundation.wallet.repository.EthereumNetworkRepository;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.ExpressCheckoutBuyService;
import com.asfoundation.wallet.repository.GasSettingsRepository;
import com.asfoundation.wallet.repository.GasSettingsRepositoryType;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.IpCountryCodeProvider;
import com.asfoundation.wallet.repository.MemoryCache;
import com.asfoundation.wallet.repository.NonceGetter;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.asfoundation.wallet.repository.PreferenceRepositoryType;
import com.asfoundation.wallet.repository.SharedPreferenceRepository;
import com.asfoundation.wallet.repository.SignDataStandardNormalizer;
import com.asfoundation.wallet.repository.TokenRepositoryType;
import com.asfoundation.wallet.repository.TrackTransactionService;
import com.asfoundation.wallet.repository.TransactionRepositoryType;
import com.asfoundation.wallet.repository.TrustPasswordStore;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.repository.WatchedTransactionService;
import com.asfoundation.wallet.repository.Web3jProvider;
import com.asfoundation.wallet.router.GasSettingsRouter;
import com.asfoundation.wallet.service.AccountKeystoreService;
import com.asfoundation.wallet.service.AccountWalletService;
import com.asfoundation.wallet.service.RealmManager;
import com.asfoundation.wallet.service.TickerService;
import com.asfoundation.wallet.service.TokenToFiatService;
import com.asfoundation.wallet.service.TrustWalletTickerService;
import com.asfoundation.wallet.ui.AppcoinsApps;
import com.asfoundation.wallet.ui.MicroRaidenInteractor;
import com.asfoundation.wallet.ui.airdrop.AirdropChainIdMapper;
import com.asfoundation.wallet.ui.airdrop.AirdropInteractor;
import com.asfoundation.wallet.ui.airdrop.AppcoinsTransactionService;
import com.asfoundation.wallet.ui.iab.AppCoinsOperationMapper;
import com.asfoundation.wallet.ui.iab.AppCoinsOperationRepository;
import com.asfoundation.wallet.ui.iab.AppInfoProvider;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver;
import com.asfoundation.wallet.ui.iab.ApproveKeyProvider;
import com.asfoundation.wallet.ui.iab.BdsInAppPurchaseInteractor;
import com.asfoundation.wallet.ui.iab.ImageSaver;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.asfoundation.wallet.ui.iab.database.AppCoinsOperationDatabase;
import com.asfoundation.wallet.ui.iab.raiden.AppcoinsRaiden;
import com.asfoundation.wallet.ui.iab.raiden.ChannelService;
import com.asfoundation.wallet.ui.iab.raiden.NonceObtainer;
import com.asfoundation.wallet.ui.iab.raiden.PrivateKeyProvider;
import com.asfoundation.wallet.ui.iab.raiden.Raiden;
import com.asfoundation.wallet.ui.iab.raiden.RaidenFactory;
import com.asfoundation.wallet.ui.iab.raiden.RaidenRepository;
import com.asfoundation.wallet.ui.iab.raiden.Web3jNonceProvider;
import com.asfoundation.wallet.util.LogInterceptor;
import com.asfoundation.wallet.util.TransferParser;
import com.bds.microraidenj.MicroRaidenBDS;
import com.google.gson.Gson;
import com.jakewharton.rxrelay.PublishRelay;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static com.asfoundation.wallet.AirdropService.BASE_URL;

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

  @Singleton @Provides RaidenRepository provideRaidenRepository(
      SharedPreferenceRepository sharedPreferenceRepository) {
    return sharedPreferenceRepository;
  }

  @Singleton @Provides EthereumNetworkRepositoryType provideEthereumNetworkRepository(
      PreferenceRepositoryType preferenceRepository, TickerService tickerService) {
    return new EthereumNetworkRepository(preferenceRepository, tickerService);
  }

  @Singleton @Provides SharedPreferenceRepository providePreferenceRepository(Context context) {
    return new SharedPreferenceRepository(context);
  }

  @Singleton @Provides PreferenceRepositoryType providePreferenceRepositoryType(
      SharedPreferenceRepository sharedPreferenceRepository) {
    return sharedPreferenceRepository;
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

  @Singleton @Provides Logger provideLogger() {
    return new FabricLogger();
  }

  @Singleton @Provides RealmManager provideRealmManager() {
    return new RealmManager();
  }

  @Singleton @Provides BillingPaymentProofSubmission providesBillingPaymentProofSubmission(
      RemoteRepository.BdsApi api, WalletService walletService) {
    return new BillingPaymentProofSubmission.Builder().setApi(api)
        .setWalletService(walletService)
        .build();
  }

  @Provides ApproveService provideApproveService(SendTransactionInteract sendTransactionInteract,
      ErrorMapper errorMapper,
      @Named("no_wait_transaction") TrackTransactionService pendingTransactionService,
      BillingPaymentProofSubmission billingPaymentProofSubmission) {
    return new ApproveService(new WatchedTransactionService(sendTransactionInteract::approve,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), errorMapper,
        Schedulers.io(), pendingTransactionService),
        new ApproveTransactionValidator(sendTransactionInteract, billingPaymentProofSubmission));
  }

  @Provides BuyService provideBuyService(SendTransactionInteract sendTransactionInteract,
      ErrorMapper errorMapper,
      @Named("wait_pending_transaction") TrackTransactionService pendingTransactionService,
      BillingPaymentProofSubmission billingPaymentProofSubmission,
      DefaultTokenProvider defaultTokenProvider, CountryCodeProvider countryCodeProvider,
      DataMapper dataMapper, BillingFactory billingFactory) {
    return new BuyService(new WatchedTransactionService(sendTransactionInteract::buy,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), errorMapper,
        Schedulers.io(), new BdsPendingTransactionService(billingFactory, Schedulers.io(), 5,
        billingPaymentProofSubmission)),
        new BuyTransactionValidator(sendTransactionInteract, billingPaymentProofSubmission,
            defaultTokenProvider), defaultTokenProvider, countryCodeProvider, dataMapper);
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

  @Singleton @Provides InAppPurchaseService provideInAppPurchaseService(
      ApproveService approveService, BuyService buyService, NonceGetter nonceGetter,
      BalanceService balanceService, ErrorMapper errorMapper) {
    return new InAppPurchaseService(new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()),
        approveService, buyService, balanceService, Schedulers.io(), errorMapper);
  }

  @Singleton @Provides InAppPurchaseInteractor provideTransactionInteractor(
      InAppPurchaseService inAppPurchaseService, FindDefaultWalletInteract defaultWalletInteract,
      FetchGasSettingsInteract gasSettingsInteract, TransferParser parser,
      RaidenRepository raidenRepository, ChannelService channelService,
      BillingFactory billingFactory, ExpressCheckoutBuyService expressCheckoutBuyService,
      BdsTransactionService bdsTransactionService) {

    return new InAppPurchaseInteractor(inAppPurchaseService, defaultWalletInteract,
        gasSettingsInteract, new BigDecimal(BuildConfig.PAYMENT_GAS_LIMIT), parser,
        raidenRepository, channelService, new BillingMessagesMapper(), billingFactory,
        new ExternalBillingSerializer(), expressCheckoutBuyService, bdsTransactionService,
        Schedulers.io());
  }

  @Singleton @Provides BdsTransactionService providesBdsTransactionService(
      BillingFactory billingFactory, BillingPaymentProofSubmission billingPaymentProofSubmission) {
    return new BdsTransactionService(Schedulers.io(),
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()), new CompositeDisposable(),
        new BdsPendingTransactionService(billingFactory, Schedulers.io(), 5,
            billingPaymentProofSubmission));
  }

  @Singleton @Provides BdsInAppPurchaseInteractor provideBdsInAppPurchaseInteractor(
      BillingPaymentProofSubmission billingPaymentProofSubmission,
      InAppPurchaseInteractor inAppPurchaseInteractor, BillingFactory billingFactory) {
    return new BdsInAppPurchaseInteractor(inAppPurchaseInteractor, billingPaymentProofSubmission,
        new ApproveKeyProvider(billingFactory));
  }

  @Provides GetDefaultWalletBalance provideGetDefaultWalletBalance(
      WalletRepositoryType walletRepository,
      EthereumNetworkRepositoryType ethereumNetworkRepository,
      FetchTokensInteract fetchTokensInteract, FindDefaultWalletInteract defaultWalletInteract) {
    return new GetDefaultWalletBalance(walletRepository, ethereumNetworkRepository,
        fetchTokensInteract, defaultWalletInteract);
  }

  @Provides FetchTokensInteract provideFetchTokensInteract(TokenRepositoryType tokenRepository,
      DefaultTokenProvider defaultTokenProvider) {
    return new FetchTokensInteract(tokenRepository, defaultTokenProvider);
  }

  @Singleton @Provides MicroRaidenBDS provideMicroRaidenBDS(Web3jProvider web3jProvider,
      GasSettingsRepositoryType gasSettings, NonceObtainer nonceObtainer) {
    return new RaidenFactory(web3jProvider, gasSettings, nonceObtainer).get();
  }

  @Provides Raiden provideRaiden(MicroRaidenBDS raiden, AccountKeystoreService accountKeyService,
      PasswordStore passwordStore) {
    return new AppcoinsRaiden(new PrivateKeyProvider(accountKeyService, passwordStore), raiden);
  }

  @Provides ChannelService provideChannelService(Raiden raiden) {
    return new ChannelService(raiden, new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()),
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()));
  }

  @Provides NonceObtainer provideNonceObtainer(Web3jProvider web3jProvider) {
    return new NonceObtainer(30000, new Web3jNonceProvider(web3jProvider));
  }

  @Provides BalanceService provideBalanceService(GetDefaultWalletBalance getDefaultWalletBalance) {
    return getDefaultWalletBalance;
  }

  @Provides TransferParser provideTransferParser(
      FindDefaultWalletInteract provideFindDefaultWalletInteract,
      TokenRepositoryType tokenRepositoryType) {
    return new TransferParser(provideFindDefaultWalletInteract, tokenRepositoryType);
  }

  @Provides FindDefaultNetworkInteract provideFindDefaultNetworkInteract(
      EthereumNetworkRepositoryType ethereumNetworkRepositoryType) {
    return new FindDefaultNetworkInteract(ethereumNetworkRepositoryType,
        AndroidSchedulers.mainThread());
  }

  @Provides DefaultTokenProvider provideDefaultTokenProvider(
      FindDefaultNetworkInteract defaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract) {
    return new BuildConfigDefaultTokenProvider(defaultNetworkInteract, findDefaultWalletInteract);
  }

  @Singleton @Provides Calculator provideMessageDigest() {
    return new Calculator();
  }

  @Singleton @Provides GasSettingsRepositoryType provideGasSettingsRepository(
      EthereumNetworkRepositoryType ethereumNetworkRepository, Web3jProvider web3jProvider) {
    return new GasSettingsRepository(ethereumNetworkRepository, web3jProvider);
  }

  @Singleton @Provides DataMapper provideDataMapper() {
    return new DataMapper();
  }

  @Singleton @Provides @Named("REGISTER_PROOF_GAS_LIMIT") BigDecimal provideRegisterPoaGasLimit() {
    return new BigDecimal(BuildConfig.REGISTER_PROOF_GAS_LIMIT);
  }

  @Singleton @Provides TransactionFactory provideTransactionFactory(Web3jProvider web3jProvider,
      WalletRepositoryType walletRepository, AccountKeystoreService accountKeystoreService,
      PasswordStore passwordStore, EthereumNetworkRepositoryType ethereumNetworkRepository,
      DataMapper dataMapper, AppCoinsAddressProxySdk adsContractAddressProvider) {
    return new TransactionFactory(web3jProvider, walletRepository, accountKeystoreService,
        passwordStore, ethereumNetworkRepository, dataMapper, adsContractAddressProvider);
  }

  @Singleton @Provides ProofWriter provideBlockChainWriter(Web3jProvider web3jProvider,
      TransactionFactory transactionFactory,
      @Named("REGISTER_PROOF_GAS_LIMIT") BigDecimal registerPoaGasLimit,
      GasSettingsRepositoryType gasSettingsRepository,
      FindDefaultWalletInteract defaultWalletInteract, WalletRepositoryType walletRepositoryType,
      EthereumNetworkRepositoryType ethereumNetwork) {
    return new BlockChainWriter(web3jProvider, transactionFactory, walletRepositoryType,
        defaultWalletInteract, gasSettingsRepository, registerPoaGasLimit, ethereumNetwork);
  }

  @Singleton @Provides AppCoinsAddressProxySdk provideAdsContractAddressSdk() {
    return new AppCoinsAddressProxyBuilder().createAddressProxySdk();
  }

  @Singleton @Provides HashCalculator provideHashCalculator(Calculator calculator) {
    return new HashCalculator(BuildConfig.LEADING_ZEROS_ON_PROOF_OF_ATTENTION, calculator);
  }

  @Provides @Named("MAX_NUMBER_PROOF_COMPONENTS") int provideMaxNumberProofComponents() {
    return 12;
  }

  @Provides TaggedCompositeDisposable provideTaggedCompositeDisposable() {
    return new TaggedCompositeDisposable(new HashMap<>());
  }

  @Singleton @Provides ProofOfAttentionService provideProofOfAttentionService(
      HashCalculator hashCalculator, ProofWriter proofWriter, TaggedCompositeDisposable disposables,
      @Named("MAX_NUMBER_PROOF_COMPONENTS") int maxNumberProofComponents,
      CountryCodeProvider countryCodeProvider) {
    return new ProofOfAttentionService(new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()),
        BuildConfig.APPLICATION_ID, hashCalculator, new CompositeDisposable(), proofWriter,
        Schedulers.computation(), maxNumberProofComponents, new BlockchainErrorMapper(),
        disposables, countryCodeProvider);
  }

  @Provides @Singleton CountryCodeProvider providesCountryCodeProvider(OkHttpClient client,
      Gson gson) {
    IpCountryCodeProvider.IpApi api = new Retrofit.Builder().baseUrl(IpCountryCodeProvider.ENDPOINT)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(IpCountryCodeProvider.IpApi.class);
    return new IpCountryCodeProvider(api);
  }

  @Provides NonceGetter provideNonceGetter(EthereumNetworkRepositoryType networkRepository,
      FindDefaultWalletInteract defaultWalletInteract) {
    return new NonceGetter(networkRepository, defaultWalletInteract);
  }

  @Provides @Singleton AppcoinsOperationsDataSaver provideInAppPurchaseDataSaver(Context context,
      List<AppcoinsOperationsDataSaver.OperationDataSource> list) {
    return new AppcoinsOperationsDataSaver(list, new AppCoinsOperationRepository(
        Room.databaseBuilder(context.getApplicationContext(), AppCoinsOperationDatabase.class,
            "appcoins_operations_data")
            .build()
            .appCoinsOperationDao(), new AppCoinsOperationMapper()),
        new AppInfoProvider(context, new ImageSaver(context.getFilesDir() + "/app_icons/")),
        Schedulers.io(), new CompositeDisposable());
  }

  @Provides OperationSources provideOperationSources(
      InAppPurchaseInteractor inAppPurchaseInteractor,
      ProofOfAttentionService proofOfAttentionService) {
    return new OperationSources(inAppPurchaseInteractor, proofOfAttentionService);
  }

  @Provides
  List<AppcoinsOperationsDataSaver.OperationDataSource> provideAppcoinsOperationListDataSource(
      OperationSources operationSources) {
    return operationSources.getSources();
  }

  @Provides AirdropChainIdMapper provideAirdropChainIdMapper(
      FindDefaultNetworkInteract defaultNetworkInteract) {
    return new AirdropChainIdMapper(defaultNetworkInteract);
  }

  @Provides AirdropService provideAirdropService(OkHttpClient client, Gson gson) {
    AirdropService.Api api = new Retrofit.Builder().baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(AirdropService.Api.class);
    return new AirdropService(api, gson, Schedulers.io());
  }

  @Singleton @Provides AirdropInteractor provideAirdropInteractor(
      PendingTransactionService pendingTransactionService, EthereumNetworkRepositoryType repository,
      AirdropService airdropService, FindDefaultWalletInteract findDefaultWalletInteract,
      AirdropChainIdMapper airdropChainIdMapper) {
    return new AirdropInteractor(
        new Airdrop(new AppcoinsTransactionService(pendingTransactionService),
            BehaviorSubject.create(), airdropService), findDefaultWalletInteract,
        airdropChainIdMapper, repository);
  }

  @Provides MicroRaidenInteractor provideMicroRaidenInteractor(Raiden raiden) {
    return new MicroRaidenInteractor(raiden);
  }

  @Singleton @Provides AppcoinsApps provideAppcoinsApps(OkHttpClient client, Gson gson) {
    AppsApi appsApi = new Retrofit.Builder().baseUrl(AppsApi.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(AppsApi.class);
    return new AppcoinsApps(new Applications.Builder().setApi(appsApi)
        .build());
  }

  @Singleton @Provides RemoteRepository.BdsApi provideBdsApi(OkHttpClient client, Gson gson) {
    String baseUrl = RemoteRepository.BASE_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(RemoteRepository.BdsApi.class);
  }

  @Singleton @Provides TokenToFiatService provideTokenToFiatService(OkHttpClient client,
      Gson gson) {
    String baseUrl = TokenToFiatService.CONVERSION_HOST;
    TokenToFiatService.TokenToFiatApi api = new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(JacksonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(TokenToFiatService.TokenToFiatApi.class);
    return new TokenToFiatService(api);
  }

  @Singleton @Provides ExpressCheckoutBuyService provideExpressCheckoutBuyService(
      TokenToFiatService tokenToFiatService) {
    return new ExpressCheckoutBuyService(tokenToFiatService);
  }

  @Singleton @Provides WalletService provideWalletService(FindDefaultWalletInteract walletInteract,
      AccountKeystoreService accountKeyService, PasswordStore passwordStore) {
    return new AccountWalletService(walletInteract, accountKeyService, passwordStore,
        new SignDataStandardNormalizer());
  }

  @Singleton @Provides BillingFactory provideBillingFactory(RemoteRepository.BdsApi bdsApi,
      WalletService walletService) {
    return merchantName -> new BdsBilling(merchantName,
        new BdsRepository(new RemoteRepository(bdsApi, new BdsApiResponseMapper()),
            new BillingThrowableCodeMapper()), walletService, new BillingThrowableCodeMapper());
  }

  @Singleton @Provides ProxyService provideProxyService(AppCoinsAddressProxySdk proxySdk) {
    return new ProxyService() {
      private static final int NETWORK_ID_ROPSTEN = 3;
      private static final int NETWORK_ID_MAIN = 1;

      @NotNull @Override public Single<String> getAppCoinsAddress(boolean debug) {
        return proxySdk.getAppCoinsAddress(debug ? NETWORK_ID_ROPSTEN : NETWORK_ID_MAIN);
      }

      @NotNull @Override public Single<String> getIabAddress(boolean debug) {
        return proxySdk.getIabAddress(debug ? NETWORK_ID_ROPSTEN : NETWORK_ID_MAIN);
      }
    };
  }

  @Singleton @Provides Adyen provideAdyen(Context context) {
    return new Adyen(context, Charset.forName("UTF-8"), rx.schedulers.Schedulers.io(),
        PublishRelay.create());
  }

  @Singleton @Provides TransactionService provideTransactionService(
      RemoteRepository.BdsApi bdsApi) {
    return new BDSTransactionService(new RemoteRepository(bdsApi, new BdsApiResponseMapper()));
  }

  @Singleton @Provides CreditCardBillingFactory provideCreditCardBillingFactory(
      TransactionService transactionService, WalletService walletService, Adyen adyen) {
    return merchantName -> new AdyenBilling(merchantName, transactionService, walletService, adyen);
  }

  @Singleton @Provides BdsPendingTransactionService provideBdsPendingTransactionService(
      BillingFactory billingFactory, BillingPaymentProofSubmission billingPaymentProofSubmission) {
    return new BdsPendingTransactionService(billingFactory, Schedulers.io(), 5,
        billingPaymentProofSubmission);
  }

  @Singleton @Provides BdsRepository provideBdsRepository(RemoteRepository.BdsApi bdsApi) {
    return new BdsRepository(new RemoteRepository(bdsApi, new BdsApiResponseMapper()),
        new BillingThrowableCodeMapper());
  }
}
