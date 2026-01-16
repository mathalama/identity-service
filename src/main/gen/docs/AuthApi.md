# AuthApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**authLoginPost**](AuthApi.md#authLoginPost) | **POST** /auth/login | Authenticate a user |
| [**authResetPost**](AuthApi.md#authResetPost) | **POST** /auth/reset | Reset a user&#39;s password |
| [**authSignupPost**](AuthApi.md#authSignupPost) | **POST** /auth/signup | Register a new user |


<a id="authLoginPost"></a>
# **authLoginPost**
> authLoginPost(signInRequest)

Authenticate a user

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AuthApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    AuthApi apiInstance = new AuthApi(defaultClient);
    SignInRequest signInRequest = new SignInRequest(); // SignInRequest | 
    try {
      apiInstance.authLoginPost(signInRequest);
    } catch (ApiException e) {
      System.err.println("Exception when calling AuthApi#authLoginPost");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **signInRequest** | [**SignInRequest**](SignInRequest.md)|  | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Login accepted. |  -  |
| **401** | Invalid credentials. |  -  |

<a id="authResetPost"></a>
# **authResetPost**
> authResetPost(resetPasswordRequest)

Reset a user&#39;s password

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AuthApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    AuthApi apiInstance = new AuthApi(defaultClient);
    ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest(); // ResetPasswordRequest | 
    try {
      apiInstance.authResetPost(resetPasswordRequest);
    } catch (ApiException e) {
      System.err.println("Exception when calling AuthApi#authResetPost");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **resetPasswordRequest** | [**ResetPasswordRequest**](ResetPasswordRequest.md)|  | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Password reset initiated. |  -  |
| **400** | Validation error. |  -  |

<a id="authSignupPost"></a>
# **authSignupPost**
> authSignupPost(signUpRegister)

Register a new user

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AuthApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    AuthApi apiInstance = new AuthApi(defaultClient);
    SignUpRegister signUpRegister = new SignUpRegister(); // SignUpRegister | 
    try {
      apiInstance.authSignupPost(signUpRegister);
    } catch (ApiException e) {
      System.err.println("Exception when calling AuthApi#authSignupPost");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **signUpRegister** | [**SignUpRegister**](SignUpRegister.md)|  | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | User created. |  -  |
| **400** | Validation error. |  -  |

