package cm.aptoide.skills

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import cm.aptoide.skills.factory.TicketApiFactory
import cm.aptoide.skills.repository.TicketsRepository
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import okhttp3.OkHttpClient

class SkillsFragment : Fragment() {

  companion object {
    fun newInstance() = SkillsFragment()

    const val RESULT_OK = 1
    const val SESSION = "SESSION"
  }

  private lateinit var viewModel: SkillsViewModel
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
    viewModel = SkillsViewModel(
        TicketsRepository(TicketApiFactory.providesTicketApi(OkHttpClient(), Gson())))

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
