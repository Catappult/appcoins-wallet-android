package com.asfoundation.wallet.di;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.room.Room;
import cm.aptoide.analytics.AnalyticsManager;
import com.adyen.checkout.core.api.Environment;
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
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer;
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary;
import com.appcoins.wallet.bdsbilling.repository.BdsRepository;
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository;
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository.BdsApi;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.commons.MemoryCache;
import com.appcoins.wallet.gamification.Gamification;
import com.appcoins.wallet.gamification.repository.PromotionsRepository;
import com.appcoins.wallet.permissions.Permissions;
import com.aptoide.apk.injector.extractor.data.Extractor;
import com.aptoide.apk.injector.extractor.data.ExtractorV1;
import com.aptoide.apk.injector.extractor.data.ExtractorV2;
import com.aptoide.apk.injector.extractor.domain.IExtract;
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxyBuilder;
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxySdk;
import com.asf.wallet.BuildConfig;
import com.asf.wallet.R.drawable;
import com.asf.wallet.R.string;
import com.asfoundation.wallet.App;
import com.asfoundation.wallet.advertise.PoaAnalyticsController;
import com.asfoundation.wallet.analytics.AnalyticsAPI;
import com.asfoundation.wallet.analytics.BackendEventLogger;
import com.asfoundation.wallet.analytics.FacebookEventLogger;
import com.asfoundation.wallet.analytics.HttpClientKnockLogger;
import com.asfoundation.wallet.analytics.KeysNormalizer;
import com.asfoundation.wallet.analytics.LogcatAnalyticsLogger;
import com.asfoundation.wallet.analytics.RakamAnalytics;
import com.asfoundation.wallet.analytics.RakamEventLogger;
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics;
import com.asfoundation.wallet.billing.CreditsRemoteRepository;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.billing.analytics.PoaAnalytics;
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics;
import com.asfoundation.wallet.billing.analytics.WalletsEventSender;
import com.asfoundation.wallet.billing.partners.AddressService;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.identification.IdsRepository;
import com.asfoundation.wallet.interact.BalanceGetter;
import com.asfoundation.wallet.interact.BuildConfigDefaultTokenProvider;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.logging.DebugReceiver;
import com.asfoundation.wallet.logging.LogReceiver;
import com.asfoundation.wallet.logging.Logger;
import com.asfoundation.wallet.logging.WalletLogger;
import com.asfoundation.wallet.navigator.UpdateNavigator;
import com.asfoundation.wallet.permissions.repository.PermissionRepository;
import com.asfoundation.wallet.permissions.repository.PermissionsDatabase;
import com.asfoundation.wallet.poa.Calculator;
import com.asfoundation.wallet.poa.CountryCodeProvider;
import com.asfoundation.wallet.poa.DataMapper;
import com.asfoundation.wallet.poa.HashCalculator;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.poa.ProofWriter;
import com.asfoundation.wallet.poa.TaggedCompositeDisposable;
import com.asfoundation.wallet.repository.BdsBackEndWriter;
import com.asfoundation.wallet.repository.ErrorMapper;
import com.asfoundation.wallet.repository.IpCountryCodeProvider;
import com.asfoundation.wallet.repository.IpCountryCodeProvider.IpApi;
import com.asfoundation.wallet.repository.OffChainTransactions;
import com.asfoundation.wallet.repository.OffChainTransactionsRepository;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.TrustPasswordStore;
import com.asfoundation.wallet.repository.Web3jProvider;
import com.asfoundation.wallet.router.GasSettingsRouter;
import com.asfoundation.wallet.service.AutoUpdateService;
import com.asfoundation.wallet.service.CampaignService;
import com.asfoundation.wallet.service.TokenRateService;
import com.asfoundation.wallet.support.SupportSharedPreferences;
import com.asfoundation.wallet.topup.TopUpAnalytics;
import com.asfoundation.wallet.topup.TopUpValuesApiResponseMapper;
import com.asfoundation.wallet.transactions.TransactionsAnalytics;
import com.asfoundation.wallet.transactions.TransactionsMapper;
import com.asfoundation.wallet.ui.airdrop.AirdropChainIdMapper;
import com.asfoundation.wallet.ui.iab.AppCoinsOperationRepository;
import com.asfoundation.wallet.ui.iab.AppInfoProvider;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver.OperationDataSource;
import com.asfoundation.wallet.ui.iab.ImageSaver;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.asfoundation.wallet.ui.iab.LocalPaymentAnalytics;
import com.asfoundation.wallet.ui.iab.PaymentMethodsMapper;
import com.asfoundation.wallet.ui.iab.RewardsManager;
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer;
import com.asfoundation.wallet.ui.iab.raiden.NonceObtainerFactory;
import com.asfoundation.wallet.ui.iab.raiden.Web3jNonceProvider;
import com.asfoundation.wallet.util.CurrencyFormatUtils;
import com.asfoundation.wallet.util.EIPTransactionParser;
import com.asfoundation.wallet.util.LogInterceptor;
import com.asfoundation.wallet.util.OneStepTransactionParser;
import com.asfoundation.wallet.util.TransferParser;
import com.asfoundation.wallet.util.UserAgentInterceptor;
import com.asfoundation.wallet.wallet_validation.generic.WalletValidationAnalytics;
import com.facebook.appevents.AppEventsLogger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import java.math.BigDecimal;
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

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.asfoundation.wallet.C.ETHEREUM_NETWORK_NAME;
import static com.asfoundation.wallet.C.ETH_SYMBOL;
import static com.asfoundation.wallet.C.ROPSTEN_NETWORK_NAME;

