package cm.aptoide.skills

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cm.aptoide.skills.databinding.FragmentSkillsBinding
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SkillsFragment : DaggerFragment() {

  companion object {
    fun newInstance() = SkillsFragment()

    const val RESULT_OK = 1
    const val SESSION = "SESSION"
  }

  @Inject
  lateinit var viewModel: SkillsViewModel
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

    binding.findOpponentButton.setOnClickListener {
      disposable.add(viewModel.getRoom("string_user_id")
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSubscribe({ showLoading(R.string.finding_room) })
          .doOnNext({
            requireActivity().setResult(RESULT_OK, buildDataIntent())
            requireActivity().finish()
          })
          .doOnNext { ticket -> println("ticket: " + ticket) }
          .subscribe())
    }
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

  private fun buildDataIntent(): Intent {
    val intent = Intent()

    intent.putExtra(SESSION, "session")

    return intent
  }
}
