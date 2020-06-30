package com.example.ex7;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ex7.data.SetUserPrettyNameRequest;
import com.example.ex7.data.User;
import com.example.ex7.data.UserResponse;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Response;

public class EditPrettyName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pretty_name);
    }

    public void onClickSaveButton(View view) {
        //todo post
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;
        try {
            EditText et = findViewById(R.id.et_pretty_name);
            String prettyName = et.getText().toString();
            Response<UserResponse> response = serverInterface.postUserPrettyName(new SetUserPrettyNameRequest(prettyName)).execute();
            User user = response.body().data;
            //todo update UI
            //update UI with the user we got
            TextView welcomeTextView = findViewById(R.id.tv_welcome);
            if (user.pretty_name != "" && user.pretty_name != null) {
                welcomeTextView.setText("Welcome again, " + user.pretty_name);
            }
            Log.e("EditPrettyName", "post pretty name");
        } catch (IOException e) {
            Toast.makeText(this, "post pretty name error.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        finish();
    }
}
