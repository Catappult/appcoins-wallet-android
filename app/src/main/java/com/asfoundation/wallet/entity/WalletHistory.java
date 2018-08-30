package com.asfoundation.wallet.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class WalletHistory {

  @JsonProperty("result") private List<MicroTransaction> result;

  public List<MicroTransaction> getResult() {
    return result;
  }

  public void setResult(List<MicroTransaction> result) {
    this.result = result;
  }

  public static class MicroTransaction {

    @JsonProperty("TxID") private String txID;
    @JsonProperty("amount") private BigInteger amount;
    @JsonProperty("block") private BigInteger block;
    @JsonProperty("icon") private String icon;
    @JsonProperty("receiver") private String receiver;
    @JsonProperty("sender") private String sender;
    @JsonProperty("ts") private Date ts;
    @JsonProperty("type") private String type;

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
