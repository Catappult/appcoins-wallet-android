package com.asfoundation.wallet.ui.rewards

import android.app.Fragment
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.asf.wallet.R

class MyLevelFragment : Fragment() {
  var layout: View? = null
  private var currentLevelImg: ImageView? = null
  private var imgLevel1: View? = null
  private var imgLevel2: View? = null
  private var imgLevel3: View? = null
  private var imgLevel4: View? = null
  private var imgLevel5: View? = null
  private var textLevel1: TextView? = null
  private var textLevel2: TextView? = null
  private var textLevel3: TextView? = null
  private var textLevel4: TextView? = null
  private var textLevel5: TextView? = null
  var progress: ProgressBar? = null
  private var level = 1
  private val step = 25

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {

    layout = inflater.inflate(R.layout.fragment_rewards_level, container, false)

    setupLayout()

    return layout
  }

  override fun onResume() {
    super.onResume()

    levelUp(0)

    val button = layout?.findViewById(R.id.details_button) as Button
    button.setOnClickListener {
      if (level < 5) {
        val currentProgress = progress?.progress!!.toFloat()
        animateProgress(progress, level * step.toFloat(), currentProgress)
        setLevelImage(level)
        level++
      } else {
        level = 3
        val currentProgress = progress?.progress!!.toFloat()
        animateProgress(progress, level * step.toFloat(), currentProgress)
        setLevelImage(level)
        level++
      }
    }
  }

  private fun setupLayout() {
    progress = layout?.findViewById(R.id.progress_bar) as ProgressBar

    imgLevel1 = layout?.findViewById(R.id.level_1) as View
    setupLevelIcon(imgLevel1, R.drawable.ic_level_comet)
    imgLevel2 = layout?.findViewById(R.id.level_2) as View
    setupLevelIcon(imgLevel2, R.drawable.ic_level_moon)
    imgLevel3 = layout?.findViewById(R.id.level_3) as View
    setupLevelIcon(imgLevel3, R.drawable.ic_level_planet)
    imgLevel4 = layout?.findViewById(R.id.level_4) as View
    setupLevelIcon(imgLevel4, R.drawable.ic_level_star)
    imgLevel5 = layout?.findViewById(R.id.level_5) as View
    setupLevelIcon(imgLevel5, R.drawable.ic_level_galaxy)

    textLevel1 = layout?.findViewById(R.id.level_1_text) as TextView
    textLevel2 = layout?.findViewById(R.id.level_2_text) as TextView
    textLevel3 = layout?.findViewById(R.id.level_3_text) as TextView
    textLevel4 = layout?.findViewById(R.id.level_4_text) as TextView
    textLevel5 = layout?.findViewById(R.id.level_5_text) as TextView

    currentLevelImg = layout?.findViewById(R.id.level_img) as ImageView
  }

  private fun animateProgress(progressBar: ProgressBar?, to: Float, from: Float) {
    val animation = ProgressAnimation(progressBar, progressBar?.progress!!.toFloat(), to)
    animation.duration = 1000
    animation.setAnimationListener(object : AnimationListener {
      override fun onAnimationRepeat(animation: Animation?) {
      }

      override fun onAnimationEnd(animation: Animation?) {
        if (from < to) {
          levelUp(to.toInt())
        } else {
          levelReUp(to.toInt())
        }
      }

      override fun onAnimationStart(animation: Animation?) {
        if (from < to) {
          levelDown(from.toInt())
        } else {
          levelLock(from.toInt())
        }
      }

    })
    progressBar.startAnimation(animation)
  }

  private fun setupLevelIcon(container: View?, resource: Int) {
    val activeIcon = container?.findViewById(R.id.level_active_icon) as ImageView
    activeIcon.setImageResource(resource)
  }

  private fun levelUp(progress: Int) {
    when (progress / step) {
      0 -> {
        animateLevelUp(imgLevel1, textLevel1, true)
      }
      1 -> {
        animateLevelUp(imgLevel2, textLevel2, true)
      }
      2 -> {
        animateLevelUp(imgLevel3, textLevel3, true)
      }
      3 -> {
        animateLevelUp(imgLevel4, textLevel4, true)
      }
      4 -> {
        animateLevelUp(imgLevel5, textLevel5, true)
      }
    }
  }

  private fun levelDown(progress: Int) {
    when (progress / step) {
      0 -> {
        animateLevelDown(imgLevel1, textLevel1)
      }
      1 -> {
        animateLevelDown(imgLevel2, textLevel2)
      }
      2 -> {
        animateLevelDown(imgLevel3, textLevel3)
      }
      3 -> {
        animateLevelDown(imgLevel4, textLevel4)
      }
      4 -> {
        animateLevelDown(imgLevel5, textLevel5)
      }
    }
  }

