package io.github.y0ngb1n.samples.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.y0ngb1n.samples.gateway.core.ClientDetailsProperties;
import io.github.y0ngb1n.samples.gateway.core.ServerWebExchangeConstants;
import io.github.y0ngb1n.samples.gateway.domain.EncryptedRequest;
import io.github.y0ngb1n.samples.gateway.domain.EncryptedResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Strings;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 使用 RSA 对请求数据解密、响应数据加密
 *
 * @author yangbin
 */
@Slf4j
@Validated
public class RsaEncryptionGlobalFilter extends AbstractEncryptionFilter {

  private final ApplicationContext applicationContext;
  private final Map<String, MessageBodyEncoder> messageBodyEncoders;
  private final Map<String, MessageBodyDecoder> messageBodyDecoders;

  private final Gson gson = new GsonBuilder().create();

  /** 客户端最近请求池 */
  private final Cache<String, String> clientRecentRequestIdCache = Caffeine.newBuilder()
      .expireAfterWrite(30, TimeUnit.SECONDS).maximumSize(50000).build();

  public RsaEncryptionGlobalFilter(ApplicationContext applicationContext, Set<MessageBodyEncoder> messageBodyEncoders,
      Set<MessageBodyDecoder> messageBodyDecoders) {
    this.applicationContext = applicationContext;
    this.messageBodyDecoders = messageBodyDecoders.stream()
        .collect(Collectors.toMap(MessageBodyDecoder::encodingType, Function.identity()));
    this.messageBodyEncoders = messageBodyEncoders.stream()
        .collect(Collectors.toMap(MessageBodyEncoder::encodingType, Function.identity()));
  }

