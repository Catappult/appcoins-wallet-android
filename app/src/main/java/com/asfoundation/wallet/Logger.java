package com.asfoundation.wallet;

public interface Logger {

  void log(Throwable throwable);

  void log(String message);
}
