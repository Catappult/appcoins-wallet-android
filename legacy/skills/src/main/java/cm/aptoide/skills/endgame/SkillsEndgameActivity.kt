package cm.aptoide.skills.endgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cm.aptoide.skills.databinding.EndgameActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillsEndgameActivity : AppCompatActivity() {

  lateinit var binding: EndgameActivityBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = EndgameActivityBinding.inflate(layoutInflater)
    setContentView(binding.root)
  }
}