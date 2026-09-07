package com.example.studentmanagement.api;

/**
 * Retrofit singleton.
 *
 * CONFIGURATION:
 * ──────────────────────────────────────────────────────
 * To point to a different backend, change Constants.BASE_URL.
 * Do NOT change the URL here — keep this file environment-agnostic.
 *
 * Logging is enabled at BODY level. For a production release,
 * wrap the interceptor addition in a BuildConfig.DEBUG check.
 * ──────────────────────────────────────────────────────
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001b\u0010\u0003\u001a\u00020\u00048FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006R\u001b\u0010\t\u001a\u00020\n8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\r\u0010\b\u001a\u0004\b\u000b\u0010\fR\u001b\u0010\u000e\u001a\u00020\u000f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0012\u0010\b\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006\u0013"}, d2 = {"Lcom/example/studentmanagement/api/RetrofitClient;", "", "()V", "api", "Lcom/example/studentmanagement/api/ApiService;", "getApi", "()Lcom/example/studentmanagement/api/ApiService;", "api$delegate", "Lkotlin/Lazy;", "okHttpClient", "Lokhttp3/OkHttpClient;", "getOkHttpClient", "()Lokhttp3/OkHttpClient;", "okHttpClient$delegate", "retrofit", "Lretrofit2/Retrofit;", "getRetrofit", "()Lretrofit2/Retrofit;", "retrofit$delegate", "app_debug"})
public final class RetrofitClient {
    
    /**
     * OkHttpClient with logging + timeout configuration.
     */
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy okHttpClient$delegate = null;
    
    /**
     * Retrofit instance pointed at Constants.BASE_URL.
     */
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy retrofit$delegate = null;
    
    /**
     * The API service implementation created by Retrofit.
     */
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy api$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.studentmanagement.api.RetrofitClient INSTANCE = null;
    
    private RetrofitClient() {
        super();
    }
    
    /**
     * OkHttpClient with logging + timeout configuration.
     */
    private final okhttp3.OkHttpClient getOkHttpClient() {
        return null;
    }
    
    /**
     * Retrofit instance pointed at Constants.BASE_URL.
     */
    private final retrofit2.Retrofit getRetrofit() {
        return null;
    }
    
    /**
     * The API service implementation created by Retrofit.
     */
    @org.jetbrains.annotations.NotNull()
    public final com.example.studentmanagement.api.ApiService getApi() {
        return null;
    }
}