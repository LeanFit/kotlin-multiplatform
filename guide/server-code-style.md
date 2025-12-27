# Garangbi Server Code Style Guide

## 참고 레퍼런스

- [Toss - Kotlin Style Guide](https://github.com/toss/kotlin-coding-conventions)
- [LINE - Kotlin Coding Conventions](https://github.com/nicholasjackson/kotlin-coding-conventions)
- [Square - Kotlin Code Style](https://github.com/square/okhttp)
- [Spring Boot Best Practices](https://github.com/spring-projects/spring-boot)

---

## 1. 패키지 구조

### 도메인 중심 패키지 구조 (Domain-Driven)

```
server/src/main/kotlin/com/gongbaek/garangbi/
├── GarangbiApplication.kt              # 애플리케이션 진입점
│
├── global/                             # 전역 설정
│   ├── config/                         # 설정 클래스
│   │   ├── AppConfig.kt
│   │   └── DatabaseConfig.kt
│   ├── common/                         # 공통 유틸리티
│   │   ├── response/
│   │   │   ├── ApiResponse.kt
│   │   │   └── ErrorResponse.kt
│   │   └── extension/
│   │       └── StringExtension.kt
│   ├── exception/                      # 전역 예외
│   │   ├── BusinessException.kt
│   │   └── ErrorCode.kt
│   └── security/                       # 보안 설정
│       └── JwtProvider.kt
│
├── domain/                             # 도메인 레이어
│   ├── user/                           # 사용자 도메인
│   │   ├── controller/
│   │   │   └── UserController.kt
│   │   ├── service/
│   │   │   └── UserService.kt
│   │   ├── repository/
│   │   │   └── UserRepository.kt
│   │   ├── entity/
│   │   │   └── User.kt
│   │   └── dto/
│   │       ├── request/
│   │       │   └── UserCreateRequest.kt
│   │       └── response/
│   │           └── UserResponse.kt
│   │
│   └── subscription/                   # 구독 도메인
│       ├── controller/
│       │   └── SubscriptionController.kt
│       ├── service/
│       │   └── SubscriptionService.kt
│       ├── repository/
│       │   └── SubscriptionRepository.kt
│       ├── entity/
│       │   └── Subscription.kt
│       └── dto/
│           ├── request/
│           │   └── SubscriptionCreateRequest.kt
│           └── response/
│               └── SubscriptionResponse.kt
│
└── infrastructure/                     # 인프라 레이어
    ├── database/
    │   └── table/
    │       ├── UsersTable.kt
    │       └── SubscriptionsTable.kt
    └── external/
        └── firebase/
            └── FirebaseClient.kt
```

---

## 2. 네이밍 컨벤션

### 클래스 네이밍

| 종류 | 규칙 | 예시 |
|------|------|------|
| Controller | `{Domain}Controller` | `UserController` |
| Service | `{Domain}Service` | `UserService` |
| Repository | `{Domain}Repository` | `UserRepository` |
| Entity | `{Domain}` | `User` |
| Table | `{Domain}sTable` | `UsersTable` |
| Request DTO | `{Action}{Domain}Request` | `CreateUserRequest` |
| Response DTO | `{Domain}Response` | `UserResponse` |
| Exception | `{Domain}Exception` | `UserNotFoundException` |

### 함수 네이밍

| 종류 | 규칙 | 예시 |
|------|------|------|
| 조회 (단건) | `get{Domain}` | `getUser()` |
| 조회 (목록) | `get{Domain}s` / `find{Domain}s` | `getUsers()`, `findByEmail()` |
| 생성 | `create{Domain}` | `createUser()` |
| 수정 | `update{Domain}` | `updateUser()` |
| 삭제 | `delete{Domain}` | `deleteUser()` |
| 검증 | `validate{Something}` | `validateEmail()` |
| 변환 | `to{Target}` | `toResponse()`, `toEntity()` |

### 변수 네이밍

```kotlin
// Bad
val u = userRepository.findById(id)
val cnt = subscriptions.size

// Good
val user = userRepository.findById(id)
val subscriptionCount = subscriptions.size
```

---

## 3. 레이어 책임

### Controller (Routes)

- HTTP 요청/응답 처리
- 입력값 검증
- Service 호출
- **비즈니스 로직 금지**

```kotlin
// Good
fun Route.createUser() {
    post("/users") {
        val request = call.receive<CreateUserRequest>()
        val user = userService.createUser(request)
        call.respond(HttpStatusCode.Created, user.toResponse())
    }
}

// Bad - 비즈니스 로직이 Controller에 있음
fun Route.createUser() {
    post("/users") {
        val request = call.receive<CreateUserRequest>()
        // 비즈니스 로직이 여기 있으면 안됨!
        val existingUser = userRepository.findByEmail(request.email)
        if (existingUser != null) throw UserAlreadyExistsException()
        // ...
    }
}
```

### Service

- 비즈니스 로직 처리
- 트랜잭션 관리
- Repository 호출 조합

```kotlin
class UserService(
    private val userRepository: UserRepository,
) {
    fun createUser(request: CreateUserRequest): User {
        validateEmailNotExists(request.email)
        return userRepository.save(request.toEntity())
    }

    private fun validateEmailNotExists(email: String) {
        if (userRepository.existsByEmail(email)) {
            throw UserAlreadyExistsException()
        }
    }
}
```

### Repository

- 데이터 접근 로직
- CRUD 연산
- **비즈니스 로직 금지**

```kotlin
class UserRepository {
    fun findById(id: UUID): User? = transaction {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    fun save(user: User): User = transaction {
        // ...
    }
}
```

---

## 4. DTO 규칙

### Request DTO

```kotlin
@Serializable
data class CreateUserRequest(
    val email: String,
    val displayName: String?,
) {
    init {
        require(email.isNotBlank()) { "email must not be blank" }
        require(email.contains("@")) { "email format is invalid" }
    }

    fun toEntity(): User = User(
        email = email,
        displayName = displayName,
    )
}
```

### Response DTO

```kotlin
@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val displayName: String?,
    val createdAt: String,
) {
    companion object {
        fun from(user: User): UserResponse = UserResponse(
            id = user.id.toString(),
            email = user.email,
            displayName = user.displayName,
            createdAt = user.createdAt.toString(),
        )
    }
}

// Extension function 방식도 가능
fun User.toResponse(): UserResponse = UserResponse.from(this)
```

---

## 5. API Response 표준

### 성공 응답

```kotlin
@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T? = null,
    val message: String? = null,
)

// 사용
call.respond(HttpStatusCode.OK, ApiResponse(data = userResponse))
```

### 에러 응답

```kotlin
@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val error: ErrorDetail,
)

@Serializable
data class ErrorDetail(
    val code: String,
    val message: String,
)

// 사용
call.respond(
    HttpStatusCode.BadRequest,
    ErrorResponse(error = ErrorDetail("USER_001", "User not found"))
)
```

---

## 6. 예외 처리

### 비즈니스 예외 정의

```kotlin
enum class ErrorCode(
    val status: HttpStatusCode,
    val code: String,
    val message: String,
) {
    // User
    USER_NOT_FOUND(HttpStatusCode.NotFound, "USER_001", "User not found"),
    USER_ALREADY_EXISTS(HttpStatusCode.Conflict, "USER_002", "User already exists"),

    // Subscription
    SUBSCRIPTION_NOT_FOUND(HttpStatusCode.NotFound, "SUB_001", "Subscription not found"),

    // Auth
    INVALID_TOKEN(HttpStatusCode.Unauthorized, "AUTH_001", "Invalid token"),
}

class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)
```

### 전역 예외 핸들러

```kotlin
fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<BusinessException> { call, cause ->
            call.respond(
                cause.errorCode.status,
                ErrorResponse(
                    error = ErrorDetail(
                        code = cause.errorCode.code,
                        message = cause.errorCode.message,
                    )
                )
            )
        }
    }
}
```

---

## 7. 코드 스타일

### Kotlin 공식 스타일 준수

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- ktlint 사용

### 들여쓰기 및 포맷팅

```kotlin
// 긴 파라미터는 줄바꿈
fun createSubscription(
    userId: UUID,
    serviceName: String,
    amount: Int,
    billingDay: Int,
): Subscription {
    // ...
}

// 체이닝은 줄바꿈
val subscriptions = subscriptionRepository
    .findByUserId(userId)
    .filter { it.isActive }
    .sortedBy { it.billingDay }
```

### Null 안전성

```kotlin
// Bad
fun getUser(id: UUID): User {
    return userRepository.findById(id)!! // NPE 위험
}

// Good
fun getUser(id: UUID): User {
    return userRepository.findById(id)
        ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
}
```

### 불변성 선호

```kotlin
// Bad
var user = userRepository.findById(id)
user = user.copy(name = "New Name")

// Good
val user = userRepository.findById(id)
val updatedUser = user.copy(name = "New Name")
```

---

## 8. 테스트

### 테스트 파일 위치

```
server/src/test/kotlin/com/gongbaek/garangbi/
├── domain/
│   ├── user/
│   │   ├── controller/
│   │   │   └── UserControllerTest.kt
│   │   └── service/
│   │       └── UserServiceTest.kt
│   └── subscription/
│       └── ...
└── integration/
    └── UserIntegrationTest.kt
```

### 테스트 네이밍

```kotlin
class UserServiceTest {
    @Test
    fun `createUser should throw exception when email already exists`() {
        // given
        // when
        // then
    }

    @Test
    fun `getUser should return user when user exists`() {
        // ...
    }
}
```

---

## 9. 주석 규칙

### 필요한 주석

```kotlin
/**
 * 사용자의 구독 서비스를 관리합니다.
 *
 * @property subscriptionRepository 구독 저장소
 */
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
)

/**
 * 사용자의 월간 총 구독료를 계산합니다.
 *
 * @param userId 사용자 ID
 * @return 월간 총 구독료 (원)
 */
fun calculateMonthlyTotal(userId: UUID): Int
```

### 불필요한 주석

```kotlin
// Bad - 코드로 충분히 이해 가능
// 사용자를 ID로 조회합니다
fun findById(id: UUID): User?

// Good - 주석 없이 명확한 코드
fun findById(id: UUID): User?
```

---

## 10. Import 규칙

```kotlin
// 1. Kotlin stdlib
import kotlin.collections.*

// 2. Java stdlib
import java.util.*

// 3. Third-party libraries
import io.ktor.server.*
import org.jetbrains.exposed.*

// 4. Project imports
import com.gongbaek.garangbi.domain.*
import com.gongbaek.garangbi.global.*
```

---

*문서 버전: 1.0*
*최종 수정일: 2025년 12월 27일*
