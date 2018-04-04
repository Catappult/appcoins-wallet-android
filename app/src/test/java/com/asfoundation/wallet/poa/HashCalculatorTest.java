package com.asfoundation.wallet.poa;

import com.google.gson.GsonBuilder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HashCalculatorTest {

  private HashCalculator hashCalculator;

  @Before public void before() throws NoSuchAlgorithmException {
    hashCalculator =
        new HashCalculator(new GsonBuilder().create(), MessageDigest.getInstance("SHA-256"));
  }

  @Test public void calculate() {
    String packageName = "packageName";
    String campaignId = "campaignId";
    String walletPackage = "package";
    Proof proof = new Proof(packageName, campaignId, Collections.emptyList(), null, walletPackage);
    Assert.assertEquals("0e02bedfad303659d486a709d9edbb34b61fba4b6cebb3d413da0abd966bae6c",
        hashCalculator.calculate(proof));
  }
}