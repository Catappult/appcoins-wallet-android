package com.asfoundation.wallet.nfts.repository

import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.nfts.domain.GasInfo
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.domain.NftTransferResult
import com.asfoundation.wallet.nfts.domain.SuccessfulNftTransfer
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.wallets.repository.BalanceRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
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
  private val rxSchedulers: com.appcoins.wallet.ui.arch.RxSchedulers,
  private val web3j: Web3j,
  private val localCurrencyConversionService: LocalCurrencyConversionService,
  private val chainID: Long,
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
      //To increase the gas for a small bit just to assure that the transaction will be accept
      val gasPrice = web3j.ethGasPrice()
          .send().gasPrice.multiply(BigInteger("3"))
          .divide(BigInteger("2"))
      if (!estimatedGas.hasError()) {
        gasLimit = estimatedGas.amountUsed
      }
      return@fromCallable GasInfo(gasPrice, gasLimit, rate.amount, rate.symbol, rate.currency)
    }
      .subscribeOn(Schedulers.io())
  }

  fun sendNFT(fromAddress: String, toAddress: String, tokenID: BigDecimal, contractAddress: String,
              schema: String, gasPrice: BigInteger, gasLimit: BigInteger,
              credentials: Credentials): Single<NftTransferResult> {
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

  private fun createTransactionDataNFT1155(
    from: String,
    to: String,
    tokenID: BigDecimal,
  ): ByteArray {
    val functionName = "safeTransferFrom"
    val params: List<Type<*>> = listOf(Address(from), Address(to), Uint256(tokenID.toBigInteger()),
        Uint256(BigInteger.ONE), DynamicBytes(byteArrayOf()))
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
      "ERC1155" -> createTransactionDataNFT1155(fromAddress, toAddress, tokenID)
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
      Numeric.toHexString(data)
    )
  }

  private fun createSendTransaction(
    fromAddress: String, toAddress: String, tokenID: BigDecimal,
    contractAddress: String, schema: String, gasPrice: BigInteger,
    gasLimit: BigInteger
  ): RawTransaction {
    val data: ByteArray = when (schema) {
      "ERC721" -> createTransactionDataNFT721(fromAddress, toAddress, tokenID)
      "ERC1155" -> createTransactionDataNFT1155(fromAddress, toAddress, tokenID)
      else -> error("Contract not handled")
    }
    val ethGetTransactionCount =
        web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
            .sendAsync()
            .get()
    val nonce = ethGetTransactionCount.transactionCount

    return RawTransaction.createTransaction(chainID, nonce, gasLimit, contractAddress,
        BigInteger.ZERO, Numeric.toHexString(data), BigInteger.valueOf(1_500_000_000L), gasPrice)
  }

  private fun signAndSendTransaction(fromAddress: String, toAddress: String, tokenID: BigDecimal,
                                     contractAddress: String, schema: String, gasPrice: BigInteger,
                                     gasLimit: BigInteger,
                                     credentials: Credentials): NftTransferResult {
    val transaction =
        createSendTransaction(fromAddress, toAddress, tokenID, contractAddress, schema, gasPrice,
            gasLimit)
    val signedTransaction: ByteArray = TransactionEncoder.signMessage(transaction, credentials)
    val raw = web3j.ethSendRawTransaction(Numeric.toHexString(signedTransaction))
        .send()
    return if (raw.transactionHash == null) {
      NftTransferErrorMapper().map(Throwable(raw.error.message))
    } else {
      SuccessfulNftTransfer()
    }
  }

  companion object {
    const val FIAT_SCALE = 4
  }
}