package com.asfoundation.wallet.poa;

import com.appcoins.wallet.bdsbilling.WalletService;
import com.appcoins.wallet.commons.MemoryCache;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.advertise.AdvertisingThrowableCodeMapper;
import com.asfoundation.wallet.advertise.CampaignInteract;
import com.asfoundation.wallet.billing.partners.AddressService;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.AutoUpdateInteract;
import com.asfoundation.wallet.interact.WalletCreatorInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.repository.BdsBackEndWriter;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.repository.WalletNotFoundException;
import com.asfoundation.wallet.service.Campaign;
import com.asfoundation.wallet.service.CampaignService;
import com.asfoundation.wallet.service.CampaignStatus;
import com.asfoundation.wallet.viewmodel.AutoUpdateModel;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProofOfAttentionServiceTest {

  private static final String SUBMIT_HASH = "hash";
  private static final String STORE_ADDRESS = "store_address";
  private static final String OEM_ADDRESS = "oem_address";

  @Mock FindDefaultWalletInteract defaultWalletInteract;
  @Mock CampaignService campaignService;
  @Mock HashCalculator hashCalculator;
  @Mock AddressService addressService;
  @Mock WalletCreatorInteract walletInteract;
  @Mock FindDefaultWalletInteract findDefaultWalletInteract;
  @Mock AdvertisingThrowableCodeMapper mapper;
  @Mock WalletService walletService;
  @Mock PreferencesRepositoryType preferences;
  @Mock AutoUpdateInteract autoUpdateInteract;
  private int chainId;
  private ProofOfAttentionService proofOfAttentionService;
  private MemoryCache<String, Proof> cache;
  private int maxNumberProofComponents = 3;
  private long nonce;
  private String packageName = "package";
  private int versionCode = 0;
  private TestScheduler testScheduler;
  private BehaviorSubject<Wallet> hasWallet;
  private String wallet;

  @Before public void before() throws NoSuchAlgorithmException {
    MockitoAnnotations.initMocks(this);
    hasWallet = BehaviorSubject.create();
    cache = new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>());
    testScheduler = new TestScheduler();
    ProofWriter proofWriter = new BdsBackEndWriter(defaultWalletInteract, campaignService);
    CampaignInteract campaignInteract =
        new CampaignInteract(campaignService, walletService, autoUpdateInteract, mapper,
            defaultWalletInteract, preferences);
    proofOfAttentionService =
        new ProofOfAttentionService(cache, BuildConfig.APPLICATION_ID, hashCalculator,
            new CompositeDisposable(), proofWriter, testScheduler, maxNumberProofComponents,
            new BackEndErrorMapper(), new TaggedCompositeDisposable(new HashMap<>()),
            () -> Single.just("PT"), addressService, walletService, campaignInteract);
    if (BuildConfig.DEBUG) {
      chainId = 3;
    } else {
      chainId = 1;
    }
    nonce = 1L;
    wallet = "wallet_address";
    when(defaultWalletInteract.find()).thenReturn(hasWallet.firstOrError());

    String campaignId = "110";
    when(campaignService.getCampaign(wallet, packageName, versionCode)).thenReturn(
        Single.just(new Campaign(campaignId, CampaignStatus.AVAILABLE, 0, 0)));

    when(campaignService.submitProof(any(Proof.class), eq(wallet))).thenReturn(
        Single.just(SUBMIT_HASH));
    when(hashCalculator.calculateNonce(any(NonceData.class))).thenReturn(nonce);

    when(addressService.getStoreAddressForPackage(any())).thenReturn(Single.just(STORE_ADDRESS));

    when(addressService.getOemAddressForPackage(any())).thenReturn(Single.just(OEM_ADDRESS));

    when(autoUpdateInteract.getAutoUpdateModel(true)).thenReturn(
        Single.just(new AutoUpdateModel()));
  }

  @Test public void setCampaignId() {
    String packageName = "packageName";
    String campaignId = "campaignId";
    TestObserver<Proof> testObserver = new TestObserver<>();
    proofOfAttentionService.setCampaignId(packageName, campaignId);
    testScheduler.triggerActions();
    cache.get(packageName)
        .subscribe(testObserver);
    testObserver.assertNoErrors()
        .assertValueCount(1)
        .assertValue(
            new Proof(packageName, campaignId, Collections.emptyList(), BuildConfig.APPLICATION_ID,
                ProofStatus.PROCESSING, 1, null, null, null));
  }

  @Test public void registerProof() {
    String packageName = "packageName";
    String campaignId = "campaignId";
    String walletPackage = BuildConfig.APPLICATION_ID;
    int timeStamp = 10;
    long timeStamp2 = 20;

    TestObserver<Proof> testObserver = new TestObserver<>();
    proofOfAttentionService.setCampaignId(packageName, campaignId);
    cache.get(packageName)
        .subscribe(testObserver);
    testScheduler.triggerActions();
    testObserver.assertNoErrors()
        .assertValueCount(1)
        .assertValue(new Proof(packageName, campaignId, Collections.emptyList(), walletPackage,
            ProofStatus.PROCESSING, 1, null, null, null));

    proofOfAttentionService.registerProof(packageName, timeStamp);

    testScheduler.triggerActions();
    List<ProofComponent> proofComponents = new ArrayList<>();
    proofComponents.add(new ProofComponent(timeStamp, nonce));
    Assert.assertEquals(testObserver.assertValueCount(2)
            .values()
            .get(1),
        new Proof(packageName, campaignId, proofComponents, walletPackage, ProofStatus.PROCESSING,
            1, null, null, null));

    proofOfAttentionService.registerProof(packageName, timeStamp2);
    testScheduler.triggerActions();

    proofComponents.add(new ProofComponent(timeStamp2, nonce));
    Assert.assertEquals(testObserver.assertValueCount(3)
            .values()
            .get(2),
        new Proof(packageName, campaignId, proofComponents, walletPackage, ProofStatus.PROCESSING,
            1, null, null, null));
  }

  @Test public void registerProofWithoutCampaignId() {
    String packageName = "packageName";
    String walletPackage = BuildConfig.APPLICATION_ID;
    int timeStamp = 10;

    TestObserver<Proof> testObserver = new TestObserver<>();
    cache.get(packageName)
        .subscribe(testObserver);

    proofOfAttentionService.registerProof(packageName, timeStamp);
    testScheduler.triggerActions();
    List<ProofComponent> proofComponents = new ArrayList<>();
    proofComponents.add(new ProofComponent(timeStamp, nonce));
    Assert.assertEquals(testObserver.assertValueCount(1)
            .values()
            .get(0),
        new Proof(packageName, null, proofComponents, walletPackage, ProofStatus.PROCESSING, 1,
            null, null, null));
  }

  @Test public void registerProofMaxComponents() {
    String packageName = "packageName";
    int timeStamp = 10;

    TestObserver<Proof> testObserver = new TestObserver<>();
    cache.get(packageName)
        .subscribe(testObserver);

    proofOfAttentionService.registerProof(packageName, timeStamp);
    proofOfAttentionService.registerProof(packageName, timeStamp);
    proofOfAttentionService.registerProof(packageName, timeStamp);
    testScheduler.triggerActions();
    testObserver.assertValueCount(3);
    Assert.assertEquals(testObserver.values()
        .get(2)
        .getProofComponentList()
        .size(), maxNumberProofComponents);
  }

  @Test public void getCompletedPoA() {
    String packageName = "packageName";
    String campaignId = "campaignId";
    int timeStamp = 10;

    hasWallet.onNext(new Wallet(wallet));

    proofOfAttentionService.start();

    TestObserver<Proof> cacheObserver = new TestObserver<>();
    cache.get(packageName)
        .subscribe(cacheObserver);

    proofOfAttentionService.setCampaignId(packageName, campaignId);

    proofOfAttentionService.registerProof(packageName, timeStamp);
    proofOfAttentionService.registerProof(packageName, timeStamp);
    proofOfAttentionService.registerProof(packageName, timeStamp);

    testScheduler.triggerActions();
    cacheObserver.assertNoErrors()
        .assertValueCount(7);
    Proof value = cacheObserver.values()
        .get(6);
    verify(campaignService, times(1)).submitProof(
        new Proof(value.getPackageName(), value.getCampaignId(), value.getProofComponentList(),
            value.getWalletPackage(), ProofStatus.SUBMITTING, 1, null, null, null, "PT"), wallet);

    Assert.assertEquals(
        new Proof(value.getPackageName(), value.getCampaignId(), value.getProofComponentList(),
            value.getWalletPackage(), ProofStatus.COMPLETED, 1, null, null, SUBMIT_HASH, "PT"),
        value);

    proofOfAttentionService.stop();
  }

  @Test public void get() {
    String packageName = "packageName";
    String campaignId = "campaignId";
    int timeStamp = 10;
    hasWallet.onNext(new Wallet(wallet));

    proofOfAttentionService.start();

    TestObserver<List<Proof>> observer = new TestObserver<>();
    proofOfAttentionService.get()
        .subscribe(observer);

    proofOfAttentionService.setCampaignId(packageName, campaignId);

    proofOfAttentionService.registerProof(packageName, timeStamp);
    proofOfAttentionService.registerProof(packageName, timeStamp);
    proofOfAttentionService.registerProof(packageName, timeStamp);
    testScheduler.triggerActions();

    observer.assertNoErrors()
        .assertValueCount(7);
    Proof proof = observer.values()
        .get(0)
        .get(0);

    Assert.assertEquals(ProofStatus.PROCESSING, proof.getProofStatus());
    Assert.assertEquals(campaignId, proof.getCampaignId());

    proof = observer.values()
        .get(1)
        .get(0);
    Assert.assertEquals(ProofStatus.PROCESSING, proof.getProofStatus());
    Assert.assertEquals(campaignId, proof.getCampaignId());
    Assert.assertEquals(1, proof.getProofComponentList()
        .size());

    proof = observer.values()
        .get(2)
        .get(0);
    Assert.assertEquals(ProofStatus.PROCESSING, proof.getProofStatus());
    Assert.assertEquals(campaignId, proof.getCampaignId());
    Assert.assertEquals(2, proof.getProofComponentList()
        .size());

    proof = observer.values()
        .get(3)
        .get(0);
    Assert.assertEquals(ProofStatus.PROCESSING, proof.getProofStatus());
    Assert.assertEquals(campaignId, proof.getCampaignId());
    Assert.assertEquals(3, proof.getProofComponentList()
        .size());

    proof = observer.values()
        .get(4)
        .get(0);
    Assert.assertEquals(ProofStatus.PROCESSING, proof.getProofStatus());
    Assert.assertEquals(campaignId, proof.getCampaignId());
    Assert.assertEquals(3, proof.getProofComponentList()
        .size());

    proof = observer.values()
        .get(5)
        .get(0);
    Assert.assertEquals(ProofStatus.SUBMITTING, proof.getProofStatus());
    Assert.assertEquals(campaignId, proof.getCampaignId());
    Assert.assertEquals(3, proof.getProofComponentList()
        .size());

    proof = observer.values()
        .get(6)
        .get(0);
    Assert.assertEquals(ProofStatus.COMPLETED, proof.getProofStatus());
    Assert.assertEquals(campaignId, proof.getCampaignId());
    Assert.assertEquals(3, proof.getProofComponentList()
        .size());
  }

  @Test public void setChainId() {
    String packageName = "packageName";
    TestObserver<Proof> testObserver = new TestObserver<>();
    proofOfAttentionService.setChainId(packageName, 2);
    cache.get(packageName)
        .subscribe(testObserver);
    testScheduler.triggerActions();
    testObserver.assertNoErrors()
        .assertValueCount(1)
        .assertValue(
            new Proof(packageName, null, Collections.emptyList(), BuildConfig.APPLICATION_ID,
                ProofStatus.PROCESSING, 2, null, null, null));
  }

  @Test public void isWalletReady() {
    TestObserver<ProofSubmissionData> ready =
        proofOfAttentionService.isWalletReady(chainId, packageName, versionCode)
            .subscribeOn(testScheduler)
            .test();
    ProofSubmissionData readyFee =
        new ProofSubmissionData(ProofSubmissionData.RequirementsStatus.READY);
    hasWallet.onNext(new Wallet(wallet));
    testScheduler.triggerActions();
    ready.assertComplete()
        .assertNoErrors()
        .assertValue(readyFee);
  }

  @Test public void noWalletReady() {
    TestObserver<ProofSubmissionData> noWallet =
        proofOfAttentionService.isWalletReady(chainId, packageName, versionCode)
            .subscribeOn(testScheduler)
            .test();
    ProofSubmissionData noWalletFee =
        new ProofSubmissionData(ProofSubmissionData.RequirementsStatus.NO_WALLET);
    hasWallet.onError(new WalletNotFoundException());
    testScheduler.triggerActions();
    noWallet.assertComplete()
        .assertNoErrors()
        .assertValue(noWalletFee);
  }

  @Test public void alreadyRewardedTest() {
    when(campaignService.getCampaign(wallet, packageName, versionCode)).thenReturn(
        Single.just(new Campaign("", CampaignStatus.NOT_ELIGIBLE, 0, 0)));
    TestObserver<ProofSubmissionData> noFunds =
        proofOfAttentionService.isWalletReady(chainId, packageName, versionCode)
            .subscribeOn(testScheduler)
            .test();
    ProofSubmissionData noFundsFee =
        new ProofSubmissionData(ProofSubmissionData.RequirementsStatus.NOT_ELIGIBLE, 0, 0);
    hasWallet.onNext(new Wallet(wallet));
    testScheduler.triggerActions();
    noFunds.assertComplete()
        .assertNoErrors()
        .assertValue(noFundsFee);
  }

  @Test public void noNetwork() {
    TestObserver<ProofSubmissionData> noFunds =
        proofOfAttentionService.isWalletReady(chainId, packageName, versionCode)
            .subscribeOn(testScheduler)
            .test();
    ProofSubmissionData noFundsFee =
        new ProofSubmissionData(ProofSubmissionData.RequirementsStatus.NO_NETWORK, 0, 0);
    hasWallet.onError(new UnknownHostException());
    testScheduler.triggerActions();
    noFunds.assertComplete()
        .assertValue(noFundsFee);
  }

  @Test public void wrongNetwork() {
    int wrongChainId;
    wrongChainId = chainId == 3 ? 1 : 3;
    TestObserver<ProofSubmissionData> noFunds =
        proofOfAttentionService.isWalletReady(wrongChainId, packageName, versionCode)
            .subscribeOn(testScheduler)
            .test();
    ProofSubmissionData wrongNetwork =
        new ProofSubmissionData(ProofSubmissionData.RequirementsStatus.WRONG_NETWORK);
    hasWallet.onError(new UnknownHostException());
    testScheduler.triggerActions();
    noFunds.assertComplete()
        .assertNoErrors()
        .assertValue(wrongNetwork);
  }

  @Test public void unknownNetwork() {
    TestObserver<ProofSubmissionData> noFunds =
        proofOfAttentionService.isWalletReady(-1, packageName, versionCode)
            .subscribeOn(testScheduler)
            .test();
    ProofSubmissionData wrongNetwork =
        new ProofSubmissionData(ProofSubmissionData.RequirementsStatus.UNKNOWN_NETWORK);
    hasWallet.onError(new UnknownHostException());
    testScheduler.triggerActions();
    noFunds.assertComplete()
        .assertNoErrors()
        .assertValue(wrongNetwork);
  }

  @Test public void formatMinutes() {
    String format2Digit = String.format("%02d", 10);
    String format1Digit = String.format("%02d", 5);
    String formatZero = String.format("%02d", 0);
    Assert.assertEquals(format1Digit, "05");
    Assert.assertEquals(format2Digit, "10");
    Assert.assertNotEquals(format1Digit, "5");
    Assert.assertEquals(formatZero, "00");
  }
}