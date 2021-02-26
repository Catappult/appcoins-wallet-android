package com.asfoundation.wallet.promotions.voucher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.e_voucher_details_content_scrolling.*
import kotlinx.android.synthetic.main.e_voucher_details_download_app_layout.*
import kotlinx.android.synthetic.main.layout_app_bar.*
import javax.inject.Inject

class EVoucherDetailsFragment : DaggerFragment(), EVoucherDetailsView {

  private lateinit var onBackPressedSubject: PublishSubject<Any>
  private lateinit var skuButtonsAdapter: SkuButtonsAdapter
  private val skuButtonClick = PublishSubject.create<Int>()

  @Inject
  lateinit var presenter: EVoucherDetailsPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
    onBackPressedSubject = PublishSubject.create()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return if (item.itemId == android.R.id.home) {
      onBackPressedSubject.onNext(Unit)
      true
    } else {
      super.onOptionsItemSelected(item)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_e_voucher_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun setupUi(title: String, packageName: String, skuButtonModels: List<SkuButtonModel>) {
    val appCompatActivity = getActivity() as AppCompatActivity
    appCompatActivity.toolbar.title = title

    skuButtonsAdapter = SkuButtonsAdapter(
        appCompatActivity.applicationContext,
        skuButtonModels,
        skuButtonClick)
    diamond_buttons_recycler_view.layoutManager = GridLayoutManager(requireContext(), 3)
    diamond_buttons_recycler_view.addItemDecoration(MarginItemDecoration(8))
    diamond_buttons_recycler_view.adapter = skuButtonsAdapter
  }

  override fun onNextClicks(): Observable<SkuButtonModel> {
    return RxView.clicks(next_button)
        .map { skuButtonsAdapter.getSelectedSku() }
  }

  override fun onCancelClicks(): Observable<Any> {
    return RxView.clicks(cancel_button)
  }

  override fun onBackPressed(): Observable<Any> {
    return onBackPressedSubject
  }

  override fun onSkuButtonClick(): Observable<Int> {
    return skuButtonClick
  }

  override fun onDownloadAppButtonClick(): Observable<Any> {
    return RxView.clicks(download_app_button)
  }

  override fun setSelectedSku(index: Int) {
    skuButtonsAdapter.setSelectedSku(index)
    diamond_buttons_recycler_view.layoutManager?.findViewByPosition(index)?.isActivated = true
    next_button.setEnabled(true)
  }

  override fun onDestroy() {
    super.onDestroy()

    presenter.stop()
  }

  companion object {
    internal const val TITLE = "title"
    internal const val PACKAGE_NAME = "packageName"

    @JvmStatic
    fun newInstance(title: String, packageName: String): EVoucherDetailsFragment {
      val fragment = EVoucherDetailsFragment()
      fragment.arguments = Bundle().apply {
        putString(TITLE, title)
        putString(PACKAGE_NAME, packageName)
      }
      return fragment
    }
  }
}
