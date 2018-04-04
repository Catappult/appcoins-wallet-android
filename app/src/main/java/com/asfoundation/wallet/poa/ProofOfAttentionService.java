package com.asfoundation.wallet.poa;

import android.support.annotation.NonNull;
import com.asfoundation.wallet.repository.Cache;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;

public class ProofOfAttentionService {
  private final Cache<String, Proof> cache;
  private final String walletPackage;
  private final HashCalculator hashCalculator;
  private final CompositeDisposable compositeDisposable;
  private final BlockChainWriter blockChainWriter;
  private final int maxNumberProofComponents;

  public ProofOfAttentionService(Cache<String, Proof> cache, String walletPackage,
      HashCalculator hashCalculator, CompositeDisposable compositeDisposable,
      BlockChainWriter blockChainWriter, int maxNumberProofComponents) {
    this.cache = cache;
    this.walletPackage = walletPackage;
    this.hashCalculator = hashCalculator;
    this.compositeDisposable = compositeDisposable;
    this.blockChainWriter = blockChainWriter;
    this.maxNumberProofComponents = maxNumberProofComponents;
  }

  public void start() {
    compositeDisposable.add(getReadyPoA().flatMap(
        (Proof proof) -> cache.remove(proof.getPackageName())
            .andThen(Observable.just(new Proof(proof.getPackageName(), proof.getCampaignId(),
                proof.getProofComponentList(), hashCalculator.calculate(proof),
                proof.getWalletPackage())))
            .doOnNext(blockChainWriter::writeProof)
            .doOnError(Throwable::printStackTrace)
            .onErrorResumeNext(cache.save(proof.getPackageName(), proof)
                .toObservable()))
        .subscribe());
  }

  public void stop() {
    compositeDisposable.clear();
  }

  public Completable setCampaignId(String packageName, String campaignId) {
    return getPreviousProof(packageName).flatMapCompletable(proof -> cache.save(packageName,
        new Proof(packageName, campaignId, proof.getProofComponentList(), proof.getProofId(),
            walletPackage)));
  }

  public Completable registerProof(String packageName, long timeStamp, String data) {
    return getPreviousProof(packageName).flatMapCompletable(proof -> cache.save(packageName,
        new Proof(proof.getPackageName(), proof.getCampaignId(),
            createProofComponentList(timeStamp, data, proof), proof.getProofId(), walletPackage)));
  }

  @NonNull private ArrayList<ProofComponent> createProofComponentList(long timeStamp, String data,
      Proof proof) {
    ArrayList<ProofComponent> list = new ArrayList<>(proof.getProofComponentList());
    if (list.size() < maxNumberProofComponents) {
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
        .size() == maxNumberProofComponents
        && (proof.getProofId() == null || proof.getProofId()
        .isEmpty());
  }
}
