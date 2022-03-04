package com.asfoundation.wallet.nfts.repository

import android.util.Log
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.nfts.domain.GasInfo
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.wallets.repository.BalanceRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.bouncycastle.util.encoders.Hex
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.generated.Bytes1
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class NFTRepository @Inject constructor(
  private val nftApi: NftApi,
  private val rxSchedulers: RxSchedulers,
  private val web3j: Web3j,
  private val localCurrencyConversionService: LocalCurrencyConversionService
) {

  fun getNFTAssetList(address: String): Single<List<NFTItem>> {
    return nftApi.getWalletNFTs(address)
      .map { response ->
        response.map { assetResponse ->
          NFTItem(
            assetResponse.name, assetResponse.description, assetResponse.imagePreviewUrl,
            assetResponse.tokenId.toString() + assetResponse.contractAddress,
            assetResponse.schema, assetResponse.tokenId, assetResponse.contractAddress
          )
        }
      }
      .subscribeOn(rxSchedulers.io)
  }

  fun estimateSendNFTGas(
    fromAddress: String, toAddress: String, tokenID: BigDecimal,
    contractAddress: String, schema: String,
    selectedCurrency: String
  ): Single<GasInfo> {
    return Single.fromCallable {
      val rate = localCurrencyConversionService.getValueToFiat(
        "1.0", "ETH", selectedCurrency,
        BalanceRepository.FIAT_SCALE
      )
        .blockingGet()
      val estimateGasTransaction =
        createEstimateGasTransaction(fromAddress, toAddress, tokenID, contractAddress, schema)
      val estimatedGas = web3j.ethEstimateGas(estimateGasTransaction)
        .send()
      var gasLimit = BigInteger.ZERO
      val gasPrice = web3j.ethGasPrice()
        .send().gasPrice
      if (!estimatedGas.hasError()) {
        gasLimit = estimatedGas.amountUsed
      }
      Log.d("NFT", rate.currency)
      return@fromCallable GasInfo(gasPrice, gasLimit, rate.amount, rate.symbol, rate.currency)
    }
      .subscribeOn(Schedulers.io())
  }

  fun sendNFT(
    fromAddress: String, toAddress: String, tokenID: BigDecimal, contractAddress: String,
    schema: String, gasPrice: BigInteger, gasLimit: BigInteger,
    credentials: Credentials
  ): Single<String> {
    return Single.fromCallable {
      signAndSendTransaction(
        fromAddress, toAddress, tokenID, contractAddress, schema, gasPrice,
        gasLimit, credentials
      )
    }
      .subscribeOn(Schedulers.io())
  }

  private fun createTransactionDataNFT721(
    from: String,
    to: String,
    tokenID: BigDecimal,
  ): ByteArray {
    val functionName = "transferFrom"
    val params: List<Type<*>> = listOf(Address(from), Address(to), Uint256(tokenID.toBigInteger()))
    val returnTypes: List<TypeReference<*>> = listOf(object : TypeReference<Bool?>() {})
    val function = Function(functionName, params, returnTypes)
    val encodedFunction = FunctionEncoder.encode(function)
    return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction))
  }

  //TODO fix this function to work with NFT1155
  private fun createTransactionDataNFT1155(
    from: String,
    to: String,
    tokenID: BigDecimal,
  ): ByteArray {
    val functionName = "safeTransferFrom"
    val params: List<Type<*>> = listOf(
      Address(from), Address(to), Uint256(tokenID.toBigInteger()),
      Bytes1(BigInteger.ZERO.toByteArray())
    )
    val returnTypes: List<TypeReference<*>> = listOf(object : TypeReference<Bool?>() {})
    val function = Function(functionName, params, returnTypes)
    val encodedFunction = FunctionEncoder.encode(function)
    return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction))
  }

  private fun createEstimateGasTransaction(
    fromAddress: String, toAddress: String,
    tokenID: BigDecimal, contractAddress: String,
    schema: String
  ): Transaction {
    val data: ByteArray = when (schema) {
      "ERC721" -> createTransactionDataNFT721(fromAddress, toAddress, tokenID)
      //"ERC1155" -> createTransactionDataNFT1155(fromAddress, toAddress, tokenID)
      else -> error("Contract not handled")
    }
    val ethGetTransactionCount =
      web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
        .send()
    val nonce = ethGetTransactionCount.transactionCount
    val gasPrice = web3j.ethGasPrice()
      .send().gasPrice
    val gasLimit = BigDecimal(144000).toBigInteger()
    return Transaction(
      fromAddress, nonce, gasPrice, gasLimit, contractAddress, BigInteger.ZERO,
      Hex.toHexString(data)
    )
  }

  private fun createSendTransaction(
    fromAddress: String, toAddress: String, tokenID: BigDecimal,
    contractAddress: String, schema: String, gasPrice: BigInteger,
    gasLimit: BigInteger
  ): RawTransaction {
    val data: ByteArray = when (schema) {
      "ERC721" -> createTransactionDataNFT721(fromAddress, toAddress, tokenID)
      //"ERC1155" -> createTransactionDataNFT1155(fromAddress, toAddress, tokenID)
      else -> error("Contract not handled")
    }
    val ethGetTransactionCount =
      web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
        .sendAsync()
        .get()
    val nonce = ethGetTransactionCount.transactionCount
    return RawTransaction.createTransaction(
      nonce, gasPrice, gasLimit, contractAddress,
      Hex.toHexString(data)
    )
  }

  private fun signAndSendTransaction(
    fromAddress: String, toAddress: String, tokenID: BigDecimal,
    contractAddress: String, schema: String, gasPrice: BigInteger,
    gasLimit: BigInteger, credentials: Credentials
  ): String {
    val transaction =
      createSendTransaction(
        fromAddress, toAddress, tokenID, contractAddress, schema, gasPrice,
        gasLimit
      )
    val signedTransaction: ByteArray = TransactionEncoder.signMessage(transaction, credentials)
    val raw = web3j.ethSendRawTransaction(Numeric.toHexString(signedTransaction))
      .send()
    return if (raw.transactionHash == null) {
      Log.d("NFT", raw.error.message)
      raw.error.message
    } else {
      Log.d("NFT", raw.transactionHash)
      raw.transactionHash
    }
  }

  companion object {
    const val FIAT_SCALE = 4
  }
}