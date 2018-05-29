package com.asfoundation.wallet.interact.contract.proxy;

import com.asf.wallet.BuildConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import static org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction;

public class Web3jProxyContract implements ProxyContract {
  private final Web3jProvider web3jProvider;

  public Web3jProxyContract(Web3jProvider web3jProvider) {
    this.web3jProvider = web3jProvider;
  }

  @Override public String getContractAddressById(String fromAddress, int chainId, String id) {
    List<Type> arguments = new ArrayList<>();
    List<TypeReference<?>> returnValues = new ArrayList<>();
    returnValues.add(new TypeReference<Address>() {
    });
    arguments.add(stringToBytes32(id));
    Function getContractAddressById =
        new Function("getContractAddressById", arguments, returnValues);
    String encodedFunction = FunctionEncoder.encode(getContractAddressById);
    Transaction ethCallTransaction =
        createEthCallTransaction(fromAddress, getProxyContractAddress(chainId), encodedFunction);
    try {
      EthCall rawResponse = web3jProvider.get(chainId)
          .ethCall(ethCallTransaction, DefaultBlockParameterName.LATEST)
          .send();
      if (!rawResponse.hasError()) {
        List<Type> response = FunctionReturnDecoder.decode(rawResponse.getValue(),
            getContractAddressById.getOutputParameters());
        return ((Address) response.get(0)).getValue();
      } else {
        throw new RuntimeException(mapErrorToMessage(rawResponse.getError()));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String mapErrorToMessage(Response.Error error) {
    return "Code: "
        + error.getCode()
        + "\nmessage: "
        + error.getMessage()
        + "\nData: "
        + error.getData();
  }

  private Bytes32 stringToBytes32(String string) {
    byte[] value = new byte[32];
    byte[] bytes = string.getBytes();
    System.arraycopy(bytes, 0, value, 0, bytes.length);
    return new Bytes32(value);
  }

  private String getProxyContractAddress(int chainId) {
    switch (chainId) {
      case 3:
        return BuildConfig.ROPSTEN_NETWORK_PROXY_CONTRACT_ADDRESS;
      default:
        return BuildConfig.MAIN_NETWORK_PROXY_CONTRACT_ADDRESS;
    }
  }
}
