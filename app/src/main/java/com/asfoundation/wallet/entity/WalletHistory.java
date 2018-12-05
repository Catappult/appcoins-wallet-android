package com.asfoundation.wallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

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
    @JsonProperty("block") private BigInteger block;
    @JsonProperty("bonus") private BigDecimal bonus;
    @JsonProperty("icon") private String icon;
    @JsonProperty("receiver") private String receiver;
    @JsonProperty("sender") private String sender;
    @JsonProperty("ts") private Date ts;
    @JsonProperty("type") private String type;
    @JsonProperty("status") private Status status;

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

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
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
  }
}
