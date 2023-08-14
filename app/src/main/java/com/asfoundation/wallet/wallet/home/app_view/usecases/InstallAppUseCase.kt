package com.asfoundation.wallet.wallet.home.app_view.usecases

import rx.Completable
import rx.functions.Action0
import javax.inject.Inject

class InstallAppUseCase @Inject constructor(

) {
  operator fun invoke(gamePackage: String) {

  }

  private fun handleInstallButtonClick() {
    view.getLifecycleEvent()
      .filter { lifecycleEvent -> lifecycleEvent === View.LifecycleEvent.CREATE }
      .flatMap { create -> accountManager.accountStatus() }
      .first()
      .observeOn(viewScheduler)
      .flatMap { account ->
        view.installAppClick()
          .flatMapCompletable { action ->
            var completable: Completable? = null
            when (action) {
              INSTALL, UPDATE -> completable = appViewManager.getAppModel()
                .flatMapCompletable { appModel ->
                  appViewManager.getAdsVisibilityStatus()
                    .flatMapCompletable { status ->
                      downloadApp(
                        action, appModel, status, appModel.getOpenType()
                            === AppViewFragment.OpenType.APK_FY_INSTALL_POPUP
                      ).observeOn(
                        viewScheduler
                      )

                    }
                }

              OPEN -> completable = appViewManager.getAppModel()
                .observeOn(viewScheduler)
                .flatMapCompletable { appViewViewModel -> openInstalledApp(appViewViewModel.getPackageName()) }

              else -> completable =
                Completable.error(IllegalArgumentException("Invalid type of action"))
            }
            completable
          }
          .doOnError { throwable ->
            crashReport.log(throwable)
            if (throwable is InvalidAppException) {
              view.showInvalidAppInfoErrorDialog()
            } else {
              view.showGenericErrorDialog()
            }
          }
          .retry()
      }
      .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
      .subscribe({ created -> }) { error -> throw IllegalStateException(error) }
  }
}