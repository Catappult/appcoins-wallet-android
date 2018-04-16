package com.asfoundation.wallet.poa;

import android.support.annotation.NonNull;
import com.asfoundation.wallet.repository.Cache;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
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
  private final Scheduler computationScheduler;
  private final BlockchainErrorMapper errorMapper;

  public ProofOfAttentionService(Cache<String, Proof> cache, String walletPackage,
      HashCalculator hashCalculator, CompositeDisposable compositeDisposable,
      BlockChainWriter blockChainWriter, Scheduler computationScheduler,
      int maxNumberProofComponents, BlockchainErrorMapper errorMapper) {
    this.cache = cache;
    this.walletPackage = walletPackage;
    this.hashCalculator = hashCalculator;
    this.compositeDisposable = compositeDisposable;
    this.blockChainWriter = blockChainWriter;
    this.computationScheduler = computationScheduler;
    this.maxNumberProofComponents = maxNumberProofComponents;
    this.errorMapper = errorMapper;
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

  private Single<String> writeOnBlockChain(Proof proof) throws NoSuchAlgorithmException {
    String calculate = hashCalculator.calculate(
        new StatelessProof(proof.getPackageName(), proof.getCampaignId(),
            proof.getProofComponentList(), proof.getProofId(), proof.getWalletPackage()));
    Proof completedProof =
        new Proof(proof.getPackageName(), proof.getCampaignId(), proof.getProofComponentList(),
            calculate, proof.getWalletPackage(), ProofStatus.SUBMITTING);
    return blockChainWriter.writeProof(completedProof)
        .doOnSuccess(hash -> cache.saveSync(completedProof.getPackageName(),
            new Proof(completedProof.getPackageName(), completedProof.getCampaignId(),
                completedProof.getProofComponentList(), completedProof.getProofId(), walletPackage,
                ProofStatus.COMPLETED)));
  }

  public void stop() {
    compositeDisposable.clear();
  }

  public Completable setCampaignId(String packageName, String campaignId) {
    return Completable.fromAction(() -> setCampaignIdSync(packageName, campaignId));
  }

  private void setCampaignIdSync(String packageName, String campaignId) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, campaignId, proof.getProofComponentList(), proof.getProofId(),
              walletPackage, ProofStatus.PROCESSING));
    }
  }

  private void updateProofStatus(String packageName, ProofStatus proofStatus) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, proof.getCampaignId(), proof.getProofComponentList(),
              proof.getProofId(), walletPackage, proofStatus));
    }
  }

  private void setSetProofSync(String packageName, long timeStamp, long nonce) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName, new Proof(proof.getPackageName(), proof.getCampaignId(),
          createProofComponentList(timeStamp, nonce, proof), proof.getProofId(), walletPackage,
          ProofStatus.PROCESSING));
    }
  }

  public Completable registerProof(String packageName, long timeStamp) {
    return Single.defer(
        () -> Single.just(hashCalculator.calculateNonce(new NonceData(timeStamp, packageName))))
        .doOnSuccess(nonce -> setSetProofSync(packageName, timeStamp, nonce))
        .toCompletable();
  }

  @NonNull
  private List<ProofComponent> createProofComponentList(long timeStamp, long nonce, Proof proof) {
    ArrayList<ProofComponent> list = new ArrayList<>(proof.getProofComponentList());
    if (list.size() < maxNumberProofComponents) {
      list.add(new ProofComponent(timeStamp, nonce));
    }
    return list;
  }

  private Proof getPreviousProofSync(String packageName) {
    if (cache.containsSync(packageName)) {
      return cache.getSync(packageName);
    } else {
      return new Proof(packageName, walletPackage, ProofStatus.PROCESSING);
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
}
