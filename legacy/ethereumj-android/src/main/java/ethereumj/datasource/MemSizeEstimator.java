package ethereumj.datasource;

public interface MemSizeEstimator<E> {

  MemSizeEstimator<byte[]> ByteArrayEstimator = bytes -> bytes == null ? 0 : bytes.length + 16;

  long estimateSize(E e);
}
