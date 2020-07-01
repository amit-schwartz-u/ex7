package com.example.ex7.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ex7.data.SetUserPrettyNameRequest;
import com.example.ex7.data.UserResponse;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Response;


public class PostNewUserNameWorker extends Worker {
    public PostNewUserNameWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;
        String token = "token ";
        token += getInputData().getString("key_get_token");
        String newName = getInputData().getString("key_get_new_name");
        try {
            SetUserPrettyNameRequest setPrettyNameRequest = new SetUserPrettyNameRequest();
            setPrettyNameRequest.pretty_name = newName;
            Response<UserResponse> response = serverInterface.postUserPrettyName(setPrettyNameRequest,
                    token).execute();

            UserResponse userResponse = response.body();
            String toJson = new Gson().toJson(userResponse);
            Data data = new Data.Builder().putString("key_get_pretty_name", toJson).build();
            return Result.success(data);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}