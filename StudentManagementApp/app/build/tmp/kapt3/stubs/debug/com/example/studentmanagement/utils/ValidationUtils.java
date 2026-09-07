package com.example.studentmanagement.utils;

/**
 * Reusable, unit-testable validation utilities.
 *
 * All functions return a [ValidationResult] so callers can display
 * the specific error message directly on the relevant input field.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010$\n\u0002\b\f\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u001aB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u0006J\u000e\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\u0006J\u000e\u0010\r\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006J\"\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\u000f2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u0006J\u000e\u0010\u0010\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u0006J\u000e\u0010\u0012\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0006J:\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\u000f2\u0006\u0010\u0011\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\u0006JJ\u0010\u0016\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\u000f2\u0006\u0010\u0011\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u00062\u0006\u0010\u0017\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\u0006J\u000e\u0010\u0018\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\u0006J\u000e\u0010\u0019\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\u0006\u00a8\u0006\u001b"}, d2 = {"Lcom/example/studentmanagement/utils/ValidationUtils;", "", "()V", "isValidEmail", "", "email", "", "validateConfirmPassword", "Lcom/example/studentmanagement/utils/ValidationUtils$ValidationResult;", "password", "confirm", "validateDepartment", "department", "validateEmail", "validateLoginFields", "", "validateName", "name", "validatePassword", "validateProfileFields", "rollNumber", "year", "validateRegisterFields", "confirmPassword", "validateRollNumber", "validateYear", "ValidationResult", "app_debug"})
public final class ValidationUtils {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.studentmanagement.utils.ValidationUtils INSTANCE = null;
    
    private ValidationUtils() {
        super();
    }
    
    /**
     * Returns true if the email matches Android's built-in EMAIL_ADDRESS pattern.
     */
    public final boolean isValidEmail(@org.jetbrains.annotations.NotNull()
    java.lang.String email) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.studentmanagement.utils.ValidationUtils.ValidationResult validateEmail(@org.jetbrains.annotations.NotNull()
    java.lang.String email) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.studentmanagement.utils.ValidationUtils.ValidationResult validatePassword(@org.jetbrains.annotations.NotNull()
    java.lang.String password) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.studentmanagement.utils.ValidationUtils.ValidationResult validateConfirmPassword(@org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    java.lang.String confirm) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.studentmanagement.utils.ValidationUtils.ValidationResult validateName(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.studentmanagement.utils.ValidationUtils.ValidationResult validateRollNumber(@org.jetbrains.annotations.NotNull()
    java.lang.String rollNumber) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.studentmanagement.utils.ValidationUtils.ValidationResult validateDepartment(@org.jetbrains.annotations.NotNull()
    java.lang.String department) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.studentmanagement.utils.ValidationUtils.ValidationResult validateYear(@org.jetbrains.annotations.NotNull()
    java.lang.String year) {
        return null;
    }
    
    /**
     * Validates all login fields.
     * Returns a map of field name → error string (empty map = all valid).
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.String> validateLoginFields(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String password) {
        return null;
    }
    
    /**
     * Validates all registration fields.
     * Returns a map of field name → error string (empty map = all valid).
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.String> validateRegisterFields(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String rollNumber, @org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    java.lang.String confirmPassword, @org.jetbrains.annotations.NotNull()
    java.lang.String department, @org.jetbrains.annotations.NotNull()
    java.lang.String year) {
        return null;
    }
    
    /**
     * Validates all profile edit fields.
     * Returns a map of field name → error string (empty map = all valid).
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.String> validateProfileFields(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String rollNumber, @org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String department, @org.jetbrains.annotations.NotNull()
    java.lang.String year) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0002\u0003\u0004B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0002\u0005\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/example/studentmanagement/utils/ValidationUtils$ValidationResult;", "", "()V", "Invalid", "Valid", "Lcom/example/studentmanagement/utils/ValidationUtils$ValidationResult$Invalid;", "Lcom/example/studentmanagement/utils/ValidationUtils$ValidationResult$Valid;", "app_debug"})
    public static abstract class ValidationResult {
        
        private ValidationResult() {
            super();
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/example/studentmanagement/utils/ValidationUtils$ValidationResult$Invalid;", "Lcom/example/studentmanagement/utils/ValidationUtils$ValidationResult;", "errorMessage", "", "(Ljava/lang/String;)V", "getErrorMessage", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
        public static final class Invalid extends com.example.studentmanagement.utils.ValidationUtils.ValidationResult {
            @org.jetbrains.annotations.NotNull()
            private final java.lang.String errorMessage = null;
            
            public Invalid(@org.jetbrains.annotations.NotNull()
            java.lang.String errorMessage) {
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String getErrorMessage() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String component1() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.example.studentmanagement.utils.ValidationUtils.ValidationResult.Invalid copy(@org.jetbrains.annotations.NotNull()
            java.lang.String errorMessage) {
                return null;
            }
            
            @java.lang.Override()
            public boolean equals(@org.jetbrains.annotations.Nullable()
            java.lang.Object other) {
                return false;
            }
            
            @java.lang.Override()
            public int hashCode() {
                return 0;
            }
            
            @java.lang.Override()
            @org.jetbrains.annotations.NotNull()
            public java.lang.String toString() {
                return null;
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/studentmanagement/utils/ValidationUtils$ValidationResult$Valid;", "Lcom/example/studentmanagement/utils/ValidationUtils$ValidationResult;", "()V", "app_debug"})
        public static final class Valid extends com.example.studentmanagement.utils.ValidationUtils.ValidationResult {
            @org.jetbrains.annotations.NotNull()
            public static final com.example.studentmanagement.utils.ValidationUtils.ValidationResult.Valid INSTANCE = null;
            
            private Valid() {
            }
        }
    }
}