package io.github.y0ngb1n.samples.gateway.filter;

import io.github.y0ngb1n.samples.gateway.core.ServerWebExchangeConstants;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 缓存请求体全局过滤器
 *
 * @author yangbin
 */
@Slf4j
@Component
public class CacheRequestBodyGlobalFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    final boolean isPostMethod = Objects.equals(HttpMethod.POST, exchange.getRequest().getMethod());
    if (!isPostMethod) {
      return chain.filter(exchange);
    }

    // DataBufferUtils.join 拿到请求中的数据 => DataBuffer
    return DataBufferUtils.join(exchange.getRequest().getBody()).flatMap(dataBuffer -> {
      // 确保数据缓冲区不被释放，必须要 DataBufferUtils.retain(dataBuffer)
      DataBufferUtils.retain(dataBuffer);
      // defer、just 都是去创建数据源，得到当前数据的副本
      Flux<DataBuffer> cachedFlux = Flux
          .defer(() -> Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));
      // 重新包装 ServerHttpRequest，重写 getBody 方法，能够返回请求数据
      ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
        @Override
        public Flux<DataBuffer> getBody() {
          return cachedFlux;
        }
      };
      exchange.getAttributes().put(ServerWebExchangeConstants.CACHED_REQUEST_BODY_ATTR,
          parseRequestBodyFromCachedRequest(mutatedRequest));
      // 将包装之后的 ServerHttpRequest 向下继续传递
      return chain.filter(exchange.mutate().request(mutatedRequest).build());
    });
  }

  /** 从 POST 请求中获取请求体数据 */
  private String parseRequestBodyFromCachedRequest(ServerHttpRequest request) {

    // 获取请求体
    final Flux<DataBuffer> requestBody = request.getBody();
    final AtomicReference<String> requestBodyRef = new AtomicReference<>();
    // 订阅缓冲区去消费请求体中的数据
    requestBody.subscribe(dataBuffer -> {
      final CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
      // 一定要使用 DataBufferUtils.release(dataBuffer) 释放掉，否则会出现内存泄露
      // DataBufferUtils.retain(dataBuffer) => DataBufferUtils.release(dataBuffer)
      DataBufferUtils.release(dataBuffer);
      requestBodyRef.set(charBuffer.toString());
    });
    // 获得请求体数据
    return requestBodyRef.get();
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE + 2;
  }
}
