package com.asfoundation.wallet.nfts.repository

import android.util.Log
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.nfts.domain.NFTItem
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.spongycastle.util.encoders.Hex
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.generated.Bytes1
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3jFactory
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger


class NFTRepository(private val nftApi: NftApi, private val rxSchedulers: RxSchedulers,
                    private val nftNetwork: String) {

  fun getNFTAssetList(address: String): Single<List<NFTItem>> {
    return nftApi.getWalletNFTs(address)
        .map { response ->
          response.map { assetResponse ->
            NFTItem(assetResponse.name, assetResponse.description, assetResponse.imagePreviewUrl,
                assetResponse.tokenId.toString() + assetResponse.contractAddress,
                assetResponse.schema, assetResponse.tokenId, assetResponse.contractAddress)
          }
        }
        .subscribeOn(rxSchedulers.io)
  }

  fun estimateSendNFTGas(fromAddress: String, toAddress: String, tokenID: BigDecimal,
                         contractAddress: String,
                         schema: String): Single<Pair<BigInteger, BigInteger>> {
    return Single.fromCallable {
      val web3j = Web3jFactory.build(HttpService(nftNetwork))
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
      return@fromCallable Pair(gasPrice, gasLimit)
    }
        .subscribeOn(Schedulers.io())
  }

  fun sendNFT(fromAddress: String, toAddress: String, tokenID: BigDecimal, contractAddress: String,
              schema: String, gasPrice: BigInteger, gasLimit: BigInteger,
              credentials: Credentials): Single<String> {
    return Single.fromCallable {
      signAndSendTransaction(fromAddress, toAddress, tokenID, contractAddress, schema, gasPrice,
          gasLimit, credentials)
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
    val params: List<Type<*>> = listOf(Address(from), Address(to), Uint256(tokenID.toBigInteger()),
        Bytes1(BigInteger.ZERO.toByteArray()))
    val returnTypes: List<TypeReference<*>> = listOf(object : TypeReference<Bool?>() {})
    val function = Function(functionName, params, returnTypes)
    val encodedFunction = FunctionEncoder.encode(function)
    return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction))
  }

  private fun createEstimateGasTransaction(fromAddress: String, toAddress: String,
                                           tokenID: BigDecimal, contractAddress: String,
                                           schema: String): Transaction {
    val data: ByteArray = when (schema) {
      "ERC721" -> createTransactionDataNFT721(fromAddress, toAddress, tokenID)
      //"ERC1155" -> createTransactionDataNFT1155(fromAddress, toAddress, tokenID)
      else -> error("Contract not handled")
    }
    val web3j = Web3jFactory.build(HttpService(nftNetwork))
    val ethGetTransactionCount =
        web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
            .send()
    val nonce = ethGetTransactionCount.transactionCount
    val gasPrice = web3j.ethGasPrice()
        .send().gasPrice
    var gasLimit = BigDecimal(144000).toBigInteger()
    Log.d("NFT", gasPrice.toString() + "   " + gasLimit.toString())
    return Transaction(fromAddress, nonce, gasPrice, gasLimit, contractAddress, BigInteger.ZERO,
        Hex.toHexString(data))
  }

  private fun createSendTransaction(fromAddress: String, toAddress: String, tokenID: BigDecimal,
                                    contractAddress: String, schema: String, gasPrice: BigInteger,
                                    gasLimit: BigInteger): RawTransaction {
    val data: ByteArray = when (schema) {
      "ERC721" -> createTransactionDataNFT721(fromAddress, toAddress, tokenID)
      //"ERC1155" -> createTransactionDataNFT1155(fromAddress, toAddress, tokenID)
      else -> error("Contract not handled")
    }
    val web3j = Web3jFactory.build(HttpService(nftNetwork))
    val ethGetTransactionCount =
        web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
            .sendAsync()
            .get()
    val nonce = ethGetTransactionCount.transactionCount
    return RawTransaction.createTransaction(nonce, gasPrice, gasLimit, contractAddress,
        Hex.toHexString(data))
  }

  private fun signAndSendTransaction(fromAddress: String, toAddress: String, tokenID: BigDecimal,
                                     contractAddress: String, schema: String, gasPrice: BigInteger,
                                     gasLimit: BigInteger, credentials: Credentials): String {
    val web3j = Web3jFactory.build(HttpService(nftNetwork))
    val transaction =
        createSendTransaction(fromAddress, toAddress, tokenID, contractAddress, schema, gasPrice,
            gasLimit)
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
}