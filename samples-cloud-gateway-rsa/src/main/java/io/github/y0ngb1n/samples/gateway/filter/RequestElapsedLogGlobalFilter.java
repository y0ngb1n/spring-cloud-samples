package io.github.y0ngb1n.samples.gateway.filter;

import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求耗时日志全局过滤器
 *
 * @author yangbin
 */
@Slf4j
@Component
public class RequestElapsedLogGlobalFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

    final Stopwatch stopwatch = Stopwatch.createStarted();
    final String requestURI = exchange.getRequest().getURI().getPath();

    return chain.filter(exchange).then(Mono.fromRunnable(
        () -> log.info("[{}] elapsed [{}ms]", requestURI, stopwatch.elapsed(TimeUnit.MILLISECONDS))));
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE + 1;
  }
}
