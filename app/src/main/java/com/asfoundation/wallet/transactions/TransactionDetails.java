package com.asfoundation.wallet.transactions;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Created by Joao Raimundo on 18/05/2018.
 */
public class TransactionDetails implements Parcelable {

  public static final Creator<TransactionDetails> CREATOR = new Creator<TransactionDetails>() {
    @Override public TransactionDetails createFromParcel(Parcel in) {
      return new TransactionDetails(in);
    }

    @Override public TransactionDetails[] newArray(int size) {
      return new TransactionDetails[size];
    }
  };
  String sourceName;
  Icon icon;
  String description;

  public TransactionDetails(@Nullable String sourceName, Icon icon, String description) {
    this.sourceName = sourceName;
    this.icon = icon;
    this.description = description;
  }

  protected TransactionDetails(Parcel in) {
    sourceName = in.readString();
    String iconSource = in.readString();
    String iconType = in.readString();
    icon = new Icon(Icon.Type.valueOf(iconType), iconSource);
    description = in.readString();
  }

  @Override public int hashCode() {
    int result = sourceName != null ? sourceName.hashCode() : 0;
    result = 31 * result + (icon != null ? icon.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TransactionDetails)) return false;

    TransactionDetails that = (TransactionDetails) o;

    if (!Objects.equals(sourceName, that.sourceName)) {
      return false;
    }
    if (!Objects.equals(icon, that.icon)) return false;
    return Objects.equals(description, that.description);
  }

  @Override public String toString() {
    return "TransactionDetails{"
        + "sourceName='"
        + sourceName
        + '\''
        + ", icon='"
        + icon
        + '\''
        + ", description='"
        + description
        + '\''
        + '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(sourceName);
    dest.writeString(icon.uri);
    dest.writeString(icon.type.name());
    dest.writeString(description);
  }

  public Icon getIcon() {
    return icon;
  }

  public String getSourceName() {
    return sourceName;
  }

  public String getDescription() {
    return description;
  }

  public static class Icon {
    private final Type type;
    private final String uri;

    public Icon(Type type, String uri) {
      this.type = type;
      this.uri = uri;
    }

    public Type getType() {
      return type;
    }

    public String getUri() {
      return uri;
    }

    @Override public int hashCode() {
      int result = type.hashCode();
      result = 31 * result + uri.hashCode();
      return result;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Icon)) return false;

      Icon icon = (Icon) o;

      if (type != icon.type) return false;
      return uri.equals(icon.uri);
    }

    public enum Type {
      FILE, URL
    }
  }
}
