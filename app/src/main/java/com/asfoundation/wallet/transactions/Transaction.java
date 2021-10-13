package com.asfoundation.wallet.transactions;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
  @Nullable private final SubType subType;
  @Nullable private final String title;
  @Nullable private final String description;
  @Nullable private final Perk perk;
  private final String approveTransactionId;
  private final TransactionType type;
  private final long timeStamp;
  private final long processedTime;
  private final TransactionStatus status;
  private final String value;
  private final String from;
  private final String to;
  @Nullable private final TransactionDetails details;
  @Nullable private final String currency;
  @Nullable private final List<Operation> operations;
  @Nullable private final List<Transaction> linkedTx;
  @Nullable private final String paidAmount;
  @Nullable private final String paidCurrency;

  public Transaction(String transactionId, TransactionType type, @Nullable SubType subType,
      @Nullable String title, @Nullable String description, @Nullable Perk perk,
      @Nullable String approveTransactionId, long timeStamp, long processedTime,
      TransactionStatus status, String value, String from, String to,
      @Nullable TransactionDetails details, @Nullable String currency,
      @Nullable List<Operation> operations, @Nullable List<Transaction> linkedTx,
      @Nullable String paidAmount, @Nullable String paidCurrency) {
    this.transactionId = transactionId;
    this.subType = subType;
    this.title = title;
    this.description = description;
    this.perk = perk;
    this.approveTransactionId = approveTransactionId;
    this.type = type;
    this.timeStamp = timeStamp;
    this.processedTime = processedTime;
    this.status = status;
    this.value = value;
    this.from = from;
    this.to = to;
    this.details = details;
    this.currency = currency;
    this.operations = operations;
    this.linkedTx = linkedTx;
    this.paidAmount = paidAmount;
    this.paidCurrency = paidCurrency;
  }

  protected Transaction(Parcel in) {
    transactionId = in.readString();
    subType = SubType.fromInt(in.readInt());
    title = in.readString();
    description = in.readString();
    perk = Perk.fromInt(in.readInt());
    approveTransactionId = in.readString();
    type = TransactionType.fromInt(in.readInt());
    timeStamp = in.readLong();
    processedTime = in.readLong();
    status = TransactionStatus.fromInt(in.readInt());
    value = in.readString();
    from = in.readString();
    to = in.readString();
    details = in.readParcelable(TransactionDetails.class.getClassLoader());
    currency = in.readString();
    Parcelable[] operationParcelable = in.readParcelableArray(Operation.class.getClassLoader());
    operations = new ArrayList<>();
    if (operationParcelable != null) {
      Operation[] operationsArray =
          Arrays.copyOf(operationParcelable, operationParcelable.length, Operation[].class);
      operations.addAll(Arrays.asList(operationsArray));
    }
    Parcelable[] linkedTxParcelable = in.readParcelableArray(Transaction.class.getClassLoader());
    linkedTx = new ArrayList<>();
    if (linkedTxParcelable != null) {
      Transaction[] linkedTxArray =
          Arrays.copyOf(linkedTxParcelable, linkedTxParcelable.length, Transaction[].class);
      linkedTx.addAll(Arrays.asList(linkedTxArray));
    }
    paidAmount = in.readString();
    paidCurrency = in.readString();
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(transactionId);
    if (subType != null) {
      dest.writeInt(subType.ordinal());
    } else {
      dest.writeInt(-1);
    }
    dest.writeString(title);
    dest.writeString(description);
    if (perk != null) {
      dest.writeInt(perk.ordinal());
    } else {
      dest.writeInt(-1);
    }
    dest.writeString(approveTransactionId);
    dest.writeInt(type.ordinal());
    dest.writeLong(timeStamp);
    dest.writeLong(processedTime);
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
    Transaction[] linkedTxArray = new Transaction[0];
    if (linkedTx != null) {
      linkedTxArray = new Transaction[linkedTx.size()];
      linkedTx.toArray(linkedTxArray);
    }
    dest.writeParcelableArray(linkedTxArray, flags);
    dest.writeString(paidAmount);
    dest.writeString(paidCurrency);
  }

  @Override public int hashCode() {
    int result = transactionId.hashCode();
    result = 31 * result + (subType != null ? subType.hashCode() : 0);
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (perk != null ? perk.hashCode() : 0);
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
    result = 31 * result + (linkedTx != null ? linkedTx.hashCode() : 0);
    result = 31 * result + (paidAmount != null ? paidAmount.hashCode() : 0);
    result = 31 * result + (paidCurrency != null ? paidCurrency.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Transaction)) return false;

    Transaction that = (Transaction) o;

    if (timeStamp != that.timeStamp) return false;
    if (!transactionId.equals(that.transactionId)) return false;
    if (!Objects.equals(subType, that.subType)) return false;
    if (!Objects.equals(title, that.title)) return false;
    if (!Objects.equals(description, that.description)) return false;
    if (!Objects.equals(perk, that.perk)) return false;
    if (!Objects.equals(approveTransactionId, that.approveTransactionId)) return false;
    if (type != that.type) return false;
    if (status != that.status) return false;
    if (!value.equals(that.value)) return false;
    if (!from.equals(that.from)) return false;
    if (!to.equals(that.to)) return false;
    if (!Objects.equals(details, that.details)) return false;
    if (!Objects.equals(currency, that.currency)) return false;
    if (!Objects.equals(operations, that.operations)) return false;
    if (!Objects.equals(paidAmount, that.paidAmount)) return false;
    if (!Objects.equals(paidCurrency, that.paidCurrency)) return false;
    return Objects.equals(linkedTx, that.linkedTx);
  }

  @Override public String toString() {
    return "Transaction{"
        + "transactionId='"
        + transactionId
        + '\''
        + ", subType="
        + subType
        + ", title='"
        + title
        + '\''
        + ", description='"
        + description
        + '\''
        + ", perk="
        + perk
        + ", approveTransactionId='"
        + approveTransactionId
        + '\''
        + ", type="
        + type
        + ", timeStamp="
        + timeStamp
        + ", processedTime="
        + processedTime
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
        + ", linkedTx="
        + linkedTx
        + ", paidAmount="
        + paidAmount
        + ", paidCurrency="
        + paidCurrency
        + '}';
  }

  @Nullable public String getApproveTransactionId() {
    return approveTransactionId;
  }

  @Nullable public SubType getSubType() {
    return subType;
  }

  @Nullable public Perk getPerk() {
    return perk;
  }

  @Nullable public String getTitle() {
    return title;
  }

  @Nullable public String getDescription() {
    return description;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public long getProcessedTime() {
    return processedTime;
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

  @Nullable public TransactionDetails getDetails() {
    return details;
  }

  @Nullable public List<Operation> getOperations() {
    return operations;
  }

  @Nullable public String getCurrency() {
    return currency;
  }

  @Nullable public List<Transaction> getLinkedTx() {
    return linkedTx;
  }

  @Nullable public String getPaidAmount() {
    return paidAmount;
  }

  @Nullable public String getPaidCurrency() {
    return paidCurrency;
  }

  public enum TransactionType {
    STANDARD, IAP, ADS, IAP_OFFCHAIN, ADS_OFFCHAIN, BONUS, TOP_UP, TRANSFER_OFF_CHAIN,
    ETHER_TRANSFER, BONUS_REVERT, TOP_UP_REVERT, IAP_REVERT, SUBS_OFFCHAIN, ESKILLS_REWARD;

    static TransactionType fromInt(int type) {
      switch (type) {
        case 1:
          return IAP;
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
        case 9:
          return BONUS_REVERT;
        case 10:
          return TOP_UP_REVERT;
        case 11:
          return IAP_REVERT;
        case 12:
          return SUBS_OFFCHAIN;
        case 13:
          return ESKILLS_REWARD;
        default:
          return STANDARD;
      }
    }
  }

  public enum SubType {
    PERK_PROMOTION, UNKNOWN;

    static SubType fromInt(int type) {
      if (type != -1) {
        if (type == 0) {
          return PERK_PROMOTION;
        } else {
          return UNKNOWN;
        }
      } else {
        return null;
      }
    }
  }

  public enum Perk {
    GAMIFICATION_LEVEL_UP, PACKAGE_PERK, UNKNOWN;

    static Perk fromInt(int type) {
      if (type != -1) {
        if (type == 0) {
          return GAMIFICATION_LEVEL_UP;
        } else if (type == 1) {
          return PACKAGE_PERK;
        } else {
          return UNKNOWN;
        }
      } else {
        return null;
      }
    }
  }

  public enum TransactionStatus {
    SUCCESS, FAILED, PENDING;

    static TransactionStatus fromInt(int status) {
      switch (status) {
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
