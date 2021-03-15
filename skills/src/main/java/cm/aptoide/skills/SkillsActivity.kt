package cm.aptoide.skills

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SkillsActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_skills)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
          .add(R.id.fragment_container, SkillsFragment.newInstance())
          .commit()
    }

  }
}