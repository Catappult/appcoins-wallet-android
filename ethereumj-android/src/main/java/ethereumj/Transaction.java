package ethereumj;

import ethereumj.crypto.ECKey;
import ethereumj.crypto.ECKey.ECDSASignature;
import ethereumj.crypto.ECKey.MissingPrivateKeyException;
import ethereumj.crypto.HashUtil;
import ethereumj.datasource.MemSizeEstimator;
import ethereumj.util.ByteUtil;
import ethereumj.util.RLP;
import ethereumj.util.RLPElement;
import ethereumj.util.RLPItem;
import ethereumj.util.RLPList;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.logging.Logger;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import static ethereumj.datasource.MemSizeEstimator.ByteArrayEstimator;
import static ethereumj.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static ethereumj.util.ByteUtil.ZERO_BYTE_ARRAY;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

public class Transaction {

  public static final int HASH_LENGTH = 32;
  public static final int ADDRESS_LENGTH = 20;
  private static final Logger logger = Logger.getLogger(Transaction.class.getName());
  private static final BigInteger DEFAULT_GAS_PRICE = new BigInteger("10000000000000");
  private static final BigInteger DEFAULT_BALANCE_GAS = new BigInteger("21000");
  private static final int CHAIN_ID_INC = 35;
  private static final int LOWER_REAL_V = 27;
  protected byte[] sendAddress;
  protected byte[] rlpEncoded;
  public static final MemSizeEstimator<Transaction> MemEstimator =
      tx -> ByteArrayEstimator.estimateSize(tx.hash)
          + ByteArrayEstimator.estimateSize(tx.nonce)
          + ByteArrayEstimator.estimateSize(tx.value)
          + ByteArrayEstimator.estimateSize(tx.gasPrice)
          + ByteArrayEstimator.estimateSize(tx.gasLimit)
          + ByteArrayEstimator.estimateSize(tx.data)
          + ByteArrayEstimator.estimateSize(tx.sendAddress)
          + ByteArrayEstimator.estimateSize(tx.rlpEncoded)
          + ByteArrayEstimator.estimateSize(tx.rawHash)
          + (tx.chainId != null ? 24 : 0)
          + (tx.signature != null ? 208 : 0)
          + 16;
  protected boolean parsed;
  private byte[] hash;
  private byte[] nonce;
  private byte[] value;
  private byte[] receiveAddress;
  private byte[] gasPrice;
  private byte[] gasLimit;
  private byte[] data;
  private Integer chainId;
  private ECDSASignature signature;
  private byte[] rlpRaw;

  public Transaction(byte[] rawData) {
    this.rlpEncoded = rawData;
    parsed = false;
  }

