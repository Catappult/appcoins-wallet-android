package com.asfoundation.wallet.ui.balance

import android.animation.Animator
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import com.asf.wallet.R
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.BaseActivity
import com.google.android.material.snackbar.Snackbar
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.import_wallet_layout.*
import kotlinx.android.synthetic.main.remove_wallet_activity_layout.*


class ImportWalletActivity : BaseActivity(), ImportWalletActivityView {

  private var fileChosenSubject: PublishSubject<String>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    fileChosenSubject = PublishSubject.create()
    setContentView(R.layout.import_wallet_layout)
    toolbar()
    navigateToInitialImportFragment()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      if (wallet_remove_animation == null || wallet_remove_animation.visibility != View.VISIBLE) super.onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onBackPressed() {
    if (wallet_remove_animation == null || wallet_remove_animation.visibility != View.VISIBLE) super.onBackPressed()
  }

  override fun navigateToPasswordView(keystore: String) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, ImportWalletPasswordFragment.newInstance(keystore))
        .addToBackStack(ImportWalletPasswordFragment::class.java.simpleName)
        .commit()
  }

  override fun showWalletImportAnimation() {
    import_wallet_animation_group.visibility = View.VISIBLE
    background.visibility = View.VISIBLE
    background.animation = AnimationUtils.loadAnimation(this, R.anim.fast_fade_in_animation)
    import_wallet_animation.playAnimation()
  }

  override fun showWalletImportedAnimation() {
    import_wallet_animation.setAnimation(R.raw.success_animation)
    import_wallet_text.text = getText(R.string.provide_wallet_created_header)
    import_wallet_text.visibility = View.VISIBLE
    import_wallet_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {
      }

      override fun onAnimationEnd(animation: Animator?) {
        navigateToTransactions()
      }

      override fun onAnimationCancel(animation: Animator?) {
      }

      override fun onAnimationStart(animation: Animator?) {
      }
    })
    import_wallet_animation.repeatCount = 0
    import_wallet_animation.playAnimation()
  }

  override fun launchFileIntent() {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = "text/*"
    intent.addCategory(Intent.CATEGORY_OPENABLE)

    try {
      startActivityForResult(Intent.createChooser(intent, ""), 1234)
    } catch (ex: ActivityNotFoundException) { // Potentially direct the user to the Market with a Dialog
      Snackbar.make(main_view, "Please install a File Manager.", Snackbar.LENGTH_SHORT)
          .show()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 1234) {
      val mData = data?.data
      Log.d("TAG123", mData.toString())
    }
  }

  override fun fileChosen() {

  }

  override fun hideAnimation() {
    import_wallet_animation.cancelAnimation()
    import_wallet_animation_group.visibility = View.GONE
  }

  private fun navigateToTransactions() {
    TransactionsRouter().open(this, true)
    finish()
  }

  private fun navigateToInitialImportFragment() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, ImportWalletFragment())
        .commit()
  }
}
