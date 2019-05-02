package com.asfoundation.wallet.poa;

import androidx.annotation.NonNull;
import com.appcoins.wallet.commons.Repository;
import com.asfoundation.wallet.billing.partners.AddressService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProofOfAttentionService {
  private final Repository<String, Proof> cache;
  private final String walletPackage;
  private final HashCalculator hashCalculator;
  private final CompositeDisposable compositeDisposable;
  private final ProofWriter proofWriter;
  private final int maxNumberProofComponents;
  private final Scheduler computationScheduler;
  private final BackEndErrorMapper errorMapper;
  private final TaggedCompositeDisposable disposables;
  private final CountryCodeProvider countryCodeProvider;
  private final AddressService partnerAddressService;

  public ProofOfAttentionService(Repository<String, Proof> cache, String walletPackage,
      HashCalculator hashCalculator, CompositeDisposable compositeDisposable,
      ProofWriter proofWriter, Scheduler computationScheduler, int maxNumberProofComponents,
      BackEndErrorMapper errorMapper, TaggedCompositeDisposable disposables,
      CountryCodeProvider countryCodeProvider, AddressService partnerAddressService) {
    this.cache = cache;
    this.walletPackage = walletPackage;
    this.hashCalculator = hashCalculator;
    this.compositeDisposable = compositeDisposable;
    this.proofWriter = proofWriter;
    this.computationScheduler = computationScheduler;
    this.maxNumberProofComponents = maxNumberProofComponents;
    this.errorMapper = errorMapper;
    this.disposables = disposables;
    this.countryCodeProvider = countryCodeProvider;
    this.partnerAddressService = partnerAddressService;
  }

  public void start() {
    compositeDisposable.add(getReadyPoA().observeOn(computationScheduler)
        .flatMapSingle(proof -> submitProof(proof).doOnError(
            throwable -> handleError(throwable, proof.getPackageName()))
            .doOnSubscribe(
                disposable -> updateProofStatus(proof.getPackageName(), ProofStatus.SUBMITTING)))
        .retry()
        .subscribe());

    compositeDisposable.add(getReadyCountryCode().observeOn(computationScheduler)
        .flatMapSingle(proof -> countryCodeProvider.getCountryCode()
            .doOnSuccess(countryCode -> setCountryCodeSync(proof.getPackageName(), countryCode))
            .doOnError(throwable -> handleError(throwable, proof.getPackageName())))
        .retry()
        .subscribe());
  }

  private void setCountryCodeSync(String packageName, String countryCode) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, proof.getCampaignId(), proof.getProofComponentList(),
              walletPackage, ProofStatus.PROCESSING, proof.getChainId(), proof.getOemAddress(),
              proof.getStoreAddress(), proof.getGasPrice(), proof.getGasLimit(), proof.getHash(),
              countryCode));
    }
  }

  private void handleError(Throwable throwable, String proofPackageName) {
    ProofStatus proofStatus;
    switch (errorMapper.map(throwable)) {
      case BACKEND_CAMPAIGN_NOT_AVAILABLE:
        proofStatus = ProofStatus.NOT_AVAILABLE;
        break;
      case BACKEND_CAMPAIGN_NOT_AVAILABLE_ON_COUNTRY:
        proofStatus = ProofStatus.NOT_AVAILABLE_ON_COUNTRY;
        break;
      case BACKEND_ALREADY_AWARDED:
        proofStatus = ProofStatus.ALREADY_REWARDED;
        break;
      case BACKEND_INVALID_DATA:
        proofStatus = ProofStatus.INVALID_DATA;
        break;
      case NO_INTERNET:
        proofStatus = ProofStatus.NO_INTERNET;
        break;
      case BACKEND_GENERIC_ERROR:
      default:
        throwable.printStackTrace();
        proofStatus = ProofStatus.GENERAL_ERROR;
        break;
    }

    updateProofStatus(proofPackageName, proofStatus);
  }

  private Single<String> submitProof(Proof proof) {
    Proof completedProof =
        new Proof(proof.getPackageName(), proof.getCampaignId(), proof.getProofComponentList(),
            proof.getWalletPackage(), ProofStatus.SUBMITTING, proof.getChainId(),
            proof.getOemAddress(), proof.getStoreAddress(), proof.getGasPrice(),
            proof.getGasLimit(), proof.getHash(), proof.getCountryCode());
    return proofWriter.writeProof(completedProof)
        .doOnSuccess(hash -> cache.saveSync(completedProof.getPackageName(),
            new Proof(completedProof.getPackageName(), completedProof.getCampaignId(),
                completedProof.getProofComponentList(), walletPackage, ProofStatus.COMPLETED,
                proof.getChainId(), proof.getOemAddress(), proof.getStoreAddress(),
                proof.getGasPrice(), proof.getGasLimit(), hash, proof.getCountryCode())));
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
              proof.getStoreAddress(), proof.getGasPrice(), proof.getGasLimit(), proof.getHash(),
              proof.getCountryCode()));
    }
  }

  public void setChainId(String packageName, int chainId) {
    disposables.add(packageName, Completable.fromAction(() -> setChainIdSync(packageName, chainId))
        .subscribe());
  }

  private void setChainIdSync(String packageName, int chainId) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, proof.getCampaignId(), proof.getProofComponentList(),
              walletPackage, ProofStatus.PROCESSING, chainId, proof.getOemAddress(),
              proof.getStoreAddress(), proof.getGasPrice(), proof.getGasLimit(), proof.getHash(),
              proof.getCountryCode()));
    }
  }

  private void updateProofStatus(String packageName, ProofStatus proofStatus) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, proof.getCampaignId(), proof.getProofComponentList(),
              walletPackage, proofStatus, proof.getChainId(), proof.getOemAddress(),
              proof.getStoreAddress(), proof.getGasPrice(), proof.getGasLimit(), proof.getHash(),
              proof.getCountryCode()));
    }
  }

  private void setSetProofSync(String packageName, long timeStamp, long nonce) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName, new Proof(proof.getPackageName(), proof.getCampaignId(),
          createProofComponentList(timeStamp, nonce, proof), walletPackage, ProofStatus.PROCESSING,
          proof.getChainId(), proof.getOemAddress(), proof.getStoreAddress(), proof.getGasPrice(),
          proof.getGasLimit(), proof.getHash(), proof.getCountryCode()));
    }
  }

  public void registerProof(String packageName, long timeStamp) {
    Proof proof = getPreviousProofSync(packageName);
    if (areComponentsMissing(proof)) {
      disposables.add(packageName, Observable.fromCallable(
          () -> hashCalculator.calculateNonce(new NonceData(timeStamp, packageName)))
          .doOnNext(nonce -> setSetProofSync(packageName, timeStamp, nonce))
          .ignoreElements()
          .subscribeOn(computationScheduler)
          .subscribe());
    }
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
        .size() == maxNumberProofComponents
        && proof.getProofStatus()
        .equals(ProofStatus.PROCESSING)
        && proof.getCountryCode() != null;
  }

  private Observable<Proof> getReadyCountryCode() {
    return cache.getAll()
        .flatMap(proofs -> Observable.fromIterable(proofs)
            .filter(this::isReadyToGetCountryCode));
  }

  private boolean isReadyToGetCountryCode(Proof proof) {
    return proof.getCampaignId() != null
        && !proof.getCampaignId()
        .isEmpty()
        && proof.getProofComponentList()
        .size() == maxNumberProofComponents
        && proof.getProofStatus()
        .equals(ProofStatus.PROCESSING)
        && proof.getCountryCode() == null;
  }

  private boolean areComponentsMissing(Proof proof) {
    return proof.getProofComponentList()
        .size() < maxNumberProofComponents;
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

  public void setOemAddress(String packageName) {
    disposables.add(packageName, partnerAddressService.getOemAddressForPackage(packageName)
        .flatMapCompletable(
            address -> Completable.fromAction(() -> setOemAddressSync(packageName, address)))
        .subscribeOn(computationScheduler)
        .subscribe());
  }

  private void setOemAddressSync(String packageName, String address) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, proof.getCampaignId(), proof.getProofComponentList(),
              walletPackage, ProofStatus.PROCESSING, proof.getChainId(), address,
              proof.getStoreAddress(), proof.getGasPrice(), proof.getGasLimit(), proof.getHash(),
              proof.getCountryCode()));
    }
  }

  private void setGasSettingsSync(String packageName, BigDecimal gasPrice, BigDecimal gasLimit) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, proof.getCampaignId(), proof.getProofComponentList(),
              walletPackage, ProofStatus.PROCESSING, proof.getChainId(), proof.getOemAddress(),
              proof.getStoreAddress(), gasPrice, gasLimit, proof.getHash(),
              proof.getCountryCode()));
    }
  }

  public void setStoreAddress(String packageName) {
    disposables.add(packageName, partnerAddressService.getStoreAddressForPackage(packageName)
        .flatMapCompletable(
            address -> Completable.fromAction(() -> setStoreAddressSync(packageName, address)))
        .subscribeOn(computationScheduler)
        .subscribe());
  }

  private void setStoreAddressSync(String packageName, String address) {
    synchronized (this) {
      Proof proof = getPreviousProofSync(packageName);
      cache.saveSync(packageName,
          new Proof(packageName, proof.getCampaignId(), proof.getProofComponentList(),
              walletPackage, ProofStatus.PROCESSING, proof.getChainId(), proof.getOemAddress(),
              address, proof.getGasPrice(), proof.getGasLimit(), proof.getHash(),
              proof.getCountryCode()));
    }
  }

  public Single<ProofSubmissionFeeData> isWalletReady(int chainId) {
    return Single.defer(() -> {
      synchronized (this) {
        return proofWriter.hasWalletPrepared(chainId);
      }
    });
  }

  public void setGasSettings(String packageName, BigDecimal gasPrice, BigDecimal gasLimit) {
    disposables.add(packageName,
        Completable.fromAction(() -> setGasSettingsSync(packageName, gasPrice, gasLimit))
            .subscribe());
  }
}
