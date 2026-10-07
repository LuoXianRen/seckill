package com.example.seckill.exception;

import com.example.seckill.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常。
 *
 * @author example
 */
@Getter
public class GlobalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ResultCode resultCode;

    public GlobalException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public GlobalException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
