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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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
    when(hashCalculator.calculate(any())).thenReturn("hash");
    when(blockChainWriter.writeProof(any(Proof.class))).thenReturn(Single.just("hash"));
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
        .assertValue(new Proof(packageName, campaignId, Collections.emptyList(), null,
            BuildConfig.APPLICATION_ID, ProofStatus.PROCESSING, 1));
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
        .assertValue(
            new Proof(packageName, campaignId, Collections.emptyList(), null, walletPackage,
                ProofStatus.PROCESSING, 1));

    proofOfAttentionService.registerProof(packageName, timeStamp);

    testScheduler.triggerActions();
    List<ProofComponent> proofComponents = new ArrayList<>();
    proofComponents.add(new ProofComponent(timeStamp, nonce));
    Assert.assertEquals(testObserver.assertValueCount(2)
        .values()
        .get(1), new Proof(packageName, campaignId, proofComponents, null, walletPackage,
        ProofStatus.PROCESSING, 1));

    proofOfAttentionService.registerProof(packageName, timeStamp2);
    testScheduler.triggerActions();

    proofComponents.add(new ProofComponent(timeStamp2, nonce));
    Assert.assertEquals(testObserver.assertValueCount(3)
        .values()
        .get(2), new Proof(packageName, campaignId, proofComponents, null, walletPackage,
        ProofStatus.PROCESSING, 1));
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
        new Proof(packageName, null, proofComponents, null, walletPackage, ProofStatus.PROCESSING,
            1));
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

  @Test public void getCompletedPoA() throws NoSuchAlgorithmException {
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
            hashCalculator.calculate(value), value.getWalletPackage(), ProofStatus.SUBMITTING, 1));

    Assert.assertEquals(
        new Proof(value.getPackageName(), value.getCampaignId(), value.getProofComponentList(),
            hashCalculator.calculate(value), value.getWalletPackage(), ProofStatus.COMPLETED, 1),
        value);

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
            new Proof(packageName, null, Collections.emptyList(), null, BuildConfig.APPLICATION_ID,
                ProofStatus.PROCESSING, 2));
  }
}