package com.asfoundation.wallet.poa;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;

public class DataMapper {
  public byte[] getData(Proof proof) throws UnsupportedEncodingException {
    Utf8String packageName = new Utf8String(proof.getPackageName());

    Bytes32 bidId = stringToBytes32(proof.getCampaignId());

    List<Uint256> timeStampList = new ArrayList<>();
    List<Uint256> nonceList = new ArrayList<>();
    List<ProofComponent> proofComponentList = proof.getProofComponentList();
    for (int i = 0; i < proofComponentList.size(); i++) {
      ProofComponent proof1 = proofComponentList.get(i);
      timeStampList.add(new Uint256(proof1.getTimeStamp()));
      nonceList.add(new Uint256(proof1.getNonce()));
    }

    List<Type> params = Arrays.asList(packageName, bidId, new DynamicArray<>(timeStampList),
        new DynamicArray<>(nonceList));
    List<TypeReference<?>> returnTypes = Collections.singletonList(new TypeReference<Bool>() {
    });
    Function function = new Function("registerPoA", params, returnTypes);
    String encodedFunction = FunctionEncoder.encode(function);
    return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
  }

  private Bytes32 stringToBytes32(String string) throws UnsupportedEncodingException {
    byte[] byteValue = string.getBytes("UTF-8");
    byte[] byteValueLen32 = new byte[32];
    System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
    return new Bytes32(byteValueLen32);
  }
}
