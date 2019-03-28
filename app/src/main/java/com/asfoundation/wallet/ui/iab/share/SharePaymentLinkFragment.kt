package com.asfoundation.wallet.ui.iab.share

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.content.res.ResourcesCompat
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.AndroidBug5497Workaround
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
  private var iabView: IabView? = null

  private lateinit var androidBug5497Workaround: AndroidBug5497Workaround

  companion object {

    private const val PARAM_DOMAIN = "AMOUNT_DOMAIN"
    private const val PARAM_SKUID = "AMOUNT_SKUID"
    private const val PARAM_AMOUNT = "PARAM_AMOUNT"
    private const val PARAM_CURRENCY = "PARAM_CURRENCY"

    @JvmStatic
    fun newInstance(domain: String, skuId: String?, originalAmount: String?,
                    originalCurrency: String?): SharePaymentLinkFragment =
        SharePaymentLinkFragment().apply {
          arguments = Bundle(2).apply {
            putString(
                PARAM_DOMAIN, domain)
            putString(
                PARAM_SKUID, skuId)
            putString(
                PARAM_AMOUNT, originalAmount)
            putString(
                PARAM_CURRENCY, originalCurrency)
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

  private val originalAmount: String? by lazy {
    if (arguments!!.containsKey(
            PARAM_AMOUNT)) {
      arguments!!.getString(
          PARAM_AMOUNT)
    } else {
      throw IllegalArgumentException("Domain not found")
    }
  }

  private val originalCurrency: String? by lazy {
    if (arguments!!.containsKey(
            PARAM_CURRENCY)) {
      arguments!!.getString(
          PARAM_CURRENCY)
    } else {
      throw IllegalArgumentException("Domain not found")
    }
  }

  val skuId: String? by lazy {
    if (arguments!!.containsKey(
            PARAM_SKUID)) {
      val value = arguments!!.getString(
          PARAM_SKUID) ?: return@lazy null
      value
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
    if (context !is IabView) {
      throw IllegalStateException("Regular buy fragment must be attached to IAB activity")
    }
    iabView = context
    super.onAttach(context)

    androidBug5497Workaround = AndroidBug5497Workaround(activity!!)
    androidBug5497Workaround.addListener()
  }

  override fun onDetach() {
    iabView = null
    androidBug5497Workaround.removeListener()
    super.onDetach()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun getShareButtonClick(): Observable<SharePaymentLinkFragmentView.SharePaymentData> {
    return RxView.clicks(share_btn).map {
      val message = if (note.text.isNotEmpty()) note.text.toString() else null
      SharePaymentLinkFragmentView.SharePaymentData(domain, skuId, message, originalAmount,
          originalCurrency)
    }
  }

  override fun getCancelButtonClick(): Observable<Any> {
    return RxView.clicks(close_btn)
  }

  override fun showFetchingLinkInfo() {
    share_link_title.text = "We are generating your link..."
    share_link_title.setTextColor(
        ResourcesCompat.getColor(resources, R.color.share_link_title_color, null))
    close_btn.visibility = View.INVISIBLE
    share_btn.visibility = View.INVISIBLE
  }

  override fun showErrorInfo() {
    share_link_title.text = "Something went wrong, please try again"
    share_link_title.setTextColor(
        ResourcesCompat.getColor(resources, R.color.share_link_error_text_color, null))
    close_btn.visibility = View.VISIBLE
    share_btn.visibility = View.VISIBLE
  }

  override fun shareLink(url: String) {
    share_link_title.text = getString(R.string.askafriend_share_body)
    share_link_title.setTextColor(
        ResourcesCompat.getColor(resources, R.color.share_link_title_color, null))
    close_btn.visibility = View.VISIBLE
    share_btn.visibility = View.VISIBLE

    ShareCompat.IntentBuilder.from(activity).setText(url)
        .setType("text/plain")
        .setChooserTitle("Ask someone via:")
        .startChooser()
  }

  override fun close() {
    iabView?.close(Bundle())
  }
}