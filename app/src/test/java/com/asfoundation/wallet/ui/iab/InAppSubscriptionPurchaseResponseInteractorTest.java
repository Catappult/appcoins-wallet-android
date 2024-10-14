package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission;
import com.appcoins.wallet.bdsbilling.ProxyService;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService;
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType;
import com.appcoins.wallet.core.network.microservices.model.Gateway;
import com.appcoins.wallet.core.network.microservices.model.Transaction;
import com.appcoins.wallet.core.utils.android_common.RxSchedulers;
import com.appcoins.wallet.core.utils.jvm_common.CountryCodeProvider;
import com.appcoins.wallet.core.utils.jvm_common.MemoryCache;
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue;
import com.appcoins.wallet.feature.changecurrency.data.currencies.LocalCurrencyConversionService;
import com.appcoins.wallet.feature.walletInfo.data.wallet.FindDefaultWalletInteract;
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet;
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.HasEnoughBalanceUseCase;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.repository.AllowanceService;
import com.asfoundation.wallet.repository.ApproveService;
import com.asfoundation.wallet.repository.BdsPendingTransactionService;
import com.asfoundation.wallet.repository.BdsTransactionProvider;
import com.asfoundation.wallet.repository.BdsTransactionService;
import com.asfoundation.wallet.repository.BuyService;
import com.asfoundation.wallet.repository.CurrencyConversionService;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.PaymentErrorMapper;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.asfoundation.wallet.repository.TransactionSender;
import com.asfoundation.wallet.repository.TransactionValidator;
import com.asfoundation.wallet.repository.WatchedTransactionService;
import com.asfoundation.wallet.service.TokenRateService;
import com.asfoundation.wallet.ui.iab.database.AppCoinsOperationEntity;
import com.asfoundation.wallet.util.EIPTransactionParser;
import com.asfoundation.wallet.util.FakeSchedulers;
import com.asfoundation.wallet.util.OneStepTransactionParser;
import com.asfoundation.wallet.util.TransferParser;
import com.google.gson.Gson;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by trinkes on 14/03/2018.
 */
public class InAppSubscriptionPurchaseResponseInteractorTest {

  private static final String APPROVE_HASH = "approve_hash";
  private static final String BUY_HASH = "buy_hash";
  private static final String PACKAGE_NAME = "package_name";
  private static final String APPLICATION_NAME = "application_name";
  private static final String ICON_PATH = "icon_path";
  private static final String SKU = "sku";
  private static final String UID = "uid";

  @Mock FetchGasSettingsInteract gasSettingsInteract;
  @Mock BdsTransactionProvider transactionProvider;
  @Mock SendTransactionInteract sendTransactionInteract;
  @Mock PendingTransactionService pendingTransactionService;
  @Mock FindDefaultWalletInteract defaultWalletInteract;
  @Mock AppInfoProvider appInfoProvider;
  @Mock TransactionSender transactionSender;
  @Mock TransactionValidator transactionValidator;
  @Mock DefaultTokenProvider defaultTokenProvider;
  @Mock CountryCodeProvider countryCodeProvider;
  @Mock Billing billing;
  @Mock BdsPendingTransactionService transactionService;
  @Mock ProxyService proxyService;
  @Mock TokenRateService conversionService;
  @Mock AddressService addressService;
  @Mock AllowanceService allowanceService;
  @Mock HasEnoughBalanceUseCase hasEnoughBalanceUseCase;
  private BdsInAppPurchaseInteractor inAppPurchaseInteractor;

  @Before public void before()
      throws AppInfoProvider.UnknownApplicationException, ImageSaver.SaveException {
    MockitoAnnotations.initMocks(this);
    BillingPaymentProofSubmission billingPaymentProofSubmission =
        Mockito.mock(BillingPaymentProofSubmission.class);

    Gson gson = new Gson();
    when(gasSettingsInteract.fetch(anyBoolean())).thenReturn(
        Single.just(new GasSettings(new BigDecimal(1), new BigDecimal(2))));

    when(sendTransactionInteract.approve(any(TransactionBuilder.class))).thenReturn(
        Single.just(APPROVE_HASH));

    when(countryCodeProvider.getCountryCode()).thenReturn(Single.just("PT"));
    when(transactionService.checkTransactionState(anyString())).thenReturn(
        Observable.just(new PendingTransaction(BUY_HASH, false)));
    when(sendTransactionInteract.buy(any(TransactionBuilder.class))).thenReturn(
        Single.just(BUY_HASH));

    TokenInfo tokenInfo =
        new TokenInfo("0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", "Appcoins", "APPC", 18);
    when(defaultTokenProvider.getDefaultToken()).thenReturn(Single.just(tokenInfo));

    PublishSubject<PendingTransaction> pendingApproveState = PublishSubject.create();
    PublishSubject<PendingTransaction> pendingBuyState = PublishSubject.create();
    when(pendingTransactionService.checkTransactionState(APPROVE_HASH)).thenReturn(
        pendingApproveState);
    when(pendingTransactionService.checkTransactionState(BUY_HASH)).thenReturn(pendingBuyState);

    when(defaultWalletInteract.find()).thenReturn(Single.just(new Wallet("wallet_address")));

    TestScheduler scheduler = new TestScheduler();
    RxSchedulers fakeSchedulers = new FakeSchedulers();

    when(transactionSender.send(any(TransactionBuilder.class))).thenReturn(Single.just(BUY_HASH));

    WatchedTransactionService buyTransactionService =
        new WatchedTransactionService(transactionSender,
            new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()),
            new PaymentErrorMapper(gson), scheduler, pendingTransactionService);

