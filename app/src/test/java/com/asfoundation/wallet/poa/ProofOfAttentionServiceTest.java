package com.asfoundation.wallet.poa;

import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.repository.MemoryCache;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
            new BlockchainErrorMapper());

    nonce = 1L;
    when(hashCalculator.calculateNonce(any(NonceData.class))).thenReturn(nonce);
    when(hashCalculator.calculate(any())).thenReturn("hash");
    when(blockChainWriter.writeProof(any(Proof.class))).thenReturn(Single.just("hash"));
  }

  @Test public void setCampaignId() {
    String packageName = "packageName";
    String campaignId = "campaignId";
    TestObserver<Proof> testObserver = new TestObserver<>();
    proofOfAttentionService.setCampaignId(packageName, campaignId)
        .blockingAwait();
    cache.get(packageName)
        .subscribe(testObserver);
    testObserver.assertNoErrors()
        .assertValueCount(1)
        .assertValue(new Proof(packageName, campaignId, Collections.emptyList(), null,
            BuildConfig.APPLICATION_ID, ProofStatus.PROCESSING));
  }

  @Test public void registerProof() {
    String packageName = "packageName";
    String campaignId = "campaignId";
    String walletPackage = BuildConfig.APPLICATION_ID;
    int timeStamp = 10;
    long timeStamp2 = 20;

    TestObserver<Proof> testObserver = new TestObserver<>();
    proofOfAttentionService.setCampaignId(packageName, campaignId)
        .blockingAwait();
    cache.get(packageName)
        .subscribe(testObserver);
    testObserver.assertNoErrors()
        .assertValueCount(1)
        .assertValue(
            new Proof(packageName, campaignId, Collections.emptyList(), null, walletPackage,
                ProofStatus.PROCESSING));

    TestObserver<Object> registerObservable = new TestObserver<>();
    proofOfAttentionService.registerProof(packageName, timeStamp)
        .subscribe(registerObservable);

    registerObservable.assertComplete()
        .assertNoErrors();

    List<ProofComponent> proofComponents = new ArrayList<>();
    proofComponents.add(new ProofComponent(timeStamp, nonce));
    Assert.assertEquals(testObserver.assertValueCount(2)
        .values()
        .get(1), new Proof(packageName, campaignId, proofComponents, null, walletPackage,
        ProofStatus.PROCESSING));

    TestObserver<Object> registerObservable2 = new TestObserver<>();
    proofOfAttentionService.registerProof(packageName, timeStamp2)
        .subscribe(registerObservable2);

    registerObservable2.assertComplete()
        .assertNoErrors();

    proofComponents.add(new ProofComponent(timeStamp2, nonce));
    Assert.assertEquals(testObserver.assertValueCount(3)
        .values()
        .get(2), new Proof(packageName, campaignId, proofComponents, null, walletPackage,
        ProofStatus.PROCESSING));
  }

  @Test public void registerProofWithoutCampaignId() {
    String packageName = "packageName";
    String walletPackage = BuildConfig.APPLICATION_ID;
    int timeStamp = 10;

    TestObserver<Proof> testObserver = new TestObserver<>();
    cache.get(packageName)
        .subscribe(testObserver);

    TestObserver<Object> registerObservable = new TestObserver<>();
    proofOfAttentionService.registerProof(packageName, timeStamp)
        .subscribe(registerObservable);

    registerObservable.assertComplete()
        .assertNoErrors();

    List<ProofComponent> proofComponents = new ArrayList<>();
    proofComponents.add(new ProofComponent(timeStamp, nonce));
    Assert.assertEquals(testObserver.assertValueCount(1)
            .values()
            .get(0),
        new Proof(packageName, null, proofComponents, null, walletPackage, ProofStatus.PROCESSING));
  }

  @Test public void registerProofMaxComponents() {
    String packageName = "packageName";
    int timeStamp = 10;

    TestObserver<Proof> testObserver = new TestObserver<>();
    cache.get(packageName)
        .subscribe(testObserver);

    proofOfAttentionService.registerProof(packageName, timeStamp)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp)
        .blockingAwait();

    testObserver.assertValueCount(3);
    Assert.assertEquals(testObserver.values()
        .get(2)
        .getProofComponentList()
        .size(), maxNumberProofComponents);
  }

  @Test public void getCompletedPoA() throws NoSuchAlgorithmException {
    String packageName = "packageName";
    String campaignId = "campaignId";
    int timeStamp = 10;

    proofOfAttentionService.start();

    TestObserver<Proof> cacheObserver = new TestObserver<>();
    cache.get(packageName)
        .subscribe(cacheObserver);

    proofOfAttentionService.setCampaignId(packageName, campaignId)
        .blockingAwait();

    proofOfAttentionService.registerProof(packageName, timeStamp)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp)
        .blockingAwait();

    testScheduler.advanceTimeBy(1, TimeUnit.SECONDS);
    cacheObserver.assertNoErrors()
        .assertValueCount(6);
    Proof value = cacheObserver.values()
        .get(5);
    Proof proof =
        new Proof(value.getPackageName(), value.getCampaignId(), value.getProofComponentList(),
            hashCalculator.calculate(value), value.getWalletPackage(), ProofStatus.COMPLETED);
    verify(blockChainWriter, times(1)).writeProof(proof);
    Assert.assertEquals(proof.getCampaignId(), campaignId);
    Assert.assertEquals(proof.getPackageName(), packageName);
    Assert.assertEquals(proof.getProofId(), hashCalculator.calculate(
        new Proof(proof.getPackageName(), proof.getCampaignId(), proof.getProofComponentList(),
            "proof_id", proof.getWalletPackage(), ProofStatus.COMPLETED)));
    Assert.assertEquals(proof.getProofComponentList()
        .size(), maxNumberProofComponents);
    Assert.assertEquals(proof.getWalletPackage(), BuildConfig.APPLICATION_ID);

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

    proofOfAttentionService.setCampaignId(packageName, campaignId)
        .blockingAwait();

    proofOfAttentionService.registerProof(packageName, timeStamp)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp)
        .blockingAwait();
    testScheduler.advanceTimeBy(1, TimeUnit.SECONDS);

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
}