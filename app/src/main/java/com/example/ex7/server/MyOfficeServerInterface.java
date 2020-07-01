package com.example.ex7.server;

import com.example.ex7.data.SetUserPrettyNameRequest;
import com.example.ex7.data.TokenResponse;
import com.example.ex7.data.User;
import com.example.ex7.data.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MyOfficeServerInterface {

    @GET("/users/0")
    Call<User> connectivityCheck();

    @GET("/user/")
    Call<UserResponse> getUserFromServer(@Header("Authorization") String token);

    @GET("/users/{user_name}/token/")
    Call<TokenResponse> getUserToken(@Path("user_name") String userName);

    @Headers({"Content-Type:application/json"})
    @POST("/user/edit/")
    Call<UserResponse> postUserPrettyName(@Body SetUserPrettyNameRequest request, @Header("Authorization") String token);

}



/*



 server endpoint:
 https://jsonplaceholder.typicode.com/


 methods to have:
 * connectivity check A - users/0
 * connectivity check B - todos/0
 * getUser(user_id)
 * getAllTodos(user_id)
 * putNewTodo(todoId)

 */