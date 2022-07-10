package io.github.y0ngb1n.samples.gateway;

import io.github.y0ngb1n.samples.gateway.core.RsaUtils;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;

/** @author yangbin */
public class RsaTest {

  private static final String PUBLIC_KEY =
      "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsEHQQUxKePf41yf1pQapfdfaPp+22sj0sWsLu/z+/kxNXvGQhq+gop8jM66XLqu6UKz5uRvz++oLvVBWx2roc1AZgxUXnEmGSXMDdb7OUoPpJ4fAekAcoEalejchNE9hAfMVA1sbd4FC7037x3CGm9lOL8auXTVIIRrVt4Ryw9oBHRMNpTTg0LIrsMEeO+niFP9YMl48Ze3Ppfv6AKNx/e73Cd/3B85ju7LxLJgVcAXkjnQ0+/mekdeGdDqc4tD4w7fD30o+fANYZLz76GnV0H++5BnAZdOxgbEs9Lwv8EXPsUv9XIhI42a8oDOeS9IyGaCg/0kiWe//82/knec+6wIDAQAB";
  private static final String PRIVATE_KEY =
      "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCwQdBBTEp49/jXJ/WlBql919o+n7bayPSxawu7/P7+TE1e8ZCGr6CinyMzrpcuq7pQrPm5G/P76gu9UFbHauhzUBmDFRecSYZJcwN1vs5Sg+knh8B6QBygRqV6NyE0T2EB8xUDWxt3gULvTfvHcIab2U4vxq5dNUghGtW3hHLD2gEdEw2lNODQsiuwwR476eIU/1gyXjxl7c+l+/oAo3H97vcJ3/cHzmO7svEsmBVwBeSOdDT7+Z6R14Z0Opzi0PjDt8PfSj58A1hkvPvoadXQf77kGcBl07GBsSz0vC/wRc+xS/1ciEjjZrygM55L0jIZoKD/SSJZ7//zb+Sd5z7rAgMBAAECggEAeqYRaJ7R48OKIQ3pXWB5aNxfPdydvTjBLQQ0MigmuIYYAM1PqKllukPKlxgSk6NkDuePDkRpTw7aBG53NGUsQ7GlTxXKTDLNgybJbZHMuJaaE6vfQDKEuPLwC095h5EFC/o+0QsA3fPQw/0cCvg+cwyP36b9hlahTogB16YOxkEwGSyvi4HsZO500i7TadjJUsuMOqoxMqcuIqMBzwvrwInrj9EoAz3qDOfgmgymk6VGBmrSuX+mmWJQrkE6kQ7bpDbtxo52Mq1pvJfAqBh2G+ZKviMUnbagI4otqzknG6R/AHYDRLQ1oGNe7Hf6EINeMgF2LaHRsS7e6gCK4rQZIQKBgQDqhdRFbcbNUGX1EFV36WYImYavJoq/97UoChSOT1Vhzo6QET4+2uQG/y1GV9HIldctI6IZIPK3g2I7cENbTLRGqeIwFFflRnUr9zaIOsmdKrk/HCeZLAKJiaPp3jLsTBwRMD6Gk5l/uXP9FAuhe0D2js9mvdkB7+C+1m5VY1RsDQKBgQDAZf+0THBYN6R4jGpzXrqesxudoIbIi6JXkneBr6f0Oh9rVwwnNYbtjMvUAFQjwm1DsLF/utv4OmVWxz0yQoHZCqnNjxrfv4nWascF5pA4toBh5GKWmBV5SSpsWdaEFIxkl50D2WUEMUj/ofFjIrgV5r8g4zKmoOO3FSZyW2KA1wKBgF/Eapw3abkpnZjnJJ+k1Z+pYdBgQsSFWJqTEzXRu6IHATxfbjWomPkqdsiE0AvC9G1gYa81PyglJJX1D+xWdD9u3AiaJhJgJTTg7InQx8fATIky7BCmYhrb8+1qaQ0JP2TdeIrn9wSrLGwqJV25wRtt0c+4sscRt4r2cXO47h55AoGAUQvmfjXg8VhnIRzNTtItvOJoFKtle59OgKNDUk8Dah7900K07ONgdjrkNeb5iqLBUk1hSBQXy2YWAEsnnT9k4V/bjarlwDE4SClsszCfujQuuo3xdcyU5yWKCGCDu9nIFivDybZPFkgInx7Bx2f9scQk6R6r3INTyXDQZEXYpbsCgYA+6+NnS1N0QVeOenXizB3q4sOhexVKgJdCnunycsX8SNiPnqnY5py7m3Xo5lYsQSg5J9csbPwoAh+/aCWhnA0GGdQUnLZuG1L0z3a38A3Of2bPZhgZxHHitxdi3olGbcOC/x6kDexl08ZlZGURvWr60lKnrHqUM6jrqqsI7f39hA==";

  @Test
  public void test_encrypt() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("username", "rosh");
    jsonObject.put("password", "123456");
    String str = jsonObject.toJSONString();
    String encrypt = RsaUtils.encrypt(str, PUBLIC_KEY);

    System.out.println(RsaUtils.encryptPrivateKey(str, PRIVATE_KEY));
    System.out.println(
        RsaUtils.decryptPublicKey(RsaUtils.encryptPrivateKey(str, PRIVATE_KEY), PUBLIC_KEY));
    System.out.println(encrypt);
  }

  @Test
  public void test_decrypt() {
    String decrypt =
        RsaUtils.decrypt(
            "Uw57DJW8TdgrMgBCyRt+OQQSp3tHO5t/IcP3qvnUNEJyE1yuRHPqpO0Xjq7uNuhbAWD+/bfS3gkP1rkuYa7t9/IvoVqnO+MI7/Qqejkk022kJ8zp7eJl0Btu+zN3QMZqf7f2bHrHa0xpsvdWUvMswNwjALhWVfqgzaOv9odDO8vUcm9BCC4cBvBNBxZHezW0ZlbI4rTVYXwZ5pNJf1vsCkS9+pFSdg6y8Xmp6AM76XDkRcVQN/FDELk6M8BDZr/pcEgOcFybLnUFXsxy0W+C0haiBZ/GJP+j9b74tohWpnkB8CFEzrQmNerzYayFCBsVGuuPo3hFW8Bu8vj9xY29/Q==",
            PRIVATE_KEY);
    System.out.println(decrypt);
  }
}