  @Override
  protected ServerHttpRequestDecorator processRequest(ServerWebExchange exchange, DataBuffer dataBuffer,
      DataBufferFactory bufferFactory) {
    final ServerHttpRequest request = exchange.getRequest();
    ServerHttpRequestDecorator requestDecorator = new ServerHttpRequestDecorator(request);
    // 仅处理 POST 请求
    final HttpMethod requestMethod = request.getMethod();
    if (HttpMethod.POST.equals(requestMethod)) {
      // 获取请求数据
      DataBufferUtils.retain(dataBuffer);
      Flux<DataBuffer> cachedFlux = Flux
          .defer(() -> Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));
      String requestBody = extractCachedRequestBody(cachedFlux);
      log.debug("\n【请求地址】{} {}\n【请求内容】{}", requestMethod, exchange.getRequest().getPath(), requestBody);

      final EncryptedRequest encryptedRequest = gson.fromJson(requestBody, EncryptedRequest.class);
      // 校验请求数据是否合法
      final RsaEncryptionGlobalFilter self = applicationContext.getBean(this.getClass());
      self.validateRequest(exchange, encryptedRequest);
      exchange.getAttributes().put(ServerWebExchangeConstants.REQUEST_ID_ATTR, encryptedRequest.getRequestId());
      // 解密请求中的业务数据
      final String decryptedData = encryptedRequest.decrypt();
      // 将请求的业务数据传给下游
      final byte[] decryptedContentBytes = decryptedData.getBytes();
      requestDecorator = new ServerHttpRequestDecorator(request) {
        @Override
        public HttpHeaders getHeaders() {
          // 根据解密后的数据重新构建请求
          HttpHeaders headers = new HttpHeaders();
          headers.putAll(request.getHeaders());
          // 由于修改了传递数据，需要重新设置 CONTENT_LENGTH，需使用字节长度而不是字符串长度
          int length = decryptedContentBytes.length;
          if (length > 0) {
            headers.setContentLength(length);
          } else {
            headers.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
          }
          headers.setContentType(MediaType.APPLICATION_JSON);
          return headers;
        }

        @Override
        public Flux<DataBuffer> getBody() {
          return Flux.just(bufferFactory.wrap(decryptedContentBytes));
        }
      };
    }
    return requestDecorator;
  }

  private static String extractCachedRequestBody(Flux<DataBuffer> requestBodyDataBuffer) {
    AtomicReference<String> requestContentRef = new AtomicReference<>();
    requestBodyDataBuffer.subscribe(buffer -> {
      byte[] bytes = new byte[buffer.readableByteCount()];
      buffer.read(bytes);
      DataBufferUtils.release(buffer);
      requestContentRef.set(Strings.fromUTF8ByteArray(bytes));
    });
    return requestContentRef.get();
  }

  /**
   * 校验请求数据是否合法
   *
   * @param encryptedRequest
   *            加密的请求
   */
  public void validateRequest(ServerWebExchange exchange, @Valid EncryptedRequest encryptedRequest) {
    final RuntimeException defaultException = new RuntimeException("无效请求，请检查参数");
    // 校验请求有效期
    final long requestTimeMillis = Long.parseLong(encryptedRequest.getTimestamp()) * 1000;
    final long currentTimeMillis = System.currentTimeMillis();
    final boolean isRequestExpired = Math.abs(requestTimeMillis - currentTimeMillis) > TimeUnit.MINUTES.toMillis(3);
    if (isRequestExpired) {
      log.error("无效请求，请检查参数：时间过期, {}", encryptedRequest);
      throw defaultException;
    }

    final ClientDetailsProperties.ClientDetails clientDetails = getRequestClientDetails(exchange);
    encryptedRequest.setClientDetails(clientDetails);

    // 请求去重
    final String requestId = encryptedRequest.getRequestId();
    final String cacheKey = String.format("%s_%s", clientDetails.getId(), requestId);
    final boolean isRequestIdRepeated = Objects.nonNull(clientRecentRequestIdCache.getIfPresent(cacheKey));
    if (isRequestIdRepeated) {
      log.error("无效请求，请检查参数：请求重复, {}", encryptedRequest);
      throw defaultException;
    }
    clientRecentRequestIdCache.put(cacheKey, requestId);

    // 签名验证
    final boolean isSignatureMatched = encryptedRequest.verifySignature();
    if (!isSignatureMatched) {
      log.error("无效请求，请检查参数：签名错误, {}", encryptedRequest);
      throw defaultException;
    }
  }

  @Override
  protected ServerHttpResponseDecorator processResponse(ServerWebExchange exchange, DataBufferFactory bufferFactory) {
    final ServerHttpResponse response = exchange.getResponse();
    final ClientDetailsProperties.ClientDetails clientDetails = getRequestClientDetails(exchange);
    return new ServerHttpResponseDecorator(response) {
      @Override
      public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        final ClientResponse clientResponse = prepareClientResponse(body, response.getHeaders());
        final Mono<String> modifiedBody = extractBody(exchange, clientResponse).flatMap(originalContent -> {
          EncryptedResponse encryptedResponse = new EncryptedResponse(clientDetails);
          encryptedResponse.setRequestId(exchange.getAttribute(ServerWebExchangeConstants.REQUEST_ID_ATTR));
          encryptedResponse.encrypt(originalContent);
          encryptedResponse.generateSignature();

          String encryptedContent = gson.toJson(encryptedResponse);
          log.debug("\n【响应内容】{}\n【网关加密】{}", originalContent, encryptedContent);
          return Mono.just(Objects.requireNonNull(encryptedContent));
        }).switchIfEmpty(Mono.empty());
        final BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters
            .fromPublisher(modifiedBody, String.class);
        final CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange,
            response.getHeaders());
        return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
          Mono<DataBuffer> messageBody = updateBody(getDelegate(), outputMessage);
          HttpHeaders headers = getDelegate().getHeaders();
          headers.setContentType(MediaType.APPLICATION_JSON);
          if (headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
            messageBody = messageBody.doOnNext(data -> headers.setContentLength(data.readableByteCount()));
          }
          return getDelegate().writeWith(messageBody);
        }));
      }

      private Mono<DataBuffer> updateBody(ServerHttpResponse httpResponse, CachedBodyOutputMessage message) {
        Mono<DataBuffer> response = DataBufferUtils.join(message.getBody());
        List<String> encodingHeaders = httpResponse.getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
        for (String encoding : encodingHeaders) {
          MessageBodyEncoder encoder = messageBodyEncoders.get(encoding);
          if (encoder != null) {
            DataBufferFactory dataBufferFactory = httpResponse.bufferFactory();
            response = response.publishOn(Schedulers.parallel()).map(encoder::encode)
                .map(dataBufferFactory::wrap);
            break;
          }
        }
        return response;
      }

      private Mono<String> extractBody(ServerWebExchange _exchange, ClientResponse clientResponse) {
        List<String> encodingHeaders = _exchange.getResponse().getHeaders()
            .getOrEmpty(HttpHeaders.CONTENT_ENCODING);
        for (String encoding : encodingHeaders) {
          MessageBodyDecoder decoder = messageBodyDecoders.get(encoding);
          if (decoder != null) {
            return clientResponse.bodyToMono(byte[].class).publishOn(Schedulers.parallel())
                .map(decoder::decode).map(bytes -> _exchange.getResponse().bufferFactory().wrap(bytes))
                .map(buffer -> prepareClientResponse(Mono.just(buffer),
                    _exchange.getResponse().getHeaders()))
                .flatMap(response -> response.bodyToMono(String.class));
          }
        }
        return clientResponse.bodyToMono(String.class);
      }

      private ClientResponse prepareClientResponse(Publisher<? extends DataBuffer> body,
          HttpHeaders httpHeaders) {
        return ClientResponse
            .create(Objects.requireNonNull(response.getStatusCode()),
                HandlerStrategies.withDefaults().messageReaders())
            .headers(headers -> headers.putAll(httpHeaders)).body(Flux.from(body)).build();
      }
    };
  }

  /** 获取当前请求的客户端配置信息 */
  private ClientDetailsProperties.ClientDetails getRequestClientDetails(ServerWebExchange exchange) {
    final ClientDetailsProperties.ClientDetails clientDetails = (ClientDetailsProperties.ClientDetails) exchange
        .getAttributes().get(ServerWebExchangeConstants.REQUEST_CLIENT_DETAILS_ATTR);
    Assert.isTrue(Objects.nonNull(clientDetails), "未初始化客户端配置");
    return clientDetails;
  }
}
