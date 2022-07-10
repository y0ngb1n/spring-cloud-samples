package io.github.y0ngb1n.samples.gateway.core;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 客户端配置信息 */
@Data
@ConfigurationProperties(prefix = "app.authorize")
public class ClientDetailsProperties {

  private List<ClientDetails> clients;

  /**
   * 查找与客户端ID匹配的客户端配置信息
   *
   * @param clientId 客户端ID
   * @return 客户端配置信息
   */
  public Optional<ClientDetails> find(String clientId) {
    return this.getClients().stream()
        .filter(client -> Objects.equals(clientId, client.getId()))
        .findFirst();
  }

  /** 客户端配置信息 */
  @Data
  public static class ClientDetails {

    /** 业务标识 */
    private String id;

    /** 业务名称 */
    private String name;

    /** 公钥/帐号 */
    private String key;

    /** 私钥/密码 */
    private String secret;
  }
}
