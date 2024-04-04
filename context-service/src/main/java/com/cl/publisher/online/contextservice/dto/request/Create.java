package com.cl.publisher.online.contextservice.dto.request;

import java.io.Serializable;
import java.util.Map;

public class Create implements Serializable {
    private static final long serialVersionUID = 1L;
    private String token;
    private Map<String, Object> value;

    /**
     * @return String return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return Map<String, Object> return the value
     */
    public Map<String, Object> getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Map<String, Object> value) {
        this.value = value;
    }

}
