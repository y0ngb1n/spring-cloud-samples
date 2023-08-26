package io.github.y0ngb1n.samples.gateway.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.asymmetric.RSA;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import io.github.y0ngb1n.samples.gateway.core.ClientDetailsProperties;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.Assert;

/** @author yangbin */
@Data
@Slf4j
public abstract class BaseEncryption {
  private static final long serialVersionUID = -3256712373758117012L;

  /** 请求流水号 */
  @NotBlank
  protected String requestId;

  /** 请求时间戳 */
  @NotBlank
  @Pattern(regexp = "\\d{10}", message = "错误的时间戳")
  protected String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

  /** 业务数据 */
  @NotBlank
  protected String payload;

  /** 请求签名 */
  @NotBlank
  protected String signature;

  /** 请求客户端配置 */
  protected transient ClientDetailsProperties.ClientDetails clientDetails;

  /** RSA 实例 */
  protected RSA rsa() {
    Assert.isTrue(Objects.nonNull(clientDetails), "未初始化客户端配置");
    return new RSA(this.clientDetails.getSecret(), this.clientDetails.getKey());
  }

  /** 签发数据签名 */
  protected String sign() {
    Map<String, Object> data = Maps.newTreeMap();
    BeanUtil.beanToMap(this, data, false, true);
    data.remove("signature");
    data.remove("clientDetails");
    final String text = Joiner.on("&").withKeyValueSeparator("=").join(data)
        .concat(String.format("&clientId=%s", clientDetails.getId()));
    final String sha256Hex = DigestUtils.sha256Hex(text);
    log.debug("\n【签名摘要】明文内容：{}\n【签名摘要】数字摘要：{}", text, sha256Hex);
    return sha256Hex;
  }
}
