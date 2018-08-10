package com.asfoundation.wallet.util;

/**
 * String format utility class.
 */
public class StringUtils {

  /**
   * Resize the given string to the given size and set the given ellipsize string in the end of the
   * formatted string.
   * @param str The string to be formatted
   * @param size The size of the formatted string
   * @param ellipsize The ellipsize string to pe added to the end of the string
   *
   * @return The resized String
   */
  public static String resizeString(String str, int size, String ellipsize) {
    return resizeString(str, size, true, ellipsize);
  }

  /**
   * Resize the given string to the given size and set the given ellipsize string in end of the
   * string if the resize is to be done fromStart, puts the ellipsize string in the start otherwise.
   * @param str The string to be formatted
   * @param size The size of the formatted string
   * @param ellipsize The ellipsize string to pe added to the end of the string
   * @param fromStart Boolean indicating if the resize is done from the start or from the end.
   *
   * @return The resized String
   */
  public static String resizeString(String str, int size, boolean fromStart, String ellipsize) {
    StringBuilder builder = new StringBuilder();
    int startIndex = fromStart ? 0 : str.length() - size;
    int endIndex = fromStart ? size : str.length();
    if (ellipsize == null) {
      ellipsize = "...";
    }

    builder.append(str.substring(startIndex, endIndex));
    if (fromStart) {
      builder.append(ellipsize);
    } else {
      builder.insert(0, ellipsize);
    }
    return builder.toString();
  }
}
