package com.asfoundation.wallet.poa;

import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.repository.BlockChainWriter;
import com.asfoundation.wallet.repository.MemoryCache;
import com.asfoundation.wallet.repository.WalletNotFoundException;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProofOfAttentionServiceTest {

  @Mock BlockChainWriter blockChainWriter;
  @Mock HashCalculator hashCalculator;
  private ProofOfAttentionService proofOfAttentionService;
  private MemoryCache<String, Proof> cache;
  private int maxNumberProofComponents = 3;
  private long nonce;
  private TestScheduler testScheduler;

  @Before public void before() throws NoSuchAlgorithmException {
    MockitoAnnotations.initMocks(this);
    cache = new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>());
    testScheduler = new TestScheduler();
    proofOfAttentionService =
        new ProofOfAttentionService(cache, BuildConfig.APPLICATION_ID, hashCalculator,
            new CompositeDisposable(), blockChainWriter, testScheduler, maxNumberProofComponents,
            new BlockchainErrorMapper(), new TaggedCompositeDisposable(new HashMap<>()));

    nonce = 1L;
    when(hashCalculator.calculateNonce(any(NonceData.class))).thenReturn(nonce);
    when(blockChainWriter.writeProof(any(Proof.class))).thenReturn(Single.just("hash"));
    AtomicInteger i = new AtomicInteger();
    when(blockChainWriter.hasEnoughFunds()).thenReturn(Single.create(e -> {
      int index = i.getAndIncrement();
      if (index == 0) {
        e.onSuccess(true);
      }
      if (index == 1) {
        e.onSuccess(false);
      }
      if (index > 1) {
        e.onError(new WalletNotFoundException());
      }
    }));
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
                ProofStatus.PROCESSING, 1, null, null));
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
            ProofStatus.PROCESSING, 1, null, null));

    proofOfAttentionService.registerProof(packageName, timeStamp);

    testScheduler.triggerActions();
    List<ProofComponent> proofComponents = new ArrayList<>();
    proofComponents.add(new ProofComponent(timeStamp, nonce));
    Assert.assertEquals(testObserver.assertValueCount(2)
            .values()
            .get(1),
        new Proof(packageName, campaignId, proofComponents, walletPackage, ProofStatus.PROCESSING,
            1, null, null));

    proofOfAttentionService.registerProof(packageName, timeStamp2);
    testScheduler.triggerActions();

    proofComponents.add(new ProofComponent(timeStamp2, nonce));
    Assert.assertEquals(testObserver.assertValueCount(3)
            .values()
            .get(2),
        new Proof(packageName, campaignId, proofComponents, walletPackage, ProofStatus.PROCESSING,
            1, null, null));
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
            null, null));
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
        .assertValueCount(6);
    Proof value = cacheObserver.values()
        .get(5);
    verify(blockChainWriter, times(1)).writeProof(
        new Proof(value.getPackageName(), value.getCampaignId(), value.getProofComponentList(),
            value.getWalletPackage(), ProofStatus.SUBMITTING, 1, null, null));

    Assert.assertEquals(
        new Proof(value.getPackageName(), value.getCampaignId(), value.getProofComponentList(),
            value.getWalletPackage(), ProofStatus.COMPLETED, 1, null, null), value);

    proofOfAttentionService.stop();
  }

  @Test public void get() {
    String packageName = "packageName";
    String campaignId = "campaignId";
    int timeStamp = 10;

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
        .assertValueCount(6);
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
    Assert.assertEquals(ProofStatus.SUBMITTING, proof.getProofStatus());
    Assert.assertEquals(campaignId, proof.getCampaignId());
    Assert.assertEquals(3, proof.getProofComponentList()
        .size());

    proof = observer.values()
        .get(5)
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
                ProofStatus.PROCESSING, 2, null, null));
  }

  @Test public void isWalletReady() {
    TestObserver<ProofOfAttentionService.RequirementsStatus> ready =
        proofOfAttentionService.isWalletReady()
            .test();
    TestObserver<ProofOfAttentionService.RequirementsStatus> noFunds =
        proofOfAttentionService.isWalletReady()
            .test();
    TestObserver<ProofOfAttentionService.RequirementsStatus> noWallet =
        proofOfAttentionService.isWalletReady()
            .test();
    ready.assertComplete()
        .assertNoErrors()
        .assertValue(ProofOfAttentionService.RequirementsStatus.READY);
    noFunds.assertComplete()
        .assertNoErrors()
        .assertValue(ProofOfAttentionService.RequirementsStatus.NO_FUNDS);
    noWallet.assertComplete()
        .assertNoErrors()
        .assertValue(ProofOfAttentionService.RequirementsStatus.NO_WALLET);
  }
}