@Module class ToolsModule {

  @Provides Context provideContext(App application) {
    return application.getApplicationContext();
  }

  @Singleton @Provides Gson provideGson() {
    return new Gson();
  }

  @Singleton @Provides @Named("user_agent") String provideUserAgent(Context context) {
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    DisplayMetrics displayMetrics = new DisplayMetrics();
    display.getRealMetrics(displayMetrics);

    return "AppCoins_Wallet/"
        + BuildConfig.VERSION_NAME
        + " (Linux; Android "
        + VERSION.RELEASE.replaceAll(";", " ")
        + "; "
        + VERSION.SDK_INT
        + "; "
        + Build.MODEL.replaceAll(";", " ")
        + " Build/"
        + Build.PRODUCT.replace(";", " ")
        + "; "
        + System.getProperty("os.arch")
        + "; "
        + BuildConfig.APPLICATION_ID
        + "; "
        + BuildConfig.VERSION_CODE
        + "; "
        + displayMetrics.widthPixels
        + "x"
        + displayMetrics.heightPixels
        + ")";
  }

  @Singleton @Provides OkHttpClient okHttpClient(@Named("user_agent") String userAgent) {
    return new OkHttpClient.Builder().addInterceptor(new UserAgentInterceptor(userAgent))
        .addInterceptor(new LogInterceptor())
        .connectTimeout(15, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.MINUTES)
        .writeTimeout(30, TimeUnit.MINUTES)
        .build();
  }

  @Singleton @Provides PasswordStore passwordStore(Context context, Logger logger) {
    return new TrustPasswordStore(context, logger);
  }

  @Singleton @Provides Logger provideLogger() {
    ArrayList<LogReceiver> receivers = new ArrayList<>();
    if (BuildConfig.DEBUG) {
      receivers.add(new DebugReceiver());
    }
    return new WalletLogger(receivers);
  }

  @Singleton @Provides BillingPaymentProofSubmission providesBillingPaymentProofSubmission(
      BdsApi api, WalletService walletService, BdsApiSecondary bdsApi) {
    return new BillingPaymentProofSubmissionImpl.Builder().setApi(api)
        .setBdsApiSecondary(bdsApi)
        .setWalletService(walletService)
        .build();
  }

  @Singleton @Provides ErrorMapper provideErrorMapper() {
    return new ErrorMapper();
  }

  @Provides GasSettingsRouter provideGasSettingsRouter() {
    return new GasSettingsRouter();
  }

  @Provides LocalPaymentAnalytics provideLocalPaymentAnalytics(BillingAnalytics billingAnalytics,
      InAppPurchaseInteractor inAppPurchaseInteractor) {
    return new LocalPaymentAnalytics(billingAnalytics, inAppPurchaseInteractor, Schedulers.io());
  }

  @Provides PaymentMethodsMapper providePaymentMethodsMapper() {
    return new PaymentMethodsMapper();
  }

