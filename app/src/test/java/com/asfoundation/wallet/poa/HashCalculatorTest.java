package com.asfoundation.wallet.poa;

import com.google.gson.GsonBuilder;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
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
    Proof proof = new Proof(packageName, campaignId, Collections.emptyList(), null, walletPackage);
    Assert.assertEquals("0e02bedfad303659d486a709d9edbb34b61fba4b6cebb3d413da0abd966bae6c",
        hashCalculator.calculate(proof));
  }

  @Test public void calculateNonce() throws NoSuchAlgorithmException {
    Assert.assertEquals(1, hashCalculator.calculateNonce(new NonceData(1618307, "package_name")));
  }
}