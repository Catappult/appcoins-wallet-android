package com.appcoins.wallet.core.network.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true) public class WalletHistory {

  @JsonProperty("result") private List<Transaction> result;

  public List<Transaction> getResult() {
    return result;
  }

  public void setResult(List<Transaction> result) {
    this.result = result;
  }

  public enum Status {
    SUCCESS, FAIL
  }

  @JsonIgnoreProperties(ignoreUnknown = true) public static class Transaction {

    @JsonProperty("app") private String app;
    @JsonProperty("sku") private String sku;
    @JsonProperty("TxID") private String txID;
    @JsonProperty("amount") private BigInteger amount;
    @JsonProperty("paid_currency_amount") @Nullable private String paidAmount;
    @JsonProperty("paid_currency") @Nullable private String paidCurrency;
    @JsonProperty("block") private BigInteger block;
    @JsonProperty("bonus") private BigDecimal bonus;
    @JsonProperty("icon") private String icon;
    @JsonProperty("receiver") private String receiver;
    @JsonProperty("sender") private String sender;
    @JsonProperty("ts") private Date ts;
    @JsonProperty("processed_time") private Date processedTime;
    @JsonProperty("type") private String type;
    @JsonProperty("subtype") private String subType;
    @JsonProperty("method") private String method;
    @JsonProperty("title") private String title;
    @JsonProperty("description") private String description;
    @JsonProperty("perk") private String perk;
    @JsonProperty("status") private Status status;
    @JsonProperty("operations") private List<Operation> operations;
    @JsonProperty("linked_tx") private List<String> linkedTx;
    @JsonProperty("reference") private String orderReference;

    public List<Operation> getOperations() {
      return operations;
    }

    public void setOperations(List<Operation> operations) {
      this.operations = operations;
    }

    public String getSku() {
      return sku;
    }

    public void setSku(String sku) {
      this.sku = sku;
    }

    public Status getStatus() {
      return status;
    }

    public void setStatus(Status status) {
      this.status = status;
    }

    public String getApp() {
      return app;
    }

    public void setApp(String app) {
      this.app = app;
    }

    public String getTxID() {
      return txID;
    }

    public void setTxID(String txID) {
      this.txID = txID;
    }

    public BigInteger getAmount() {
      return amount;
    }

    public void setAmount(BigInteger amount) {
      this.amount = amount;
    }

    public String getPaidAmount() {
      return paidAmount;
    }

    public void setPaidAmount(String paidAmount) {
      this.paidAmount = paidAmount;
    }

    public String getPaidCurrency() {
      return paidCurrency;
    }

    public void setPaidCurrency(String paidCurrency) {
      this.paidCurrency = paidCurrency;
    }

    public BigInteger getBlock() {
      return block;
    }

    public void setBlock(BigInteger block) {
      this.block = block;
    }

    public String getIcon() {
      return icon;
    }

    public void setIcon(String icon) {
      this.icon = icon;
    }

    public String getReceiver() {
      return receiver;
    }

    public void setReceiver(String receiver) {
      this.receiver = receiver;
    }

    public BigDecimal getBonus() {
      return bonus;
    }

    public void setBonus(BigDecimal bonus) {
      this.bonus = bonus;
    }

    public String getSender() {
      return sender;
    }

    public void setSender(String sender) {
      this.sender = sender;
    }

    public Date getTs() {
      return ts;
    }

    public void setTs(Date ts) {
      this.ts = ts;
    }

    public Date getProcessedTime() {
      return processedTime;
    }

    public void setProcessedTime(Date processedTime) {
      this.processedTime = processedTime;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getMethod() {
      return method;
    }

    public void setMethod(String method) {
      this.method = method;
    }

    public List<String> getLinkedTx() {
      return linkedTx;
    }

    public void setLinkedTx(List<String> linkedTx) {
      this.linkedTx = linkedTx;
    }

    public String getOrderReference() {
      return orderReference;
    }

    public void setOrderReference(String orderReference) {
      this.orderReference = orderReference;
    }

    @Override public String toString() {
      return "Result{"
          + "txID='"
          + txID
          + '\''
          + ", amount="
          + amount
          + ", block="
          + block
          + ", receiver='"
          + receiver
          + '\''
          + ", sender='"
          + sender
          + '\''
          + ", ts='"
          + ts
          + '\''
          + ", type='"
          + type
          + '\''
          + '}';
    }

    public String getSubType() {
      return subType;
    }

    public void setSubType(String subType) {
      this.subType = subType;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getPerk() {
      return perk;
    }

    public void setPerk(String perk) {
      this.perk = perk;
    }
  }

  public static class Operation {
    @JsonProperty("TxID") private String transactionId;
    private String fee;
    private String receiver;
    private String sender;

    public String getTransactionId() {
      return transactionId;
    }

    public void setTransactionId(String transactionId) {
      this.transactionId = transactionId;
    }

    public String getFee() {
      return fee;
    }

    public void setFee(String fee) {
      this.fee = fee;
    }

    public String getSender() {
      return sender;
    }

    public void setSender(String sender) {
      this.sender = sender;
    }

    public String getReceiver() {
      return receiver;
    }

    public void setReceiver(String receiver) {
      this.receiver = receiver;
    }
  }
}
