package io.github.y0ngb1n.samples.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Maps;
import io.github.y0ngb1n.samples.gateway.core.RsaUtils;
import java.util.HashMap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Gateway RSA Samples
 *
 * @author yangbin
 */
@SpringBootApplication
public class SamplesGatewayRsaApplication {

  public static void main(String[] args) throws JsonProcessingException {
    final HashMap<String, Object> rsaKeyMap = Maps.newHashMap();
    rsaKeyMap.put("key1", RsaUtils.generateRasKey());
    rsaKeyMap.put("key2", RsaUtils.generateRasKey());

    final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    final String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rsaKeyMap);
    System.out.println(json);
    SpringApplication.run(SamplesGatewayRsaApplication.class, args);
  }
}
