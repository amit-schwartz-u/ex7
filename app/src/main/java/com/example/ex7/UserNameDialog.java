package com.example.ex7;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.ex7.data.TokenResponse;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;

import java.io.IOException;

import retrofit2.Response;

public class UserNameDialog extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name_dialog);
    }

    public void onClickSaveButton(View view) {
        final SharedPreferences reader = getApplicationContext().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        EditText et = findViewById(R.id.editText);
        String userName = et.getText().toString();
        preferencesEditor.putString("userName", userName);
        preferencesEditor.apply();
        getToken(userName);
        finish();
    }

    private void getToken(String userName) {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;
        try {
            Response<TokenResponse> response = serverInterface.getUserToken(userName).execute();
            TokenResponse tokenResponse = response.body();
            MainActivity.token = tokenResponse.data;
            Log.e("MainActivity", "got token data: " + tokenResponse.data);
        } catch (IOException e) {
            Log.e("MainActivity", "didn't get token");
        }
    }
}
