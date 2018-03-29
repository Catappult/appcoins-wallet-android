package com.asfoundation.wallet.poa;

import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.repository.MemoryCache;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.asfoundation.wallet.poa.ProofOfAttentionService.MAX_NUMBER_PROOF_COMPONENTS;

public class ProofOfAttentionServiceTest {

  private ProofOfAttentionService proofOfAttentionService;
  private MemoryCache<String, Proof> cache;

  @Before public void before() {
    cache = new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>());
    proofOfAttentionService =
        new ProofOfAttentionService(cache, BuildConfig.APPLICATION_ID, new HashCalculator(),
            new CompositeDisposable());
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
        .assertValue(new Proof(packageName, campaignId, Collections.emptyList(), null,
            BuildConfig.APPLICATION_ID));
  }

  @Test public void registerProof() {
    String packageName = "packageName";
    String campaignId = "campaignId";
    String data = "data";
    String walletPackage = BuildConfig.APPLICATION_ID;
    int timeStamp = 10;
    long timeStamp2 = 20;

    TestObserver<Proof> testObserver = new TestObserver<>();
    proofOfAttentionService.setCampaignId(packageName, campaignId)
        .blockingAwait();
    cache.get(packageName)
        .subscribe(testObserver);
    testObserver.assertNoErrors()
        .assertValueCount(1)
        .assertValue(
            new Proof(packageName, campaignId, Collections.emptyList(), null, walletPackage));

    TestObserver<Object> registerObservable = new TestObserver<>();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .subscribe(registerObservable);

    registerObservable.assertComplete()
        .assertNoErrors();

    List<ProofComponent> proofComponents = new ArrayList<>();
    proofComponents.add(new ProofComponent(timeStamp, data));
    Assert.assertEquals(testObserver.assertValueCount(2)
        .values()
        .get(1), new Proof(packageName, campaignId, proofComponents, null, walletPackage));

    TestObserver<Object> registerObservable2 = new TestObserver<>();
    proofOfAttentionService.registerProof(packageName, timeStamp2, data)
        .subscribe(registerObservable2);

    registerObservable2.assertComplete()
        .assertNoErrors();

    proofComponents.add(new ProofComponent(timeStamp2, data));
    Assert.assertEquals(testObserver.assertValueCount(3)
        .values()
        .get(2), new Proof(packageName, campaignId, proofComponents, null, walletPackage));
  }

  @Test public void registerProofWithoutCampaignId() {
    String packageName = "packageName";
    String walletPackage = BuildConfig.APPLICATION_ID;
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
        .get(0), new Proof(packageName, null, proofComponents, null, walletPackage));
  }

  @Test public void registerProofMaxComponents() {
    String packageName = "packageName";
    String data = "data";
    int timeStamp = 10;

    TestObserver<Proof> testObserver = new TestObserver<>();
    cache.get(packageName)
        .subscribe(testObserver);

    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();

    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();

    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();

    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();

    testObserver.assertValueCount(16);
    Assert.assertEquals(testObserver.values()
        .get(15)
        .getProofComponentList()
        .size(), MAX_NUMBER_PROOF_COMPONENTS);
  }

  @Test public void getCompletedPoA() {
    String packageName = "packageName";
    String data = "data";
    String campaignId = "campaignId";
    int timeStamp = 10;

    TestObserver<Proof> testObserver = new TestObserver<>();
    cache.get(packageName)
        .subscribe(testObserver);
    proofOfAttentionService.start();

    proofOfAttentionService.setCampaignId(packageName, campaignId)
        .blockingAwait();

    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();

    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();

    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();
    proofOfAttentionService.registerProof(packageName, timeStamp, data)
        .blockingAwait();

    TestObserver<Proof> completedPoA = new TestObserver<>();
    proofOfAttentionService.getReadyToSignProofs()
        .subscribe(completedPoA);

    completedPoA.assertNoErrors()
        .assertValueCount(1);

    proofOfAttentionService.stop();
  }

  @Test public void test() throws IOException, NoSuchAlgorithmException {
    String packageName = "packageName";
    String campaignId = "campaignId";
    String walletPackage = BuildConfig.APPLICATION_ID;
    System.out.println(
        checksum(new Proof(packageName, campaignId, Collections.emptyList(), null, walletPackage)));
  }

  private String checksum(Object obj) throws IOException, NoSuchAlgorithmException {

    if (obj == null) {
      return BigInteger.ZERO.toString();
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(obj);
    oos.close();

    MessageDigest m = MessageDigest.getInstance("SHA-256");
    m.update(baos.toByteArray());

    return new BigInteger(1, m.digest()).toString();
  }
}