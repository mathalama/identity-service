# UsersApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**userAddUserPost**](UsersApi.md#userAddUserPost) | **POST** /user/addUser | Create a user (admin) |
| [**userDeleteAllUsersPost**](UsersApi.md#userDeleteAllUsersPost) | **POST** /user/deleteAllUsers | Delete all users |
| [**userDeleteUserUsernamePost**](UsersApi.md#userDeleteUserUsernamePost) | **POST** /user/deleteUser/{username} | Delete a user by username |
| [**userGetAllUsersGet**](UsersApi.md#userGetAllUsersGet) | **GET** /user/getAllUsers | List all users |


<a id="userAddUserPost"></a>
# **userAddUserPost**
> userAddUserPost(signUpRegister)

Create a user (admin)

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UsersApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    UsersApi apiInstance = new UsersApi(defaultClient);
    SignUpRegister signUpRegister = new SignUpRegister(); // SignUpRegister | 
    try {
      apiInstance.userAddUserPost(signUpRegister);
    } catch (ApiException e) {
      System.err.println("Exception when calling UsersApi#userAddUserPost");
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

<a id="userDeleteAllUsersPost"></a>
# **userDeleteAllUsersPost**
> userDeleteAllUsersPost()

Delete all users

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UsersApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    UsersApi apiInstance = new UsersApi(defaultClient);
    try {
      apiInstance.userDeleteAllUsersPost();
    } catch (ApiException e) {
      System.err.println("Exception when calling UsersApi#userDeleteAllUsersPost");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | All users deleted. |  -  |

<a id="userDeleteUserUsernamePost"></a>
# **userDeleteUserUsernamePost**
> userDeleteUserUsernamePost(username)

Delete a user by username

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UsersApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    UsersApi apiInstance = new UsersApi(defaultClient);
    String username = "username_example"; // String | 
    try {
      apiInstance.userDeleteUserUsernamePost(username);
    } catch (ApiException e) {
      System.err.println("Exception when calling UsersApi#userDeleteUserUsernamePost");
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
| **username** | **String**|  | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User deleted. |  -  |
| **404** | User not found. |  -  |

<a id="userGetAllUsersGet"></a>
# **userGetAllUsersGet**
> List&lt;UserResponse&gt; userGetAllUsersGet()

List all users

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UsersApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    UsersApi apiInstance = new UsersApi(defaultClient);
    try {
      List<UserResponse> result = apiInstance.userGetAllUsersGet();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UsersApi#userGetAllUsersGet");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;UserResponse&gt;**](UserResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of users. |  -  |

