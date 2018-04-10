package com.asfoundation.wallet.poa;

import java.util.Collections;
import java.util.List;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.utils.Numeric;

public class DataMapper {
  public byte[] getData(String proof) {
    Utf8String dataParam = new Utf8String(proof);
    List<Type> params = Collections.singletonList(dataParam);
    List<TypeReference<?>> returnTypes = Collections.singletonList(new TypeReference<Bool>() {
    });
    Function function = new Function("saveProofOfAttention", params, returnTypes);
    String encodedFunction = FunctionEncoder.encode(function);
    return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
  }
}
