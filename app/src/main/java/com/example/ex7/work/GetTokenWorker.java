package com.example.ex7.work;

import android.content.Context;
import android.util.Log;

import com.example.ex7.data.TokenResponse;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Response;

public class GetTokenWorker extends Worker {

    public GetTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;
        String userName = getInputData().getString("key_user_id");
        try {
            Response<TokenResponse> response = serverInterface.getUserToken(userName).execute();
            TokenResponse tokenResponse = response.body();

            Log.e("MainActivity", "got token data: " + tokenResponse.data);
            Data outputData = new Data.Builder()
                    .putString("token", tokenResponse.data)
                    .build();
            return Result.success(outputData);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MainActivity", "didn't get token");
            return Result.retry();
        }
    }
}