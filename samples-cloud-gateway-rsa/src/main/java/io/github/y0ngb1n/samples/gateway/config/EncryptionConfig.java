package io.github.y0ngb1n.samples.gateway.config;

import io.github.y0ngb1n.samples.gateway.core.ClientDetailsProperties;
import io.github.y0ngb1n.samples.gateway.filter.AbstractEncryptionFilter;
import io.github.y0ngb1n.samples.gateway.filter.RsaEncryptionGlobalFilter;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * 网关加解密配置
 *
 * @author yangbin
 */
@EnableConfigurationProperties({ClientDetailsProperties.class})
@RequiredArgsConstructor
public class EncryptionConfig {

  private final ApplicationContext applicationContext;

  @Bean
  public AbstractEncryptionFilter rsaEncryptGlobalFilter(
      Set<MessageBodyEncoder> messageBodyEncoders, Set<MessageBodyDecoder> messageBodyDecoders) {
    return new RsaEncryptionGlobalFilter(
        applicationContext, messageBodyEncoders, messageBodyDecoders);
  }
}
