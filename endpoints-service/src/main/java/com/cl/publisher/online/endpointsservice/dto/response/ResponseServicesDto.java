package com.cl.publisher.online.endpointsservice.dto.response;

import java.util.ArrayList;

public class ResponseServicesDto {

    private Object data;
    private ArrayList<ErrorDto> errors;
    private String msgUser;

    public ResponseServicesDto(
            Object data,
            ArrayList<ErrorDto> errors,
            String msgUser) {
        this.data = data;
        this.errors = errors;
        this.msgUser = msgUser;
    }

    /**
     * @return Object return the data
     */
    public Object getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * @return ArrayList<ErrorDto> return the errors
     */
    public ArrayList<ErrorDto> getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(ArrayList<ErrorDto> errors) {
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
