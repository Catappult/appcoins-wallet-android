package com.asfoundation.wallet.transactions;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

public class Transaction implements Parcelable {
  public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
    @Override public Transaction createFromParcel(Parcel in) {
      return new Transaction(in);
    }

    @Override public Transaction[] newArray(int size) {
      return new Transaction[size];
    }
  };
  private final String transactionId;
  @Nullable private final String approveTransactionId;
  private final TransactionType type;
  private final long timeStamp;
  private final TransactionStatus status;
  private final String value;
  private final String from;
  private final String to;
  @Nullable private final TransactionDetails details;
  @Nullable private final String currency;
  @Nullable private final List<Operation> operations;

  public Transaction(String transactionId, TransactionType type,
      @Nullable String approveTransactionId, long timeStamp, TransactionStatus status, String value,
      String from, String to, @Nullable TransactionDetails details, String currency,
      List<Operation> operations) {
    this.transactionId = transactionId;
    this.approveTransactionId = approveTransactionId;
    this.type = type;
    this.timeStamp = timeStamp;
    this.status = status;
    this.value = value;
    this.from = from;
    this.to = to;
    this.details = details;
    this.currency = currency;
    this.operations = operations;
  }

  protected Transaction(Parcel in) {
    transactionId = in.readString();
    approveTransactionId = in.readString();
    type = TransactionType.fromInt(in.readInt());
    timeStamp = in.readLong();
    status = TransactionStatus.fromInt(in.readInt());
    value = in.readString();
    from = in.readString();
    to = in.readString();
    details = in.readParcelable(TransactionDetails.class.getClassLoader());
    currency = in.readString();
    Parcelable[] parcelableArray = in.readParcelableArray(Operation.class.getClassLoader());
    operations = new ArrayList<>();
    if (parcelableArray != null) {
      Operation[] operationsArray =
          Arrays.copyOf(parcelableArray, parcelableArray.length, Operation[].class);
      operations.addAll(Arrays.asList(operationsArray));
    }
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(transactionId);
    dest.writeString(approveTransactionId);
    dest.writeInt(type.ordinal());
    dest.writeLong(timeStamp);
    dest.writeInt(status.ordinal());
    dest.writeString(value);
    dest.writeString(from);
    dest.writeString(to);
    dest.writeParcelable(details, flags);
    dest.writeString(currency);
    Operation[] operationsArray = new Operation[0];
    if (operations != null) {
      operationsArray = new Operation[operations.size()];
      operations.toArray(operationsArray);
    }
    dest.writeParcelableArray(operationsArray, flags);
  }

  @Override public int hashCode() {
    int result = transactionId.hashCode();
    result = 31 * result + (approveTransactionId != null ? approveTransactionId.hashCode() : 0);
    result = 31 * result + type.hashCode();
    result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
    result = 31 * result + status.hashCode();
    result = 31 * result + value.hashCode();
    result = 31 * result + from.hashCode();
    result = 31 * result + to.hashCode();
    result = 31 * result + (details != null ? details.hashCode() : 0);
    result = 31 * result + (currency != null ? currency.hashCode() : 0);
    result = 31 * result + (operations != null ? operations.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Transaction)) return false;

    Transaction that = (Transaction) o;

    if (timeStamp != that.timeStamp) return false;
    if (!transactionId.equals(that.transactionId)) return false;
    if (approveTransactionId != null ? !approveTransactionId.equals(that.approveTransactionId)
        : that.approveTransactionId != null) {
      return false;
    }
    if (type != that.type) return false;
    if (status != that.status) return false;
    if (!value.equals(that.value)) return false;
    if (!from.equals(that.from)) return false;
    if (!to.equals(that.to)) return false;
    if (details != null ? !details.equals(that.details) : that.details != null) return false;
    if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;
    return operations != null ? operations.equals(that.operations) : that.operations == null;
  }

  @Override public String toString() {
    return "Transaction{"
        + "transactionId='"
        + transactionId
        + '\''
        + ", approveTransactionId='"
        + approveTransactionId
        + '\''
        + ", type="
        + type
        + ", timeStamp="
        + timeStamp
        + ", status="
        + status
        + ", value='"
        + value
        + '\''
        + ", from='"
        + from
        + '\''
        + ", to='"
        + to
        + '\''
        + ", details="
        + details
        + ", currency='"
        + currency
        + '\''
        + ", operations="
        + operations
        + '}';
  }

  public String getApproveTransactionId() {
    return approveTransactionId;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public TransactionType getType() {
    return type;
  }

  public TransactionStatus getStatus() {
    return status;
  }

  public String getValue() {
    return value;
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }

  public TransactionDetails getDetails() {
    return details;
  }

  public List<Operation> getOperations() {
    return operations;
  }

  public String getCurrency() {
    return currency;
  }

  public enum TransactionType {
    STANDARD, IAB, ADS, IAP_OFFCHAIN, ADS_OFFCHAIN, BONUS, TOP_UP, TRANSFER_OFF_CHAIN;

    static TransactionType fromInt(int type) {
      switch (type) {
        case 0:
          return STANDARD;
        case 1:
          return IAB;
        case 2:
          return ADS;
        case 3:
          return IAP_OFFCHAIN;
        case 4:
          return ADS_OFFCHAIN;
        case 5:
          return BONUS;
        case 6:
          return TOP_UP;
        case 7:
          return TRANSFER_OFF_CHAIN;
        default:
          return STANDARD;
      }
    }
  }

  public enum TransactionStatus {
    SUCCESS, FAILED, PENDING;

    static TransactionStatus fromInt(int status) {
      switch (status) {
        case 0:
          return SUCCESS;
        case 1:
          return FAILED;
        case 2:
          return PENDING;
        default:
          return SUCCESS;
      }
    }
  }
}