  public Transaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress,
      byte[] value, byte[] data, Integer chainId) {
    this.nonce = nonce;
    this.gasPrice = gasPrice;
    this.gasLimit = gasLimit;
    this.receiveAddress = receiveAddress;
    if (ByteUtil.isSingleZero(value)) {
      this.value = EMPTY_BYTE_ARRAY;
    } else {
      this.value = value;
    }
    this.data = data;
    this.chainId = chainId;

    if (receiveAddress == null) {
      this.receiveAddress = EMPTY_BYTE_ARRAY;
    }

    parsed = true;
  }

  public Transaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress,
      byte[] value, byte[] data) {
    this(nonce, gasPrice, gasLimit, receiveAddress, value, data, null);
  }

  public Transaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress,
      byte[] value, byte[] data, byte[] r, byte[] s, byte v, Integer chainId) {
    this(nonce, gasPrice, gasLimit, receiveAddress, value, data, chainId);
    this.signature = ECDSASignature.fromComponents(r, s, v);
  }

  private byte[] rawHash;

  public static Transaction createDefault(String to, BigInteger amount, BigInteger nonce) {
    return create(to, amount, nonce, DEFAULT_GAS_PRICE, DEFAULT_BALANCE_GAS);
  }

  public static Transaction createDefault(String to, BigInteger amount, BigInteger nonce,
      Integer chainId) {
    return create(to, amount, nonce, DEFAULT_GAS_PRICE, DEFAULT_BALANCE_GAS, chainId);
  }

  public static Transaction create(String to, BigInteger amount, BigInteger nonce,
      BigInteger gasPrice, BigInteger gasLimit) {
    return new Transaction(BigIntegers.asUnsignedByteArray(nonce),
        BigIntegers.asUnsignedByteArray(gasPrice), BigIntegers.asUnsignedByteArray(gasLimit),
        Hex.decode(to), BigIntegers.asUnsignedByteArray(amount), null);
  }

  public static Transaction create(String to, BigInteger amount, BigInteger nonce,
      BigInteger gasPrice, BigInteger gasLimit, Integer chainId) {
    return new Transaction(BigIntegers.asUnsignedByteArray(nonce),
        BigIntegers.asUnsignedByteArray(gasPrice), BigIntegers.asUnsignedByteArray(gasLimit),
        Hex.decode(to), BigIntegers.asUnsignedByteArray(amount), null, chainId);
  }

  private Integer extractChainIdFromV(BigInteger bv) {
    if (bv.bitLength() > 31) {
      return Integer.MAX_VALUE;
    }
    long v = bv.longValue();
    if (v == LOWER_REAL_V || v == (LOWER_REAL_V + 1)) return null;
    return (int) ((v - CHAIN_ID_INC) / 2);
  }

  private byte getRealV(BigInteger bv) {
    if (bv.bitLength() > 31) {
      return 0;
    }
    long v = bv.longValue();
    if (v == LOWER_REAL_V || v == (LOWER_REAL_V + 1)) return (byte) v;
    byte realV = LOWER_REAL_V;
    int inc = 0;
    if ((int) v % 2 == 0) inc = 1;
    return (byte) (realV + inc);
  }

  public synchronized void verify() {
    rlpParse();
    validate();
  }

  public synchronized void rlpParse() {
    if (parsed) return;
    try {
      RLPList decodedTxList = RLP.decode2(rlpEncoded);
      RLPList transaction = (RLPList) decodedTxList.get(0);

      if (transaction.size() > 9) throw new RuntimeException("Too many RLP elements");
      for (RLPElement rlpElement : transaction) {
        if (!(rlpElement instanceof RLPItem)) {
          throw new RuntimeException("Transaction RLP elements shouldn't be lists");
        }
      }

      this.nonce = transaction.get(0)
          .getRLPData();
      this.gasPrice = transaction.get(1)
          .getRLPData();
      this.gasLimit = transaction.get(2)
          .getRLPData();
      this.receiveAddress = transaction.get(3)
          .getRLPData();
      this.value = transaction.get(4)
          .getRLPData();
      this.data = transaction.get(5)
          .getRLPData();
      if (transaction.get(6)
          .getRLPData() != null) {
        byte[] vData = transaction.get(6)
            .getRLPData();
        BigInteger v = ByteUtil.bytesToBigInteger(vData);
        this.chainId = extractChainIdFromV(v);
        byte[] r = transaction.get(7)
            .getRLPData();
        byte[] s = transaction.get(8)
            .getRLPData();
        this.signature = ECDSASignature.fromComponents(r, s, getRealV(v));
      } else {
        logger.info("RLP encoded tx is not signed!");
      }
      this.parsed = true;
      this.hash = getHash();
    } catch (Exception e) {
      throw new RuntimeException("Error on parsing RLP", e);
    }
  }

  private void validate() {
    if (getNonce().length > HASH_LENGTH) throw new RuntimeException("Nonce is not valid");
    if (receiveAddress != null
        && receiveAddress.length != 0
        && receiveAddress.length != ADDRESS_LENGTH) {
      throw new RuntimeException("Receive address is not valid");
    }
    if (gasLimit.length > HASH_LENGTH) throw new RuntimeException("Gas Limit is not valid");
    if (gasPrice != null && gasPrice.length > HASH_LENGTH) {
      throw new RuntimeException("Gas Price is not valid");
    }
    if (value != null && value.length > HASH_LENGTH) {
      throw new RuntimeException("Value is not valid");
    }
    if (getSignature() != null) {
      if (BigIntegers.asUnsignedByteArray(signature.r).length > HASH_LENGTH) {
        throw new RuntimeException("Signature R is not valid");
      }
      if (BigIntegers.asUnsignedByteArray(signature.s).length > HASH_LENGTH) {
        throw new RuntimeException("Signature S is not valid");
      }
      if (getSender() != null && getSender().length != ADDRESS_LENGTH) {
        throw new RuntimeException("Sender is not valid");
      }
    }
  }

  public boolean isParsed() {
    return parsed;
  }

  public byte[] getHash() {
    if (!isEmpty(hash)) return hash;

    rlpParse();
    byte[] plainMsg = this.getEncoded();
    return HashUtil.sha3(plainMsg);
  }

  public Transaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress,
      byte[] value, byte[] data, byte[] r, byte[] s, byte v) {
    this(nonce, gasPrice, gasLimit, receiveAddress, value, data, r, s, v, null);
  }

  public byte[] getNonce() {
    rlpParse();

    return nonce == null ? ZERO_BYTE_ARRAY : nonce;
  }

  protected void setNonce(byte[] nonce) {
    this.nonce = nonce;
    parsed = true;
  }

  public boolean isValueTx() {
    rlpParse();
    return value != null;
  }

  public byte[] getValue() {
    rlpParse();
    return value == null ? ZERO_BYTE_ARRAY : value;
  }

  protected void setValue(byte[] value) {
    this.value = value;
    parsed = true;
  }

  public byte[] getReceiveAddress() {
    rlpParse();
    return receiveAddress;
  }

  protected void setReceiveAddress(byte[] receiveAddress) {
    this.receiveAddress = receiveAddress;
    parsed = true;
  }

  public byte[] getGasPrice() {
    rlpParse();
    return gasPrice == null ? ZERO_BYTE_ARRAY : gasPrice;
  }

  protected void setGasPrice(byte[] gasPrice) {
    this.gasPrice = gasPrice;
    parsed = true;
  }

  public byte[] getGasLimit() {
    rlpParse();
    return gasLimit == null ? ZERO_BYTE_ARRAY : gasLimit;
  }

  protected void setGasLimit(byte[] gasLimit) {
    this.gasLimit = gasLimit;
    parsed = true;
  }

  public long nonZeroDataBytes() {
    if (data == null) return 0;
    int counter = 0;
    for (byte aData : data) {
      if (aData != 0) ++counter;
    }
    return counter;
  }

  public long zeroDataBytes() {
    if (data == null) return 0;
    int counter = 0;
    for (byte aData : data) {
      if (aData == 0) ++counter;
    }
    return counter;
  }

  public byte[] getData() {
    rlpParse();
    return data;
  }

  protected void setData(byte[] data) {
    this.data = data;
    parsed = true;
  }

  public ECDSASignature getSignature() {
    rlpParse();
    return signature;
  }

  public byte[] getContractAddress() {
    if (!isContractCreation()) return null;
    return HashUtil.calcNewAddr(this.getSender(), this.getNonce());
  }

  public boolean isContractCreation() {
    rlpParse();
    return this.receiveAddress == null || Arrays.equals(this.receiveAddress, EMPTY_BYTE_ARRAY);
  }

  public ECKey getKey() {
    byte[] hash = getRawHash();
    return ECKey.recoverFromSignature(signature.v, signature, hash);
  }

  public synchronized byte[] getSender() {
    try {
      if (sendAddress == null && getSignature() != null) {
        sendAddress = ECKey.signatureToAddress(getRawHash(), getSignature());
      }
      return sendAddress;
    } catch (SignatureException e) {
      logger.severe(e.getMessage());
    }
    return null;
  }

  public Integer getChainId() {
    rlpParse();
    return chainId;
  }

  public void sign(byte[] privKeyBytes) throws MissingPrivateKeyException {
    sign(ECKey.fromPrivate(privKeyBytes));
  }

  public void sign(ECKey key) throws MissingPrivateKeyException {
    this.signature = key.sign(this.getRawHash());
    this.rlpEncoded = null;
  }

  public String toString(int maxDataSize) {
    rlpParse();
    String dataS;
    if (data == null) {
      dataS = "";
    } else if (data.length < maxDataSize) {
      dataS = ByteUtil.toHexString(data);
    } else {
      dataS = ByteUtil.toHexString(Arrays.copyOfRange(data, 0, maxDataSize))
          + "... ("
          + data.length
          + " bytes)";
    }
    return "TransactionData ["
        + "hash="
        + ByteUtil.toHexString(hash)
        + "  nonce="
        + ByteUtil.toHexString(nonce)
        + ", gasPrice="
        + ByteUtil.toHexString(gasPrice)
        + ", gas="
        + ByteUtil.toHexString(gasLimit)
        + ", receiveAddress="
        + ByteUtil.toHexString(receiveAddress)
        + ", sendAddress="
        + ByteUtil.toHexString(getSender())
        + ", value="
        + ByteUtil.toHexString(value)
        + ", data="
        + dataS
        + ", signatureV="
        + (signature == null ? "" : signature.v)
        + ", signatureR="
        + (signature == null ? ""
        : ByteUtil.toHexString(BigIntegers.asUnsignedByteArray(signature.r)))
        + ", signatureS="
        + (signature == null ? ""
        : ByteUtil.toHexString(BigIntegers.asUnsignedByteArray(signature.s)))
        + "]";
  }

  public byte[] getEncodedRaw() {

    rlpParse();
    if (rlpRaw != null) return rlpRaw;

    byte[] nonce = null;
    if (this.nonce == null || this.nonce.length == 1 && this.nonce[0] == 0) {
      nonce = RLP.encodeElement(null);
    } else {
      nonce = RLP.encodeElement(this.nonce);
    }
    byte[] gasPrice = RLP.encodeElement(this.gasPrice);
    byte[] gasLimit = RLP.encodeElement(this.gasLimit);
    byte[] receiveAddress = RLP.encodeElement(this.receiveAddress);
    byte[] value = RLP.encodeElement(this.value);
    byte[] data = RLP.encodeElement(this.data);

    if (chainId == null) {
      rlpRaw = RLP.encodeList(nonce, gasPrice, gasLimit, receiveAddress, value, data);
    } else {
      byte[] v, r, s;
      v = RLP.encodeInt(chainId);
      r = RLP.encodeElement(EMPTY_BYTE_ARRAY);
      s = RLP.encodeElement(EMPTY_BYTE_ARRAY);
      rlpRaw = RLP.encodeList(nonce, gasPrice, gasLimit, receiveAddress, value, data, v, r, s);
    }
    return rlpRaw;
  }

  public byte[] getEncoded() {

    if (rlpEncoded != null) return rlpEncoded;

    byte[] nonce = null;
    if (this.nonce == null || this.nonce.length == 1 && this.nonce[0] == 0) {
      nonce = RLP.encodeElement(null);
    } else {
      nonce = RLP.encodeElement(this.nonce);
    }
    byte[] gasPrice = RLP.encodeElement(this.gasPrice);
    byte[] gasLimit = RLP.encodeElement(this.gasLimit);
    byte[] receiveAddress = RLP.encodeElement(this.receiveAddress);
    byte[] value = RLP.encodeElement(this.value);
    byte[] data = RLP.encodeElement(this.data);

    byte[] v, r, s;

    if (signature != null) {
      int encodeV;
      if (chainId == null) {
        encodeV = signature.v;
      } else {
        encodeV = signature.v - LOWER_REAL_V;
        encodeV += chainId * 2 + CHAIN_ID_INC;
      }
      v = RLP.encodeInt(encodeV);
      r = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.r));
      s = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.s));
    } else {
      v = chainId == null ? RLP.encodeElement(EMPTY_BYTE_ARRAY) : RLP.encodeInt(chainId);
      r = RLP.encodeElement(EMPTY_BYTE_ARRAY);
      s = RLP.encodeElement(EMPTY_BYTE_ARRAY);
    }

    this.rlpEncoded =
        RLP.encodeList(nonce, gasPrice, gasLimit, receiveAddress, value, data, v, r, s);

    this.hash = this.getHash();

    return rlpEncoded;
  }

  @Override public int hashCode() {

    byte[] hash = this.getHash();
    int hashCode = 0;

    for (int i = 0; i < hash.length; ++i) {
      hashCode += hash[i] * i;
    }

    return hashCode;
  }

  @Override public boolean equals(Object obj) {

    if (!(obj instanceof Transaction)) return false;
    Transaction tx = (Transaction) obj;

    return tx.hashCode() == this.hashCode();
  }

  @Override public String toString() {
    return toString(Integer.MAX_VALUE);
  }

  public byte[] getRawHash() {
    rlpParse();
    if (rawHash != null) return rawHash;
    byte[] plainMsg = this.getEncodedRaw();
    return rawHash = HashUtil.sha3(plainMsg);
  }
}
