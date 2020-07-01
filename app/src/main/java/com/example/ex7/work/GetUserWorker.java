package com.example.ex7.work;

import android.content.Context;

import com.example.ex7.data.User;
import com.example.ex7.data.UserResponse;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import java.io.IOException;
import androidx.concurrent.futures.CallbackToFutureAdapter;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUserWorker extends Worker {

    private CallbackToFutureAdapter.Completer<Result> callback = null;

    public GetUserWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;

        String token = "token ";
        token += getInputData().getString("token");
        try {
            Response<UserResponse> response = serverInterface.getUserFromServer(token).execute();
            UserResponse userResponse = response.body();


            String userAsJson = new Gson().toJson(userResponse);

            Data outputData = new Data.Builder()
                    .putString("key_output_user", userAsJson)
                    .build();
            return Result.success(outputData);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }

}