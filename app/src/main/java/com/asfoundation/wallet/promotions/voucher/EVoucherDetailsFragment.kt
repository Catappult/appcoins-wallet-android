package com.asfoundation.wallet.promotions.voucher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.layout_app_bar.*
import javax.inject.Inject

class EVoucherDetailsFragment : DaggerFragment(), EVoucherDetailsView {

  lateinit var nextButton: Button
  lateinit var cancelButton: Button
  lateinit var downloadButton: Button
  lateinit var recyclerView: RecyclerView
  lateinit var skuButtonsAdapter: SkuButtonsAdapter
  val skuButtonClick = PublishSubject.create<Int>()
  val disposables = CompositeDisposable()

  @Inject
  lateinit var presenter: EVoucherDetailsPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return if (item.itemId == android.R.id.home) {
      activity?.finish()
      true
    } else {
      super.onOptionsItemSelected(item)
    }
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_e_voucher_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun setupUi(title: String, packageName: String) {
    val appCompatActivity = getActivity() as AppCompatActivity
    appCompatActivity.toolbar.title = title

    nextButton = requireView().findViewById(R.id.next_button)
    cancelButton = requireView().findViewById(R.id.cancel_button)
    downloadButton = requireView().findViewById(R.id.download_app_button)
    recyclerView = requireView().findViewById(R.id.diamond_buttons_recycler_view)
    disposables.add(presenter.getDiamondModels()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess {
          skuButtonsAdapter = SkuButtonsAdapter(
              appCompatActivity.applicationContext,
              it,
              skuButtonClick)
          recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
          recyclerView.addItemDecoration(MarginItemDecoration(8))
          recyclerView.adapter = skuButtonsAdapter
        }
        .subscribe())
    downloadButton.setOnClickListener {
      startActivity(
          Intent(
              Intent.ACTION_VIEW,
              Uri.parse("market://details?id=" + packageName)
          )
      )
    }
    disposables.add(skuButtonClick.subscribe { nextButton.setEnabled(true) })
  }

  override fun onDestroyView() {
    disposables.clear()
    super.onDestroyView()
  }

  override fun onNextClicks(): Observable<Any> {
    return RxView.clicks(nextButton)
  }

  override fun onCancelClicks(): Observable<Any> {
    return RxView.clicks(cancelButton)
  }

  override fun onSkuButtonClick(): Observable<Int> {
    return skuButtonClick
  }

  override fun setSelectedSku(index: Int) {
    skuButtonsAdapter.setSelectedSku(index)
    recyclerView.layoutManager?.findViewByPosition(index)?.isActivated = true
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
