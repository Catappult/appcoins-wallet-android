package ethereumj.util;

import ethereumj.util.FastByteComparisons.LexicographicalComparerHolder.PureJavaComparer;

@SuppressWarnings("restriction") public abstract class FastByteComparisons {

  public static boolean equal(byte[] b1, byte[] b2) {
    return b1.length == b2.length && compareTo(b1, 0, b1.length, b2, 0, b2.length) == 0;
  }

  public static int compareTo(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
    return LexicographicalComparerHolder.BEST_COMPARER.compareTo(b1, s1, l1, b2, s2, l2);
  }

  private static Comparer<byte[]> lexicographicalComparerJavaImpl() {
    return PureJavaComparer.INSTANCE;
  }

  private interface Comparer<T> {
    int compareTo(T buffer1, int offset1, int length1, T buffer2, int offset2, int length2);
  }

  public static class LexicographicalComparerHolder {
    static final String UNSAFE_COMPARER_NAME =
        LexicographicalComparerHolder.class.getName() + "$UnsafeComparer";

    static final Comparer<byte[]> BEST_COMPARER = getBestComparer();

    static Comparer<byte[]> getBestComparer() {
      try {
        Class<?> theClass = Class.forName(UNSAFE_COMPARER_NAME);

        @SuppressWarnings("unchecked") Comparer<byte[]> comparer =
            (Comparer<byte[]>) theClass.getEnumConstants()[0];
        return comparer;
      } catch (Throwable t) {
        return lexicographicalComparerJavaImpl();
      }
    }

    public enum PureJavaComparer implements Comparer<byte[]> {
      INSTANCE;

      @Override
      public int compareTo(byte[] buffer1, int offset1, int length1, byte[] buffer2, int offset2,
          int length2) {
        if (buffer1 == buffer2 && offset1 == offset2 && length1 == length2) {
          return 0;
        }
        int end1 = offset1 + length1;
        int end2 = offset2 + length2;
        for (int i = offset1, j = offset2; i < end1 && j < end2; i++, j++) {
          int a = (buffer1[i] & 0xff);
          int b = (buffer2[j] & 0xff);
          if (a != b) {
            return a - b;
          }
        }
        return length1 - length2;
      }
    }
  }
}
