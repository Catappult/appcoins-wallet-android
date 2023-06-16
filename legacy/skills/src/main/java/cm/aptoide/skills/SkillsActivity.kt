package cm.aptoide.skills

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cm.aptoide.skills.databinding.EndgameActivityBinding
import cm.aptoide.skills.endgame.SkillsEndgameFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillsActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    intent.getStringExtra("ESKILLS_URI")?.let { uri ->
      if (uri.contains("endgame", true)) {
        val binding = EndgameActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction()
          .add(binding.rankingsFragmentContainer.id, SkillsEndgameFragment.newInstance())
          .commit()
      } else {
        setContentView(R.layout.activity_skills)
        if (savedInstanceState == null) {
          supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, SkillsFragment.newInstance())
            .commit()
        } else {
          null
        }
      }
    }
  }
}