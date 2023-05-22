package com.asfoundation.wallet.nfts.ui.nftTransactDialog

import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.nfts.domain.GasInfo
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.domain.NftTransferResult
import com.asfoundation.wallet.nfts.usecases.EstimateNFTSendGasUseCase
import com.asfoundation.wallet.nfts.usecases.SendNFTUseCase
import java.math.BigInteger

object NFTTransactSideEffect : SideEffect


data class NFTTransactState(
  val data: NFTItem,
  val gasPriceAsync: Async<GasInfo> = Async.Uninitialized,
  val transactionResultAsync: Async<NftTransferResult> = Async.Uninitialized
) : ViewState

class NFTTransactDialogViewModel(private val data: NFTItem,
                                 private val estimateNFTSendGas: EstimateNFTSendGasUseCase,
                                 private val sendNFT: SendNFTUseCase) :
    BaseViewModel<NFTTransactState, NFTTransactSideEffect>(initialState(data)) {

  companion object {
    fun initialState(data: NFTItem): NFTTransactState {
      return NFTTransactState(data)
    }
  }

  fun send(toAddress: String, gasPrice: BigInteger, gasLimit: BigInteger) {
    sendNFT(toAddress, data, gasPrice, gasLimit).asAsyncToState(
        NFTTransactState::transactionResultAsync) {
      copy(transactionResultAsync = it)
    }
        .repeatableScopedSubscribe(NFTTransactState::transactionResultAsync.name) { e ->
          e.printStackTrace()
        }
  }

  fun estimateGas(toAddress: String) {
    estimateNFTSendGas(data, toAddress).asAsyncToState(NFTTransactState::gasPriceAsync) {
      copy(gasPriceAsync = it)
    }
        .repeatableScopedSubscribe(NFTTransactState::gasPriceAsync.name) { e ->
          e.printStackTrace()
        }
  }
}


