package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.TokenInfo
import com.asfoundation.wallet.interact.DefaultTokenProvider
import io.reactivex.Single
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import java.math.BigDecimal
import java.math.BigInteger

class AllowanceService(private val web3j: Web3j,
                       private val defaultTokenProvider: DefaultTokenProvider) {

  fun checkAllowance(owner: String, spender: String,
                     tokenAddress: String): Single<BigDecimal> {
    return defaultTokenProvider.defaultToken
        .map { tokenInfo: TokenInfo ->

          val function =
              allowance(owner, spender)
          val responseValue =
              callSmartContractFunction(function, tokenAddress, owner)
          val response =
              FunctionReturnDecoder.decode(responseValue,
                  function.outputParameters)

          if (response.size == 1) {
            return@map BigDecimal((response[0] as Uint256).value)
                .multiply(BigDecimal(BigInteger.ONE, tokenInfo.decimals))
          } else {
            throw IllegalStateException("Failed to execute contract call!")
          }

        }
  }

  @Throws(Exception::class)
  private fun callSmartContractFunction(function: Function,
                                        contractAddress: String,
                                        walletAddress: String): String {
    val encodedFunction = FunctionEncoder.encode(function)
    val transaction =
        Transaction.createEthCallTransaction(walletAddress,
            contractAddress, encodedFunction)
    return web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
        .send()
        .value
  }

  companion object {
    private fun allowance(owner: String,
                          spender: String): Function {
      return Function("allowance",
          listOf(Address(owner),
              Address(spender)),
          listOf(object : TypeReference<Uint256?>() {}))
    }
  }

}