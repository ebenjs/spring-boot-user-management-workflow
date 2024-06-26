package com.ebenjs.models.responses;

import com.ebenjs.enums.ApiResponseStatus;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class BaseApiResponse<T> {
    private ApiResponseStatus status;
    private String message;
    private int httpCode;
    private T data;
}
