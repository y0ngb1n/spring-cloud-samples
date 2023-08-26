package io.github.y0ngb1n.samples.gateway.domain;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 加密的请求实体
 *
 * @author yangbin
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EncryptedRequest extends BaseEncryption implements Serializable {
  private static final long serialVersionUID = -8386162285451102460L;

  /** 验证签名 */
  public boolean verifySignature() {
    final String sha256Hex = this.sign();
    final String requestSha256Hex = StrUtil.str(rsa().decrypt(this.signature, KeyType.PrivateKey),
        StandardCharsets.UTF_8);
    return Objects.equals(requestSha256Hex, sha256Hex);
  }

  /** 解密业务数据 */
  public String decrypt() {
    return new String(rsa().decrypt(this.payload, KeyType.PrivateKey), StandardCharsets.UTF_8);
  }
}
