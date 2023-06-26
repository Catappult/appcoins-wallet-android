package cm.aptoide.skills.endgame.rankings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import cm.aptoide.skills.R
import cm.aptoide.skills.databinding.FragmentRankingsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SkillsRankingsFragment : Fragment() {

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val SKU_KEY = "SKU_KEY"
    const val PACKAGE_NAME = "PACKAGE_NAME"
    fun newInstance(userWalletAddress: String, packageName: String, sku: String): SkillsRankingsFragment {
      return SkillsRankingsFragment().apply {
        arguments = Bundle().apply {
          putString(WALLET_ADDRESS_KEY, userWalletAddress)
          putString(PACKAGE_NAME, packageName)
          putString(SKU_KEY, sku)
        }
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View = FragmentRankingsBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    var walletAddress: String? = null
    var packageName: String? = null
    var sku: String? = null
    if (arguments != null) {
      walletAddress = requireArguments().getString(WALLET_ADDRESS_KEY)
      packageName = requireArguments().getString(PACKAGE_NAME)
      sku = requireArguments().getString(SKU_KEY).toString()
    }
    val rankingsPagerAdapter = RankingsPagerAdapter(this, walletAddress!!, packageName!!, sku!!)
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


}