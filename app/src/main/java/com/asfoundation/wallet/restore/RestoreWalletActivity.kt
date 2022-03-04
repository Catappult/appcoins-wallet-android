package com.asfoundation.wallet.restore

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import com.asf.wallet.R
import com.asfoundation.wallet.navigator.ActivityNavigator
import com.asfoundation.wallet.recover.RecoverWalletFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_restore_wallet.*
import kotlinx.android.synthetic.main.remove_wallet_activity_layout.*
import javax.inject.Inject

@AndroidEntryPoint
class RestoreWalletActivity : ActivityNavigator(), RestoreWalletActivityView {

  companion object {
    private const val RC_READ_EXTERNAL_PERMISSION_CODE = 1002
    const val FILE_INTENT_CODE = 1003

    @JvmStatic
    fun newIntent(context: Context) = Intent(context, RestoreWalletActivity::class.java)
  }

  private var fileChosenSubject: PublishSubject<Uri>? = null
  private var onPermissionSubject: PublishSubject<Unit>? = null

  @Inject
  lateinit var presenter: RestoreWalletActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    fileChosenSubject = PublishSubject.create()
    onPermissionSubject = PublishSubject.create()
    setContentView(R.layout.activity_restore_wallet)
    toolbar()
    presenter.present(savedInstanceState)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      presenter.sendBackEvent()
      if (wallet_remove_animation == null || wallet_remove_animation.visibility != View.VISIBLE) super.onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == FILE_INTENT_CODE && resultCode == Activity.RESULT_OK && data != null) {
      val fileUri = data.data ?: Uri.parse("")
      fileChosenSubject?.onNext(fileUri)
    }
  }

  override fun onBackPressed() {
    if (wallet_remove_animation == null || wallet_remove_animation.visibility != View.VISIBLE) super.onBackPressed()
  }

  override fun showWalletRestoreAnimation() {
    import_wallet_animation_group.visibility = View.VISIBLE
    background.visibility = View.VISIBLE
    background.animation = AnimationUtils.loadAnimation(this, R.anim.fast_fade_in_animation)
    import_wallet_animation.playAnimation()
  }

  override fun showWalletRestoredAnimation() {
    import_wallet_animation.setAnimation(R.raw.success_animation)
    import_wallet_text.text = getText(R.string.provide_wallet_created_header)
    import_wallet_text.visibility = View.VISIBLE
    import_wallet_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) = Unit
      override fun onAnimationEnd(animation: Animator?) = presenter.onAnimationEnd()
      override fun onAnimationCancel(animation: Animator?) = Unit
      override fun onAnimationStart(animation: Animator?) = Unit
    })
    import_wallet_animation.repeatCount = 0
    import_wallet_animation.playAnimation()
  }

  override fun hideAnimation() {
    import_wallet_animation.cancelAnimation()
    import_wallet_animation_group.visibility = View.GONE
  }

  override fun onFileChosen() = fileChosenSubject!!

  override fun askForReadPermissions() {
    if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
      onPermissionSubject?.onNext(Unit)
    } else {
      requestStorageReadPermission()
    }
  }

  private fun requestStorageReadPermission() {
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
        RC_READ_EXTERNAL_PERMISSION_CODE)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                          grantResults: IntArray) {
    if (requestCode == RC_READ_EXTERNAL_PERMISSION_CODE) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        onPermissionSubject?.onNext(Unit)
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  override fun onPermissionsGiven() = onPermissionSubject!!

  override fun getCurrentFragment(): String {
    val fragments = supportFragmentManager.fragments
    return if (fragments.isNotEmpty()) fragments[0]::class.java.simpleName
    else RecoverWalletFragment::class.java.simpleName
  }

  override fun endActivity() = finish()

  override fun onDestroy() {
    onPermissionSubject = null
    fileChosenSubject = null
    super.onDestroy()
  }
}
