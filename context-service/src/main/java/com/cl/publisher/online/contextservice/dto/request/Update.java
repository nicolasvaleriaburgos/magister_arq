package com.cl.publisher.online.contextservice.dto.request;

import java.io.Serializable;
import java.util.Map;

public class Update implements Serializable {
    private static final long serialVersionUID = 1L;

    private String token;
    private Map<String, Object> value;

    /**
     * It returns the token
     * 
     * @return The token
     */
    public String getToken() {
        return token;
    }

    /**
     * > This function returns the serialVersionUID of the class
     * 
     * @return The serialVersionUID is being returned.
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * > This function returns a map of string to object
     * 
     * @return A map of strings to objects.
     */
    public Map<String, Object> getValue() {
        return value;
    }

    /**
     * This function sets the token to the token passed in as a parameter
     * 
     * @param token The token that you received from the server.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * > This function sets the value of the `value` variable to the value of the
     * `value` parameter
     * 
     * @param value The value of the key.
     */
    public void setValue(Map<String, Object> value) {
        this.value = value;
    }
}
