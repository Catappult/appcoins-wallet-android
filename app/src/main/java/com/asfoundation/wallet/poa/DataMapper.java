package com.asfoundation.wallet.poa;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes2;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.utils.Numeric;

public class DataMapper {
  public byte[] getData(Proof proof) {
    Utf8String packageName = new Utf8String(proof.getPackageName());

    Bytes32 bidId = stringToBytes32(proof.getCampaignId());

    List<Uint64> timeStampList = new ArrayList<>();
    List<Uint64> nonceList = new ArrayList<>();
    List<ProofComponent> proofComponentList = proof.getProofComponentList();
    for (int i = 0; i < proofComponentList.size(); i++) {
      ProofComponent proof1 = proofComponentList.get(i);
      timeStampList.add(new Uint64(proof1.getTimeStamp()));
      nonceList.add(new Uint64(proof1.getNonce()));
    }
    Address storeAddress = new Address(proof.getStoreAddress());
    Address oemAddress = new Address(proof.getOemAddress());
    Utf8String walletName = new Utf8String(proof.getWalletPackage());
    Bytes2 countryCode = new Bytes2(convertCountryCode(proof.getCountryCode()));

    List<Type> params = Arrays.asList(packageName, bidId, new DynamicArray<>(timeStampList),
        new DynamicArray<>(nonceList), storeAddress, oemAddress, walletName, countryCode);
    List<TypeReference<?>> returnTypes = Collections.singletonList(new TypeReference<Bool>() {
    });
    Function function = new Function("registerPoA", params, returnTypes);
    String encodedFunction = FunctionEncoder.encode(function);
    return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
  }

  private Bytes32 stringToBytes32(String string) {
    BigInteger bidId = new BigInteger(string);
    byte[] value = new byte[32];
    System.arraycopy(bidId.toByteArray(), 0, value, value.length - bidId.toByteArray().length,
        bidId.toByteArray().length);
    return new Bytes32(value);
  }

  public byte[] convertCountryCode(String countryCode) {
    byte[] data = new byte[2];
    char[] chars = countryCode.toCharArray();
    //mapDarkIcons country code for contract's format
    int index = ((int) chars[0] - 65) * 26 + ((int) chars[1] - 65);

    data[0] = (byte) ((index >>> 8) & 0xFF);
    data[1] = (byte) (index & 0xFF);
    return data;
  }
}