  @Provides MultiWalletNonceObtainer provideNonceObtainer(Web3jProvider web3jProvider) {
    return new MultiWalletNonceObtainer(
        new NonceObtainerFactory(30000, new Web3jNonceProvider(web3jProvider)));
  }

  @Provides EIPTransactionParser provideEIPTransferParser(
      DefaultTokenProvider defaultTokenProvider) {
    return new EIPTransactionParser(defaultTokenProvider);
  }

  @Provides OneStepTransactionParser provideOneStepTransferParser(ProxyService proxyService,
      Billing billing, TokenRateService tokenRateService,
      DefaultTokenProvider defaultTokenProvider) {
    return new OneStepTransactionParser(proxyService, billing, tokenRateService,
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()), defaultTokenProvider);
  }

  @Provides TransferParser provideTransferParser(EIPTransactionParser eipTransactionParser,
      OneStepTransactionParser oneStepTransactionParser) {
    return new TransferParser(eipTransactionParser, oneStepTransactionParser);
  }

  @Provides DefaultTokenProvider provideDefaultTokenProvider(
      FindDefaultWalletInteract findDefaultWalletInteract, NetworkInfo networkInfo) {
    return new BuildConfigDefaultTokenProvider(findDefaultWalletInteract, networkInfo);
  }

  @Singleton @Provides Calculator provideMessageDigest() {
    return new Calculator();
  }

  @Singleton @Provides DataMapper provideDataMapper() {
    return new DataMapper();
  }

  @Singleton @Provides @Named("REGISTER_PROOF_GAS_LIMIT") BigDecimal provideRegisterPoaGasLimit() {
    return new BigDecimal(BuildConfig.REGISTER_PROOF_GAS_LIMIT);
  }

  @Singleton @Provides ProofWriter provideBdsBackEndWriter(
      FindDefaultWalletInteract defaultWalletInteract, CampaignService campaignService) {
    return new BdsBackEndWriter(defaultWalletInteract, campaignService);
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

  @Provides @Singleton CountryCodeProvider providesCountryCodeProvider(OkHttpClient client,
      Gson gson) {
    IpApi api = new Retrofit.Builder().baseUrl(IpCountryCodeProvider.ENDPOINT)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(IpApi.class);
    return new IpCountryCodeProvider(api);
  }

  @Provides @Singleton AppcoinsOperationsDataSaver provideInAppPurchaseDataSaver(Context context,
      List<OperationDataSource> list, AppCoinsOperationRepository appCoinsOperationRepository) {
    return new AppcoinsOperationsDataSaver(list, appCoinsOperationRepository,
        new AppInfoProvider(context, new ImageSaver(context.getFilesDir() + "/app_icons/")),
        Schedulers.io(), new CompositeDisposable());
  }

  @Provides OperationSources provideOperationSources(
      InAppPurchaseInteractor inAppPurchaseInteractor,
      ProofOfAttentionService proofOfAttentionService) {
    return new OperationSources(inAppPurchaseInteractor, proofOfAttentionService);
  }

  @Provides List<OperationDataSource> provideAppcoinsOperationListDataSource(
      OperationSources operationSources) {
    return operationSources.getSources();
  }

  @Provides AirdropChainIdMapper provideAirdropChainIdMapper(NetworkInfo networkInfo) {
    return new AirdropChainIdMapper(networkInfo);
  }

  @Singleton @Provides Billing provideBillingFactory(WalletService walletService,
      BdsRepository bdsRepository) {
    return new BdsBilling(bdsRepository, walletService, new BillingThrowableCodeMapper());
  }

  @Singleton @Provides AppcoinsRewards provideAppcoinsRewards(WalletService walletService,
      Billing billing, BackendApi backendApi, RemoteRepository remoteRepository) {
    return new AppcoinsRewards(
        new BdsAppcoinsRewardsRepository(new CreditsRemoteRepository(backendApi, remoteRepository)),
        new com.appcoins.wallet.appcoins.rewards.repository.WalletService() {
          @Override public @NotNull Single<String> getWalletAddress() {
            return walletService.getWalletAddress();
          }

          @Override public @NotNull Single<String> signContent(@NotNull String content) {
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

  @Provides Environment provideAdyenEnvironment() {
    if (BuildConfig.DEBUG) {
      return Environment.TEST;
    } else {
      return Environment.EUROPE;
    }
  }

  @Singleton @Provides SharedPreferences provideSharedPreferences(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  @Provides Gamification provideGamification(PromotionsRepository promotionsRepository) {
    return new Gamification(promotionsRepository);
  }

  @Singleton @Provides BalanceGetter provideBalanceGetter(AppcoinsRewards appcoinsRewards) {
    return new BalanceGetter() {
      @Override public @NotNull Single<BigDecimal> getBalance(@NotNull String address) {
        return appcoinsRewards.getBalance(address)
            .subscribeOn(Schedulers.io());
      }

      @Override public @NotNull Single<BigDecimal> getBalance() {
        return null;
      }
    };
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

  @Singleton @Provides @Named("rakam_event_list") List<String> provideRakamEventList() {
    List<String> list = new ArrayList<>();
    list.add(BillingAnalytics.RAKAM_PRESELECTED_PAYMENT_METHOD);
    list.add(BillingAnalytics.RAKAM_PAYMENT_METHOD);
    list.add(BillingAnalytics.RAKAM_PAYMENT_CONFIRMATION);
    list.add(BillingAnalytics.RAKAM_PAYMENT_CONCLUSION);
    list.add(BillingAnalytics.RAKAM_PAYMENT_START);
    list.add(BillingAnalytics.RAKAM_PAYPAL_URL);
    list.add(TopUpAnalytics.WALLET_TOP_UP_START);
    list.add(TopUpAnalytics.WALLET_TOP_UP_SELECTION);
    list.add(TopUpAnalytics.WALLET_TOP_UP_CONFIRMATION);
    list.add(TopUpAnalytics.WALLET_TOP_UP_CONCLUSION);
    list.add(TopUpAnalytics.WALLET_TOP_UP_PAYPAL_URL);
    list.add(PoaAnalytics.RAKAM_POA_EVENT);
    list.add(WalletValidationAnalytics.WALLET_PHONE_NUMBER_VERIFICATION);
    list.add(WalletValidationAnalytics.WALLET_CODE_VERIFICATION);
    list.add(WalletValidationAnalytics.WALLET_VERIFICATION_CONFIRMATION);
    list.add(WalletsAnalytics.WALLET_CREATE_BACKUP);
    list.add(WalletsAnalytics.WALLET_SAVE_BACKUP);
    list.add(WalletsAnalytics.WALLET_CONFIRMATION_BACKUP);
    list.add(WalletsAnalytics.WALLET_SAVE_FILE);
    list.add(WalletsAnalytics.WALLET_IMPORT_RESTORE);
    list.add(WalletsAnalytics.WALLET_PASSWORD_RESTORE);
    return list;
  }

  @Singleton @Provides AnalyticsManager provideAnalyticsManager(OkHttpClient okHttpClient,
      AnalyticsAPI api, Context context, @Named("bi_event_list") List<String> biEventList,
      @Named("facebook_event_list") List<String> facebookEventList,
      @Named("rakam_event_list") List<String> rakamEventList) {

    return new AnalyticsManager.Builder().addLogger(new BackendEventLogger(api), biEventList)
        .addLogger(new FacebookEventLogger(AppEventsLogger.newLogger(context)), facebookEventList)
        .addLogger(new RakamEventLogger(), rakamEventList)
        .setAnalyticsNormalizer(new KeysNormalizer())
        .setDebugLogger(new LogcatAnalyticsLogger())
        .setKnockLogger(new HttpClientKnockLogger(okHttpClient))
        .build();
  }

  @Singleton @Provides WalletsEventSender provideWalletEventSender(AnalyticsManager analytics) {
    return new WalletsAnalytics(analytics);
  }

  @Singleton @Provides BillingAnalytics provideBillingAnalytics(AnalyticsManager analytics) {
    return new BillingAnalytics(analytics);
  }

  @Singleton @Provides PoaAnalytics providePoAAnalytics(AnalyticsManager analytics) {
    return new PoaAnalytics(analytics);
  }

  @Singleton @Provides PoaAnalyticsController providesPoaAnalyticsController() {
    return new PoaAnalyticsController(new CopyOnWriteArrayList<>());
  }

  @Singleton @Provides Permissions providesPermissions(Context context) {
    return new Permissions(new PermissionRepository(
        Room.databaseBuilder(context.getApplicationContext(), PermissionsDatabase.class,
            "permissions_database")
            .build()
            .permissionsDao()));
  }

  @Provides ObjectMapper providesObjectMapper() {
    return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Provides TopUpValuesApiResponseMapper providesTopUpValuesApiResponseMapper() {
    return new TopUpValuesApiResponseMapper();
  }

  @Singleton @Provides TransactionsAnalytics providesTransactionsAnalytics(
      @NotNull AnalyticsManager analytics) {
    return new TransactionsAnalytics(analytics);
  }

  @Singleton @Provides GamificationAnalytics provideGamificationAnalytics(
      AnalyticsManager analytics) {
    return new GamificationAnalytics(analytics);
  }

  @Provides OffChainTransactions providesOffChainTransactions(
      OffChainTransactionsRepository repository, TransactionsMapper mapper) {
    return new OffChainTransactions(repository, mapper, getVersionCode());
  }

  private String getVersionCode() {
    return String.valueOf(com.asf.wallet.BuildConfig.VERSION_CODE);
  }

  @Provides TransactionsMapper provideTransactionsMapper(DefaultTokenProvider defaultTokenProvider,
      AppCoinsOperationRepository appCoinsOperationRepository) {
    return new TransactionsMapper(defaultTokenProvider, appCoinsOperationRepository,
        Schedulers.io());
  }

  @Singleton @Provides NotificationManager provideNotificationManager(Context context) {
    return (NotificationManager) context.getApplicationContext()
        .getSystemService(NOTIFICATION_SERVICE);
  }

  @Singleton @Provides @Named("heads_up") Builder provideHeadsUpNotificationBuilder(Context context,
      NotificationManager notificationManager) {
    Builder builder;
    String channelId = "notification_channel_heads_up_id";
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      CharSequence channelName = "Notification channel";
      int importance = NotificationManager.IMPORTANCE_HIGH;
      NotificationChannel notificationChannel =
          new NotificationChannel(channelId, channelName, importance);
      builder = new Builder(context, channelId);

      notificationManager.createNotificationChannel(notificationChannel);
    } else {
      builder = new Builder(context, channelId);
      builder.setVibrate(new long[0]);
    }
    return builder.setContentTitle(context.getString(string.app_name))
        .setSmallIcon(drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setAutoCancel(true);
  }

  @Singleton @Provides IExtract provideIExtract() {
    return new Extractor(new ExtractorV1(), new ExtractorV2());
  }

  @Singleton @Provides PackageManager providePackageManager(Context context) {
    return context.getPackageManager();
  }

  @Singleton @Provides AutoUpdateService.AutoUpdateApi provideAutoUpdateApi(OkHttpClient client,
      Gson gson) {
    String baseUrl = BuildConfig.BACKEND_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(AutoUpdateService.AutoUpdateApi.class);
  }

  @Provides @Named("local_version_code") int provideLocalVersionCode(Context context,
      PackageManager packageManager) {
    try {
      return packageManager.getPackageInfo(context.getPackageName(), 0).versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      return -1;
    }
  }

  @Provides UpdateNavigator provideUpdateNavigator() {
    return new UpdateNavigator();
  }

  @Singleton @Provides SupportSharedPreferences provideSupportSharedPreferences(
      SharedPreferences preferences) {
    return new SupportSharedPreferences(preferences);
  }

  @Singleton @Provides RakamAnalytics provideRakamAnalyticsSetup(Context context,
      IdsRepository idsRepository, Logger logger) {
    return new RakamAnalytics(context, idsRepository, logger);
  }

  @Singleton @Provides TopUpAnalytics provideTopUpAnalytics(AnalyticsManager analyticsManager) {
    return new TopUpAnalytics(analyticsManager);
  }

  @Singleton @Provides CurrencyFormatUtils provideCurrencyFormatUtils() {
    return CurrencyFormatUtils.Companion.create();
  }

  @Singleton @Provides WalletValidationAnalytics provideWalletValidationAnalytics(
      AnalyticsManager analyticsManager) {
    return new WalletValidationAnalytics(analyticsManager);
  }

  @Provides ContentResolver provideContentResolver(Context context) {
    return context.getContentResolver();
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
}