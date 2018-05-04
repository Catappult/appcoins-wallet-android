package com.asfoundation.wallet.poa;

import android.support.annotation.NonNull;
import com.asfoundation.wallet.repository.Cache;
import com.asfoundation.wallet.repository.WalletNotFoundException;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;
import java.util.List;

public class ProofOfAttentionService {
  private final Cache<String, Proof> cache;
  private final String walletPackage;
  private final HashCalculator hashCalculator;
  private final CompositeDisposable compositeDisposable;
  private final ProofWriter proofWriter;
  private final int maxNumberProofComponents;
  private final Scheduler computationScheduler;
  private final BlockchainErrorMapper errorMapper;
  private final TaggedCompositeDisposable disposables;

  public ProofOfAttentionService(Cache<String, Proof> cache, String walletPackage,
      HashCalculator hashCalculator, CompositeDisposable compositeDisposable,
      ProofWriter proofWriter, Scheduler computationScheduler, int maxNumberProofComponents,
      BlockchainErrorMapper errorMapper, TaggedCompositeDisposable disposables) {
    this.cache = cache;
    this.walletPackage = walletPackage;
    this.hashCalculator = hashCalculator;
    this.compositeDisposable = compositeDisposable;
    this.proofWriter = proofWriter;
    this.computationScheduler = computationScheduler;
    this.maxNumberProofComponents = maxNumberProofComponents;
    this.errorMapper = errorMapper;
    this.disposables = disposables;
  }

  public void start() {
    compositeDisposable.add(getReadyPoA().observeOn(computationScheduler)
        .flatMapSingle(proof -> writeOnBlockChain(proof).doOnError(
            throwable -> handleError(throwable, proof.getPackageName()))
            .doOnSubscribe(
                disposable -> updateProofStatus(proof.getPackageName(), ProofStatus.SUBMITTING)))
        .retry()
        .subscribe());
  }

  private void handleError(Throwable throwable, String proofPackageName) {
    ProofStatus proofStatus;
    switch (errorMapper.map(throwable)) {
      default:
      case WRONG_NETWORK:
      case UNKNOWN_TOKEN:
      case NONCE_ERROR:
      case INVALID_BLOCKCHAIN_ERROR:
      case TRANSACTION_NOT_FOUND:
        throwable.printStackTrace();
        proofStatus = ProofStatus.GENERAL_ERROR;
        break;
      case NO_FUNDS:
        proofStatus = ProofStatus.NO_FUNDS;
        break;
      case NO_INTERNET:
        proofStatus = ProofStatus.NO_INTERNET;
        break;
      case NO_WALLET:
        proofStatus = ProofStatus.NO_WALLET;
        break;
    }
    updateProofStatus(proofPackageName, proofStatus);
  }

  private Single<String> writeOnBlockChain(Proof proof) {
    Proof completedProof =
        new Proof(proof.getPackageName(), proof.getCampaignId(), proof.getProofComponentList(),
            proof.getWalletPackage(), ProofStatus.SUBMITTING, proof.getChainId(),
            proof.getOemAddress(), proof.getStoreAddress());
    return proofWriter.writeProof(completedProof)
        .doOnSuccess(hash -> cache.saveSync(completedProof.getPackageName(),
            new Proof(completedProof.getPackageName(), completedProof.getCampaignId(),
                completedProof.getProofComponentList(), walletPackage, ProofStatus.COMPLETED,
                proof.getChainId(), proof.getOemAddress(), proof.getStoreAddress())));
  }

  public void stop() {
    compositeDisposable.clear();
    disposables.disposeAll();
  }

  public void setCampaignId(String packageName, String campaignId) {
    disposables.add(packageName,
        Completable.fromAction(() -> setCampaignIdSync(packageName, campaignId))
            .subscribeOn(computationScheduler)
            .subscribe());
  }

