package com.example.studentmanagement.utils;

/**
 * App-wide constants.
 *
 * TEAM INTEGRATION NOTE:
 * - BASE_URL is set to the Android emulator localhost alias (10.0.2.2:3000).
 * - When Member 4 deploys the backend, update BASE_URL to the real server URL here only.
 * - Do NOT hardcode URLs elsewhere in the codebase.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u000e\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/example/studentmanagement/utils/Constants;", "", "()V", "BASE_URL", "", "CONNECT_TIMEOUT", "", "DB_NAME", "DB_VERSION", "", "EXTRA_FROM_REGISTER", "EXTRA_STUDENT_ID", "KEY_AUTH_TOKEN", "KEY_LOGGED_IN", "KEY_STUDENT_EMAIL", "KEY_STUDENT_ID", "KEY_STUDENT_NAME", "MAX_PASSWORD_LENGTH", "MIN_PASSWORD_LENGTH", "PREF_FILE", "READ_TIMEOUT", "SPLASH_DELAY_MS", "WRITE_TIMEOUT", "app_debug"})
public final class Constants {
    
    /**
     * Development backend URL (Android emulator → localhost:3000).
     * Member 4: Replace this URL with the deployed server address when ready.
     *
     * For a physical device on the same WiFi, use your machine's LAN IP:
     *  e.g. "http://192.168.1.100:3000/"
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String BASE_URL = "http://10.0.2.2:3000/";
    public static final long CONNECT_TIMEOUT = 15L;
    public static final long READ_TIMEOUT = 30L;
    public static final long WRITE_TIMEOUT = 30L;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREF_FILE = "student_session";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_LOGGED_IN = "logged_in";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_STUDENT_ID = "student_id";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_STUDENT_NAME = "name";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_STUDENT_EMAIL = "email";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_AUTH_TOKEN = "auth_token";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_STUDENT_ID = "extra_student_id";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_FROM_REGISTER = "extra_from_register";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DB_NAME = "student_management.db";
    public static final int DB_VERSION = 1;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 64;
    public static final long SPLASH_DELAY_MS = 1500L;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.studentmanagement.utils.Constants INSTANCE = null;
    
    private Constants() {
        super();
    }
}