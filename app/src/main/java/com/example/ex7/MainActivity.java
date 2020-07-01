package com.example.ex7;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.ex7.data.User;
import com.example.ex7.data.UserResponse;
import com.example.ex7.work.ConnectivityCheckWorker;
import com.example.ex7.work.GetTokenWorker;
import com.example.ex7.work.GetUserWorker;
import com.example.ex7.work.PostNewUserNameWorker;
import com.google.gson.Gson;


import java.util.List;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {
    private static final String USER_ID = "3";
    public static final String EMPTY_STRING = "";
    private static String TAG = "MainActivity";
    public String token;
    public TextView welcomeTextView;
    public EditText prettyNameEditText;
    private ImageView userImage;
    public Button setPrettyNameButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeTextView = findViewById(R.id.tv_welcome);
        prettyNameEditText = findViewById(R.id.et_pretty_name);

        userImage = findViewById(R.id.user_image);
        setPrettyNameButton = findViewById(R.id.btn_save_pretty_name);
        token = MyPreferences.getUserTokenFromPreferences(getApplicationContext());
        if (token == "") {
            handleUserName();
        } else {
            getUser();
        }
        checkConnectivityAndSetUI();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getUserNameFromPreferences().equals("")) {
            if (checkIfFirstTimeInApp()) {
                welcomeTextView.setText("welcome, " + getUserNameFromPreferences());
                Log.d("MainActivity", "calling get token");
                getToken();
                setFirstTimeToFalse();
            }
        }
    }

    private void handleUserName() {
        if (checkIfFirstTimeInApp()) {
            askForUserName();
        }
    }


    private void askForUserName() {
        startActivity(new Intent(MainActivity.this, UserNameDialog.class));
    }

    private boolean checkIfFirstTimeInApp() {
        final SharedPreferences reader = getApplicationContext().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        return reader.getBoolean("isFirstRun", true);
    }

    private void setFirstTimeToFalse() {
        final SharedPreferences reader = getApplicationContext().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        preferencesEditor.putBoolean("isFirstRun", false);
        preferencesEditor.apply();
    }

    private String getUserNameFromPreferences() {
        final SharedPreferences reader = getApplicationContext().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        return reader.getString("userName", "");
    }


    private void checkConnectivityAndSetUI() {

        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(ConnectivityCheckWorker.class)

                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                // if we will remove the constraints - then the connectivity check will happen immediately
                // if we will add the constraints - then the connectivity check will happen only after we have access to the internet
                .build();

        Operation runningWork = WorkManager.getInstance().enqueue(checkConnectivityWork);

        runningWork.getState().observe(this, new Observer<Operation.State>() {
            @Override
            public void onChanged(Operation.State state) {
                if (state == null) return;

                if (state instanceof Operation.State.SUCCESS) {
                    // update UI - connected
                } else {
                    // update UI - not connected :(
                }
            }
        });
    }

    public void getToken() {
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(GetTokenWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("key_user_id", getUserNameFromPreferences()).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(checkConnectivityWork);

        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {

            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
                // so check for that
                if (workInfos == null || workInfos.isEmpty())
                    return;
                if (workInfos.get(0).getState() == WorkInfo.State.FAILED ||
                        workInfos.get(0).getState() != WorkInfo.State.SUCCEEDED) {
                    return;
                }
                WorkInfo info = workInfos.get(0);

                // now we can use it
                token = info.getOutputData().getString("token");

                Log.d(TAG, "got token: " + token);

                MyPreferences.setUserToken(getApplicationContext(),token);
                // update UI with the user we got
                getUser();
            }
        });
    }

    private void getUser() {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Loading user...");
        progressDialog.show();
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(GetUserWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("token", this.token).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(checkConnectivityWork);

        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
                // so check for that
                if (workInfos == null || workInfos.isEmpty())
                    return;

                WorkInfo info = workInfos.get(0);

                // now we can use it
                String userAsJson = info.getOutputData().getString("key_output_user");
                Log.d(TAG, "got user: " + userAsJson);
                progressDialog.dismiss();
                showMainActivityViews();

                UserResponse userResponse = new Gson().fromJson(userAsJson, UserResponse.class);
                if (userResponse != null) {
                    User user = userResponse.data;
                    // update UI with the user we got
                    if (user.pretty_name != "" && user.pretty_name != null) {
                        welcomeTextView.setText("Welcome again, " + user.pretty_name);
                    } else {
                        welcomeTextView.setText("Welcome, " + getUserNameFromPreferences());
                    }
                    setMainActivityUi(userResponse, user.username);
                }
            }
        });
    }

    public void setNewPrettyName(final String newName) {
        hideMainActivityViews(); //todo
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Loading...");
        progressDialog.show();

        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest
                .Builder(PostNewUserNameWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("key_get_new_name", newName)
                        .putString("key_get_token", token).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(oneTimeWorkRequest);
        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString())
                .observe(this, new Observer<List<WorkInfo>>() {
                    @Override
                    public void onChanged(List<WorkInfo> workInfos) {
                        if (workInfos == null || workInfos.isEmpty()) {
                            return;
                        }
                        if (workInfos.get(0).getState() == WorkInfo.State.FAILED ||
                                workInfos.get(0).getState() != WorkInfo.State.SUCCEEDED) {
                            return;
                        }

                        progressDialog.dismiss();
                        showMainActivityViews();

                        WorkInfo info = workInfos.get(0);
                        String AsJson = info.getOutputData().getString("key_get_pretty_name");
                        UserResponse userResponse = new Gson().fromJson(AsJson, UserResponse.class);

                        String userName = userResponse.data.pretty_name;
                        if (userName == null || EMPTY_STRING.equals(userName)) {
                            userName = userResponse.data.username;
                        }

                        setMainActivityUi(userResponse, userName);
                    }
                });
    }

    private void setMainActivityUi(UserResponse userResponse, String userPrettyName) {
        welcomeTextView.setText("Welcome again, " + userPrettyName + "!");
        CircularProgressDrawable circularProgressDrawable = new
                CircularProgressDrawable(MainActivity.this);
        circularProgressDrawable.setStrokeWidth(10f);
        circularProgressDrawable.setCenterRadius(60f);
        circularProgressDrawable.start();
        Glide.with(MainActivity.this)
                .load(Uri.parse("https://hujipostpc2019.pythonanywhere.com" +
                        userResponse.data.image_url))
                .placeholder(circularProgressDrawable)
                .into(userImage);
    }

    public void onClickSetPrettyNameButton(View view) {
        String prettyName = prettyNameEditText.getText().toString();
        setNewPrettyName(prettyName);
    }

    private void hideMainActivityViews() {
        welcomeTextView.setVisibility(View.INVISIBLE);
        prettyNameEditText.setVisibility(View.INVISIBLE);
        setPrettyNameButton.setVisibility(View.INVISIBLE);
        userImage.setVisibility(View.INVISIBLE);
    }

    private void showMainActivityViews() {
        welcomeTextView.setVisibility(View.VISIBLE);
        prettyNameEditText.setVisibility(View.VISIBLE);
        setPrettyNameButton.setVisibility(View.VISIBLE);
        userImage.setVisibility(View.VISIBLE);
    }
}