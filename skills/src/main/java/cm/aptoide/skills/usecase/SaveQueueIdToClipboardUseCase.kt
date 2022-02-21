package cm.aptoide.skills.usecase

import android.content.ClipData
import android.content.ClipboardManager
import javax.inject.Inject

class SaveQueueIdToClipboardUseCase @Inject constructor(private val clipboardManager: ClipboardManager) {
  companion object {
    private const val SKILLS_ROOM_NAME = "skills_room_name"
  }

  operator fun invoke(queueId: String) {
    val clip = ClipData.newPlainText(SKILLS_ROOM_NAME, queueId)
    clipboardManager.setPrimaryClip(clip)
  }
}
