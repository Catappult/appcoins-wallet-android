package com.asfoundation.wallet.permissions.manage.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcoins.wallet.permissions.ApplicationPermission
import com.appcoins.wallet.permissions.PermissionName
import com.asfoundation.wallet.permissions.PermissionsInteractor
import com.appcoins.wallet.core.utils.android_common.applicationinfo.ApplicationInfoProvider
import com.asf.wallet.databinding.FragmentPermissionsListLayoutBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxrelay2.BehaviorRelay
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AndroidEntryPoint
class PermissionsListFragment : BasePageViewFragment(), PermissionsListView {
  companion object {
    fun newInstance(): Fragment {
      return PermissionsListFragment()
    }
  }

  @Inject
  lateinit var permissionsInteractor: PermissionsInteractor
  private lateinit var presenter: PermissionsListPresenter
  private lateinit var adapter: PermissionsListAdapter
  private lateinit var appInfoProvider: ApplicationInfoProvider
  private lateinit var permissionClick: BehaviorRelay<ApplicationPermissionViewData>
  private var toolbarManager: ToolbarManager? = null

  private var _binding: FragmentPermissionsListLayoutBinding? = null
  // This property is only valid between onCreateView and
// onDestroyView.
  private val binding get() = _binding!!

  private val permissions_recycler_view get() = binding.permissionsRecyclerView
  private val empty_state_view get() = binding.emptyStateView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        PermissionsListPresenter(this, permissionsInteractor, AndroidSchedulers.mainThread(),
            Schedulers.io(), CompositeDisposable())
    permissionClick = BehaviorRelay.create()
    adapter = PermissionsListAdapter(mutableListOf(), permissionClick)
    appInfoProvider = ApplicationInfoProvider(requireContext())
  }

  override fun getPermissionClick(): Observable<PermissionsListView.ApplicationPermissionToggle> {
    return permissionClick.map {
      PermissionsListView.ApplicationPermissionToggle(it.packageName, it.hasPermission,
          it.apkSignature)
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    when (context) {
      is ToolbarManager -> toolbarManager = context
      else -> throw IllegalArgumentException(
          "${PermissionsListFragment::class} has to be attached to an activity that implements ${ToolbarManager::class}")
    }
  }

  override fun onDetach() {
    toolbarManager = null
    super.onDetach()
  }

  override fun showEmptyState() {
    empty_state_view.visibility = View.VISIBLE
    permissions_recycler_view.visibility = View.GONE
  }

  override fun showPermissions(permissions: List<ApplicationPermission>): Completable {
    empty_state_view.visibility = View.GONE
    permissions_recycler_view.visibility = View.VISIBLE
    return Single.fromCallable { map(permissions) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess { adapter.setPermissions(it) }
        .ignoreElement()
  }

  private fun map(permissions: List<ApplicationPermission>): List<ApplicationPermissionViewData> {
    return permissions.map {
      val appInfo = appInfoProvider.getAppInfo(it.packageName)
      ApplicationPermissionViewData(it.packageName, appInfo.appName,
          it.permissions.contains(PermissionName.WALLET_ADDRESS), appInfo.icon, it.apkSignature)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    _binding = FragmentPermissionsListLayoutBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    permissions_recycler_view.adapter = adapter
    permissions_recycler_view.layoutManager = LinearLayoutManager(context)
    toolbarManager?.setupToolbar()
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
    _binding = null
  }

}
