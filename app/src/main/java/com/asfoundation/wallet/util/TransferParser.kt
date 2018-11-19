package com.asfoundation.wallet.util

import com.asfoundation.wallet.entity.TransactionBuilder
import io.reactivex.Single
import org.kethereum.erc681.ERC681
import org.kethereum.erc681.isEthereumURLString
import org.kethereum.erc681.parseERC681

class TransferParser(private val eipTransactionParser: EIPTransactionParser,
                     private val oneStepTransactionParser: OneStepTransactionParser) {

  fun parse(uri: String): Single<TransactionBuilder> {
    if (uri.isEthereumURLString()) {
      return Single.just<ERC681>(parseERC681(uri))
          .flatMap { erc681 -> eipTransactionParser.buildTransaction(erc681) }
    } else if (OneStepTransactionParser.isOneStepURLString(uri)) {
      return oneStepTransactionParser.buildTransaction(uri)
    }
    return Single.error(RuntimeException("is not an supported URI"))
  }
}
