package com.asfoundation.wallet.poa;

import com.google.gson.GsonBuilder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HashCalculatorTest {

  private HashCalculator hashCalculator;

  @Before public void before() {
    hashCalculator = new HashCalculator(new GsonBuilder().create(), 5, new Calculator());
  }

  @Test public void calculate() throws NoSuchAlgorithmException {
    String packageName = "packageName";
    String campaignId = "campaignId";
    String walletPackage = "package";

    List<ProofComponent> proofComponentList = new ArrayList<>();
    proofComponentList.add(new ProofComponent(4810492, 1));
    StatelessProof proof =
        new StatelessProof(packageName, campaignId, proofComponentList, "proof_id", walletPackage);
    Assert.assertEquals("a31388acb13878c8ccea80c7a2ca5a982ab62cb19a5b577c912bdcf4e1cfb024",
        hashCalculator.calculate(proof));
  }

  @Test public void calculateNonce() throws NoSuchAlgorithmException {
    Assert.assertEquals(1, hashCalculator.calculateNonce(new NonceData(4810492, "package_name")));
  }
}