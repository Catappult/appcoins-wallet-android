package com.asfoundation.wallet.poa;

import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.repository.MemoryCache;
import com.google.gson.GsonBuilder;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
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
  private ProofOfAttentionService proofOfAttentionService;
  private MemoryCache<String, Proof> cache;
  private HashCalculator hashCalculator;
  private int maxNumberProofComponents = 3;

  @Before public void before() throws NoSuchAlgorithmException {
    MockitoAnnotations.initMocks(this);
    cache = new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>());
    hashCalculator =
        new HashCalculator(new GsonBuilder().create(), MessageDigest.getInstance("SHA-256"));
    proofOfAttentionService =
        new ProofOfAttentionService(cache, BuildConfig.APPLICATION_ID, hashCalculator,
            new CompositeDisposable(), blockChainWriter, maxNumberProofComponents);
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
            BuildConfig.APPLICATION_ID));
  }

  @Test public void registerProof() {
    String packageName = "packageName";
    String campaignId = "campaignId";
    String data = "data";
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
            new Proof(packageName, campaignId, Collections.emptyList(), null, walletPackage));

    TestObserver<Object> registerObservable = new TestObserver<>();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .subscribe(registerObservable);

    registerObservable.assertComplete()
        .assertNoErrors();

    List<ProofComponent> proofComponents = new ArrayList<>();
    proofComponents.add(new ProofComponent(timeStamp, data));
    Assert.assertEquals(testObserver.assertValueCount(2)
        .values()
        .get(1), new Proof(packageName, campaignId, proofComponents, null, walletPackage));

    TestObserver<Object> registerObservable2 = new TestObserver<>();
    proofOfAttentionService.registerProof(packageName, timeStamp2, data)
        .subscribe(registerObservable2);

    registerObservable2.assertComplete()
        .assertNoErrors();

    proofComponents.add(new ProofComponent(timeStamp2, data));
    Assert.assertEquals(testObserver.assertValueCount(3)
        .values()
        .get(2), new Proof(packageName, campaignId, proofComponents, null, walletPackage));
  }

  @Test public void registerProofWithoutCampaignId() {
    String packageName = "packageName";
    String walletPackage = BuildConfig.APPLICATION_ID;
    String data = "data";
    int timeStamp = 10;

    TestObserver<Proof> testObserver = new TestObserver<>();
    cache.get(packageName)
        .subscribe(testObserver);

    TestObserver<Object> registerObservable = new TestObserver<>();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .subscribe(registerObservable);

    registerObservable.assertComplete()
        .assertNoErrors();

    List<ProofComponent> proofComponents = new ArrayList<>();
    proofComponents.add(new ProofComponent(timeStamp, data));
    Assert.assertEquals(testObserver.assertValueCount(1)
        .values()
        .get(0), new Proof(packageName, null, proofComponents, null, walletPackage));
  }

  @Test public void registerProofMaxComponents() {
    String packageName = "packageName";
    String data = "data";
    int timeStamp = 10;

    TestObserver<Proof> testObserver = new TestObserver<>();
    cache.get(packageName)
        .subscribe(testObserver);

    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();

    testObserver.assertValueCount(3);
    Assert.assertEquals(testObserver.values()
        .get(2)
        .getProofComponentList()
        .size(), maxNumberProofComponents);
  }

  @Test public void getCompletedPoA() {
    String packageName = "packageName";
    String data = "data";
    String campaignId = "campaignId";
    int timeStamp = 10;

    proofOfAttentionService.start();

    TestObserver<Proof> cacheObserver = new TestObserver<>();
    cache.get(packageName)
        .subscribe(cacheObserver);

    proofOfAttentionService.setCampaignId(packageName, campaignId)
        .blockingAwait();

    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();

    cacheObserver.assertNoErrors()
        .assertValueCount(4);
    Proof value = cacheObserver.values()
        .get(3);
    Proof proof =
        new Proof(value.getPackageName(), value.getCampaignId(), value.getProofComponentList(),
            hashCalculator.calculate(value), value.getWalletPackage());
    verify(blockChainWriter, times(1)).writeProof(proof);
    Assert.assertEquals(proof.getCampaignId(), campaignId);
    Assert.assertEquals(proof.getPackageName(), packageName);
    Assert.assertEquals(proof.getProofId(), hashCalculator.calculate(
        new Proof(proof.getPackageName(), proof.getCampaignId(), proof.getProofComponentList(),
            null, proof.getWalletPackage())));
    Assert.assertEquals(proof.getProofComponentList()
        .size(), maxNumberProofComponents);
    Assert.assertEquals(proof.getWalletPackage(), BuildConfig.APPLICATION_ID);

    TestObserver<Boolean> containsSubscriber = new TestObserver<>();
    cache.contains(packageName)
        .subscribe(containsSubscriber);
    containsSubscriber.assertValue(false);

    proofOfAttentionService.stop();
  }
}