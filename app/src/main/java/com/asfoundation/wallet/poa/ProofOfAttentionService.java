package com.asfoundation.wallet.poa;

import android.support.annotation.NonNull;
import com.asfoundation.wallet.repository.Cache;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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
    compositeDisposable.add(getReadyPoA().flatMapSingle(
        proof -> cache.remove(proof.getPackageName())
            .andThen(writeOnBlockChain(proof))
            .doOnError(Throwable::printStackTrace)
            .onErrorResumeNext(cache.save(proof.getPackageName(), proof)
                .toSingleDefault("")))
        .subscribe());
  }

  private Single<String> writeOnBlockChain(Proof proof) throws NoSuchAlgorithmException {
    return blockChainWriter.writeProof(
        new Proof(proof.getPackageName(), proof.getCampaignId(), proof.getProofComponentList(),
            hashCalculator.calculate(proof), proof.getWalletPackage()));
  }

  public void stop() {
    compositeDisposable.clear();
  }

  public Completable setCampaignId(String packageName, String campaignId) {
    return getPreviousProof(packageName).flatMapCompletable(proof -> cache.save(packageName,
        new Proof(packageName, campaignId, proof.getProofComponentList(), proof.getProofId(),
            walletPackage)));
  }

  public Completable registerProof(String packageName, long timeStamp) {
    return Single.defer(
        () -> Single.just(hashCalculator.calculateNonce(new NonceData(timeStamp, packageName))))
        .flatMapCompletable(nonce -> getPreviousProof(packageName).flatMapCompletable(
            proof -> cache.save(packageName,
                new Proof(proof.getPackageName(), proof.getCampaignId(),
                    createProofComponentList(timeStamp, nonce, proof), proof.getProofId(),
                    walletPackage))));
  }

  @NonNull
  private List<ProofComponent> createProofComponentList(long timeStamp, long nonce, Proof proof) {
    ArrayList<ProofComponent> list = new ArrayList<>(proof.getProofComponentList());
    if (list.size() < maxNumberProofComponents) {
      list.add(new ProofComponent(timeStamp, nonce));
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
