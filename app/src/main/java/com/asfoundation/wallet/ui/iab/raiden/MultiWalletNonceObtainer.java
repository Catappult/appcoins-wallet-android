package com.asfoundation.wallet.ui.iab.raiden;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.web3j.abi.datatypes.Address;

public class MultiWalletNonceObtainer {
  private static final String TAG = MultiWalletNonceObtainer.class.getSimpleName();
  private final Map<Key, NonceObtainer> nonceObtainers;
  private final NonceObtainerFactory nonceObtainerFactory;

  public MultiWalletNonceObtainer(NonceObtainerFactory nonceObtainerFactory) {
    this.nonceObtainers = new HashMap<>();
    this.nonceObtainerFactory = nonceObtainerFactory;
  }

  public BigInteger getNonce(Address address, long chainId) {
    return getNonceObtainer(address, chainId).getNonce();
  }

  public boolean consumeNonce(BigInteger nonce, Address address, long chainId) {
    return getNonceObtainer(address, chainId).consumeNonce(nonce);
  }

  private NonceObtainer getNonceObtainer(Address address, long chainId) {
    NonceObtainer nonceObtainer = nonceObtainers.get(new Key(address, chainId));
    if (nonceObtainer == null) {
      nonceObtainer = nonceObtainerFactory.build(address);
      nonceObtainers.put(new Key(address, chainId), nonceObtainer);
    }
    return nonceObtainer;
  }

  private static class Key {
    private final Address address;
    private final long chainId;

    private Key(Address address, long chainId) {
      this.address = address;
      this.chainId = chainId;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Key)) return false;

      Key key = (Key) o;

      if (chainId != key.chainId) return false;
      return address.equals(key.address);
    }

    @Override public int hashCode() {
      int result = address.hashCode();
      result = 31 * result + (int) (chainId ^ (chainId >>> 32));
      return result;
    }
  }
}
