package cm.aptoide.skills

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cm.aptoide.skills.endgame.SkillsEndgameFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillsActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_skills)
    intent.getStringExtra("ESKILLS_URI")?.let { uri ->
      if (uri.contains("endgame", true)) {
        supportFragmentManager.beginTransaction()
          .replace(R.id.fragment_container, SkillsEndgameFragment.newInstance(uri))
          .addToBackStack(SkillsEndgameFragment::class.java.simpleName)
          .commit()
      } else {
        if (savedInstanceState == null) {
          supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SkillsFragment.newInstance())
            .commit()
        } else {
          null
        }
      }
    }
  }
}