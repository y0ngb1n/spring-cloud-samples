package io.github.y0ngb1n.samples.gateway.filter;

import io.github.y0ngb1n.samples.gateway.core.ClientDetailsProperties;
import io.github.y0ngb1n.samples.gateway.core.ClientDetailsProperties.ClientDetails;
import io.github.y0ngb1n.samples.gateway.core.RequestHeaderConstants;
import io.github.y0ngb1n.samples.gateway.core.ServerWebExchangeConstants;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequiredClientIdGlobalFilter implements GlobalFilter, Ordered {

  public static final int FILTER_ORDER = AbstractEncryptionFilter.FILTER_ORDER - 1;

  private final ClientDetailsProperties clientDetailsProperties;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    final String requestClientId =
        exchange.getRequest().getHeaders().getFirst(RequestHeaderConstants.CLIENT_ID);
    final Optional<ClientDetailsProperties.ClientDetails> clientDetailsOptional =
        clientDetailsProperties.find(requestClientId);
    final ClientDetails clientDetails =
        clientDetailsOptional.<RuntimeException>orElseThrow(
            () -> {
              throw new RuntimeException("非法请求客户端");
            });
    log.debug("【客户端】{}", clientDetails);
    exchange
        .getAttributes()
        .put(ServerWebExchangeConstants.REQUEST_CLIENT_DETAILS_ATTR, clientDetails);
    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return FILTER_ORDER;
  }
}
