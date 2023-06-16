package cm.aptoide.skills.endgame.rankings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cm.aptoide.skills.R
import cm.aptoide.skills.endgame.model.MatchDetails
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable

@AndroidEntryPoint
class RankingsActivity : AppCompatActivity() {
  private val disposables = CompositeDisposable()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_rankings)
    val userWalletAddress = intent.extras
      ?.getString(WALLET_ADDRESS_KEY)
    val sku = intent.extras
      ?.getString(SKU_KEY)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .add(
          R.id.fragment_container,
          SkillsRankingsFragment.newInstance(
            userWalletAddress!!,
            sku!!
          )
        )
        .commit()
    }
  }

  override fun onDestroy() {
    disposables.clear()
    super.onDestroy()
  }

  companion object {
    private const val WALLET_ADDRESS_KEY = "WALLET_ADDRESS_KEY"
    private const val MATCH_ENVIRONMENT = "MATCH_ENVIRONMENT"
    private const val SKU_KEY = "SKU_KEY"
    fun create(
      context: Context?, walletAddress: String?, sku: String?,
      matchEnvironment: MatchDetails.Environment?
    ): Intent {
      val intent = Intent(context, RankingsActivity::class.java)
      intent.putExtra(WALLET_ADDRESS_KEY, walletAddress)
      intent.putExtra(MATCH_ENVIRONMENT, matchEnvironment)
      intent.putExtra(SKU_KEY, sku)
      return intent
    }
  }
}