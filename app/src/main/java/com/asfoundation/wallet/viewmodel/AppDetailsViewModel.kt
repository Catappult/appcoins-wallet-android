package com.asfoundation.wallet.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.ui.widgets.GameDetailsData
import com.asfoundation.wallet.home.usecases.GetGameDetailsUseCase
import com.asfoundation.wallet.wallet.home.app_view.usecases.InstallAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppDetailsViewModel
@Inject
constructor(
  private val getGameDetailsUseCase: GetGameDetailsUseCase,
  private val installAppUseCase: InstallAppUseCase,
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

  fun installApp(packageName: String) {
    installAppUseCase(packageName)
  }
}