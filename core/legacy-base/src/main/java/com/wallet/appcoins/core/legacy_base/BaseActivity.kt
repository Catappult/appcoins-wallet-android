package com.wallet.appcoins.core.legacy_base

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.core.analytics.analytics.legacy.PageViewAnalytics
import com.appcoins.wallet.core.utils.android_common.KeyboardUtils
import com.wallet.appcoins.core.legacy_base.ActivityResultSharer.ActivityResultListener
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity(), ActivityResultSharer {
    private var activityResultListeners: MutableList<ActivityResultListener>? = null


    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Inject
    lateinit var pageViewAnalytics: PageViewAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        activityResultListeners = ArrayList()
        super.onCreate(savedInstanceState)
        val window = window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    protected fun sendPageViewEvent() {
        pageViewAnalytics!!.sendPageViewEvent(javaClass.simpleName)
    }

    /** Testing the functionality of the base activity without this 2 functions
     * protected Toolbar toolbar() {
     * Toolbar toolbar = findViewById(R.id.toolbar);
     * toolbar.setVisibility(View.VISIBLE);
     * if (toolbar != null) {
     * setSupportActionBar(toolbar);
     * toolbar.setTitle(getTitle());
     * }
     * enableDisplayHomeAsUp();
     * return toolbar;
     * }
     *
     * protected void setCollapsingTitle(String title) {
     * CollapsingToolbarLayout collapsing = findViewById(R.id.toolbar_layout);
     * if (collapsing != null) {
     * collapsing.setTitle(title);
     * }
     * }
     */
    protected fun setTitle(title: String?) {
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = title
        }
    }

    protected fun enableDisplayHomeAsUp() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    protected fun disableDisplayHomeAsUp() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            KeyboardUtils.hideKeyboard(window.decorView
                    .rootView)
            finish()
        }
        return true
    }

    override fun addOnActivityListener(listener: ActivityResultListener) {
        activityResultListeners!!.add(listener)
    }

    override fun remove(listener: ActivityResultListener) {
        activityResultListeners!!.remove(listener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (listener in activityResultListeners!!) {
            listener.onActivityResult(requestCode, resultCode, data)
        }
    }
}