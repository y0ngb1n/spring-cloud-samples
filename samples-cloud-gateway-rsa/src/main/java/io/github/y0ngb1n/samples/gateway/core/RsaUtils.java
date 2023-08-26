package io.github.y0ngb1n.samples.gateway.core;

import io.github.y0ngb1n.samples.gateway.excepiton.RsaException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

/**
 * RSA 加解密工具类
 *
 * @author yangbin
 */
@Slf4j
public class RsaUtils {

  public static final String PUBLIC_KEY = "public_key";

  public static final String PRIVATE_KEY = "private_key";

  public static Map<String, String> generateRasKey() {
    Map<String, String> rs = new HashMap<>();
    try {
      // KeyPairGenerator 类用于生成公钥和私钥对，基于 RSA 算法生成对象
      KeyPairGenerator keyPairGen = null;
      keyPairGen = KeyPairGenerator.getInstance("RSA");
      keyPairGen.initialize(2048, new SecureRandom());
      // 生成一个密钥对，保存在 keyPair 中
      KeyPair keyPair = keyPairGen.generateKeyPair();
      // 得到私钥/公钥
      RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
      RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
      String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
      // 得到私钥字符串
      String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));
      // 将公钥和私钥保存到 Map
      rs.put(PUBLIC_KEY, publicKeyString);
      rs.put(PRIVATE_KEY, privateKeyString);
    } catch (Exception e) {
      log.error("RsaUtils invoke genKeyPair failed.", e);
      throw new RsaException("RsaUtils invoke genKeyPair failed.");
    }
    return rs;
  }

  public static String encrypt(String str, String publicKey) {
    try {
      // base64 编码的公钥
      byte[] decoded = Base64.decodeBase64(publicKey);
      RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
          .generatePublic(new X509EncodedKeySpec(decoded));
      // RSA 加密
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, pubKey);
      return Base64.encodeBase64String(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      log.error("RsaUtils invoke encrypt failed.", e);
      throw new RsaException("RsaUtils invoke encrypt failed.");
    }
  }

  public static String decrypt(String str, String privateKey) {
    try {
      // 64 位解码加密后的字符串
      byte[] inputByte = Base64.decodeBase64(str.getBytes(StandardCharsets.UTF_8));
      // base64 编码的私钥
      byte[] decoded = Base64.decodeBase64(privateKey);
      RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
          .generatePrivate(new PKCS8EncodedKeySpec(decoded));
      // RSA 解密
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, priKey);
      return new String(cipher.doFinal(inputByte));
    } catch (Exception e) {
      log.error("RsaUtils invoke decrypt failed.", e);
      throw new RsaException("RsaUtils invoke decrypt failed.");
    }
  }

  public static String encryptPrivateKey(String str, String privateKey) {
    try {
      // base64 编码的公钥
      byte[] decoded = Base64.decodeBase64(privateKey);
      RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
          .generatePrivate(new PKCS8EncodedKeySpec(decoded));
      // RSA 加密
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, priKey);
      return Base64.encodeBase64String(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      log.error("RsaUtils invoke encrypt failed.", e);
      throw new RsaException("RsaUtils invoke encrypt failed.");
    }
  }

  public static String decryptPublicKey(String str, String publicKey) {
    try {
      // 64 位解码加密后的字符串
      byte[] inputByte = Base64.decodeBase64(str.getBytes(StandardCharsets.UTF_8));
      // base64 编码的私钥
      byte[] decoded = Base64.decodeBase64(publicKey);
      RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
          .generatePublic(new X509EncodedKeySpec(decoded));
      // RSA 解密
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, pubKey);
      return new String(cipher.doFinal(inputByte));
    } catch (Exception e) {
      log.error("RsaUtils invoke decrypt failed.", e);
      throw new RsaException("RsaUtils invoke decrypt failed.");
    }
  }
}
