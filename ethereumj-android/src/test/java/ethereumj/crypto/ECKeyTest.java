package ethereumj.crypto;

import ethereumj.util.ByteUtil;
import java.math.BigInteger;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class ECKeyTest {
  @Test public void normalizeKey() {
    assertEquals(Hex.toHexString(ByteUtil.bigIntegerToBytes(new BigInteger(
            "108191279464149841717973793827743547514612142936774797668560805101538080531630"))),
        "ef3218186444063140bdf989bff7180db151db7055efb77e532ef81e12a924ae");

    assertEquals(Hex.toHexString(ByteUtil.bigIntegerToBytes(new BigInteger(
            "426204606861928785801456429701551205832906413890409391640200265609207827881"), 32)),
        "00f139277e032a7151577ea8aaa69a93333d7ab784b33929f5edfb0e3aef4da9");

    assertEquals(Hex.toHexString(ByteUtil.bigIntegerToBytes(new BigInteger(
            "42620460686192878580145642970155120583290641389040939164020026560920782788198437520934857"),
        32)), "55b320a98dbce93fd31247160b55326175c47f10a14aa65572c6d0dfd019af14");
  }
}