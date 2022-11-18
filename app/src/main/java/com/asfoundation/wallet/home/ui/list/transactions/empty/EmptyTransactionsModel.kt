package com.asfoundation.wallet.home.ui.list.transactions.empty

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.home.ui.list.HomeListClick
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.rd.PageIndicatorView
import java.util.*

@EpoxyModelClass
abstract class EmptyTransactionsModel :
  EpoxyModelWithHolder<EmptyTransactionsModel.EmptyTransactionsHolder>() {

  companion object {
    const val CAROUSEL_TOP_APPS: String = "bundle"
    const val CAROUSEL_GAMIFICATION: String = "gamification"
    const val SCROLL_DELAY: Long = 500
    const val SCROLL_PERIOD: Long = 3000
  }

  var currentPage = 0;

  @EpoxyAttribute
  var bonus: Double = 0.0

  @EpoxyAttribute
  var clickListener: ((HomeListClick) -> Unit)? = null

  private val emptyPagerController = EmptyTransactionsController()

  override fun bind(holder: EmptyTransactionsHolder) {
    super.bind(holder)
    val ctx = holder.itemView.context
    emptyPagerController.clickListener = clickListener
    holder.viewPager.adapter = emptyPagerController.adapter
    val data = listOf(
      EmptyItem(
        CAROUSEL_TOP_APPS, R.raw.carousel_empty_screen_animation,
        ctx.getString(R.string.home_empty_discover_apps_body)
      ),
      EmptyItem(
        CAROUSEL_GAMIFICATION, R.raw.transactions_empty_screen_animation,
        ctx.getString(R.string.gamification_home_body, bonus.toString())
      )
    )
    holder.pageIndicator.count = data.size
    holder.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        holder.pageIndicator.selection = currentPage
      }
    })
    autoScroll(holder, data)
    emptyPagerController.setData(data)
  }

  private fun autoScroll(holder: EmptyTransactionsHolder, data: List<EmptyItem>) {
    val handler = Handler(Looper.getMainLooper())
    val update = Runnable {
      currentPage = (currentPage + 1) % data.size;
      holder.viewPager.setCurrentItem(currentPage, true);
    }
    Timer().schedule(object : TimerTask() {
      override fun run() {
        handler.post(update)
      }
    }, SCROLL_DELAY, SCROLL_PERIOD)
  }

  override fun getDefaultLayout(): Int = R.layout.layout_empty_transactions

  class EmptyTransactionsHolder : BaseViewHolder() {
    val pageIndicator by bind<PageIndicatorView>(R.id.page_indicator)
    val noTransactionsText by bind<TextView>(R.id.no_transactions_text)
    val viewPager by bind<ViewPager2>(R.id.empty_transactions_viewpager)
  }
}