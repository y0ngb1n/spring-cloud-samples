package io.github.y0ngb1n.samples.gateway.filter;

import io.github.y0ngb1n.samples.gateway.core.RequestHeaderConstants;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** 加密过滤器 */
@Slf4j
public abstract class AbstractEncryptionFilter implements GlobalFilter, Ordered {

  public static final int FILTER_ORDER = NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;

  /** 开启加密调试模式标识 */
  public static final String ENCRYPT_DEBUG_ENABLED_KEY = RequestHeaderConstants.API_DEBUG;

  /** 开启加密调试模式标识值 */
  public static final String ENCRYPT_DEBUG_ENABLED_VALUE = "DEBUG";

  /** 默认关闭加密调试模式标识值 */
  public static final String ENCRYPT_DEBUG_ENABLED_DEFAULT_VALUE = "ENCRYPT";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if (isEncryptDebugEnabled(exchange)) {
      return chain.filter(exchange);
    }
    return DataBufferUtils.join(exchange.getRequest().getBody()).flatMap(dataBuffer -> {
      final DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
      final ServerHttpRequestDecorator requestDecorator = processRequest(exchange, dataBuffer, bufferFactory);
      final ServerHttpResponseDecorator responseDecorator = processResponse(exchange, bufferFactory);
      return chain.filter(exchange.mutate().request(requestDecorator).response(responseDecorator).build());
    });
  }

  /** 处理请求 */
  protected ServerHttpRequestDecorator processRequest(ServerWebExchange exchange, DataBuffer dataBuffer,
      DataBufferFactory bufferFactory) {
    return new ServerHttpRequestDecorator(exchange.getRequest());
  }

  /** 处理响应 */
  protected ServerHttpResponseDecorator processResponse(ServerWebExchange exchange, DataBufferFactory bufferFactory) {
    return new ServerHttpResponseDecorator(exchange.getResponse());
  }

  /** 是否开启加密调试模式 */
  protected boolean isEncryptDebugEnabled(ServerWebExchange exchange) {
    boolean isEncryptDebugEnabled = false;
    if (log.isDebugEnabled()) {
      // 设置是否加密标识
      List<String> encryptDebugHeaders = exchange.getRequest().getHeaders().get(ENCRYPT_DEBUG_ENABLED_KEY);
      String encryptDebugValue = encryptDebugHeaders != null
          ? encryptDebugHeaders.get(0)
          : ENCRYPT_DEBUG_ENABLED_DEFAULT_VALUE;
      exchange.getAttributes().put(ENCRYPT_DEBUG_ENABLED_KEY, encryptDebugValue);
      isEncryptDebugEnabled = ENCRYPT_DEBUG_ENABLED_VALUE.equals(encryptDebugValue);
    }
    return isEncryptDebugEnabled;
  }

  @Override
  public int getOrder() {
    return FILTER_ORDER;
  }
}
