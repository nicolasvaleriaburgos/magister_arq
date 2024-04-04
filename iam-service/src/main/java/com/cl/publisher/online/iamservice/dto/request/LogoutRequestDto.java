package com.cl.publisher.online.iamservice.dto.request;

public class LogoutRequestDto {
    private Long userId;

    public Long getUserId() {
        return this.userId;
    }

    @Override
    public String toString() {
        return "LogoutRequestDto: { userId:" + this.userId + " }";
    }
}
