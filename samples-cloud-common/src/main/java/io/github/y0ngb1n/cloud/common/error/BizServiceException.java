package io.github.y0ngb1n.cloud.common.error;

import io.github.y0ngb1n.cloud.common.api.ErrorCode;
import lombok.Getter;

/**
 * 定义业务异常.
 *
 * @author yangbin
 */
public class BizServiceException extends RuntimeException {

  @Getter
  private final ErrorCode errorCode;

  public BizServiceException(String message) {
    super(message);
    this.errorCode = ErrorCode.FAILURE;
  }

  public BizServiceException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public BizServiceException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public BizServiceException(ErrorCode errorCode, Throwable cause) {
    super(cause);
    this.errorCode = errorCode;
  }

  public BizServiceException(String message, Throwable cause) {
    super(message, cause);
    this.errorCode = ErrorCode.FAILURE;
  }

  /**
   * for better performance
   *
   * @return Throwable
   */
  @Override
  public Throwable fillInStackTrace() {
    return this;
  }

  public Throwable doFillInStackTrace() {
    return super.fillInStackTrace();
  }
}
