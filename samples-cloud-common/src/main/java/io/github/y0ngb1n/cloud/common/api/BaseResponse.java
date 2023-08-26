package io.github.y0ngb1n.cloud.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 定义服务接口通用响应.
 *
 * @author yangbin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseResponse {

  /** 错误码. */
  @Builder.Default
  private ErrorCode code = ErrorCode.SUCCESS;

  /** 错误信息. */
  private String message;

  public boolean isSuccess() {
    return code == ErrorCode.SUCCESS;
  }
}
