package com.asfoundation.wallet.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.room.Room;
import cm.aptoide.analytics.AnalyticsManager;
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards;
import com.appcoins.wallet.appcoins.rewards.repository.BdsAppcoinsRewardsRepository;
import com.appcoins.wallet.appcoins.rewards.repository.backend.BackendApi;
import com.appcoins.wallet.bdsbilling.BdsBilling;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission;
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmissionImpl;
import com.appcoins.wallet.bdsbilling.BillingThrowableCodeMapper;
import com.appcoins.wallet.bdsbilling.ProxyService;
import com.appcoins.wallet.bdsbilling.WalletService;
import com.appcoins.wallet.bdsbilling.repository.BdsApiResponseMapper;
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary;
import com.appcoins.wallet.bdsbilling.repository.BdsRepository;
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer;
import com.appcoins.wallet.commons.MemoryCache;
import com.appcoins.wallet.gamification.Gamification;
import com.appcoins.wallet.gamification.repository.BdsGamificationRepository;
import com.appcoins.wallet.gamification.repository.GamificationApi;
import com.appcoins.wallet.permissions.Permissions;
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxyBuilder;
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxySdk;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.Airdrop;
import com.asfoundation.wallet.AirdropService;
import com.asfoundation.wallet.App;
import com.asfoundation.wallet.FabricLogger;
import com.asfoundation.wallet.Logger;
import com.asfoundation.wallet.advertise.PoaAnalyticsController;
import com.asfoundation.wallet.analytics.AnalyticsAPI;
import com.asfoundation.wallet.analytics.BackendEventLogger;
import com.asfoundation.wallet.analytics.FacebookEventLogger;
import com.asfoundation.wallet.analytics.HttpClientKnockLogger;
import com.asfoundation.wallet.analytics.KeysNormalizer;
import com.asfoundation.wallet.analytics.LogcatAnalyticsLogger;
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics;
import com.asfoundation.wallet.apps.Applications;
import com.asfoundation.wallet.billing.BDSTransactionService;
import com.asfoundation.wallet.billing.CreditsRemoteRepository;
import com.asfoundation.wallet.billing.TransactionService;
import com.asfoundation.wallet.billing.adyen.Adyen;
import com.asfoundation.wallet.billing.adyen.AdyenBillingService;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.billing.analytics.PoaAnalytics;
import com.asfoundation.wallet.billing.partners.AddressService;
import com.asfoundation.wallet.billing.partners.BdsPartnersApi;
import com.asfoundation.wallet.billing.partners.InstallerService;
import com.asfoundation.wallet.billing.partners.InstallerSourceService;
import com.asfoundation.wallet.billing.partners.PartnerAddressService;
import com.asfoundation.wallet.billing.partners.PartnerWalletAddressService;
import com.asfoundation.wallet.billing.partners.WalletAddressService;
import com.asfoundation.wallet.billing.purchase.BillingFactory;
import com.asfoundation.wallet.billing.share.BdsShareLinkRepository;
import com.asfoundation.wallet.billing.share.ShareLinkRepository;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.BalanceGetter;
import com.asfoundation.wallet.interact.BuildConfigDefaultTokenProvider;
import com.asfoundation.wallet.interact.CreateWalletInteract;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.FetchCreditsInteract;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FetchTokensInteract;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.GetDefaultWalletBalance;
import com.asfoundation.wallet.interact.PaymentReceiverInteract;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.interact.SmsValidationInteract;
import com.asfoundation.wallet.permissions.PermissionsInteractor;
import com.asfoundation.wallet.permissions.repository.PermissionRepository;
import com.asfoundation.wallet.permissions.repository.PermissionsDatabase;
import com.asfoundation.wallet.poa.BackEndErrorMapper;
import com.asfoundation.wallet.poa.Calculator;
import com.asfoundation.wallet.poa.CountryCodeProvider;
import com.asfoundation.wallet.poa.DataMapper;
import com.asfoundation.wallet.poa.HashCalculator;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.poa.ProofWriter;
import com.asfoundation.wallet.poa.TaggedCompositeDisposable;
import com.asfoundation.wallet.repository.ApproveService;
import com.asfoundation.wallet.repository.ApproveTransactionValidatorBds;
import com.asfoundation.wallet.repository.BalanceService;
import com.asfoundation.wallet.repository.BdsBackEndWriter;
import com.asfoundation.wallet.repository.BdsPendingTransactionService;
import com.asfoundation.wallet.repository.BdsTransactionService;
import com.asfoundation.wallet.repository.BuyService;
import com.asfoundation.wallet.repository.BuyTransactionValidatorBds;
import com.asfoundation.wallet.repository.CurrencyConversionService;
import com.asfoundation.wallet.repository.ErrorMapper;
import com.asfoundation.wallet.repository.GasSettingsRepository;
import com.asfoundation.wallet.repository.GasSettingsRepositoryType;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.IpCountryCodeProvider;
import com.asfoundation.wallet.repository.NoValidateTransactionValidator;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.asfoundation.wallet.repository.PreferenceRepositoryType;
import com.asfoundation.wallet.repository.SharedPreferenceRepository;
import com.asfoundation.wallet.repository.SignDataStandardNormalizer;
import com.asfoundation.wallet.repository.SmsValidationRepositoryType;
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
import com.asfoundation.wallet.service.AppsApi;
import com.asfoundation.wallet.service.BDSAppsApi;
import com.asfoundation.wallet.service.LocalCurrencyConversionService;
import com.asfoundation.wallet.service.PoASubmissionService;
import com.asfoundation.wallet.service.RealmManager;
import com.asfoundation.wallet.service.SmsValidationApi;
import com.asfoundation.wallet.service.TickerService;
import com.asfoundation.wallet.service.TokenRateService;
import com.asfoundation.wallet.service.TrustWalletTickerService;
import com.asfoundation.wallet.topup.TopUpInteractor;
import com.asfoundation.wallet.transactions.TransactionsAnalytics;
import com.asfoundation.wallet.ui.AppcoinsApps;
import com.asfoundation.wallet.ui.airdrop.AirdropChainIdMapper;
import com.asfoundation.wallet.ui.airdrop.AirdropInteractor;
import com.asfoundation.wallet.ui.airdrop.AppcoinsTransactionService;
import com.asfoundation.wallet.ui.gamification.GamificationInteractor;
import com.asfoundation.wallet.ui.gamification.LevelResourcesMapper;
import com.asfoundation.wallet.ui.gamification.SharedPreferencesGamificationLocalData;
import com.asfoundation.wallet.ui.iab.AppCoinsOperationMapper;
import com.asfoundation.wallet.ui.iab.AppCoinsOperationRepository;
import com.asfoundation.wallet.ui.iab.AppInfoProvider;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver;
import com.asfoundation.wallet.ui.iab.ApproveKeyProvider;
import com.asfoundation.wallet.ui.iab.AsfInAppPurchaseInteractor;
import com.asfoundation.wallet.ui.iab.BdsInAppPurchaseInteractor;
import com.asfoundation.wallet.ui.iab.ImageSaver;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.asfoundation.wallet.ui.iab.RewardsManager;
import com.asfoundation.wallet.ui.iab.database.AppCoinsOperationDatabase;
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer;
import com.asfoundation.wallet.ui.iab.raiden.NonceObtainerFactory;
import com.asfoundation.wallet.ui.iab.raiden.Web3jNonceProvider;
import com.asfoundation.wallet.ui.iab.share.ShareLinkInteractor;
import com.asfoundation.wallet.ui.onboarding.OnboardingInteract;
import com.asfoundation.wallet.ui.transact.TransactionDataValidator;
import com.asfoundation.wallet.ui.transact.TransferInteractor;
import com.asfoundation.wallet.util.DeviceInfo;
import com.asfoundation.wallet.util.EIPTransactionParser;
import com.asfoundation.wallet.util.LogInterceptor;
import com.asfoundation.wallet.util.OneStepTransactionParser;
import com.asfoundation.wallet.util.TransactionIdHelper;
import com.asfoundation.wallet.util.TransferParser;
import com.facebook.appevents.AppEventsLogger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jakewharton.rxrelay2.BehaviorRelay;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
import static com.asfoundation.wallet.service.AppsApi.API_BASE_URL;

