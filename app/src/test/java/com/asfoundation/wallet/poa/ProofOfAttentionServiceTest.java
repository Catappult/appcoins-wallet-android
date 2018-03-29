package com.asfoundation.wallet.poa;

import com.asfoundation.wallet.repository.MemoryCache;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProofOfAttentionServiceTest {

  private ProofOfAttentionService proofOfAttentionService;
  private MemoryCache<String, Proof> cache;

  @Before public void before() {
    cache = new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>());
    proofOfAttentionService = new ProofOfAttentionService(cache);
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
        .assertValue(new Proof(packageName, campaignId));
  }

  @Test public void registerProof() {
    String packageName = "packageName";
    String campaignId = "campaignId";
    String data = "data";
    int timeStamp = 10;
    long timeStamp2 = 20;

    TestObserver<Proof> testObserver = new TestObserver<>();
    proofOfAttentionService.setCampaignId(packageName, campaignId)
        .blockingAwait();
    cache.get(packageName)
        .subscribe(testObserver);
    testObserver.assertNoErrors()
        .assertValueCount(1)
        .assertValue(new Proof(packageName, campaignId));

    TestObserver<Object> registerObservable = new TestObserver<>();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .subscribe(registerObservable);

    registerObservable.assertComplete()
        .assertNoErrors();

    List<ProofComponent> proofComponents = new ArrayList<>();
    proofComponents.add(new ProofComponent(timeStamp, data));
    Assert.assertEquals(testObserver.assertValueCount(2)
        .values()
        .get(1), new Proof(packageName, campaignId, proofComponents));

    TestObserver<Object> registerObservable2 = new TestObserver<>();
    proofOfAttentionService.registerProof(packageName, timeStamp2, data)
        .subscribe(registerObservable2);

    registerObservable2.assertComplete()
        .assertNoErrors();

    proofComponents.add(new ProofComponent(timeStamp2, data));
    Assert.assertEquals(testObserver.assertValueCount(3)
        .values()
        .get(2), new Proof(packageName, campaignId, proofComponents));
  }

  @Test public void registerProofWithoutCampaignId() {
    String packageName = "packageName";
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
        .get(0), new Proof(packageName, null, proofComponents));
  }
}