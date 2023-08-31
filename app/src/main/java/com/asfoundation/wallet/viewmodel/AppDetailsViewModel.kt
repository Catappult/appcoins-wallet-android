package com.asfoundation.wallet.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.ui.widgets.GameDetailsData
import com.asfoundation.wallet.home.usecases.GetGameDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppDetailsViewModel
@Inject
constructor(
  private val getGameDetailsUseCase: GetGameDetailsUseCase,
  private val rxSchedulers: RxSchedulers,
) : ViewModel() {

  val gameDetails = mutableStateOf(GameDetailsData("", "", "", "", "", listOf(), 0.0, 0, 0))

  init {

  }

  fun fetchGameDetails(packageName: String) {
    getGameDetailsUseCase(packageName)
      .subscribeOn(rxSchedulers.io)
      .subscribe(
        { details -> gameDetails.value = details },
        { e -> e.printStackTrace() }
      )

  }
}