  private void setCampaignIdSync(String packageName, String campaignId) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, campaignId, proof.getProofComponentList(), walletPackage,
              ProofStatus.PROCESSING, proof.getChainId(), proof.getOemAddress(),
              proof.getStoreAddress()));
    }
  }

  public void setChainId(String packageName, int chainId) {
    disposables.add(packageName, Completable.fromAction(() -> setChainIdSync(packageName, chainId))
        .subscribeOn(computationScheduler)
        .subscribe());
  }

  private void setChainIdSync(String packageName, int chainId) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, proof.getCampaignId(), proof.getProofComponentList(),
              walletPackage, ProofStatus.PROCESSING, chainId, proof.getOemAddress(),
              proof.getStoreAddress()));
    }
  }

  private void updateProofStatus(String packageName, ProofStatus proofStatus) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, proof.getCampaignId(), proof.getProofComponentList(),
              walletPackage, proofStatus, proof.getChainId(), proof.getOemAddress(),
              proof.getStoreAddress()));
    }
  }

  private void setSetProofSync(String packageName, long timeStamp, long nonce) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName, new Proof(proof.getPackageName(), proof.getCampaignId(),
          createProofComponentList(timeStamp, nonce, proof), walletPackage, ProofStatus.PROCESSING,
          proof.getChainId(), proof.getOemAddress(), proof.getStoreAddress()));
    }
  }

  public void registerProof(String packageName, long timeStamp) {
    disposables.add(packageName, Single.defer(
        () -> Single.just(hashCalculator.calculateNonce(new NonceData(timeStamp, packageName))))
        .doOnSuccess(nonce -> setSetProofSync(packageName, timeStamp, nonce))
        .toCompletable()
        .subscribeOn(computationScheduler)
        .subscribe());
  }

  @NonNull
  private List<ProofComponent> createProofComponentList(long timeStamp, long nonce, Proof proof) {
    ArrayList<ProofComponent> list = new ArrayList<>(proof.getProofComponentList());
    int i;
    for (i = 0; i < list.size(); i++) {
      ProofComponent proofComponent = list.get(i);
      if (proofComponent.getTimeStamp() > timeStamp) {
        break;
      }
    }
    if (list.size() < maxNumberProofComponents) {
      list.add(i, new ProofComponent(timeStamp, nonce));
    }
    return list;
  }

  private Proof getPreviousProofSync(String packageName) {
    if (cache.containsSync(packageName)) {
      return cache.getSync(packageName);
    } else {
      return new Proof(packageName, walletPackage, ProofStatus.PROCESSING, 1);
    }
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
        .size() == maxNumberProofComponents && proof.getProofStatus()
        .equals(ProofStatus.PROCESSING);
  }

  public Observable<List<Proof>> get() {
    return cache.getAll();
  }

  public void remove(String packageName) {
    synchronized (this) {
      cache.removeSync(packageName);
    }
  }

  public void cancel(String packageName) {
    disposables.dispose(packageName);
    updateProofStatus(packageName, ProofStatus.CANCELLED);
  }

  public void setOemAddress(String packageName, String address) {
    disposables.add(packageName,
        Completable.fromAction(() -> setOemAddressSync(packageName, address))
            .subscribeOn(computationScheduler)
            .subscribe());
  }

  private void setOemAddressSync(String packageName, String address) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, proof.getCampaignId(), proof.getProofComponentList(),
              walletPackage, ProofStatus.PROCESSING, proof.getChainId(), address,
              proof.getStoreAddress()));
    }
  }

  public void setStoreAddress(String packageName, String address) {
    disposables.add(packageName,
        Completable.fromAction(() -> setStoreAddressSync(packageName, address))
            .subscribeOn(computationScheduler)
            .subscribe());
  }

  private void setStoreAddressSync(String packageName, String address) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, proof.getCampaignId(), proof.getProofComponentList(),
              walletPackage, ProofStatus.PROCESSING, proof.getChainId(), proof.getOemAddress(),
              address));
    }
  }

  public Single<RequirementsStatus> isWalletReady() {
    return proofWriter.hasEnoughFunds()
        .map(hasFunds -> {
          if (hasFunds) {
            return RequirementsStatus.READY;
          }
          return RequirementsStatus.NO_FUNDS;
        })
        .onErrorResumeNext(throwable -> {
          if (throwable instanceof WalletNotFoundException) {
            return Single.just(RequirementsStatus.NO_WALLET);
          }
          return Single.error(throwable);
        });
  }

  public enum RequirementsStatus {
    READY, NO_FUNDS, NO_WALLET
  }
}
