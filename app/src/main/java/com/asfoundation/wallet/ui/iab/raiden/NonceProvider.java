package com.asfoundation.wallet.ui.iab.raiden;

import com.asf.microraidenj.type.Address;
import java.io.IOException;
import java.math.BigInteger;

interface NonceProvider {
  BigInteger getNonce(Address address) throws IOException;
}
