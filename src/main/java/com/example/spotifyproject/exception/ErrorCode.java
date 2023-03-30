package com.example.spotifyproject.exception;

public enum ErrorCode {
    unknown(400),
    unauthorized(401),
    forbidden(403),
    resource_missing(404),
    account_already_exists(409),
    account_missing(404),
    password_mismatch(409),
    account_already_verified(403),
    account_not_verified(403),
    code_expired(410),
    code_mismatch(409),
    internal_server_error(500),
    song_is_already_liked(403),
    category_is_already_likes(403),
    paid_more(403);

    private final int httpCode;

    ErrorCode(int httpCode) {
        this.httpCode = httpCode;
    }
    public int getHttpCode() {
        return httpCode;
    }
}
