package com.asfoundation.wallet.poa;

import com.asfoundation.wallet.repository.MemoryCache;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import java.util.concurrent.ConcurrentHashMap;
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
}