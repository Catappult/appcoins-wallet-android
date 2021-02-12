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
import kotlinx.android.synthetic.main.layout_app_bar.*
import rx.subjects.PublishSubject
import javax.inject.Inject

class EVoucherDetailsFragment : DaggerFragment(), EVoucherDetailsView {

  lateinit var nextButton: Button
  lateinit var cancelButton: Button
  lateinit var downloadButton: Button
  lateinit var recyclerView: RecyclerView
  lateinit var skuButtonsAdapter: SkuButtonsAdapter

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
    val skuButtonClick = PublishSubject.create<Any>()

    nextButton = requireView().findViewById(R.id.next_button)
    cancelButton = requireView().findViewById(R.id.cancel_button)
    downloadButton = requireView().findViewById(R.id.download_app_button)
    recyclerView = requireView().findViewById(R.id.diamond_buttons_recycler_view)
    skuButtonsAdapter = SkuButtonsAdapter(
        appCompatActivity.applicationContext,
        presenter.getDiamondModels(),
        skuButtonClick)
    recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
    recyclerView.addItemDecoration(MarginItemDecoration(8))
    recyclerView.adapter = skuButtonsAdapter
    downloadButton.setOnClickListener {
      startActivity(
          Intent(
              Intent.ACTION_VIEW,
              Uri.parse("market://details?id=" + packageName)
          )
      )
    }
    skuButtonClick.subscribe { nextButton.setEnabled(true) }
  }

  override fun onNextClicks(): Observable<Any> {
    return RxView.clicks(nextButton)
  }

  override fun onCancelClicks(): Observable<Any> {
    return RxView.clicks(cancelButton)
  }

  companion object {
    internal const val PACKAGE_NAME = "packageName"

    @JvmStatic
    fun newInstance(packageName: String): EVoucherDetailsFragment {
      val fragment = EVoucherDetailsFragment()
      fragment.arguments = Bundle().apply {
        putString(PACKAGE_NAME, packageName)
      }
      return fragment
    }
  }
}
