package com.example.backend.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.enums.ResponseStatus;
import lombok.Data;


import java.io.Serializable;

@Data
public class BaseResponse implements Serializable {
    private String code;
    private String message;
    private Object data;

    public static BaseResponse success() {
        BaseResponse response = new BaseResponse();
        response.setCode(ResponseStatus.SUCCESS.getCode());
        response.setMessage(ResponseStatus.SUCCESS.getMessage());
        return response;
    }

    public static BaseResponse success(Object data) {
        BaseResponse response = success();
        if (data instanceof IPage)
            response.setData(RetPage.copyFromIPage((IPage) data));
        else
            response.setData(data);
        return response;
    }

    public static BaseResponse error(String code, String message) {
        BaseResponse response = new BaseResponse();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    public static BaseResponse error(String code, String message, Object data) {
        BaseResponse response = error(code, message);
        response.setData(data);
        return response;
    }

    public static BaseResponse error(ResponseStatus r) {
        BaseResponse response = new BaseResponse();
        response.setCode(r.getCode());
        response.setMessage(r.getMessage());
        return response;
    }

    public static BaseResponse error(ResponseStatus r, Object data) {
        BaseResponse response = error(r);
        response.setData(data);
        return response;
    }

}