  private fun levelLock(progress: Int) {
    when (progress / step) {
      0 -> {
        animateLevelToLock(imgLevel1!!, textLevel1)
      }
      1 -> {
        animateLevelToLock(imgLevel2!!, textLevel2)
      }
      2 -> {
        animateLevelToLock(imgLevel3!!, textLevel3)
      }
      3 -> {
        animateLevelToLock(imgLevel4!!, textLevel4)
      }
      4 -> {
        animateLevelToLock(imgLevel5!!, textLevel5)
      }
    }
  }

  private fun levelReUp(progress: Int) {
    when (progress / step) {
      0 -> {
        animateLevelUp(imgLevel1!!, textLevel1, false)
      }
      1 -> {
        animateLevelUp(imgLevel2!!, textLevel2, false)
      }
      2 -> {
        animateLevelUp(imgLevel3!!, textLevel3, false)
      }
      3 -> {
        animateLevelUp(imgLevel4!!, textLevel4, false)
      }
      4 -> {
        animateLevelUp(imgLevel5!!, textLevel5, false)
      }
    }
  }

  private fun animateLevelUp(levelIcon: View?, levelText: TextView?, newLevel: Boolean) {
    val activeIcon = levelIcon?.findViewById(R.id.level_active_icon) as ImageView
    val listener = object : AnimationListener {
      override fun onAnimationRepeat(animation: Animation?) {
      }

      override fun onAnimationEnd(animation: Animation?) {
        activeIcon.visibility = View.VISIBLE
      }

      override fun onAnimationStart(animation: Animation?) {
      }
    }
    if (newLevel) startBounceAnimation(activeIcon, listener) else startRebounceAnimation(activeIcon,
        listener)
    levelText?.isEnabled = true
    levelText?.visibility = View.VISIBLE
  }

  private fun animateLevelDown(levelIcon: View?, levelText: TextView?) {
    val activeIcon = levelIcon?.findViewById(R.id.level_active_icon) as ImageView
    startReduceAnimation(activeIcon)
    levelText?.isEnabled = false
    levelText?.visibility = View.INVISIBLE
  }

  private fun animateLevelToLock(levelIcon: View?, levelText: TextView?) {
    var icon: ImageView = levelIcon?.findViewById(R.id.level_active_icon) as ImageView
    startShrinkAnimation(icon)
    icon.visibility = View.INVISIBLE
    levelText?.isEnabled = false
    levelText?.visibility = View.VISIBLE
  }

  private fun startBounceAnimation(view: View?, listener: AnimationListener) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.bounce_animation)
    animation.setAnimationListener(listener)
    animation.fillAfter = true
    view?.startAnimation(animation)
  }

  private fun startRebounceAnimation(view: View?, listener: AnimationListener) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.rebounce_animation)
    animation.setAnimationListener(listener)
    animation.fillAfter = true
    view?.startAnimation(animation)
  }

  private fun startReduceAnimation(view: View?) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.reduce_animation)
    animation.fillAfter = true
    view?.startAnimation(animation)
  }

  private fun startShrinkAnimation(view: View?) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.shrink_animation)
    animation.fillAfter = true
    view?.startAnimation(animation)
  }

  private fun setLevelImage(level: Int) {
    when (level) {
      0 -> animateImageSwap(currentLevelImg, R.drawable.level_comet)
      1 -> animateImageSwap(currentLevelImg, R.drawable.level_moon)
      2 -> animateImageSwap(currentLevelImg, R.drawable.level_planet)
      3 -> animateImageSwap(currentLevelImg, R.drawable.level_star)
      4 -> animateImageSwap(currentLevelImg, R.drawable.level_galaxy)
    }
  }

  private fun animateImageSwap(imageView: ImageView?, newImage: Int) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.fade_out_animation)
    animation.fillAfter = true
    animation.setAnimationListener(object: AnimationListener {
      override fun onAnimationRepeat(animation: Animation?) {
      }

      override fun onAnimationEnd(animation: Animation?) {
        imageView?.setImageResource(newImage)
        val animation = AnimationUtils.loadAnimation(activity, R.anim.fade_in_animation)
        animation.fillAfter = true
        imageView?.startAnimation(animation)
      }

      override fun onAnimationStart(animation: Animation?) {
      }

    })
    imageView?.startAnimation(animation)


  }
}