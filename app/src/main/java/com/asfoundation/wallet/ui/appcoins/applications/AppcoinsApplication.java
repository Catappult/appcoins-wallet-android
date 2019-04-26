package com.asfoundation.wallet.ui.appcoins.applications;

public class AppcoinsApplication {

  private final String name;
  private final double rating;
  private final String iconUrl;
  private final String featuredGraphic;
  private final String packageName;
  private final String uniqueName;

  public AppcoinsApplication(String name, double rating, String iconUrl, String featuredGraphic,
      String packageName, String uniqueName) {
    this.name = name;
    this.rating = rating;
    this.iconUrl = iconUrl;
    this.featuredGraphic = featuredGraphic;
    this.packageName = packageName;
    this.uniqueName = uniqueName;
  }

  @Override public int hashCode() {
    int result;
    long temp;
    result = name.hashCode();
    temp = Double.doubleToLongBits(rating);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + iconUrl.hashCode();
    result = 31 * result + featuredGraphic.hashCode();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AppcoinsApplication)) return false;

    AppcoinsApplication that = (AppcoinsApplication) o;

    if (Double.compare(that.rating, rating) != 0) return false;
    if (!name.equals(that.name)) return false;
    if (!iconUrl.equals(that.iconUrl)) return false;
    if (!featuredGraphic.equals(that.featuredGraphic)) return false;
    return packageName.equals(that.packageName);
  }

  @Override public String toString() {
    return "AppcoinsApplication{"
        + "name='"
        + name
        + '\''
        + ", rating="
        + rating
        + ", iconUrl='"
        + iconUrl
        + '\''
        + '}';
  }

  public String getName() {
    return name;
  }

  public double getRating() {
    return rating;
  }

  public String getIcon() {
    return iconUrl;
  }

  public String getFeaturedGraphic() {
    return featuredGraphic;
  }

  public String getUniqueName() {
    return uniqueName;
  }

  public String getPackageName() {
    return packageName;
  }
}