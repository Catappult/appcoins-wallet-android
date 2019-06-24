package com.asfoundation.wallet.ui.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.asf.wallet.R

class OnboardingPagerAdapter : PagerAdapter() {
  private val titles =
      intArrayOf(R.string.intro_1_title, R.string.intro_2_title, R.string.intro_3_title,
          R.string.intro_4_title)
  private val messages =
      intArrayOf(R.string.intro_1_body, R.string.intro_2_body, R.string.intro_3_body,
          R.string.intro_4_body)

  override fun getCount(): Int {
    return titles.size
  }

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val view = LayoutInflater.from(container.context)
        .inflate(R.layout.layout_page_intro, container, false)
    (view.findViewById<View>(R.id.title) as TextView).setText(titles[position])
    (view.findViewById<View>(R.id.message) as TextView).setText(messages[position])
    container.addView(view)
    return view
  }

  override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    container.removeView(`object` as View)
  }

  override fun isViewFromObject(view: View, `object`: Any): Boolean {
    return view === `object`
  }
}