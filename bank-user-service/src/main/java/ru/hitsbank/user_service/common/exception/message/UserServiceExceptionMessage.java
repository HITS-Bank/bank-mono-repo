package ru.hitsbank.user_service.common.exception.message;

public final class UserServiceExceptionMessage {

    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String FORBIDDEN_ACTION = "Forbidden action";
    public static final String INVALID_REFRESH_TOKEN = "Invalid or expired refresh token";
    public static final String INITIATOR_NOT_FOUND = "Initiator user not found";
    public static final String INITIATOR_BANNED = "Initiator user is banned";
    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_ALREADY_BANNED = "User is already banned";
    public static final String USER_NOT_BANNED = "User is not banned";

    private UserServiceExceptionMessage() {}
}
