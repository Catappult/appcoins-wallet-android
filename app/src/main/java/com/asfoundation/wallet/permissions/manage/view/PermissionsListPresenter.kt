package com.asfoundation.wallet.permissions.manage.view

import com.asfoundation.wallet.permissions.PermissionsInteractor
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PermissionsListPresenter(private val view: PermissionsListView,
                               private val permissionsInteractor: PermissionsInteractor,
                               private val viewScheduler: Scheduler,
                               private val disposables: CompositeDisposable) {
  fun present() {
    showPermissionsList()
  }

  private fun showPermissionsList() {
    disposables.add(
        permissionsInteractor.getPermissions().subscribeOn(Schedulers.io()).observeOn(viewScheduler)
            .doOnNext { view.showPermissions(it) }.subscribe())
  }

  fun stop() {
    disposables.clear()
  }

}
