package com.asfoundation.wallet.nfts.ui.nftTransactDialog

import android.util.Log
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.usecases.EstimateNFTSendGasUseCase
import com.asfoundation.wallet.nfts.usecases.SendNFTUseCase
import java.math.BigInteger

object NFTTransactSideEffect : SideEffect


data class NFTTransactState(val data: NFTItem,
                            val gasPriceAsync: Async<BigInteger> = Async.Uninitialized) : ViewState

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
    sendNFT(toAddress, data, gasPrice, gasLimit)
  }

  fun estimateGas(toAddress: String) {
    Log.d("NFT", "view model")
    estimateNFTSendGas(data, toAddress).asAsyncToState(NFTTransactState::gasPriceAsync) {
      copy(gasPriceAsync = it)
    }
        .repeatableScopedSubscribe(NFTTransactState::gasPriceAsync.name) { e ->
          e.printStackTrace()
        }
  }
}


