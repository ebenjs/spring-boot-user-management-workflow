package com.ebenjs.models.responses;

import com.ebenjs.enums.ApiResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class BaseApiResponse<T> {
    private ApiResponseStatus status;
    private String message;
    private int httpCode;
    private T data;
}