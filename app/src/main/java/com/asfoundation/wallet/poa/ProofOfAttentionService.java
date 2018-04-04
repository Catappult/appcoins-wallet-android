package com.asfoundation.wallet.poa;

import android.support.annotation.NonNull;
import com.asfoundation.wallet.repository.Cache;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;

public class ProofOfAttentionService {
  public static final int MAX_NUMBER_PROOF_COMPONENTS = 12;
  private final Cache<String, Proof> cache;
  private final String walletPackage;
  private final HashCalculator hashCalculator;
  private final CompositeDisposable compositeDisposable;
  private final BlockChainWriter blockChainWriter;

  public ProofOfAttentionService(Cache<String, Proof> cache, String walletPackage,
      HashCalculator hashCalculator, CompositeDisposable compositeDisposable,
      BlockChainWriter blockChainWriter) {
    this.cache = cache;
    this.walletPackage = walletPackage;
    this.hashCalculator = hashCalculator;
    this.compositeDisposable = compositeDisposable;
    this.blockChainWriter = blockChainWriter;
  }

  public void start() {
    compositeDisposable.add(getReadyPoA().flatMapCompletable(
        proof -> cache.save(proof.getPackageName(),
            new Proof(proof.getPackageName(), proof.getCampaignId(), proof.getProofComponentList(),
                hashCalculator.calculate(proof), proof.getWalletPackage())))
        .doOnError(Throwable::printStackTrace)
        .retry()
        .subscribe());
  }

  public Single<String> sendSignedProof(String proof) {
    return blockChainWriter.writeProof(proof);
  }

  public void stop() {
    compositeDisposable.clear();
  }

  Completable setCampaignId(String packageName, String campaignId) {
    return getPreviousProof(packageName).flatMapCompletable(proof -> cache.save(packageName,
        new Proof(packageName, campaignId, proof.getProofComponentList(), proof.getProofId(),
            walletPackage)));
  }

  Completable registerProof(String packageName, long timeStamp, String data) {
    return getPreviousProof(packageName).flatMapCompletable(proof -> cache.save(packageName,
        new Proof(proof.getPackageName(), proof.getCampaignId(),
            createProofComponentList(timeStamp, data, proof), proof.getProofId(), walletPackage)));
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
            return Single.just(new Proof(packageName, walletPackage));
          }
        });
  }

  private Observable<Proof> getReadyPoA() {
    return cache.getAll()
        .flatMap(proofs -> Observable.fromIterable(proofs)
            .filter(this::isReadyToComputePoAId));
  }

  private boolean isReadyToComputePoAId(Proof proof) {
    return proof.getCampaignId() != null
        && !proof.getCampaignId()
        .isEmpty()
        && proof.getProofComponentList()
        .size() == MAX_NUMBER_PROOF_COMPONENTS
        && (proof.getProofId() == null || proof.getProofId()
        .isEmpty());
  }

  public Observable<Proof> getReadyToSignProof() {
    return cache.getAll()
        .flatMap(proofs -> Observable.fromIterable(proofs)
            .filter(this::isReadyToSign))
        .flatMap(proof -> cache.remove(proof.getPackageName())
            .toSingleDefault(true)
            .toObservable()
            .map(__ -> proof));
  }

  private boolean isReadyToSign(Proof proof) {
    return proof.getCampaignId() != null
        && !proof.getCampaignId()
        .isEmpty()
        && proof.getProofComponentList()
        .size() == MAX_NUMBER_PROOF_COMPONENTS
        && proof.getProofId() != null
        && !proof.getProofId()
        .isEmpty();
  }
}
