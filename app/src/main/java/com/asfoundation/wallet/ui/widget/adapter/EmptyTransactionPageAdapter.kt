package com.asfoundation.wallet.ui.widget.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R

class EmptyTransactionPageAdapter(private val anim: IntArray,
                                  private val body: Array<String?>,
                                  private val action: IntArray,
                                  private val numberPages: Int,
                                  private val viewPager: ViewPager) : PagerAdapter() {


  override fun getCount(): Int {
    return numberPages
  }

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val view = LayoutInflater.from(container.context)
        .inflate(R.layout.layout_empty_transactions_viewpager, container, false)
    (view.findViewById<View>(R.id.transactions_empty_screen_animation) as LottieAnimationView).setAnimation(anim[position])
    (view.findViewById<View>(R.id.empty_body_text) as TextView).text = body[position]
    (view.findViewById<View>(R.id.empty_action_text) as TextView).setText(action[position])

    viewPager.addOnPageChangeListener(EmptyTransactionsPageChangeListener(view))

    when (action[position]) {
      R.string.home_empty_discover_apps_button -> {
        (view.findViewById<View>(R.id.empty_action_text) as TextView).setOnClickListener {
          Log.d("Lottie Animation", "onClick: APTOIDE")
        }
        (view.findViewById<View>(R.id.transactions_empty_screen_animation) as LottieAnimationView).setOnClickListener {
          Log.d("Lottie Animation", "onClick: APTOIDE")
        }
      }
      R.string.gamification_home_button -> {
        (view.findViewById<View>(R.id.empty_action_text) as TextView).setOnClickListener {
          Log.d("Lottie Animation", "onClick: GAMIFICATION")
        }
        (view.findViewById<View>(R.id.transactions_empty_screen_animation) as LottieAnimationView).setOnClickListener {
          Log.d("Lottie Animation", "onClick: GAMIFICATION")
        }
      }
    }
    container.addView(view)
    return view
  }

  override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    container.removeView(`object` as View)
  }

  override fun isViewFromObject(view: View, `object`: Any): Boolean {
    return view === `object`
  }

  fun randomizeCarouselContent() {
    val randomIndex = Math.random()
    if (randomIndex >= 0.5) {
      invertAnimationContent(anim)
      invertBodyContent(body)
      invertActionContent(action)
    }
  }

  private fun invertAnimationContent(animContent: IntArray) {
    val tempAnim = animContent[0]
    animContent[0] = animContent[1]
    animContent[1] = tempAnim
  }

  private fun invertBodyContent(bodyContent: Array<String?>) {
    val tempBody = bodyContent[0]
    bodyContent[0] = bodyContent[1]
    bodyContent[1] = tempBody
  }

  private fun invertActionContent(actionContent: IntArray) {
    val tempAction = actionContent[0]
    actionContent[0] = actionContent[1]
    actionContent[1] = tempAction
  }
}