package io.github.y0ngb1n.samples.gateway.domain;

import cn.hutool.crypto.asymmetric.KeyType;
import io.github.y0ngb1n.samples.gateway.core.ClientDetailsProperties.ClientDetails;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 加密的响应实体
 *
 * @author yangbin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EncryptedResponse extends BaseEncryption implements Serializable {
  private static final long serialVersionUID = -7256712373758117014L;

  public EncryptedResponse(ClientDetails clientDetails) {
    this.clientDetails = clientDetails;
  }

  /** 加密业务数据 */
  public void encrypt(final String data) {
    this.payload = rsa().encryptBase64(data, KeyType.PublicKey);
  }

  /** 生成签名 */
  public void generateSignature() {
    this.signature = rsa().encryptBase64(this.sign(), KeyType.PublicKey);
  }
}
