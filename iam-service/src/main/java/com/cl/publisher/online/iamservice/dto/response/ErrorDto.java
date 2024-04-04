package com.cl.publisher.online.iamservice.dto.response;

import com.cl.publisher.online.iamservice.enums.LevelError;

public class ErrorDto {
    private String code;
    private String message;
    private LevelError level;
    private String description;

    public ErrorDto(String code, String message, LevelError level, String description) {
        this.code = code;
        this.message = message;
        this.level = level;
        this.description = description;
    }

    /**
     * @return String return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return String return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return LevelError return the level
     */
    public LevelError getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(LevelError level) {
        this.level = level;
    }

    /**
     * @return String return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
