package com.asfoundation.wallet.di;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
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
import com.appcoins.wallet.bdsbilling.repository.BdsApiResponseMapper;
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary;
import com.appcoins.wallet.bdsbilling.repository.BdsRepository;
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository;
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository.BdsApi;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository;
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper;
import com.appcoins.wallet.commons.MemoryCache;
import com.appcoins.wallet.gamification.Gamification;
import com.appcoins.wallet.gamification.repository.BdsPromotionsRepository;
import com.appcoins.wallet.gamification.repository.GamificationApi;
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
import com.asfoundation.wallet.Airdrop;
import com.asfoundation.wallet.AirdropService;
import com.asfoundation.wallet.AirdropService.Api;
import com.asfoundation.wallet.App;
import com.asfoundation.wallet.FlurryLogger;
import com.asfoundation.wallet.Logger;
import com.asfoundation.wallet.advertise.AdvertisingThrowableCodeMapper;
import com.asfoundation.wallet.advertise.CampaignInteract;
import com.asfoundation.wallet.advertise.PoaAnalyticsController;
import com.asfoundation.wallet.analytics.AnalyticsAPI;
import com.asfoundation.wallet.analytics.BackendEventLogger;
import com.asfoundation.wallet.analytics.FacebookEventLogger;
import com.asfoundation.wallet.analytics.HttpClientKnockLogger;
import com.asfoundation.wallet.analytics.KeysNormalizer;
import com.asfoundation.wallet.analytics.LogcatAnalyticsLogger;
import com.asfoundation.wallet.analytics.RakamAnalyticsSetup;
import com.asfoundation.wallet.analytics.RakamEventLogger;
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics;
import com.asfoundation.wallet.apps.Applications;
import com.asfoundation.wallet.billing.CreditsRemoteRepository;
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.billing.analytics.PoaAnalytics;
import com.asfoundation.wallet.billing.partners.AddressService;
import com.asfoundation.wallet.billing.partners.BdsPartnersApi;
import com.asfoundation.wallet.billing.partners.InstallerService;
import com.asfoundation.wallet.billing.partners.InstallerSourceService;
import com.asfoundation.wallet.billing.partners.OemIdExtractorService;
import com.asfoundation.wallet.billing.partners.OemIdExtractorV1;
import com.asfoundation.wallet.billing.partners.OemIdExtractorV2;
import com.asfoundation.wallet.billing.partners.PartnerAddressService;
import com.asfoundation.wallet.billing.partners.PartnerWalletAddressService;
import com.asfoundation.wallet.billing.partners.WalletAddressService;
import com.asfoundation.wallet.billing.purchase.InAppDeepLinkRepository;
import com.asfoundation.wallet.billing.purchase.LocalPayementsLinkRepository;
import com.asfoundation.wallet.billing.purchase.LocalPayementsLinkRepository.DeepLinkApi;
import com.asfoundation.wallet.billing.share.BdsShareLinkRepository;
import com.asfoundation.wallet.billing.share.BdsShareLinkRepository.BdsShareLinkApi;
import com.asfoundation.wallet.billing.share.ShareLinkRepository;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.identification.IdsRepository;
import com.asfoundation.wallet.interact.AutoUpdateInteract;
import com.asfoundation.wallet.interact.BalanceGetter;
import com.asfoundation.wallet.interact.BuildConfigDefaultTokenProvider;
import com.asfoundation.wallet.interact.CreateWalletInteract;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.FetchCreditsInteract;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.GetDefaultWalletBalance;
import com.asfoundation.wallet.interact.PaymentReceiverInteract;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.interact.SmsValidationInteract;
import com.asfoundation.wallet.navigator.UpdateNavigator;
import com.asfoundation.wallet.permissions.PermissionsInteractor;
import com.asfoundation.wallet.permissions.repository.PermissionRepository;
import com.asfoundation.wallet.permissions.repository.PermissionsDatabase;
import com.asfoundation.wallet.poa.BackEndErrorMapper;
import com.asfoundation.wallet.poa.BlockchainErrorMapper;
import com.asfoundation.wallet.poa.Calculator;
import com.asfoundation.wallet.poa.CountryCodeProvider;
import com.asfoundation.wallet.poa.DataMapper;
import com.asfoundation.wallet.poa.HashCalculator;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.poa.ProofWriter;
import com.asfoundation.wallet.poa.TaggedCompositeDisposable;
import com.asfoundation.wallet.promotions.PromotionsInteractor;
import com.asfoundation.wallet.promotions.PromotionsInteractorContract;
import com.asfoundation.wallet.referrals.ReferralInteractor;
import com.asfoundation.wallet.referrals.ReferralInteractorContract;
import com.asfoundation.wallet.referrals.SharedPreferencesReferralLocalData;
import com.asfoundation.wallet.repository.ApproveService;
import com.asfoundation.wallet.repository.ApproveTransactionValidatorBds;
import com.asfoundation.wallet.repository.AutoUpdateRepository;
import com.asfoundation.wallet.repository.BackendTransactionRepository;
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
import com.asfoundation.wallet.repository.IpCountryCodeProvider.IpApi;
import com.asfoundation.wallet.repository.NoValidateTransactionValidator;
import com.asfoundation.wallet.repository.OffChainTransactions;
import com.asfoundation.wallet.repository.OffChainTransactionsRepository;
import com.asfoundation.wallet.repository.OffChainTransactionsRepository.TransactionsApi;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.repository.SharedPreferencesRepository;
import com.asfoundation.wallet.repository.SignDataStandardNormalizer;
import com.asfoundation.wallet.repository.SmsValidationRepositoryType;
import com.asfoundation.wallet.repository.TokenRepositoryType;
import com.asfoundation.wallet.repository.TrackTransactionService;
import com.asfoundation.wallet.repository.TransactionMapper;
import com.asfoundation.wallet.repository.TransactionRepositoryType;
import com.asfoundation.wallet.repository.TransactionsDao;
import com.asfoundation.wallet.repository.TransactionsDatabase;
import com.asfoundation.wallet.repository.TransactionsLocalRepository;
import com.asfoundation.wallet.repository.TransactionsRepository;
import com.asfoundation.wallet.repository.TrustPasswordStore;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.repository.WatchedTransactionService;
import com.asfoundation.wallet.repository.Web3jProvider;
import com.asfoundation.wallet.router.GasSettingsRouter;
import com.asfoundation.wallet.service.AccountKeystoreService;
import com.asfoundation.wallet.service.AccountWalletService;
import com.asfoundation.wallet.service.AppsApi;
import com.asfoundation.wallet.service.AutoUpdateService;
import com.asfoundation.wallet.service.BDSAppsApi;
import com.asfoundation.wallet.service.CampaignService;
import com.asfoundation.wallet.service.CampaignService.CampaignApi;
import com.asfoundation.wallet.service.GasService;
import com.asfoundation.wallet.service.LocalCurrencyConversionService;
import com.asfoundation.wallet.service.LocalCurrencyConversionService.TokenToLocalFiatApi;
import com.asfoundation.wallet.service.SmsValidationApi;
import com.asfoundation.wallet.service.TokenRateService;
import com.asfoundation.wallet.service.TokenRateService.TokenToFiatApi;
import com.asfoundation.wallet.support.SupportInteractor;
import com.asfoundation.wallet.topup.TopUpAnalytics;
import com.asfoundation.wallet.topup.TopUpInteractor;
import com.asfoundation.wallet.topup.TopUpLimitValues;
import com.asfoundation.wallet.topup.TopUpValuesApiResponseMapper;
import com.asfoundation.wallet.topup.TopUpValuesService;
import com.asfoundation.wallet.topup.TopUpValuesService.TopUpValuesApi;
import com.asfoundation.wallet.transactions.TransactionsAnalytics;
import com.asfoundation.wallet.transactions.TransactionsMapper;
import com.asfoundation.wallet.ui.AppcoinsApps;
import com.asfoundation.wallet.ui.airdrop.AirdropChainIdMapper;
import com.asfoundation.wallet.ui.airdrop.AirdropInteractor;
import com.asfoundation.wallet.ui.airdrop.AppcoinsTransactionService;
import com.asfoundation.wallet.ui.balance.AppcoinsBalanceRepository;
import com.asfoundation.wallet.ui.balance.BalanceInteract;
import com.asfoundation.wallet.ui.balance.BalanceRepository;
import com.asfoundation.wallet.ui.balance.database.BalanceDetailsDatabase;
import com.asfoundation.wallet.ui.balance.database.BalanceDetailsMapper;
import com.asfoundation.wallet.ui.gamification.GamificationInteractor;
import com.asfoundation.wallet.ui.gamification.SharedPreferencesGamificationLocalData;
import com.asfoundation.wallet.ui.iab.AppCoinsOperationMapper;
import com.asfoundation.wallet.ui.iab.AppCoinsOperationRepository;
import com.asfoundation.wallet.ui.iab.AppInfoProvider;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver.OperationDataSource;
import com.asfoundation.wallet.ui.iab.ApproveKeyProvider;
import com.asfoundation.wallet.ui.iab.AsfInAppPurchaseInteractor;
import com.asfoundation.wallet.ui.iab.BdsInAppPurchaseInteractor;
import com.asfoundation.wallet.ui.iab.ImageSaver;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.asfoundation.wallet.ui.iab.LocalPaymentAnalytics;
import com.asfoundation.wallet.ui.iab.LocalPaymentInteractor;
import com.asfoundation.wallet.ui.iab.PaymentMethodsMapper;
import com.asfoundation.wallet.ui.iab.RewardsManager;
import com.asfoundation.wallet.ui.iab.database.AppCoinsOperationDatabase;
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer;
import com.asfoundation.wallet.ui.iab.raiden.NonceObtainerFactory;
import com.asfoundation.wallet.ui.iab.raiden.Web3jNonceProvider;
import com.asfoundation.wallet.ui.iab.share.ShareLinkInteractor;
import com.asfoundation.wallet.ui.onboarding.OnboardingInteract;
import com.asfoundation.wallet.ui.transact.TransactionDataValidator;
import com.asfoundation.wallet.ui.transact.TransferInteractor;
import com.asfoundation.wallet.util.CurrencyFormatUtils;
import com.asfoundation.wallet.util.DeviceInfo;
import com.asfoundation.wallet.util.EIPTransactionParser;
import com.asfoundation.wallet.util.LogInterceptor;
import com.asfoundation.wallet.util.OneStepTransactionParser;
import com.asfoundation.wallet.util.TransferParser;
import com.asfoundation.wallet.util.UserAgentInterceptor;
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract;
import com.asfoundation.wallet.wallet_blocked.WalletStatusApi;
import com.asfoundation.wallet.wallet_blocked.WalletStatusRepository;
import com.facebook.appevents.AppEventsLogger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.asfoundation.wallet.AirdropService.BASE_URL;
import static com.asfoundation.wallet.service.AppsApi.API_BASE_URL;

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

  @Singleton @Provides SharedPreferencesRepository providePreferencesRepository(Context context) {
    return new SharedPreferencesRepository(context);
  }

  @Singleton @Provides PreferencesRepositoryType providePreferenceRepositoryType(
      SharedPreferencesRepository sharedPreferenceRepository) {
    return sharedPreferenceRepository;
  }

  @Singleton @Provides PasswordStore passwordStore(Context context, Logger logger) {
    return new TrustPasswordStore(context, logger);
  }

  @Singleton @Provides Logger provideLogger() {
    return new FlurryLogger();
  }

  @Singleton @Provides BillingPaymentProofSubmission providesBillingPaymentProofSubmission(
      BdsApi api, WalletService walletService, BdsApiSecondary bdsApi) {
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
    return new FetchGasSettingsInteract(gasSettingsRepository, Schedulers.io(),
        AndroidSchedulers.mainThread());
  }

  @Provides FindDefaultWalletInteract provideFindDefaultWalletInteract(
      WalletRepositoryType walletRepository) {
    return new FindDefaultWalletInteract(walletRepository);
  }

  @Provides WalletBlockedInteract provideWalletBlockedInteract(
      FindDefaultWalletInteract findDefaultWalletInteract,
      WalletStatusRepository walletStatusRepository) {
    return new WalletBlockedInteract(findDefaultWalletInteract, walletStatusRepository);
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
        billingMessagesMapper, billing, currencyConversionService, bdsTransactionService,
        Schedulers.io());
  }

  @Singleton @Provides @Named("ASF_IN_APP_INTERACTOR")
  AsfInAppPurchaseInteractor provideAsfInAppPurchaseInteractor(
      @Named("ASF_IN_APP_PURCHASE_SERVICE") InAppPurchaseService inAppPurchaseService,
      FindDefaultWalletInteract defaultWalletInteract, FetchGasSettingsInteract gasSettingsInteract,
      TransferParser parser, Billing billing, CurrencyConversionService currencyConversionService,
      BdsTransactionService bdsTransactionService, BillingMessagesMapper billingMessagesMapper) {
    return new AsfInAppPurchaseInteractor(inAppPurchaseService, defaultWalletInteract,
        gasSettingsInteract, new BigDecimal(BuildConfig.PAYMENT_GAS_LIMIT), parser,
        billingMessagesMapper, billing, currencyConversionService, bdsTransactionService,
        Schedulers.io());
  }

  @Singleton @Provides InAppPurchaseInteractor provideDualInAppPurchaseInteractor(
      BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor,
      @Named("ASF_IN_APP_INTERACTOR") AsfInAppPurchaseInteractor asfInAppPurchaseInteractor,
      AppcoinsRewards appcoinsRewards, Billing billing, SharedPreferences sharedPreferences,
      PackageManager packageManager) {
    return new InAppPurchaseInteractor(asfInAppPurchaseInteractor, bdsInAppPurchaseInteractor,
        new ExternalBillingSerializer(), appcoinsRewards, billing, sharedPreferences,
        packageManager);
  }

  @Provides LocalPaymentInteractor provideLocalPaymentInteractor(InAppDeepLinkRepository repository,
      WalletService walletService, AddressService partnerAddressService,
      InAppPurchaseInteractor inAppPurchaseInteractor, Billing billing,
      BillingMessagesMapper billingMessagesMapper) {
    return new LocalPaymentInteractor(repository, walletService, partnerAddressService,
        inAppPurchaseInteractor, billing, billingMessagesMapper);
  }

  @Provides LocalPaymentAnalytics provideLocalPaymentAnalytics(BillingAnalytics billingAnalytics,
      InAppPurchaseInteractor inAppPurchaseInteractor) {
    return new LocalPaymentAnalytics(billingAnalytics, inAppPurchaseInteractor, Schedulers.io());
  }

  @Provides PaymentMethodsMapper providePaymentMethodsMapper() {
    return new PaymentMethodsMapper();
  }

  @Provides GetDefaultWalletBalance provideGetDefaultWalletBalance(
      WalletRepositoryType walletRepository, FindDefaultWalletInteract defaultWalletInteract,
      FetchCreditsInteract fetchCreditsInteract, NetworkInfo networkInfo,
      TokenRepositoryType tokenRepositoryType) {
    return new GetDefaultWalletBalance(walletRepository, defaultWalletInteract,
        fetchCreditsInteract, networkInfo, tokenRepositoryType);
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
      GasService gasService) {
    return new GasSettingsRepository(gasService);
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

  @Singleton @Provides ProofOfAttentionService provideProofOfAttentionService(
      HashCalculator hashCalculator, ProofWriter proofWriter, TaggedCompositeDisposable disposables,
      @Named("MAX_NUMBER_PROOF_COMPONENTS") int maxNumberProofComponents,
      CountryCodeProvider countryCodeProvider, AddressService addressService,
      CreateWalletInteract createWalletInteract,
      FindDefaultWalletInteract findDefaultWalletInteract, CampaignInteract campaignInteract) {
    return new ProofOfAttentionService(new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()),
        BuildConfig.APPLICATION_ID, hashCalculator, new CompositeDisposable(), proofWriter,
        Schedulers.computation(), maxNumberProofComponents, new BackEndErrorMapper(), disposables,
        countryCodeProvider, addressService, createWalletInteract, findDefaultWalletInteract,
        campaignInteract);
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

  @Singleton @Provides AppCoinsOperationRepository providesAppCoinsOperationRepository(
      Context context) {
    return new AppCoinsOperationRepository(
        Room.databaseBuilder(context.getApplicationContext(), AppCoinsOperationDatabase.class,
            "appcoins_operations_data")
            .build()
            .appCoinsOperationDao(), new AppCoinsOperationMapper());
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

  @Provides AirdropService provideAirdropService(OkHttpClient client, Gson gson) {
    Api api = new Retrofit.Builder().baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(Api.class);
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

  @Singleton @Provides BdsApi provideBdsApi(OkHttpClient client, Gson gson) {
    String baseUrl = BuildConfig.BASE_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(BdsApi.class);
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
    TokenToFiatApi api = new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(TokenToFiatApi.class);
    return new TokenRateService(api);
  }

  @Singleton @Provides LocalCurrencyConversionService provideLocalCurrencyConversionService(
      OkHttpClient client, ObjectMapper objectMapper) {
    String baseUrl = LocalCurrencyConversionService.CONVERSION_HOST;
    TokenToLocalFiatApi api = new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(TokenToLocalFiatApi.class);
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

  @Singleton @Provides RemoteRepository provideRemoteRepository(BdsApi bdsApi,
      BdsApiSecondary api) {
    return new RemoteRepository(bdsApi, new BdsApiResponseMapper(), api);
  }

  @Singleton @Provides ProxyService provideProxyService(AppCoinsAddressProxySdk proxySdk) {
    return new ProxyService() {
      private static final int NETWORK_ID_ROPSTEN = 3;
      private static final int NETWORK_ID_MAIN = 1;

      @Override public @NotNull Single<String> getAppCoinsAddress(boolean debug) {
        return proxySdk.getAppCoinsAddress(debug ? NETWORK_ID_ROPSTEN : NETWORK_ID_MAIN);
      }

      @Override public @NotNull Single<String> getIabAddress(boolean debug) {
        return proxySdk.getIabAddress(debug ? NETWORK_ID_ROPSTEN : NETWORK_ID_MAIN);
      }
    };
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

  @Provides AdyenPaymentInteractor provideAdyenPaymentInteractor(
      AdyenPaymentRepository adyenPaymentRepository,
      InAppPurchaseInteractor inAppPurchaseInteractor, AddressService partnerAddressService,
      Billing billing, WalletService walletService, SupportInteractor supportInteractor) {
    return new AdyenPaymentInteractor(adyenPaymentRepository, inAppPurchaseInteractor,
        inAppPurchaseInteractor.getBillingMessagesMapper(), partnerAddressService, billing,
        walletService, supportInteractor);
  }

  @Singleton @Provides AdyenPaymentRepository provideAdyenPaymentRepository(OkHttpClient client) {
    AdyenPaymentRepository.AdyenApi api = new Retrofit.Builder().baseUrl(
        BuildConfig.BASE_HOST + "/broker/8.20191202/gateways/adyen_v2/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(AdyenPaymentRepository.AdyenApi.class);
    return new AdyenPaymentRepository(api, new AdyenResponseMapper());
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

  @Singleton @Provides CampaignService providePoASubmissionService(OkHttpClient client) {
    String baseUrl = CampaignService.SERVICE_HOST;
    CampaignApi api = new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(CampaignApi.class);
    return new CampaignService(api, BuildConfig.VERSION_CODE, Schedulers.io());
  }

  @Provides Gamification provideGamification(PromotionsRepository promotionsRepository) {
    return new Gamification(promotionsRepository);
  }

  @Provides PromotionsRepository providePromotionsRepository(GamificationApi api,
      SharedPreferences preferences) {
    return new BdsPromotionsRepository(api, new SharedPreferencesGamificationLocalData(preferences),
        getVersionCode());
  }

  @Provides GamificationApi provideGamificationApi(OkHttpClient client) {
    String baseUrl = CampaignService.SERVICE_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(GamificationApi.class);
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
      @Override public @NotNull Single<BigDecimal> getBalance(@NotNull String address) {
        return appcoinsRewards.getBalance(address)
            .subscribeOn(Schedulers.io());
      }

      @Override public @NotNull Single<BigDecimal> getBalance() {
        return null;
      }
    };
  }

  @Singleton @Provides AnalyticsAPI provideAnalyticsAPI(OkHttpClient client,
      ObjectMapper objectMapper) {
    return new Retrofit.Builder().baseUrl("https://ws75.aptoide.com/api/7/")
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

  @Singleton @Provides @Named("rakam_event_list") List<String> provideRakamEventList() {
    List<String> list = new ArrayList<>();
    list.add(BillingAnalytics.RAKAM_PRESELECTED_PAYMENT_METHOD);
    list.add(BillingAnalytics.RAKAM_PAYMENT_METHOD);
    list.add(BillingAnalytics.RAKAM_PAYMENT_CONFIRMATION);
    list.add(BillingAnalytics.RAKAM_PAYMENT_CONCLUSION);
    list.add(BillingAnalytics.RAKAM_PAYMENT_START);
    list.add(TopUpAnalytics.WALLET_TOP_UP_START);
    list.add(TopUpAnalytics.WALLET_TOP_UP_SELECTION);
    list.add(TopUpAnalytics.WALLET_TOP_UP_CONFIRMATION);
    list.add(TopUpAnalytics.WALLET_TOP_UP_CONCLUSION);
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

  @Provides CreateWalletInteract provideCreateAccountInteract(
      WalletRepositoryType accountRepository, PasswordStore passwordStore) {
    return new CreateWalletInteract(accountRepository, passwordStore);
  }

  @Provides PaymentReceiverInteract providePaymentReceiverInteract(
      CreateWalletInteract createWalletInteract) {
    return new PaymentReceiverInteract(createWalletInteract);
  }

  @Provides OnboardingInteract provideOnboardingInteract(CreateWalletInteract createWalletInteract,
      WalletService walletService, PreferencesRepositoryType preferencesRepositoryType) {
    return new OnboardingInteract(createWalletInteract, walletService, preferencesRepositoryType);
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

  @Provides GamificationInteractor provideGamificationInteractor(Gamification gamification,
      FindDefaultWalletInteract defaultWallet, LocalCurrencyConversionService conversionService) {
    return new GamificationInteractor(gamification, defaultWallet, conversionService);
  }

  @Provides PromotionsInteractorContract providePromotionsInteractor(
      ReferralInteractorContract referralInteractor, GamificationInteractor gamificationInteractor,
      PromotionsRepository promotionsRepository,
      FindDefaultWalletInteract findDefaultWalletInteract) {
    return new PromotionsInteractor(referralInteractor, gamificationInteractor,
        promotionsRepository, findDefaultWalletInteract);
  }

  @Provides ReferralInteractorContract provideReferralInteractor(SharedPreferences preferences,
      FindDefaultWalletInteract findDefaultWalletInteract,
      PromotionsRepository promotionsRepository) {
    return new ReferralInteractor(new SharedPreferencesReferralLocalData(preferences),
        findDefaultWalletInteract, promotionsRepository);
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
      WalletAddressService addressService, OemIdExtractorService oemIdExtractorService) {
    return new PartnerAddressService(installerService, addressService,
        new DeviceInfo(Build.MANUFACTURER, Build.MODEL), oemIdExtractorService);
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
      FindDefaultWalletInteract interactor, InAppPurchaseInteractor inAppPurchaseInteractor) {
    return new ShareLinkInteractor(repository, interactor, inAppPurchaseInteractor);
  }

  @Singleton @Provides ShareLinkRepository providesShareLinkRepository(BdsShareLinkApi api) {
    return new BdsShareLinkRepository(api);
  }

  @Singleton @Provides BdsShareLinkApi provideBdsShareLinkApi(OkHttpClient client, Gson gson) {
    String baseUrl = BuildConfig.CATAPPULT_BASE_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(BdsShareLinkApi.class);
  }

  @Singleton @Provides InAppDeepLinkRepository providesDeepLinkRepository(DeepLinkApi api) {
    return new LocalPayementsLinkRepository(api);
  }

  @Singleton @Provides DeepLinkApi provideDeepLinkApi(OkHttpClient client, Gson gson) {
    String baseUrl = BuildConfig.CATAPPULT_BASE_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(DeepLinkApi.class);
  }

  @Singleton @Provides TopUpInteractor providesTopUpInteractor(BdsRepository repository,
      LocalCurrencyConversionService conversionService,
      GamificationInteractor gamificationInteractor, TopUpValuesService topUpValuesService) {
    return new TopUpInteractor(repository, conversionService, gamificationInteractor,
        topUpValuesService, new LinkedHashMap<>(), new TopUpLimitValues());
  }

  @Singleton @Provides TopUpValuesService providesTopUpValuesService(TopUpValuesApi topUpValuesApi,
      TopUpValuesApiResponseMapper responseMapper) {
    return new TopUpValuesService(topUpValuesApi, responseMapper);
  }

  @Singleton @Provides TopUpValuesApi providesTopUpValuesApi(OkHttpClient client, Gson gson) {
    String baseUrl = BuildConfig.CATAPPULT_BASE_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(TopUpValuesApi.class);
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

  @Provides OffChainTransactionsRepository providesOffChainTransactionsRepository(
      OkHttpClient client) {

    ObjectMapper objectMapper = new ObjectMapper();

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    objectMapper.setDateFormat(df);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    Retrofit retrofit =
        new Retrofit.Builder().addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .client(client)
            .baseUrl(com.asf.wallet.BuildConfig.BACKEND_HOST)
            .build();

    return new OffChainTransactionsRepository(retrofit.create(TransactionsApi.class),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US));
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

  @Singleton @Provides TransactionRepositoryType provideTransactionRepository(
      NetworkInfo networkInfo, AccountKeystoreService accountKeystoreService,
      DefaultTokenProvider defaultTokenProvider, MultiWalletNonceObtainer nonceObtainer,
      OffChainTransactions transactionsNetworkRepository, Context context,
      SharedPreferences sharedPreferences) {
    Migration MIGRATION_1_2 = new Migration(1, 2) {
      @Override public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS TransactionEntityCopy (transactionId TEXT NOT "
            + "NULL, relatedWallet TEXT NOT NULL, approveTransactionId TEXT, type TEXT NOT "
            + "NULL, timeStamp INTEGER NOT NULL, processedTime INTEGER NOT NULL, status "
            + "TEXT NOT NULL, value TEXT NOT NULL, `from` TEXT NOT NULL, `to` TEXT NOT NULL, "
            + "currency TEXT, operations TEXT, sourceName TEXT, description TEXT, "
            + "iconType TEXT, uri TEXT, PRIMARY KEY(transactionId, relatedWallet))");
        database.execSQL("INSERT INTO TransactionEntityCopy (transactionId, relatedWallet, "
            + "approveTransactionId, type, timeStamp, processedTime, status, value, `from`, `to`,"
            + " currency, operations, sourceName, description, iconType, uri) SELECT "
            + "transactionId, relatedWallet,approveTransactionId, type, timeStamp, processedTime,"
            + " status, value, `from`, `to`, currency, operations, sourceName, description, "
            + "iconType, uri FROM TransactionEntity");
        database.execSQL("DROP TABLE TransactionEntity");
        database.execSQL("ALTER TABLE TransactionEntityCopy RENAME TO TransactionEntity");
      }
    };
    TransactionsDao transactionsDao =
        Room.databaseBuilder(context.getApplicationContext(), TransactionsDatabase.class,
            "transactions_database")
            .addMigrations(MIGRATION_1_2)
            .build()
            .transactionsDao();
    TransactionsRepository localRepository =
        new TransactionsLocalRepository(transactionsDao, sharedPreferences);
    return new BackendTransactionRepository(networkInfo, accountKeystoreService,
        defaultTokenProvider, new BlockchainErrorMapper(), nonceObtainer, Schedulers.io(),
        transactionsNetworkRepository, localRepository, new TransactionMapper(),
        new CompositeDisposable(), Schedulers.io());
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

  @Singleton @Provides GasService provideGasService(OkHttpClient client, Gson gson) {
    return new Retrofit.Builder().baseUrl(GasService.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(GasService.class);
  }

  @Singleton @Provides WalletStatusApi provideWalletStatusApi(OkHttpClient client, Gson gson) {
    String baseUrl = BuildConfig.BACKEND_HOST;
    return new Retrofit.Builder().baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(WalletStatusApi.class);
  }

  @Singleton @Provides SmsValidationInteract provideSmsValidationInteract(
      SmsValidationRepositoryType smsValidationRepository,
      PreferencesRepositoryType preferencesRepositoryType) {
    return new SmsValidationInteract(smsValidationRepository, preferencesRepositoryType);
  }

  @Singleton @Provides BalanceInteract provideBalanceInteract(
      FindDefaultWalletInteract findDefaultWalletInteract, BalanceRepository balanceRepository) {
    return new BalanceInteract(findDefaultWalletInteract, balanceRepository);
  }

  @Singleton @Provides BalanceRepository provideBalanceRepository(Context context,
      LocalCurrencyConversionService localCurrencyConversionService,
      GetDefaultWalletBalance getDefaultWalletBalance) {
    return new AppcoinsBalanceRepository(getDefaultWalletBalance, localCurrencyConversionService,
        Room.databaseBuilder(context.getApplicationContext(), BalanceDetailsDatabase.class,
            "balance_details")
            .build()
            .balanceDetailsDao(), new BalanceDetailsMapper(), Schedulers.io());
  }

  @Singleton @Provides CampaignInteract provideCampaignInteract(CampaignService campaignService,
      WalletService walletService, CreateWalletInteract createWalletInteract,
      AutoUpdateInteract autoUpdateInteract, FindDefaultWalletInteract findDefaultWalletInteract,
      PreferencesRepositoryType sharedPreferences) {
    return new CampaignInteract(campaignService, walletService, createWalletInteract,
        autoUpdateInteract, new AdvertisingThrowableCodeMapper(), findDefaultWalletInteract,
        sharedPreferences);
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
        .setAutoCancel(true)
        .setOngoing(false);
  }

  @Singleton @Provides IExtract provideIExtract() {
    return new Extractor(new ExtractorV1(), new ExtractorV2());
  }

  @Singleton @Provides OemIdExtractorService provideOemIdExtractorService(Context context,
      IExtract extractor) {
    return new OemIdExtractorService(new OemIdExtractorV1(context),
        new OemIdExtractorV2(context, extractor));
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

  @Provides AutoUpdateService provideAutoUpdateService(
      AutoUpdateService.AutoUpdateApi autoUpdateApi) {
    return new AutoUpdateService(autoUpdateApi);
  }

  @Provides @Named("local_version_code") int provideLocalVersionCode(Context context,
      PackageManager packageManager) {
    try {
      return packageManager.getPackageInfo(context.getPackageName(), 0).versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      return -1;
    }
  }

  @Provides AutoUpdateRepository provideAutoUpdateRepository(AutoUpdateService autoUpdateService) {
    return new AutoUpdateRepository(autoUpdateService);
  }

  @Provides AutoUpdateInteract provideAutoUpdateInteract(AutoUpdateRepository autoUpdateRepository,
      @Named("local_version_code") int localVersionCode, PackageManager packageManager,
      PreferencesRepositoryType sharedPreferences, Context context) {
    return new AutoUpdateInteract(autoUpdateRepository, localVersionCode, Build.VERSION.SDK_INT,
        packageManager, context.getPackageName(), sharedPreferences);
  }

  @Provides UpdateNavigator provideUpdateNavigator() {
    return new UpdateNavigator();
  }

  @Singleton @Provides SupportInteractor provideSupportInteractor(SharedPreferences preferences) {
    return new SupportInteractor(preferences);
  }

  @Singleton @Provides IdsRepository provideIdsRepository(Context context,
      SharedPreferencesRepository sharedPreferencesRepository, InstallerService installerService) {
    return new IdsRepository(context.getContentResolver(), sharedPreferencesRepository,
        installerService);
  }

  @Singleton @Provides RakamAnalyticsSetup provideRakamAnalyticsSetup() {
    return new RakamAnalyticsSetup();
  }

  @Singleton @Provides TopUpAnalytics provideTopUpAnalytics(AnalyticsManager analyticsManager){
    return new TopUpAnalytics(analyticsManager);
  }

  @Singleton @Provides CurrencyFormatUtils provideCurrencyFormatUtils() {
    return CurrencyFormatUtils.Companion.create();
  }
}