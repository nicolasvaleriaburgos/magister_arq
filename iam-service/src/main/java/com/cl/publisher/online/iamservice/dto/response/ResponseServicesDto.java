package com.cl.publisher.online.iamservice.dto.response;

import java.util.List;

public class ResponseServicesDto<T> {

    private T data;
    private List<ErrorDto> errors;
    private String msgUser;

    public ResponseServicesDto(
            T data,
            List<ErrorDto> errors,
            String msgUser) {
        this.data = data;
        this.errors = errors;
        this.msgUser = msgUser;
    }

    /**
     * @return Object return the data
     */
    public T getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * @return List<ErrorDto> return the errors
     */
    public List<ErrorDto> getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(List<ErrorDto> errors) {
        this.errors = errors;
    }

    /**
     * @return String return the msgUser
     */
    public String getMsgUser() {
        return msgUser;
    }

    /**
     * @param msgUser the msgUser to set
     */
    public void setMsgUser(String msgUser) {
        this.msgUser = msgUser;
    }

}