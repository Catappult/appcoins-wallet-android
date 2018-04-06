package com.asfoundation.wallet.poa;

class NonceData {
  private final long timeStamp;
  private final String packageName;

  public NonceData(long timeStamp, String packageName) {
    this.timeStamp = timeStamp;
    this.packageName = packageName;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public String getPackageName() {
    return packageName;
  }

  @Override public String toString() {
    return "NonceData{" + "timeStamp=" + timeStamp + ", packageName='" + packageName + '\'' + '}';
  }
}