    WatchedTransactionService approveTransactionService =
        new WatchedTransactionService(transactionSender,
            new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()),
            new PaymentErrorMapper(gson), scheduler, pendingTransactionService);

    when(transactionValidator.validate(any())).thenReturn(
        Single.just(Transaction.Companion.notFound()));

    when(allowanceService.checkAllowance(any(), any(), any())).thenReturn(
        Single.just(BigDecimal.ZERO));

    InAppPurchaseService inAppPurchaseService =
        new InAppPurchaseService(new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()),
            new ApproveService(approveTransactionService, transactionValidator), allowanceService,
            new BuyService(buyTransactionService, transactionValidator, defaultTokenProvider,
                countryCodeProvider, addressService, billingPaymentProofSubmission), scheduler,
            new PaymentErrorMapper(new Gson()), hasEnoughBalanceUseCase, defaultTokenProvider);

    when(appInfoProvider.get(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
      Object[] arguments = invocation.getArguments();
      return new AppCoinsOperationEntity(((String) arguments[0]), ((String) arguments[0]),
          ((String) arguments[1]), APPLICATION_NAME, ICON_PATH, ((String) arguments[2]));
    });

    //noinspection unchecked
    when(transactionProvider.get(PACKAGE_NAME, SKU)).thenReturn(Single.just(
        new Transaction(UID, Transaction.Status.PROCESSING,
            new Gateway(Gateway.Name.appcoins, "", ""), null, null, "orderReference", null, "",
            null, null)), Single.just(new Transaction(UID, Transaction.Status.COMPLETED,
        new Gateway(Gateway.Name.appcoins, "", ""), null, null, "orderReference", null, "", null,
        null)));

    when(billing.getSkuTransaction(anyString(), anyString(), any(Scheduler.class),
        any(BillingSupportedType.class))).thenReturn(Single.just(
        new Transaction(UID, Transaction.Status.PENDING_SERVICE_AUTHORIZATION,
            new Gateway(Gateway.Name.appcoins, "", ""), null, null, "orderReference", null, "",
            null, null)));

    when(proxyService.getAppCoinsAddress(anyBoolean())).thenReturn(
        Single.just("0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3"));
    when(proxyService.getIabAddress(anyBoolean())).thenReturn(
        Single.just("0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3"));
    when(conversionService.getAppcRate(anyString())).thenReturn(
        Single.just(new FiatValue(new BigDecimal(2), "EUR", "")));

    EIPTransactionParser eipTransactionParser = new EIPTransactionParser(defaultTokenProvider);
    OneStepTransactionParser oneStepTransactionParser =
        new OneStepTransactionParser(proxyService, billing, defaultTokenProvider);

    AsfInAppPurchaseInteractor asfInAppPurchaseInteractor =
        new AsfInAppPurchaseInteractor(inAppPurchaseService, defaultWalletInteract,
            gasSettingsInteract, BigDecimal.ONE,
            new TransferParser(eipTransactionParser, oneStepTransactionParser),
            new BillingMessagesMapper(), billing,
            new CurrencyConversionService(Mockito.mock(TokenRateService.class),
                Mockito.mock(LocalCurrencyConversionService.class)),
            new BdsTransactionService(fakeSchedulers,
                new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()),
                new CompositeDisposable(), transactionService), fakeSchedulers);

    inAppPurchaseInteractor =
        new BdsInAppPurchaseInteractor(asfInAppPurchaseInteractor, billingPaymentProofSubmission,
            new ApproveKeyProvider(billing), billing);
  }

  @Test public void getTopUpChannelSuggestionValues() {
    List<BigDecimal> topUpChannelSuggestionValues =
        inAppPurchaseInteractor.getTopUpChannelSuggestionValues(new BigDecimal("7.2"));
    List<BigDecimal> list = new ArrayList<>();
    list.add(new BigDecimal("7.2"));
    list.add(new BigDecimal("10.0"));
    list.add(new BigDecimal("15.0"));
    list.add(new BigDecimal("25.0"));
    list.add(new BigDecimal("35.0"));
    Assert.assertEquals(list, topUpChannelSuggestionValues);
  }
}