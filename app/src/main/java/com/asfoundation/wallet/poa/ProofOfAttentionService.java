package com.asfoundation.wallet.poa;

import com.asfoundation.wallet.repository.Cache;
import io.reactivex.Completable;

public class ProofOfAttentionService {
  private final Cache<String, Proof> cache;

  public ProofOfAttentionService(Cache<String, Proof> cache) {
    this.cache = cache;
  }

  Completable setCampaignId(String packageName, String campaignId) {
    return cache.save(packageName, new Proof(packageName, campaignId));
  }
}
