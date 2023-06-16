package cm.aptoide.skills.endgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import cm.aptoide.skills.R
import cm.aptoide.skills.SkillsViewModel
import cm.aptoide.skills.databinding.EndgameFragmentSkillsBinding
import cm.aptoide.skills.endgame.rankings.SkillsRankingsFragment
import cm.aptoide.skills.util.EskillsUriParser
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


@AndroidEntryPoint
class SkillsEndgameFragment : Fragment() {

  companion object {
    fun newInstance() = SkillsEndgameFragment()

  }

  private val viewModel: SkillsViewModel by viewModels()  // TODO check if it is better to use a different view model

  @Inject
  lateinit var eskillsUriParser: EskillsUriParser

  private lateinit var disposable: CompositeDisposable

  private val views by viewBinding(EndgameFragmentSkillsBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = EndgameFragmentSkillsBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRankingsBtn()
    disposable = CompositeDisposable()
  }

  override fun onResume() {
    super.onResume()
    // TODO
  }

  private fun setupRankingsBtn() {
    views.rankingsButton.setOnClickListener {
      parentFragmentManager.beginTransaction()
        .replace(R.id.rankings_fragment_container, SkillsRankingsFragment.newInstance("","")) // TODO
        .commit()
    }
  }

  private fun getCachedValue(key: String): Boolean{
    val sharedPreferences = requireContext().getSharedPreferences(key, 0)
    return sharedPreferences.getBoolean(key, true)
  }
  private fun cacheValue(key: String,value: Boolean ){
    val sharedPreferences = requireContext().getSharedPreferences(key, 0)
    sharedPreferences.edit().putBoolean(key, value).apply()
  }
}