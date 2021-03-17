package cm.aptoide.skills

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
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
  lateinit var walletAddressObtainer: WalletAddressObtainer

  @Inject
  lateinit var viewModel: SkillsViewModel
  private lateinit var disposable: CompositeDisposable

  private lateinit var button: Button
  private lateinit var progressBar: ProgressBar
  private lateinit var progressBarTV: TextView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_skills, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    disposable = CompositeDisposable()

    button = view.findViewById(R.id.find_opponent_button)
    progressBar = view.findViewById(R.id.progress_bar)
    progressBarTV = view.findViewById(R.id.progress_bar_tv)

    button.setOnClickListener {
      disposable.add(viewModel.createTicket()
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSubscribe({ showLoading(R.string.creating_ticket) })
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
    button.visibility = View.GONE

    progressBarTV.text = requireContext().resources.getString(textId)

    progressBar.visibility = View.VISIBLE
    progressBarTV.visibility = View.VISIBLE
  }

  private fun buildDataIntent(): Intent {
    val intent = Intent()

    intent.putExtra(SESSION, "session")

    return intent
  }
}
