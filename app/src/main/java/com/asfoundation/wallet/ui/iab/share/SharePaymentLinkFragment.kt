package com.asfoundation.wallet.ui.iab.share

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.IabView
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_share_payment_link.*
import javax.inject.Inject

class SharePaymentLinkFragment : DaggerFragment(),
    SharePaymentLinkFragmentView {

  @Inject
  lateinit var interactor: ShareLinkInteractor

  lateinit var presenter: SharePaymentLinkPresenter
  private lateinit var iabView: IabView

  companion object {

    private const val PARAM_DOMAIN = "AMOUNT_DOMAIN"
    private const val PARAM_SKUID = "AMOUNT_SKUID"

    @JvmStatic
    fun newInstance(domain: String, skuId: String): SharePaymentLinkFragment =
        SharePaymentLinkFragment().apply {
          arguments = Bundle(2).apply {
            putString(
                PARAM_DOMAIN, domain)
            putString(
                PARAM_SKUID, skuId)
          }
        }
  }

  val domain: String by lazy {
    if (arguments!!.containsKey(
            PARAM_DOMAIN)) {
      arguments!!.getString(
          PARAM_DOMAIN)
    } else {
      throw IllegalArgumentException("Domain not found")
    }
  }

  val skuId: String by lazy {
    if (arguments!!.containsKey(
            PARAM_SKUID)) {
      arguments!!.getString(
          PARAM_SKUID)
    } else {
      throw IllegalArgumentException("SkuId not found")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        SharePaymentLinkPresenter(this, interactor, AndroidSchedulers.mainThread(), Schedulers.io(),
            CompositeDisposable())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_share_payment_link, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onAttach(context: Context) {
    if (!(context is IabView)) {
      throw IllegalStateException("Regular buy fragment must be attached to IAB activity")
    }
    iabView = context
    super.onAttach(context)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun getShareButtonClick(): Observable<SharePaymentData> {
    return RxView.clicks(share_btn).map {
      val message = if (note.text.isNotEmpty()) note.text.toString() else null
      SharePaymentData(domain, skuId, message)
    }
  }

  override fun getCancelButtonClick(): Observable<Any> {
    return RxView.clicks(close_btn)
  }

  override fun showFetchingLinkInfo() {
    share_link_title.text = "We are generating your link..."
    share_link_title.setTextColor(resources.getColor(R.color.share_link_title_color))
    close_btn.visibility = View.INVISIBLE
    share_btn.visibility = View.INVISIBLE
  }

  override fun showErrorInfo() {
    share_link_title.text = "Something went wrong, please try again"
    share_link_title.setTextColor(resources.getColor(R.color.share_link_error_text_color))
    close_btn.visibility = View.VISIBLE
    share_btn.visibility = View.VISIBLE
  }

  override fun shareLink(url: String) {
    share_link_title.text = getString(R.string.askafriend_share_body)
    share_link_title.setTextColor(resources.getColor(R.color.share_link_title_color))
    close_btn.visibility = View.VISIBLE
    share_btn.visibility = View.VISIBLE

    ShareCompat.IntentBuilder.from(activity).setText(note.text.toString() + "\n" + url)
        .setType("text/plain")
        .setChooserTitle("Ask someone via:")
        .startChooser()
  }

  override fun close() {
    iabView.close(Bundle())
  }
}

data class SharePaymentData(val domain: String, val skuId: String, val message: String?)
