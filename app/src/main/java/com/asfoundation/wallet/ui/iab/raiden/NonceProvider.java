package com.asfoundation.wallet.ui.iab.raiden;

import java.io.IOException;
import java.math.BigInteger;
import org.web3j.abi.datatypes.Address;

public interface NonceProvider {
  BigInteger getNonce(Address address) throws IOException;
}