@Module class ToolsModule {

  private final TransactionIdHelper transactionIdHelper = new TransactionIdHelper();

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
      RemoteRepository.BdsApi api, WalletService walletService, BdsApiSecondary bdsApi) {
    return new BillingPaymentProofSubmissionImpl.Builder().setApi(api)
        .setBdsApiSecondary(bdsApi)
        .setWalletService(walletService)
        .build();
  }

  @Provides @Named("APPROVE_SERVICE_ON_CHAIN") ApproveService provideApproveService(
      SendTransactionInteract sendTransactionInteract, ErrorMapper errorMapper,
      @Named("no_wait_transaction") TrackTransactionService noWaitPendingTransactionService) {
    return new ApproveService(new WatchedTransactionService(sendTransactionInteract::approve,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), errorMapper,
        Schedulers.io(), noWaitPendingTransactionService), new NoValidateTransactionValidator());
  }

  @Provides @Named("APPROVE_SERVICE_BDS") ApproveService provideApproveServiceBds(
      SendTransactionInteract sendTransactionInteract, ErrorMapper errorMapper,
      @Named("no_wait_transaction") TrackTransactionService noWaitPendingTransactionService,
      BillingPaymentProofSubmission billingPaymentProofSubmission, AddressService addressService) {
    return new ApproveService(new WatchedTransactionService(sendTransactionInteract::approve,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), errorMapper,
        Schedulers.io(), noWaitPendingTransactionService),
        new ApproveTransactionValidatorBds(sendTransactionInteract, billingPaymentProofSubmission,
            addressService));
  }

  @Provides @Named("BUY_SERVICE_ON_CHAIN") BuyService provideBuyServiceOnChain(
      SendTransactionInteract sendTransactionInteract, ErrorMapper errorMapper,
      @Named("wait_pending_transaction") TrackTransactionService pendingTransactionService,
      DefaultTokenProvider defaultTokenProvider, CountryCodeProvider countryCodeProvider,
      DataMapper dataMapper, AddressService addressService) {
    return new BuyService(new WatchedTransactionService(sendTransactionInteract::buy,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), errorMapper,
        Schedulers.io(), pendingTransactionService), new NoValidateTransactionValidator(),
        defaultTokenProvider, countryCodeProvider, dataMapper, addressService);
  }

  @Provides @Named("BUY_SERVICE_BDS") BuyService provideBuyServiceBds(
      SendTransactionInteract sendTransactionInteract, ErrorMapper errorMapper,
      BdsPendingTransactionService bdsPendingTransactionService,
      BillingPaymentProofSubmission billingPaymentProofSubmission,
      DefaultTokenProvider defaultTokenProvider, CountryCodeProvider countryCodeProvider,
      DataMapper dataMapper, AddressService addressService) {
    return new BuyService(new WatchedTransactionService(sendTransactionInteract::buy,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), errorMapper,
        Schedulers.io(), bdsPendingTransactionService),
        new BuyTransactionValidatorBds(sendTransactionInteract, billingPaymentProofSubmission,
            defaultTokenProvider, addressService), defaultTokenProvider, countryCodeProvider,
        dataMapper, addressService);
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

  @Singleton @Provides @Named("IN_APP_PURCHASE_SERVICE")
  InAppPurchaseService provideInAppPurchaseService(
      @Named("APPROVE_SERVICE_BDS") ApproveService approveService,
      @Named("BUY_SERVICE_BDS") BuyService buyService, BalanceService balanceService,
      ErrorMapper errorMapper) {
    return new InAppPurchaseService(new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()),
        approveService, buyService, balanceService, Schedulers.io(), errorMapper);
  }

  @Singleton @Provides @Named("ASF_IN_APP_PURCHASE_SERVICE")
  InAppPurchaseService provideInAppPurchaseServiceAsf(
      @Named("APPROVE_SERVICE_ON_CHAIN") ApproveService approveService,
      @Named("BUY_SERVICE_ON_CHAIN") BuyService buyService, BalanceService balanceService,
      ErrorMapper errorMapper) {
    return new InAppPurchaseService(new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()),
        approveService, buyService, balanceService, Schedulers.io(), errorMapper);
  }

  @Singleton @Provides BdsTransactionService providesBdsTransactionService(Billing billing,
      BillingPaymentProofSubmission billingPaymentProofSubmission) {
    return new BdsTransactionService(Schedulers.io(),
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()), new CompositeDisposable(),
        new BdsPendingTransactionService(billing, Schedulers.io(), 5,
            billingPaymentProofSubmission));
  }

  @Singleton @Provides BdsInAppPurchaseInteractor provideBdsInAppPurchaseInteractor(
      BillingPaymentProofSubmission billingPaymentProofSubmission,
      @Named("ASF_BDS_IN_APP_INTERACTOR") AsfInAppPurchaseInteractor inAppPurchaseInteractor,
      Billing billing) {
    return new BdsInAppPurchaseInteractor(inAppPurchaseInteractor, billingPaymentProofSubmission,
        new ApproveKeyProvider(billing), billing);
  }

  @Singleton @Provides @Named("ASF_BDS_IN_APP_INTERACTOR")
  AsfInAppPurchaseInteractor provideAsfBdsInAppPurchaseInteractor(
      @Named("IN_APP_PURCHASE_SERVICE") InAppPurchaseService inAppPurchaseService,
      FindDefaultWalletInteract defaultWalletInteract, FetchGasSettingsInteract gasSettingsInteract,
      TransferParser parser, Billing billing, CurrencyConversionService currencyConversionService,
      BdsTransactionService bdsTransactionService, BillingMessagesMapper billingMessagesMapper) {
    return new AsfInAppPurchaseInteractor(inAppPurchaseService, defaultWalletInteract,
        gasSettingsInteract, new BigDecimal(BuildConfig.PAYMENT_GAS_LIMIT), parser,
        billingMessagesMapper, billing, new ExternalBillingSerializer(), currencyConversionService,
        bdsTransactionService, Schedulers.io(), transactionIdHelper);
  }

  @Singleton @Provides @Named("ASF_IN_APP_INTERACTOR")
  AsfInAppPurchaseInteractor provideAsfInAppPurchaseInteractor(
      @Named("ASF_IN_APP_PURCHASE_SERVICE") InAppPurchaseService inAppPurchaseService,
      FindDefaultWalletInteract defaultWalletInteract, FetchGasSettingsInteract gasSettingsInteract,
      TransferParser parser, Billing billing, CurrencyConversionService currencyConversionService,
      BdsTransactionService bdsTransactionService, BillingMessagesMapper billingMessagesMapper) {
    return new AsfInAppPurchaseInteractor(inAppPurchaseService, defaultWalletInteract,
        gasSettingsInteract, new BigDecimal(BuildConfig.PAYMENT_GAS_LIMIT), parser,
        billingMessagesMapper, billing, new ExternalBillingSerializer(), currencyConversionService,
        bdsTransactionService, Schedulers.io(), transactionIdHelper);
  }

  @Singleton @Provides InAppPurchaseInteractor provideDualInAppPurchaseInteractor(
      BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor,
      @Named("ASF_IN_APP_INTERACTOR") AsfInAppPurchaseInteractor asfInAppPurchaseInteractor,
      AppcoinsRewards appcoinsRewards, Billing billing) {
    return new InAppPurchaseInteractor(asfInAppPurchaseInteractor, bdsInAppPurchaseInteractor,
        new ExternalBillingSerializer(), appcoinsRewards, billing);
  }

  @Provides GetDefaultWalletBalance provideGetDefaultWalletBalance(
      WalletRepositoryType walletRepository, FetchTokensInteract fetchTokensInteract,
      FindDefaultWalletInteract defaultWalletInteract, FetchCreditsInteract fetchCreditsInteract,
      NetworkInfo networkInfo) {
    return new GetDefaultWalletBalance(walletRepository, fetchTokensInteract, defaultWalletInteract,
        fetchCreditsInteract, networkInfo);
  }

  @Provides FetchTokensInteract provideFetchTokensInteract(TokenRepositoryType tokenRepository,
      DefaultTokenProvider defaultTokenProvider) {
    return new FetchTokensInteract(tokenRepository, defaultTokenProvider);
  }

  @Provides FetchCreditsInteract provideFetchCreditsInteract(BalanceGetter balanceGetter) {
    return new FetchCreditsInteract(balanceGetter);
  }

  @Provides MultiWalletNonceObtainer provideNonceObtainer(Web3jProvider web3jProvider) {
    return new MultiWalletNonceObtainer(
        new NonceObtainerFactory(30000, new Web3jNonceProvider(web3jProvider)));
  }

  @Provides BalanceService provideBalanceService(GetDefaultWalletBalance getDefaultWalletBalance) {
    return getDefaultWalletBalance;
  }

  @Provides EIPTransactionParser provideEIPTransferParser(
      FindDefaultWalletInteract provideFindDefaultWalletInteract,
      TokenRepositoryType tokenRepositoryType) {
    return new EIPTransactionParser(provideFindDefaultWalletInteract, tokenRepositoryType);
  }

  @Provides OneStepTransactionParser provideOneStepTransferParser(
      FindDefaultWalletInteract provideFindDefaultWalletInteract,
      TokenRepositoryType tokenRepositoryType, ProxyService proxyService, Billing billing,
      TokenRateService tokenRateService) {
    return new OneStepTransactionParser(provideFindDefaultWalletInteract, tokenRepositoryType,
        proxyService, billing, tokenRateService,
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()));
  }

  @Provides TransferParser provideTransferParser(EIPTransactionParser eipTransactionParser,
      OneStepTransactionParser oneStepTransactionParser) {
    return new TransferParser(eipTransactionParser, oneStepTransactionParser);
  }

  @Provides FindDefaultNetworkInteract provideFindDefaultNetworkInteract(NetworkInfo networkInfo) {
    return new FindDefaultNetworkInteract(networkInfo, AndroidSchedulers.mainThread());
  }

  @Provides DefaultTokenProvider provideDefaultTokenProvider(
      FindDefaultWalletInteract findDefaultWalletInteract, NetworkInfo networkInfo) {
    return new BuildConfigDefaultTokenProvider(findDefaultWalletInteract, networkInfo);
  }

  @Singleton @Provides Calculator provideMessageDigest() {
    return new Calculator();
  }

  @Singleton @Provides TransferInteractor provideTransferInteractor(
      @NotNull RewardsManager rewardsManager, @NotNull GetDefaultWalletBalance balance,
      @NotNull FindDefaultWalletInteract findWallet) {
    return new TransferInteractor(rewardsManager, new TransactionDataValidator(), balance,
        findWallet);
  }

  @Singleton @Provides GasSettingsRepositoryType provideGasSettingsRepository(
      Web3jProvider web3jProvider) {
    return new GasSettingsRepository(web3jProvider);
  }

  @Singleton @Provides DataMapper provideDataMapper() {
    return new DataMapper();
  }

  @Singleton @Provides @Named("REGISTER_PROOF_GAS_LIMIT") BigDecimal provideRegisterPoaGasLimit() {
    return new BigDecimal(BuildConfig.REGISTER_PROOF_GAS_LIMIT);
  }

  @Singleton @Provides ProofWriter provideBdsBackEndWriter(
      FindDefaultWalletInteract defaultWalletInteract, PoASubmissionService poaSubmissionService) {
    return new BdsBackEndWriter(defaultWalletInteract, poaSubmissionService);
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
      CountryCodeProvider countryCodeProvider, AddressService addressService) {
    return new ProofOfAttentionService(new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()),
        BuildConfig.APPLICATION_ID, hashCalculator, new CompositeDisposable(), proofWriter,
        Schedulers.computation(), maxNumberProofComponents, new BackEndErrorMapper(), disposables,
        countryCodeProvider, addressService);
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

  @Provides AirdropChainIdMapper provideAirdropChainIdMapper(NetworkInfo networkInfo) {
    return new AirdropChainIdMapper(networkInfo);
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
      PendingTransactionService pendingTransactionService, AirdropService airdropService,
      FindDefaultWalletInteract findDefaultWalletInteract,
      AirdropChainIdMapper airdropChainIdMapper) {
    return new AirdropInteractor(
        new Airdrop(new AppcoinsTransactionService(pendingTransactionService),
            BehaviorSubject.create(), airdropService), findDefaultWalletInteract,
        airdropChainIdMapper);
  }

  @Singleton @Provides AppcoinsApps provideAppcoinsApps(OkHttpClient client, Gson gson) {

    AppsApi appsApi = new Retrofit.Builder().baseUrl(API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(AppsApi.class);
    return new AppcoinsApps(new Applications.Builder().setApi(new BDSAppsApi(appsApi))
        .build());
  }

  @Singleton @Provides RemoteRepository.BdsApi provideBdsApi(OkHttpClient client, Gson gson) {
    String baseUrl = BuildConfig.BASE_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(RemoteRepository.BdsApi.class);
  }

  @Singleton @Provides BdsApiSecondary provideBdsApiSecondary(OkHttpClient client, Gson gson) {
    String baseUrl = BuildConfig.BDS_BASE_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(BdsApiSecondary.class);
  }

  @Singleton @Provides TokenRateService provideTokenRateService(OkHttpClient client,
      ObjectMapper objectMapper) {
    String baseUrl = TokenRateService.CONVERSION_HOST;
    TokenRateService.TokenToFiatApi api = new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(TokenRateService.TokenToFiatApi.class);
    return new TokenRateService(api);
  }

  @Singleton @Provides LocalCurrencyConversionService provideLocalCurrencyConversionService(
      OkHttpClient client, ObjectMapper objectMapper) {
    String baseUrl = LocalCurrencyConversionService.CONVERSION_HOST;
    LocalCurrencyConversionService.TokenToLocalFiatApi api = new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(LocalCurrencyConversionService.TokenToLocalFiatApi.class);
    return new LocalCurrencyConversionService(api);
  }

  @Singleton @Provides CurrencyConversionService provideCurrencyConversionService(
      TokenRateService tokenRateService,
      LocalCurrencyConversionService localCurrencyConversionService) {
    return new CurrencyConversionService(tokenRateService, localCurrencyConversionService);
  }

  @Singleton @Provides WalletService provideWalletService(FindDefaultWalletInteract walletInteract,
      AccountKeystoreService accountKeyService, PasswordStore passwordStore) {
    return new AccountWalletService(walletInteract, accountKeyService, passwordStore,
        new SignDataStandardNormalizer());
  }

  @Singleton @Provides Billing provideBillingFactory(WalletService walletService,
      BdsRepository bdsRepository) {
    return new BdsBilling(bdsRepository, walletService, new BillingThrowableCodeMapper());
  }

  @Singleton @Provides RemoteRepository provideRemoteRepository(RemoteRepository.BdsApi bdsApi,
      BdsApiSecondary api) {
    return new RemoteRepository(bdsApi, new BdsApiResponseMapper(), api);
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
    return new Adyen(context, Charset.forName("UTF-8"), Schedulers.io(), BehaviorRelay.create());
  }

  @Singleton @Provides TransactionService provideTransactionService(
      RemoteRepository remoteRepository) {
    return new BDSTransactionService(remoteRepository);
  }

  @Singleton @Provides BillingFactory provideCreditCardBillingFactory(
      TransactionService transactionService, WalletService walletService, Adyen adyen,
      AddressService addressService) {
    return merchantName -> new AdyenBillingService(merchantName, transactionService, walletService,
        adyen, addressService);
  }

  @Singleton @Provides BdsPendingTransactionService provideBdsPendingTransactionService(
      BillingPaymentProofSubmission billingPaymentProofSubmission, Billing billing) {
    return new BdsPendingTransactionService(billing, Schedulers.io(), 5,
        billingPaymentProofSubmission);
  }

  @Singleton @Provides BdsRepository provideBdsRepository(RemoteRepository repository) {
    return new BdsRepository(repository);
  }

  @Singleton @Provides AppcoinsRewards provideAppcoinsRewards(WalletService walletService,
      Billing billing, BackendApi backendApi, RemoteRepository remoteRepository) {
    return new AppcoinsRewards(
        new BdsAppcoinsRewardsRepository(new CreditsRemoteRepository(backendApi, remoteRepository)),
        new com.appcoins.wallet.appcoins.rewards.repository.WalletService() {
          @NotNull @Override public Single<String> getWalletAddress() {
            return walletService.getWalletAddress();
          }

          @NotNull @Override public Single<String> signContent(@NotNull String content) {
            return walletService.signContent(content);
          }
        }, new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), Schedulers.io(),
        billing, new com.appcoins.wallet.appcoins.rewards.ErrorMapper());
  }

  @Singleton @Provides RewardsManager provideRewardsManager(AppcoinsRewards appcoinsRewards,
      Billing billing, AddressService addressService) {
    return new RewardsManager(appcoinsRewards, billing, addressService);
  }

  @Singleton @Provides BillingMessagesMapper provideBillingMessagesMapper() {
    return new BillingMessagesMapper(new ExternalBillingSerializer());
  }

  @Singleton @Provides SharedPreferences provideSharedPreferences(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  @Singleton @Provides PoASubmissionService providePoASubmissionService(OkHttpClient client,
      ObjectMapper objectMapper) {
    String baseUrl = PoASubmissionService.SERVICE_HOST;
    PoASubmissionService.PoASubmissionApi api = new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(PoASubmissionService.PoASubmissionApi.class);
    return new PoASubmissionService(api);
  }

  @Provides Gamification provideGamification(OkHttpClient client, SharedPreferences preferences) {
    String baseUrl = PoASubmissionService.SERVICE_HOST;
    GamificationApi api = new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(GamificationApi.class);
    return new Gamification(new BdsGamificationRepository(api,
        new SharedPreferencesGamificationLocalData(preferences)));
  }

  @Singleton @Provides BackendApi provideBackendApi(OkHttpClient client, Gson gson) {
    return new Retrofit.Builder().baseUrl(BuildConfig.BACKEND_HOST)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(BackendApi.class);
  }

  @Singleton @Provides BalanceGetter provideBalanceGetter(AppcoinsRewards appcoinsRewards) {
    return new BalanceGetter() {
      @NotNull @Override public Single<BigDecimal> getBalance(@NotNull String address) {
        return appcoinsRewards.getBalance(address)
            .subscribeOn(Schedulers.io());
      }

      @NotNull @Override public Single<BigDecimal> getBalance() {
        return null;
      }
    };
  }

  @Singleton @Provides AnalyticsAPI provideAnalyticsAPI(OkHttpClient client,
      ObjectMapper objectMapper) {
    return new Retrofit.Builder().baseUrl("http://ws75.aptoide.com/api/7/")
        .client(client)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(AnalyticsAPI.class);
  }

  @Singleton @Provides @Named("bi_event_list") List<String> provideBiEventList() {
    List<String> list = new ArrayList<>();
    list.add(BillingAnalytics.PURCHASE_DETAILS);
    list.add(BillingAnalytics.PAYMENT_METHOD_DETAILS);
    list.add(BillingAnalytics.PAYMENT);
    list.add(PoaAnalytics.POA_STARTED);
    list.add(PoaAnalytics.POA_COMPLETED);
    return list;
  }

  @Singleton @Provides @Named("facebook_event_list") List<String> provideFacebookEventList() {
    List<String> list = new ArrayList<>();
    list.add(BillingAnalytics.PURCHASE_DETAILS);
    list.add(BillingAnalytics.PAYMENT_METHOD_DETAILS);
    list.add(BillingAnalytics.PAYMENT);
    list.add(BillingAnalytics.REVENUE);
    list.add(PoaAnalytics.POA_STARTED);
    list.add(PoaAnalytics.POA_COMPLETED);
    list.add(TransactionsAnalytics.OPEN_APPLICATION);
    list.add(GamificationAnalytics.GAMIFICATION);
    list.add(GamificationAnalytics.GAMIFICATION_MORE_INFO);
    return list;
  }

  @Singleton @Provides AnalyticsManager provideAnalyticsManager(OkHttpClient okHttpClient,
      AnalyticsAPI api, Context context, @Named("bi_event_list") List<String> biEventList,
      @Named("facebook_event_list") List<String> facebookEventList) {

    return new AnalyticsManager.Builder().addLogger(new BackendEventLogger(api), biEventList)
        .addLogger(new FacebookEventLogger(AppEventsLogger.newLogger(context)), facebookEventList)
        .setAnalyticsNormalizer(new KeysNormalizer())
        .setDebugLogger(new LogcatAnalyticsLogger())
        .setKnockLogger(new HttpClientKnockLogger(okHttpClient))
        .build();
  }

  @Provides CreateWalletInteract provideCreateAccountInteract(
      WalletRepositoryType accountRepository, PasswordStore passwordStore) {
    return new CreateWalletInteract(accountRepository, passwordStore);
  }

  @Provides PaymentReceiverInteract providePaymentReceiverInteract(
      CreateWalletInteract createWalletInteract) {
    return new PaymentReceiverInteract(createWalletInteract);
  }

  @Provides OnboardingInteract provideOnboardingInteract(CreateWalletInteract createWalletInteract,
      WalletService walletService, PreferenceRepositoryType preferenceRepositoryType,
      TokenRepositoryType tokenRepository) {
    return new OnboardingInteract(createWalletInteract, walletService, preferenceRepositoryType);
  }

  @Singleton @Provides BillingAnalytics provideBillingAnalytics(AnalyticsManager analytics) {
    return new BillingAnalytics(analytics);
  }

  @Singleton @Provides PoaAnalytics providePoAAnalytics(AnalyticsManager analytics) {
    return new PoaAnalytics(analytics);
  }

  @Singleton @Provides PoaAnalyticsController providesPoaAnalyticsController() {
    return new PoaAnalyticsController(new CopyOnWriteArrayList<String>());
  }

  @Provides GamificationInteractor provideGamificationInteractor(Gamification gamification,
      FindDefaultWalletInteract defaultWallet, LocalCurrencyConversionService conversionService) {
    return new GamificationInteractor(gamification, defaultWallet, conversionService);
  }

  @Singleton @Provides LevelResourcesMapper providesLevelResourcesMapper() {
    return new LevelResourcesMapper();
  }

  @Singleton @Provides Permissions providesPermissions(Context context) {
    return new Permissions(new PermissionRepository(
        Room.databaseBuilder(context.getApplicationContext(), PermissionsDatabase.class,
            "permissions_database")
            .build()
            .permissionsDao()));
  }

  @Singleton @Provides PermissionsInteractor providesPermissionsInteractor(Permissions permissions,
      FindDefaultWalletInteract walletService) {
    return new PermissionsInteractor(permissions, walletService);
  }

  @Singleton @Provides AddressService providesAddressService(InstallerService installerService,
      WalletAddressService addressService) {
    return new PartnerAddressService(installerService, addressService,
        new DeviceInfo(Build.MANUFACTURER, Build.MODEL));
  }

  @Singleton @Provides InstallerService providesInstallerService(Context context) {
    return new InstallerSourceService(context);
  }

  @Singleton @Provides WalletAddressService providesWalletAddressService(BdsPartnersApi api) {
    return new PartnerWalletAddressService(api, BuildConfig.DEFAULT_STORE_ADDRESS,
        BuildConfig.DEFAULT_OEM_ADDRESS);
  }

  @Singleton @Provides BdsPartnersApi provideBdsPartnersApi(OkHttpClient client, Gson gson) {
    String baseUrl = BuildConfig.BASE_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(BdsPartnersApi.class);
  }

  @Provides ObjectMapper providesObjectMapper() {
    return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Provides ShareLinkInteractor providesShareLinkInteractor(ShareLinkRepository repository,
      FindDefaultWalletInteract interactor) {
    return new ShareLinkInteractor(repository, interactor);
  }

  @Singleton @Provides ShareLinkRepository providesShareLinkRepository(
      BdsShareLinkRepository.BdsShareLinkApi api) {
    return new BdsShareLinkRepository(api);
  }

  @Singleton @Provides BdsShareLinkRepository.BdsShareLinkApi provideBdsShareLinkApi(
      OkHttpClient client, Gson gson) {
    String baseUrl = BuildConfig.CATAPPULT_BASE_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(BdsShareLinkRepository.BdsShareLinkApi.class);
  }

  @Singleton @Provides TopUpInteractor providesTopUpInteractor(BdsRepository repository,
      LocalCurrencyConversionService conversionService) {
    return new TopUpInteractor(repository, conversionService);
  }

  @Singleton @Provides TransactionsAnalytics providesTransactionsAnalytics(
      @NotNull AnalyticsManager analytics) {
    return new TransactionsAnalytics(analytics);
  }

  @Singleton @Provides GamificationAnalytics provideGamificationAnalytics(
      AnalyticsManager analytics) {
    return new GamificationAnalytics(analytics);
  }

  @Singleton @Provides SmsValidationApi provideSmsValidationApi(OkHttpClient client, Gson gson) {
    String baseUrl = BuildConfig.BACKEND_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(SmsValidationApi.class);
  }

  @Singleton @Provides SmsValidationInteract provideSmsValidationInteract(
      SmsValidationRepositoryType smsValidationRepository) {
    return new SmsValidationInteract(smsValidationRepository, Schedulers.io());
  }
}