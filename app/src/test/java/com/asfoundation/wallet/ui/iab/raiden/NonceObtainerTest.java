package com.asfoundation.wallet.ui.iab.raiden;

import com.asf.microraidenj.type.Address;
import java.math.BigInteger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NonceObtainerTest {
  private static int nonce = 0;
  private NonceObtainer nonceObtainer;

  @Before public void setUp() throws Exception {
    nonceObtainer = new NonceObtainer(1, address -> new BigInteger(String.valueOf(nonce)));
  }

  @Test public void getNonce() {
    assertEquals("wrong nonce", BigInteger.ZERO,
        nonceObtainer.getNonce(Address.from("0x4FBcc5cE88493C3D9903701C143aF65F54481119")));
    assertEquals("wrong nonce", BigInteger.ZERO,
        nonceObtainer.getNonce(Address.from("0x4FBcc5cE88493C3D9903701C143aF65F54481119")));
  }

  @Test public void consumeNonce() {
    assertEquals("wrong nonce", BigInteger.ZERO,
        nonceObtainer.getNonce(Address.from("0x4FBcc5cE88493C3D9903701C143aF65F54481119")));
    nonceObtainer.consumeNonce(BigInteger.ZERO);
    assertEquals("wrong nonce", BigInteger.ONE,
        nonceObtainer.getNonce(Address.from("0x4FBcc5cE88493C3D9903701C143aF65F54481119")));
  }
}