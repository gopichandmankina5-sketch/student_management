package com.example.studentmanagement.api;

/**
 * Retrofit API service interface.
 *
 * BACKEND CONTRACT (Member 4 — Node.js/Express + MySQL):
 * ──────────────────────────────────────────────────────
 *
 * POST /api/auth/login
 *  Request:  { "email": "...", "password": "..." }
 *  Response: { "success": true, "message": "...", "studentId": 1, "token": "..." }
 *
 * POST /api/auth/register
 *  Request:  { "name": "...", "rollNumber": "...", "email": "...",
 *              "password": "...", "department": "...", "year": "..." }
 *  Response: { "success": true, "message": "...", "studentId": 1, "token": "..." }
 *
 * GET /api/students/{id}
 *  Headers:  Authorization: Bearer <token>
 *  Response: { "id": 1, "name": "...", "rollNumber": "...", "email": "...",
 *              "department": "...", "year": "..." }
 *
 * PUT /api/students/{id}
 *  Headers:  Authorization: Bearer <token>
 *  Request:  { "name": "...", "rollNumber": "...", "department": "...", "year": "..." }
 *  Response: Updated student object
 *
 * GET /api/dashboard/{studentId}
 *  Headers:  Authorization: Bearer <token>
 *  Response: { "studentId": 1, "attendancePercentage": 82,
 *              "enrolledCourses": 6, "upcomingAssignments": 3,
 *              "unreadNotifications": 2 }
 * ──────────────────────────────────────────────────────
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J(\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ(\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\f\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u001e\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u00032\b\b\u0001\u0010\u000f\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u0011J\u001e\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000e0\u00032\b\b\u0001\u0010\u000f\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u0014J2\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\f\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\b2\b\b\u0001\u0010\u0016\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\u0017\u00a8\u0006\u0018"}, d2 = {"Lcom/example/studentmanagement/api/ApiService;", "", "getDashboard", "Lretrofit2/Response;", "Lcom/example/studentmanagement/model/DashboardData;", "studentId", "", "authToken", "", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getStudent", "Lcom/example/studentmanagement/model/Student;", "id", "login", "Lcom/example/studentmanagement/model/LoginResponse;", "request", "Lcom/example/studentmanagement/model/LoginRequest;", "(Lcom/example/studentmanagement/model/LoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "register", "Lcom/example/studentmanagement/model/RegisterRequest;", "(Lcom/example/studentmanagement/model/RegisterRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateStudent", "student", "(JLjava/lang/String;Lcom/example/studentmanagement/model/Student;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface ApiService {
    
    @retrofit2.http.POST(value = "api/auth/login")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object login(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.studentmanagement.model.LoginRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.studentmanagement.model.LoginResponse>> $completion);
    
    @retrofit2.http.POST(value = "api/auth/register")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object register(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.studentmanagement.model.RegisterRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.studentmanagement.model.LoginResponse>> $completion);
    
    @retrofit2.http.GET(value = "api/students/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getStudent(@retrofit2.http.Path(value = "id")
    long id, @retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String authToken, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.studentmanagement.model.Student>> $completion);
    
    @retrofit2.http.PUT(value = "api/students/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateStudent(@retrofit2.http.Path(value = "id")
    long id, @retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String authToken, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.studentmanagement.model.Student student, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.studentmanagement.model.Student>> $completion);
    
    @retrofit2.http.GET(value = "api/dashboard/{studentId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDashboard(@retrofit2.http.Path(value = "studentId")
    long studentId, @retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String authToken, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.studentmanagement.model.DashboardData>> $completion);
}