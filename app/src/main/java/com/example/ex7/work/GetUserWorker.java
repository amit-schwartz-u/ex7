package com.example.ex7.work;

import android.content.Context;

import com.example.ex7.data.User;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;
import com.google.gson.Gson;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Response;

public class GetUserWorker extends Worker {
    public GetUserWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;

        String userId = getInputData().getString("key_user_id");
        try {
            Response<User> response = serverInterface.getUser(userId).execute();
            User user = response.body();
            String userAsJson = new Gson().toJson(user);

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