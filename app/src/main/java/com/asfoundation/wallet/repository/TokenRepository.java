package com.asfoundation.wallet.repository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes2;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;

public class TokenRepository {

  public static byte[] createTokenTransferData(String to, BigDecimal tokenAmount) {
    List<Type> params = Arrays.asList(new Address(to), new Uint256(tokenAmount.toBigInteger()));
    List<TypeReference<?>> returnTypes = Collections.singletonList(new TypeReference<Bool>() {
    });
    Function function = new Function("transfer", params, returnTypes);
    String encodedFunction = FunctionEncoder.encode(function);
    return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
  }

  public static byte[] createTokenApproveData(String spender, BigDecimal amount) {
    List<Type> params = Arrays.asList(new Address(spender), new Uint256(amount.toBigInteger()));
    List<TypeReference<?>> returnTypes = Collections.singletonList(new TypeReference<Bool>() {
    });
    Function function = new Function("approve", params, returnTypes);
    String encodedFunction = FunctionEncoder.encode(function);
    return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
  }

  static byte[] buyData(String developerAddress, String storeAddress, String oemAddress,
      String data, BigDecimal amount, String tokenAddress, String packageName, byte[] countryCode) {
    Uint256 amountParam = new Uint256(amount.toBigInteger());
    Utf8String packageNameType = new Utf8String(packageName);
    Utf8String dataParam = data == null ? new Utf8String("") : new Utf8String(data);
    Address contractAddress = new Address(tokenAddress);
    Address developerAddressParam = new Address(developerAddress);
    Address storeAddressParam = new Address(storeAddress);
    Address oemAddressParam = new Address(oemAddress);
    Bytes2 countryCodeBytes = new Bytes2(countryCode);
    List<Type> params = Arrays.asList(packageNameType, dataParam, amountParam, contractAddress,
        developerAddressParam, storeAddressParam, oemAddressParam, countryCodeBytes);
    List<TypeReference<?>> returnTypes = Collections.singletonList(new TypeReference<Bool>() {
    });
    Function function = new Function("buy", params, returnTypes);
    String encodedFunction = FunctionEncoder.encode(function);
    return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
  }
}
