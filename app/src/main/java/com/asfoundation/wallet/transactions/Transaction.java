package com.asfoundation.wallet.transactions;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class Transaction implements Parcelable {
  private final String transactionId;
  @Nullable private final String approveTransactionId;
  private final TransactionType type;
  private final long timeStamp;
  private final TransactionStatus status;
  private final String value;
  private final String from;
  private final String to;
  private final TransactionDetails details;
  private final String currency;
  private final List<Operation> operations;

  public Transaction(String transactionId, TransactionType type,
      @Nullable String approveTransactionId, long timeStamp, TransactionStatus status, String value,
      String from, String to, @Nullable TransactionDetails details, String currency, List<Operation> operations) {
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

  public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
    @Override public Transaction createFromParcel(Parcel in) {
      return new Transaction(in);
    }

    @Override public Transaction[] newArray(int size) {
      return new Transaction[size];
    }
  };

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

  protected Transaction(Parcel in) {
    transactionId = in.readString();
    approveTransactionId = in.readString();
    type = TransactionType.fromInt(in.readInt());
    timeStamp = in.readLong();
    status = TransactionStatus.fromInt(in.readInt());
    value = in.readString();
    from = in.readString();
    to = in.readString();
    details = in.readParcelable(TransactionDetails.class.getClassLoader());;
    currency = in.readString();
    Parcelable[] parcelableArray =
        in.readParcelableArray(Operation.class.getClassLoader());
    operations = new ArrayList<>();
    if (parcelableArray != null) {
      Operation[] operationsArray =
          Arrays.copyOf(parcelableArray, parcelableArray.length, Operation[].class);
      operations.addAll(Arrays.asList(operationsArray));
    }

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
    STANDARD, IAB, ADS, OPEN_CHANNEL, TOP_UP_CHANNEL, CLOSE_CHANNEL, MICRO_IAB;

    static TransactionType fromInt(int type) {
     switch (type) {
       case 0:
         return STANDARD;
       case 1:
         return IAB;
       case 2:
         return ADS;
       case 3:
         return OPEN_CHANNEL;
       case 4:
         return TOP_UP_CHANNEL;
       case 5:
         return CLOSE_CHANNEL;
       case 6:
         return MICRO_IAB;
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

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Transaction that = (Transaction) o;
    return timeStamp == that.timeStamp
        && Objects.equals(transactionId, that.transactionId)
        && Objects.equals(approveTransactionId, that.approveTransactionId)
        && type == that.type
        && status == that.status
        && Objects.equals(value, that.value)
        && Objects.equals(from, that.from)
        && Objects.equals(to, that.to)
        && Objects.equals(details, that.details)
        && Objects.equals(currency, that.currency)
        && Objects.equals(operations, that.operations);
  }

  @Override public int hashCode() {
    return Objects.hash(transactionId, approveTransactionId, type, timeStamp, status, value, from,
        to, details, currency, operations);
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
        + ", details='"
        + details
        + '\''
        + ", currency='"
        + currency
        + '\''
        + ", operations="
        + operations
        + '}';
  }
}
