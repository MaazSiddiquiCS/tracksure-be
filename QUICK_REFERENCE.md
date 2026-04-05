# TrackSure Backend - Authentication Quick Reference

## 🎯 For Frontend Developers

### Token Storage
```javascript
// After login/registration, you receive:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com"
}

// Store securely:
// - accessToken: In memory (or sessionStorage for single-tab apps)
// - refreshToken: In httpOnly cookie OR secure storage
// - userId: In localStorage for quick reference
```

### Making Authenticated Requests
```javascript
// ALWAYS include the access token in Authorization header
const response = await fetch('http://localhost:8080/api/devices', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
});

// If you get 401 Unauthorized:
// 1. Try to refresh the token using refreshToken
// 2. If refresh fails, redirect to login
```

### Token Refresh Handler
```javascript
async function refreshAccessToken(refreshToken) {
  const response = await fetch('http://localhost:8080/api/auth/refresh', {
    method: 'POST',
    headers: {
      'X-Refresh-Token': refreshToken,
      'Content-Type': 'application/json'
    }
  });

  if (response.ok) {
    const data = await response.json();
    // Store new accessToken (in memory)
    // Store new refreshToken (in httpOnly cookie or secure storage)
    return data;
  } else {
    // Redirect to login - tokens are invalid
    window.location.href = '/login';
  }
}
```

### Logout
```javascript
async function logout(refreshToken) {
  await fetch('http://localhost:8080/api/auth/logout', {
    method: 'POST',
    headers: {
      'X-Refresh-Token': refreshToken,
      'Content-Type': 'application/json'
    }
  });

  // Clear local storage
  localStorage.removeItem('userId');
  localStorage.removeItem('username');
  
  // Clear accessToken from memory
  accessToken = null;
  
  // Clear refreshToken from storage
  // ...
  
  // Redirect to login
  window.location.href = '/login';
}
```

## 🛠️ For Backend Developers

### Adding a Protected Endpoint
```java
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

  @GetMapping
  public ResponseEntity<List<DeviceResponse>> getDevices() {
    // SecurityContextHolder automatically contains the authenticated user
    UserPrincipal principal = (UserPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();
    
    Long userId = principal.getUserId();
    
    // Fetch user's devices...
    List<DeviceResponse> devices = deviceService.getUserDevices(userId);
    return ResponseEntity.ok(devices);
  }
}
```

### Getting Current User
```java
// Option 1: From SecurityContext
UserPrincipal principal = (UserPrincipal) SecurityContextHolder
    .getContext()
    .getAuthentication()
    .getPrincipal();
Long userId = principal.getUserId();

// Option 2: Using @AuthenticationPrincipal annotation (cleaner)
@GetMapping
public ResponseEntity<ProfileResponse> getProfile(
    @AuthenticationPrincipal UserPrincipal principal) {
  Long userId = principal.getUserId();
  // ...
}

// Option 3: Using custom annotation (requires implementation)
@GetMapping
public ResponseEntity<ProfileResponse> getProfile(
    @CurrentUser Long userId) {
  // ...
}
```

### Using Mappers with Authentication
```java
@PostMapping
public ResponseEntity<DeviceResponse> createDevice(
    @RequestBody @Valid DeviceRequest request,
    @AuthenticationPrincipal UserPrincipal principal) {
  
  // Set the owner from authenticated user
  request.setOwnerUserId(principal.getUserId());
  
  // Convert DTO to entity
  Device device = deviceMapper.toEntity(request);
  
  // Save and return response
  Device saved = deviceRepository.save(device);
  DeviceResponse response = deviceMapper.toResponse(saved);
  
  return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### Role-Based Authorization (Future)
```java
// When RBAC is implemented, use:

@GetMapping("/{id}/admin")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<DeviceResponse> getDeviceAdmin(
    @PathVariable Long id) {
  // Only admins can access
}

@GetMapping("/{id}")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<DeviceResponse> getDevice(
    @PathVariable Long id) {
  // Users and admins can access
}
```

## 📊 HTTP Status Codes

| Code | Scenario | Example |
|------|----------|---------|
| 200 | Success | Login successful, device retrieved |
| 201 | Resource created | User registered, device created |
| 204 | Success with no content | Logout successful |
| 400 | Bad request | Validation failed, missing field |
| 401 | Unauthorized | Invalid credentials, expired token |
| 403 | Forbidden | Not enough permissions |
| 404 | Not found | Resource doesn't exist |
| 409 | Conflict | Email already exists |
| 500 | Server error | Unexpected error |

## 🔄 Token Flow Diagram

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ 1. POST /api/auth/register
       │ {username, email, password}
       ▼
┌──────────────────────────┐
│ Backend                   │
│ 1. Validate inputs        │
│ 2. Hash password (BCrypt) │
│ 3. Save user to DB        │
│ 4. Generate JWT token     │
│ 5. Create refresh token   │
└──────┬───────────────────┘
       │ 2. Return response
       │ {accessToken, refreshToken, ...}
       ▼
┌─────────────┐
│   Client    │
│ Store tokens│
└──────┬──────┘
       │ 3. GET /api/devices
       │ Authorization: Bearer <accessToken>
       ▼
┌──────────────────────────┐
│ Backend                   │
│ 1. Validate token        │
│ 2. Check expiration      │
│ 3. Extract user info     │
│ 4. Fetch user's devices  │
└──────┬───────────────────┘
       │ 4. Return devices
       ▼
┌─────────────┐
│   Client    │ (after 24 hours)
│ Token expired
└──────┬──────┘
       │ 5. POST /api/auth/refresh
       │ X-Refresh-Token: <refreshToken>
       ▼
┌──────────────────────────┐
│ Backend                   │
│ 1. Validate refresh token │
│ 2. Check if revoked      │
│ 3. Generate new JWT      │
│ 4. Create new refresh    │
│ 5. Revoke old refresh    │
└──────┬───────────────────┘
       │ 6. Return new tokens
       ▼
┌─────────────┐
│   Client    │
│ Update tokens
└──────┬──────┘
       │ 7. Continue with new token
       ▼
```

