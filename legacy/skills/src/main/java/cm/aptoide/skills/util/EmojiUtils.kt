package cm.aptoide.skills.util

object EmojiUtils {
  fun getEmojiByUnicode(unicode: Int): String {
    return String(Character.toChars(unicode))
  }
}