package cm.aptoide.skills

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cm.aptoide.skills.databinding.FragmentSkillsBinding
import cm.aptoide.skills.entity.UserData
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SkillsFragment : DaggerFragment() {

  companion object {
    fun newInstance() = SkillsFragment()

    const val RESULT_OK = 1
    const val SESSION = "SESSION"
    const val USER_ID = "USER_ID"
    const val WALLET_ADDRESS = "WALLET_ADDRESS"
  }

  @Inject
  lateinit var viewModel: SkillsViewModel
  private lateinit var userId: String
  private lateinit var disposable: CompositeDisposable

  private lateinit var binding: FragmentSkillsBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    binding = FragmentSkillsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    disposable = CompositeDisposable()
    val intent = requireActivity().intent
    if (intent.hasExtra(USER_ID)) {
      userId = intent.getStringExtra(USER_ID)

      binding.findOpponentButton.setOnClickListener {
        disposable.add(viewModel.getRoom("string_user_id")
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe({ showLoading(R.string.finding_room) })
            .doOnNext({ userData ->
              requireActivity().setResult(RESULT_OK, buildDataIntent(userData))
              requireActivity().finish()
            })
            .doOnNext { ticket -> println("ticket: " + ticket) }
            .subscribe())
      }
    } else {
      showError(R.string.no_user_id)
    }
  }

  private fun showError(stringId: Int) {
    binding.findOpponentButton.visibility = View.GONE
    binding.errorText.visibility = View.VISIBLE
    binding.errorText.text = requireContext().resources.getString(stringId)
  }

  override fun onDestroyView() {
    disposable.clear()
    super.onDestroyView()
  }

  private fun showLoading(textId: Int) {
    binding.findOpponentButton.visibility = View.GONE

    binding.progressBarTv.text = requireContext().resources.getString(textId)

    binding.progressBar.visibility = View.VISIBLE
    binding.progressBarTv.visibility = View.VISIBLE
  }

  private fun buildDataIntent(userData: UserData): Intent {
    val intent = Intent()

    intent.putExtra(SESSION, "session")
    intent.putExtra(USER_ID, userData.userId)
    intent.putExtra(WALLET_ADDRESS, userData.walletAddress)

    return intent
  }
}
