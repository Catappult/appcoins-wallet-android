package com.asfoundation.wallet.promotions

import androidx.viewpager2.widget.ViewPager2
import io.reactivex.subjects.PublishSubject

class PerksVouchersPageChangeListener(private var pageChangedSubject: PublishSubject<Int>) :
    ViewPager2.OnPageChangeCallback() {

  override fun onPageSelected(position: Int) {
    super.onPageSelected(position)
    pageChangedSubject.onNext(position)
  }
}