## ⚠️ Common Mistakes

### ❌ Don't Do This
```javascript
// ❌ Storing token in localStorage (XSS vulnerable)
localStorage.setItem('accessToken', token);

// ❌ Sending token in URL
fetch(`http://localhost:8080/api/devices?token=${accessToken}`);

// ❌ Using token as basic auth
fetch('http://localhost:8080/api/devices', {
  headers: {
    'Authorization': 'Basic ' + token
  }
});

// ❌ Accessing token after logout
console.log(accessToken); // Don't use this anymore
```

### ✅ Do This Instead
```javascript
// ✅ Store accessToken in memory, refreshToken in httpOnly cookie
let accessToken = null;

// ✅ Always use Bearer scheme
fetch('http://localhost:8080/api/devices', {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});

// ✅ Regenerate token periodically
setInterval(async () => {
  const newTokens = await refreshAccessToken();
  accessToken = newTokens.accessToken;
}, 12 * 60 * 60 * 1000); // Every 12 hours

// ✅ Clear everything on logout
accessToken = null;
// refreshToken cleared from httpOnly cookie
// userId cleared from localStorage
```

## 🔐 Security Tips

### For Frontend
1. ✅ Use HTTPS only (never HTTP in production)
2. ✅ Store refreshToken in httpOnly cookie (not accessible to JavaScript)
3. ✅ Store accessToken in memory (cleared on page refresh)
4. ✅ Never log tokens to console
5. ✅ Always include CORS headers when needed
6. ✅ Implement token refresh before expiration
7. ✅ Clear all tokens on logout
8. ✅ Validate token format before sending

### For Backend
1. ✅ Use environment variables for secrets (JWT_SECRET)
2. ✅ Hash passwords with BCrypt (never plain text)
3. ✅ Validate all inputs (JSR-380 annotations)
4. ✅ Check token signature on every request
5. ✅ Verify token expiration
6. ✅ Revoke tokens on logout
7. ✅ Don't expose internal error details
8. ✅ Log security events (logins, logouts, failed attempts)

## 📱 Mobile App Integration

### Flutter Example
```dart
// Store tokens
final prefs = await SharedPreferences.getInstance();
prefs.setString('accessToken', loginResponse.accessToken);
// Note: Don't store refreshToken in SharedPreferences, use native secure storage

// Make authenticated request
final response = await http.get(
  Uri.parse('http://localhost:8080/api/devices'),
  headers: {
    'Authorization': 'Bearer ${prefs.getString('accessToken')}',
    'Content-Type': 'application/json',
  },
);

// Handle 401 response
if (response.statusCode == 401) {
  // Refresh token and retry
  await refreshAccessToken();
  // Retry request
}
```

### React Native Example
```javascript
// Store tokens
import * as SecureStore from 'expo-secure-store';

await SecureStore.setItemAsync('refreshToken', loginResponse.refreshToken);
let accessToken = loginResponse.accessToken; // In-memory storage

// Make authenticated request
const response = await fetch('http://localhost:8080/api/devices', {
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json',
  },
});

if (response.status === 401) {
  // Refresh and retry
}
```

## 🐛 Debugging Tips

### Check Token Contents
```javascript
// Decode JWT (only for debugging, don't use in production)
function decodeToken(token) {
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const jsonPayload = decodeURIComponent(
    atob(base64).split('').map((c) => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join('')
  );
  return JSON.parse(jsonPayload);
}

// Usage
const decoded = decodeToken(accessToken);
console.log('Token expires at:', new Date(decoded.exp * 1000));
console.log('Username:', decoded.sub);
```

### Common Error Messages

| Error | Cause | Solution |
|-------|-------|----------|
| `401 Unauthorized` | Missing/invalid token | Add token to Authorization header |
| `Authentication required` | Token expired | Refresh the token |
| `Invalid JWT signature` | Wrong secret | Verify JWT_SECRET env variable |
| `409 Conflict` | Email exists | Use different email |
| `400 Bad Request` | Validation failed | Check required fields |

## 📞 Support & Documentation

- **Detailed Guide**: See `JWT_AUTHENTICATION_GUIDE.md`
- **API Reference**: See `API_REFERENCE.md`
- **Main README**: See `AUTHENTICATION_README.md`

## 🔗 Useful Links

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT.io - Debug Tokens](https://jwt.io)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [Spring Boot Best Practices](https://spring.io/guides)

