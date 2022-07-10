package io.github.y0ngb1n.samples.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 网关配置
 *
 * @author yangbin
 */
@Configuration
@Import({EncryptionConfig.class})
public class GatewayConfig {}
