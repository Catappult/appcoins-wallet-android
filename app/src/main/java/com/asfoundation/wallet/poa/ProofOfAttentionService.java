package com.asfoundation.wallet.poa;

import android.support.annotation.NonNull;
import com.asfoundation.wallet.repository.Cache;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.util.ArrayList;

public class ProofOfAttentionService {
  public static final int MAX_NUMBER_PROOF_COMPONENTS = 12;
  private final Cache<String, Proof> cache;

  public ProofOfAttentionService(Cache<String, Proof> cache) {
    this.cache = cache;
  }

  Completable setCampaignId(String packageName, String campaignId) {
    return cache.save(packageName, new Proof(packageName, campaignId));
  }

  Completable registerProof(String packageName, long timeStamp, String data) {
    return getPreviousProof(packageName).flatMapCompletable(proof -> cache.save(packageName,
        new Proof(proof.getPackageName(), proof.getCampaignId(),
            createProofComponentList(timeStamp, data, proof))));
  }

  @NonNull private ArrayList<ProofComponent> createProofComponentList(long timeStamp, String data,
      Proof proof) {
    ArrayList<ProofComponent> list = new ArrayList<>(proof.getProofComponentList());
    if (list.size() < MAX_NUMBER_PROOF_COMPONENTS) {
      list.add(new ProofComponent(timeStamp, data));
    }
    return list;
  }

  private Single<Proof> getPreviousProof(String packageName) {
    return cache.contains(packageName)
        .flatMap(contains -> {
          if (contains) {
            return cache.get(packageName)
                .firstOrError();
          } else {
            return Single.just(new Proof(packageName));
          }
        });
  }
}
