package cm.aptoide.skills.endgame.rankings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import cm.aptoide.skills.R
import cm.aptoide.skills.SkillsViewModel
import cm.aptoide.skills.databinding.FragmentRankingsBinding
import cm.aptoide.skills.util.EskillsUriParser
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


@AndroidEntryPoint
class SkillsRankingsFragment : Fragment() {

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val SKU_KEY = "SKU_KEY"
    fun newInstance(userWalletAddress: String, sku: String): SkillsRankingsFragment {
      val args = Bundle()
      args.putString(WALLET_ADDRESS_KEY, userWalletAddress)
      args.putString(SKU_KEY, sku)
      val fragment = SkillsRankingsFragment()
      fragment.arguments = args
      return fragment
    }


  }

  @Inject
  lateinit var eskillsUriParser: EskillsUriParser

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View = FragmentRankingsBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    var walletAddress: String? = null
    var sku: String? = null
    if (arguments != null) {
      walletAddress = requireArguments().getString(WALLET_ADDRESS_KEY)
      sku = requireArguments().getString(SKU_KEY).toString()
    }
    val rankingsPagerAdapter = RankingsPagerAdapter(this, walletAddress!!, sku!!)
    val viewPager = view.findViewById<ViewPager2>(R.id.pager)
    viewPager.adapter = rankingsPagerAdapter
    val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
    TabLayoutMediator(
      tabLayout, viewPager
    ) { tab: TabLayout.Tab, position: Int ->
      tab.setText(
        rankingsPagerAdapter.getFragmentTitle(position)
      )
    }.attach()
  }

  override fun onResume() {
    super.onResume()
    // TODO
  }


  private fun getCachedValue(key: String): Boolean {
    val sharedPreferences = requireContext().getSharedPreferences(key, 0)
    return sharedPreferences.getBoolean(key, true)
  }

  private fun cacheValue(key: String, value: Boolean) {
    val sharedPreferences = requireContext().getSharedPreferences(key, 0)
    sharedPreferences.edit().putBoolean(key, value).apply()
  }
}