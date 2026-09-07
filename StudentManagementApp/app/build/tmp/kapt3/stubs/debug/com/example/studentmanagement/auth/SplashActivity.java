package com.example.studentmanagement.auth;

/**
 * Entry point of the app.
 *
 * Flow:
 *  1. Show branded splash screen for SPLASH_DELAY_MS milliseconds
 *  2. Check if a session is already active (user previously logged in)
 *  3. Navigate to Dashboard (if logged in) or Login (if not)
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0007\u001a\u00020\bH\u0002J\u0012\u0010\t\u001a\u00020\b2\b\u0010\n\u001a\u0004\u0018\u00010\u000bH\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/example/studentmanagement/auth/SplashActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/example/studentmanagement/databinding/ActivitySplashBinding;", "sessionManager", "Lcom/example/studentmanagement/auth/SessionManager;", "navigateToNextScreen", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "app_debug"})
public final class SplashActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.example.studentmanagement.databinding.ActivitySplashBinding binding;
    private com.example.studentmanagement.auth.SessionManager sessionManager;
    
    public SplashActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void navigateToNextScreen() {
    }
}